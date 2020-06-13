package giteri.network.networkStuff;

import giteri.fitting.algo.IExplorationMethod;
import giteri.fitting.algo.IExplorationMethod.ExplorationMethod;
import giteri.fitting.parameters.FittingClass;
import giteri.fitting.parameters.IModelParameter;
import giteri.fitting.parameters.IModelParameter.GenericBooleanParameter;
import giteri.fitting.parameters.IModelParameter.GenericDoubleParameter;
import giteri.fitting.parameters.IModelParameter.MemeAvailability;
import giteri.fitting.parameters.IModelParameter.MemeDiffusionProba;
import giteri.meme.entite.EntiteHandler;
import giteri.meme.entite.Meme;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.network.NetworkProperties;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.EnumExplorationMethod;
import giteri.run.configurator.Configurator.MemeList;
import giteri.run.configurator.Configurator.NetworkAttribType;
import giteri.run.interfaces.Interfaces.StatAndPlotInterface;
import giteri.tool.math.Toolz;
import giteri.tool.other.WriteNRead;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static giteri.run.configurator.Configurator.debugJarMode;

/** Classe commune à tous les statAndPlot
 *
 */
public abstract class StatAndPlotGeneric implements StatAndPlotInterface {

	//region Properties

	protected EntiteHandler entiteHandler;
	protected MemeFactory memeFactory;
	protected WriteNRead writeNRead;
	private NetworkConstructor networkConstructor;
	protected CommunicationModel communicationModel;
	protected NetworkFileLoader networkFileLoader;
	protected WorkerFactory workerFactory;

	// Element autres
	private Integer nbAction = 0;

	// Empeche l'algo de passer au step suivant de fitting sans le passage manuel
	// Mis au niveau du configurator.
	// Met en pause automatiquement quand l'algo essaye de passer au cran suivant
	public boolean autoPauseIfNexted = Configurator.autoPauseIfNexted;
	public boolean goNextStepInManuelMode = !Configurator.manuelNextStep;

	boolean debugBeforeSkip = true;
	boolean debug = Configurator.debugStatAndPlot;

	//region structure de données

	private int nbEltCirDensity = 100;
	private CircularFifoQueue<Double> cfqDensityOnFitting = new CircularFifoQueue<>(nbEltCirDensity);
	private int cptCountNbAction = 0; // compteur du nombre d'action mis a jour moins souvent
	private int moduloCount = 100; // valeur du modulo pour réel vérification du nb d'action
	private int nbActionRelaxe = 0;

	//endregion

	//endregion

	//region constructor&initializator
	/** Constructeur de cet élément de base.
	 * Ne peut etre appelé directement, classe abstract
	 */
	protected StatAndPlotGeneric(EntiteHandler entiteHandler, MemeFactory memeFactory, NetworkConstructor networkConstructor,
								  WriteNRead wnr, NetworkFileLoader nfl, WorkerFactory wf){
		this.entiteHandler = entiteHandler;
		this.memeFactory = memeFactory;
		this.networkConstructor = networkConstructor;
		this.writeNRead = wnr;
		this.networkFileLoader= nfl;
		this.workerFactory = wf;
	}

	public void setCommunicationModel(CommunicationModel com){
		this.communicationModel = com;
	}
	//endregion

	//region Interface stat&plot

	/**
	 * Lancement de thread qui va comparer un réseau lu et le réseau en cours par rapport a différente configuration
	 *
	 */
	public Double fitNetwork(Configurator.EnumLauncher typeOfLaunch, Configurator.EnumExplorationMethod typeOfExplo,
							   Optional<List<Boolean>> memeActivation, Optional<List<Double>> memeProba) {

		// appelé quand on lance en jar
		if ((typeOfLaunch == Configurator.EnumLauncher.jarC || typeOfLaunch == Configurator.EnumLauncher.jarOpenMole)) {
			return fittingLauncher(typeOfLaunch, typeOfExplo, memeActivation, memeProba);
		} else {
			// appelé depuis IHM
			(new Thread() {
				public void run() {
					fittingLauncher(typeOfLaunch, typeOfExplo, Optional.empty(), Optional.empty());
				}
			}).start();
			return 0.;
		}
	}

	/**
	 * Passage de force au step suivant en ce qui
	 * concerne le fitting.
	 *
	 */
	public void fitNextStep(){
		goNextStepInManuelMode = true;
		autoPauseIfNexted = false;
	}

	/**
	 * avg;dispersion
	 *
	 */
	public String getDDInfos(){
		Hashtable<Integer, Double> furDurchschnitt = new Hashtable<Integer, Double>();
		String total = "";
		networkConstructor.updatePreciseNetworkProperties(Configurator.getIndicateur(NetworkAttribType.DDARRAY));
		int[] dd = networkConstructor.getNetworkProperties().getDd();

		for (int i = 0; i < dd.length; i++) {
			total += "["+i+":"+dd[i]+"]";
			furDurchschnitt.put(i,(double)dd[i]);
		}

		double deviation = Toolz.getDeviation(furDurchschnitt, Optional.ofNullable(null));
		double avg = Toolz.getAvg(furDurchschnitt);
		return "" + avg + ":" + deviation + "=\n" + total;
	}

	public void incrementNbAction(){
		synchronized(nbAction)
		{
			nbAction++;
		}
	}

	/** Quadriller une plage de donnnée pour étudier les réseaux obtenus.
	 *
	 */
	public void exploFitting(){

	}

	//endregion

	//region renvoie d'une exploration method
	/** lorsque le programme est appelé depuis java - et que l'on souhaite utiliser les modelParameter pour cycler.
	 * TODO [WayPoint]- Appel fitting depuis IHM avec explo = Exhaustive
	 *
	 * @return
	 */
	private IExplorationMethod callFromJava(FittingClass fitter){
		// Tout le trala des explorateurs qui enchaine les IModelParameter
		Hashtable<Meme, GenericBooleanParameter> memeDispo = new Hashtable<>(); // Meme sur map
		Hashtable<Integer, IModelParameter<?>>  providers = new Hashtable<>(); // List de config

		// Rempli la liste des memes que l'on veut pour lancer le fitting.
		for (Meme meme : memeFactory.getMemes(Configurator.MemeList.FITTING,Configurator.ActionType.ANYTHING))
			memeDispo.put(meme, new GenericBooleanParameter());

		MemeAvailability memeProvider = new MemeAvailability(memeDispo);
		memeProvider.setEntiteHandler(entiteHandler);
		providers.put(1,memeProvider); // Détermine si on va aussi cycler sur l'existence des memes sur la map

		MemeDiffusionProba memeDiffu = new MemeDiffusionProba(memeFactory.getMemes(Configurator.MemeList.FITTING,Configurator.ActionType.ANYTHING),
				new GenericDoubleParameter(.0,.0,1.,.5));
		memeDiffu.setEntiteHandler(entiteHandler);
		providers.put(0,memeDiffu);

		memeProvider.addMemeListListener(memeDiffu);
		memeProvider.addMemeListListener(fitter);
		return ExplorationMethod.getSpecificExplorator(Configurator.explorator, providers);
	}

	/** lorsque le fitting est appelé depuis un jar, ou depuis l'IHM mais qu'on veut faire un oneshot.
	 *
	 * @return
	 */
	private IExplorationMethod callFromJar(FittingClass fitter, List<Boolean> activation, List<Double> proba){
		// appelle sec avec un seul jeu de parametre, aux probas fixés
		Hashtable<Meme, GenericDoubleParameter> memeAndProba = new Hashtable<>();
		Hashtable<Integer, IModelParameter<?>>  providers = new Hashtable<>();
		ArrayList<String> memesSelectionnes = null;
		Meme selectedMeme;
		File toWrite = writeNRead.createAndGetDirFromString(Arrays.asList("."));
		if(debugJarMode) {
			memesSelectionnes = new ArrayList<>();
			writeNRead.writeSmallFile(toWrite, "RezTemp", activation.stream().map(e -> e.toString()).collect(Collectors.toList()));
		}

		// Parcourt la liste des memes
		for (int i = 0; i < activation.size(); i++) {
			if(activation.get(i)) {
				selectedMeme = memeFactory.getIemeMemeFromSpecList(MemeList.FITTING, i);
				if(selectedMeme != null) {
					memeAndProba.put(selectedMeme, new GenericDoubleParameter(proba.get(i)));
					if (debugJarMode)
						memesSelectionnes.add(";" + memeFactory.translateMemeCombinaisonReadable(selectedMeme.toString()) + "-" + proba.get(i));
				}else {
					System.err.println("[StatAndPlotGeneric.CallFromJar]- Pas assez de meme dans la liste de fitting pour le nombre de param appelé");
				}
			}
		}
		if(debugJarMode)
//			writeNRead.writeSmallFile(toWrite, "RezTemp",
//		memeAndProba.entrySet().stream().map(( v -> v.getKey().toFourCharString().concat(v.getValue().valueString()))).collect(Collectors.toList()));
			writeNRead.writeSmallFile(toWrite, "RezTemp", memesSelectionnes);

		MemeDiffusionProba memeDiffu = new MemeDiffusionProba(memeAndProba);
		memeDiffu.setEntiteHandler(entiteHandler);
		providers.put(0,memeDiffu);

		MemeAvailability memeProvider = new MemeAvailability(memeAndProba.keySet().stream().collect(Collectors.toList()));
		memeProvider.setEntiteHandler(entiteHandler);
		providers.put(1,memeProvider); // Détermine si on va aussi cycler sur l'existence des memes sur la map

		// Astuce de renard, un poil trop complexe a faire proprement...
		//memeProvider.addMemeListListener(memeDiffu);
		memeProvider.addMemeListListener(fitter);
		return ExplorationMethod.getSpecificExplorator(EnumExplorationMethod.oneShot, providers);
	}

	//endregion

	//region fitting

	/**
	 * Refact. Fonction commune à tous les appels, première de la série.
	 *
	 */
	public double fittingLauncher(Configurator.EnumLauncher typeOfLaunch, Configurator.EnumExplorationMethod typeOfExplo,
								  Optional<List<Boolean>> memeActivationOpt, Optional<List<Double>> memeProbaOpt) {
		double resultat;

		List<Boolean> memeActivation = memeActivationOpt.orElse(new ArrayList<>());
		List<Double> memeProba = memeProbaOpt.orElse(new ArrayList<>());

		boolean jesaisjesais = true;
		if(Configurator.fullBullshi){
			if(jesaisjesais){
				memeActivation = new ArrayList<Boolean>(Arrays.asList(
						false,true,true,true,false,true,true,false,true,true,false,false,true,false,true,false,false,false,false,true


			));
				memeProba = new ArrayList<Double>(Arrays.asList(
						0.5695037109041035,0.44748070683786945,0.28690561505794965,0.5038401298660604,0.7980002913268589
						,0.5963687807889742,0.3336774014927315,0.8387268307686744,0.7280113777984465,0.6421300082365504,
						0.0693655681660803,0.4878786110664399,0.09336038347861009,0.5900261562636451,0.1640196567729506,
						0.7118382355259185,0.7875011207141009,0.36359049547786215,0.7689540437183011,0.2376116498215588


				));
			} else
				{
				memeActivation = new ArrayList<Boolean>(Arrays.asList(
						true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true
				));
				memeProba = new ArrayList<Double>(Arrays.asList(
						0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.09,0.1,0.11,0.12,0.13,0.14,0.15,0.16
				));}
		}

		// Qui définira pour la classe de fitting l'espace de recherche
		IExplorationMethod explorator = null;
		FittingClass configuration = new FittingClass();

		// Pour les cas OneShot from IHM;JAR
		if(typeOfExplo == EnumExplorationMethod.oneShot && !memeActivationOpt.isPresent())
			generateProxyParam(memeActivation,memeProba);

		// Si on veut plus d'une itération, besoin d'utiliser un explorator plus complet
		if(typeOfExplo != EnumExplorationMethod.oneShot)
			explorator = callFromJava(configuration);
		// Si appelle oneShot, depuis IHM ou depuis JAR, explorator oneShot
		else {
			explorator = callFromJar(configuration, memeActivation, memeProba);
		}

		// Classe de configuration qui contient tout ce qu'il faut pour faire une simu
		configuration.KindaConstructor(writeNRead, communicationModel,
				memeFactory, networkFileLoader, workerFactory, entiteHandler, networkConstructor, explorator);

		// ajout de la fitting classe au listener
		entiteHandler.addEntityListener(configuration);

		// Lancement d'une simulation
		resultat = factorisation(configuration);

		// retrait de la fitting classe des listener
		entiteHandler.removeEntityListener(configuration);
		return resultat;
	}

	/** Classe factorisée pour les traitements de fitting ou searching.
	 *
	 * @param fittingConfig
	 * @return le score de la fittingConfig testé
	 */
	private double factorisation(FittingClass fittingConfig){
		fittingConfig.init();
		int nbActionPassee;

		// [RUN++] boucle changement de fittingConfig
		do {
			fittingConfig.newRun();
			// [REPETITION++] On fait nbRunByConfig mesures par configuration pour étudier la variance des résultats
			for (int i = 0; i < fittingConfig.nbRepetitionByConfig; i++)
			{
				fittingConfig.newRepetition();
				cfqDensityOnFitting.clear();
				do
				{
					fittingConfig.com.view.displayMessageOnFitPanel("Work In Progress");

					// TODO a voir pour rendre le truc un peu plus flexible sur la circular queue
					// qui prend boucleExterneSize en taille mais qui n'est reset que dans newRepetition().

					// On répète x fois
					for (int x = 0; x < fittingConfig.boucleExterneSize; x++)
					{
						do{
							nbActionPassee  = getNbActionRelaxe();
						}while(nbActionPassee <= fittingConfig.nbActionByStep);

						NetworkProperties np = networkConstructor.updatePreciseNetworkProperties(Configurator.getIndicateur(NetworkAttribType.DENSITY));
						cfqDensityOnFitting.add(np.getDensity());
						fittingConfig.computeMemeAppliance();
						resetNbAction();
					}

					debugBeforeSkip = fittingConfig.continuFitting(cfqDensityOnFitting);
					if(!debugBeforeSkip){
						if(debug) System.out.println("Voudrait passer au step suivant");
					}

					// Si la condition d'arret est ok et si on a pas activé le mode manual skip
				} while( debugBeforeSkip || !goNextStepInManuelMode );

				if(Configurator.manuelNextStep)
					goNextStepInManuelMode = false;

				// Si on place automatiquement une pause avant le passage au step suivant
				if(autoPauseIfNexted)
				{
					communicationModel.suspend();
					do{
						try {			Thread.sleep(10);			} catch (InterruptedException e) {e.printStackTrace();}
					}while(autoPauseIfNexted);

					autoPauseIfNexted = true;
					communicationModel.resume();
				}

				fittingConfig.endRepetition();
			}

			fittingConfig.endRun();
			// Configuration distribution suivante
		} while(fittingConfig.explorator.gotoNext());

		if(!Configurator.jarMode)
			System.out.println(" - Fin de l'exploration - ");
		else {
			entiteHandler.stop();
			networkConstructor.stop();
			networkConstructor.resume();
		}
		return fittingConfig.endSimu();
	}


	/** Va être appelé par le start classique et pour l'instant le oneshote aussi
	 *
	 *
	 * @param activator
	 * @param proba
	 */
	private void generateProxyParam(List<Boolean> activator, List<Double> proba) {
		//jesaisjesais
		List<Integer> toConvert = new ArrayList<>(Arrays.asList(
				// 30%
				// RDM1 //1,0,1,0,1,1,1,1,0,0,1,0,0,0,1,0,1,0,0,1
				// RDM2 //1,1,1,1,0,0,1,1,1,1,1,0,0,0,1,0,0,0,0,0
				// RDM 3 // 0,1,0,0,0,0,1,1,0,1,1,1,1,0,0,1,1,1,1,0

				// 2%
				// RDM1 // 1,0,0,1,1,0,1,1,0,0,0,0,1,0,1,0,1,1,0,0
				// RDM2 //1,1,0,1,0,1,1,1,0,1,0,0,1,1,1,0,0,0,0,1
				// RDM 3 //0,1,0,0,1,0,1,0,1,1,1,0,1,0,1,1,0,1,0,0
				// RDM4 // 1,0,1,0,1,0,0,0,0,0,0,1,1,1,1,0,1,0,0,1

				// SF m2
				1,0,0,0,1,0,0,0,0,1,1,0,1,0,0,1,0,1,1,0
//				0,0,1,0,0,0,0,1,0,1,0,1,0,0,0,1,0,0,0,1
//				1,0,0,0,1,0,0,0,0,1,1,0,1,0,0,0,0,1,1,1
//				1,1,0,1,1,0,0,0,1,0,0,1,0,0,0,1,0,1,0,1

				//sw
				// 0,1,1,0,0,1,1,1,1,1,1,0,1,0,0,0,0,1,0,0


		));

		proba.addAll(Arrays.asList(
				// 30%
				// RDM 1 //0.3697724351876696,0.6360422126805652,0.22648314740837866,0.5669938663578111,0.872531810862036,0.0530274263161221,0.6516062928000813,0.35939943317312517,0.38815068877892145,0.7226281468686124,0.5256483612500935,0.8402448258457663,0.642841993052775,0.0,0.3297693977842685,0.0,0.9956300430370958,0.7991620130449733,0.4911830079668989,0.2866037016947451
				// RDM2 //
				//0.6629272381539464,0.11721078383637736,0.40146961954418436,0.5215546435610683,0.4699230338009679,0.0,0.9978063862068921,0.0,0.4866151543223006,0.0,0.0,0.3389578571760558,0.752621319133035,0.2506164729858143,0.2940049190035998,0.1640278642085331,0.9574215675141635,0.8009524016042241,0.39010940651181886,0.0
				// RDM 3 // 0.6117519572389363,0.27040198069821025,0.7798528305541176,0.31755895287617647,0.34027945672044746,0.0,0.9976097163834369,0.103923060999879,0.8413512530836512,0.0,0.4169476330973683,0.3445170033581777,1.0,0.20311380073824578,0.14928732528343658,0.163591287531604,0.18984358409135957,0.4368564621383624,0.7335221279692944,0.19495029555594234

				// 2%
				// RDM 1 //0.04471347715482504,0.3528788113349613,0.40158899806935006,0.7658296569489453,0.0,0.19107993070675344,0.5767889135954455,0.24799083896464566,0.7195121739286174,0.7581317598232669,0.10721550744052212,0.8577312982439893,0.36812427369114664,1.0,0.6678192416581893,0.6024736918937376,0.0,0.7623052700521976,0.7374858467862838,0.8560782415285597
				// RDM2 //0.4599779194292323,0.9580786240944636,0.4740766322868607,0.07218253836117065,0.2734654189627329,0.5431535368121214,0.7702181505968926,0.3239424917790896,0.4421214512125229,0.7956897511265423,0.14071632553932198,0.0,0.5006912682865697,0.2257420483615301,0.3386364043590511,0.5004961492134168,0.10128110228625607,0.20207240530397874,0.7009916716782976,1.0
				// RDM 3 // 0.672042929281056,0.9724662768601908,0.9975403062409353,0.6120367383475773,0.2642771211227636,0.5772311496505468,0.29663853459019346,1.0,0.36338404517550565,0.07611511874734159,0.9935984163638509,0.7059294733106574,0.516589736053145,0.2236529208563617,0.16844093444599154,0.7000669745706041,0.17802982738497988,0.14420201920262443,0.5802748333397799,0.143975784455376
				// RDM4 /1.0,0.07723703691054586,0.6552096444896812,1.0,1.0,0.7085303242978442,0.4473744032422093,0.998219144857299,0.0,0.12237762018893773,0.8352797104918352,0.5749112056226651,0.47604957434046113,0.027542494515254094,1.0,0.7273441272593555,0.4946438691565615,1.0,0.4899440886325759,0.7412628335797854

				// SF m2
		0.825733321261414,0.15333100631866725,0.7260139192365882,0.7263298108873217,0.4640125028777422,0.08201169510522432,0.6759213745537336,0.5391651196921081,0.41071651440124524,0.11522844782070862,0.7374528109442231,0.08833604725160571,0.05304697319728091,0.6223270294529939,0.3190164908535478,0.6508322403635294,0.5079214319110621,0.6251556056050829,0.15687511678133892,0.7025543955603364
//		0.06140229617990878,0.8854851526384078,0.17553551751419927,0.7058508191362824,0.5947457060712025,0.11873512649050677,0.8117597062656989,0.9160971841184145,0.6363980611855826,0.9431971584440189,0.5506493135102557,0.24789703976973,0.8064387966193163,0.18919872127407844,0.26926216056185326,0.8733430648873031,0.9898229357872745,0.6269296003265534,0.8934426351886451,0.9988099553478567
//		0.6514096563108682,0.2706117702833199,0.8337411623423667,0.7473187554904578,0.45005924709537404,0.08237480997786993,0.6658526351951467,0.5028698538546509,0.3892568273127753,0.11689995969291192,0.7207422056592402,0.11673519136132415,0.07967603082576308,0.6995775038420928,0.3431583179411189,0.7048515324199192,0.5082978397939087,0.625392229251524,0.4008708072724981,0.7039762238670566
//		0.17974514513656098,1.0,0.0,1.0,0.5264262259286833,0.18823350219232393,0.42462015719612195,1.0,0.0,0.9307329903313412,0.9248916436200796,0.23816707794937086,0.7927708137734336,0.29550512800183826,0.7988094007622918,0.8182434134398676,0.9726292933824087,0.2540923068452053,1.0,1.0

				// SW
				//0.44568239760027606,0.3883002067810476,0.4808979968786353,0.0,0.5789730289579404,0.4649033213955118,0.9301834207754492,0.2766020644302826,0.9861302036102717,0.7106748982001441,0.030114460764969264,0.5916158397753862,0.1897133247215042,0.5035973063192538,0.13583961547843384,0.24881822153643238,0.5753202262152508,0.7663739861378406,0.23514657159253471,0.7556758450259308
		));

		// A vérifier
		activator.addAll(toConvert.stream().map(e -> e==1).collect(Collectors.toList()));
	}

	//endregion

	//region others

	private void computingMemeAppliance(){
//		// contient les prop. d'application ou l'écart type de l'application d'un meme
//		Hashtable<Meme, Double> kvEcartTypeOrPropMemeAppliance = new Hashtable<Meme, Double>();
//
//		// Affichage dans la fenetre de l'évolution des meme appliances
//		Hashtable<Integer, Double>  kvMemeValue = new Hashtable<>() ;
//		for (Meme meme : kvEcartTypeOrPropMemeAppliance.keySet())
//			kvMemeValue.put(memeFactory.getIndexFromMeme(meme), kvEcartTypeOrPropMemeAppliance.get(meme));
//		communicationModel.view.addValueToApplianceSerie(getNbAction(), kvMemeValue);
	}

	/** avg;dispersion
	 *
	 */
	public String getDDInfosByMeme(){
		Hashtable<Integer, Double> furDurchschnitt = new Hashtable<Integer, Double>();
		String total = "";
		networkConstructor.updatePreciseNetworkProperties(Configurator.getIndicateur(NetworkAttribType.DDARRAY));
		int[] dd = networkConstructor.getNetworkProperties().getDd();

		for (int i = 0; i < dd.length; i++) {
			total += "["+i+":"+dd[i]+"]";
			furDurchschnitt.put(i,(double)dd[i]);
		}

		double deviation = Toolz.getDeviation(furDurchschnitt, Optional.ofNullable(null));
		double avg = Toolz.getAvg(furDurchschnitt);
		return "" + avg + ":" + deviation + "=\n" + total;
	}

	/** Vérifie la vraie valeur moins souvent ; le synchronized ralenti pas mal
	 *
	 * @return
	 */
	protected int getNbActionRelaxe(){
		if(cptCountNbAction++ % moduloCount == 0){
			synchronized (nbAction){
				nbActionRelaxe = nbAction;
			}
		}

		return nbActionRelaxe;
	}

	protected int getNbAction() {
		synchronized(nbAction)
		{
			return nbAction;
		}
	}

	protected void resetNbAction(){
		synchronized(nbAction){
			nbAction = 0;
			nbActionRelaxe = 0;
		}
	}

	//endregion

	//region GARBAGE

	/** lancement de thread pour la fonction de stabilité d'un réseau.
	 *
	 */
	public void testStability(){

	}

	//endregion

	//endregion
}
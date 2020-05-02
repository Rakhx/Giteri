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

		boolean jesaisjesais = true;
		if(jesaisjesais){
			memeActivation = new ArrayList<Boolean>(Arrays.asList(
					true,false,false,true,false,false,true,true,false,false,false,false,false,false,true,true

			));

			memeProba = new ArrayList<Double>(Arrays.asList(
					0.22380161025487205,0.22629436669860364,0.42688889518102785,0.5790234786489331,0.4310511301169438,0.9168190433023808,0.22989880757748155,0.4208471227112343,0.12929744927262993,0.0,0.18239071695979722,0.3043418373724804,0.21218649589021615,1.0,0.6000289473974884

			));




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
		List<Integer> toConvert = new ArrayList<>(Arrays.asList(0,0,0,1,0,0,1,0,0,0,0,1,0));
		proba.addAll(Arrays.asList(
				0.36428345569016246,0.9259845265194451,0.5053801414886911,0.2589468394646743,0.9165201850419845,0.0,0.2945136950484953,0.4551581307867684,0.7328367646124856,0.6769722633444669,0.0,0.6715130252202258,0.0,0.4775546116715207,0.504447069382873,0.8988602835056816



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
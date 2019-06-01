package giteri.network.networkStuff;

import giteri.fitting.algo.IExplorationMethod;
import giteri.meme.event.BehavTransmEvent;
import giteri.run.interfaces.Interfaces.StatAndPlotInterface;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import giteri.tool.math.Toolz;
import giteri.tool.other.WriteNRead;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.network.NetworkProperties;
import giteri.fitting.parameters.FittingClass;
import giteri.fitting.parameters.IModelParameter;
import giteri.fitting.parameters.IModelParameter.GenericBooleanParameter;
import giteri.fitting.parameters.IModelParameter.GenericDoubleParameter;
import giteri.fitting.parameters.IModelParameter.MemeAvailability;
import giteri.fitting.parameters.IModelParameter.MemeDiffusionProba;
import giteri.fitting.algo.IExplorationMethod.ExplorationMethod;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.EnumExplorationMethod;
import giteri.run.configurator.Configurator.NetworkAttribType;
import giteri.run.configurator.Configurator.MemeList;
import giteri.meme.entite.EntiteHandler;
import giteri.meme.entite.Meme;
import org.apache.commons.collections4.queue.CircularFifoQueue;

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

	/** Lancement de thread qui va comparer un réseau lu et le réseau en cours.
	 *
	 */
	public Double fitNetwork(Configurator.EnumLauncher typeOfLaunch, Configurator.EnumExplorationMethod typeOfExplo,
							   Optional<List<Boolean>> memeActivation, Optional<List<Double>> memeProba) {
		if (!(typeOfLaunch == Configurator.EnumLauncher.jarC || typeOfLaunch == Configurator.EnumLauncher.jarOpenMole)) {
			// appelé depuis IHM
			(new Thread() {
				public void run() {
					fittingLauncherVersionClean(typeOfLaunch, typeOfExplo, Optional.empty(), Optional.empty());
				}
			}).start();
			return 0.;
		} else {
			// appelé quand on lance en jar
			return fittingLauncherVersionClean(typeOfLaunch, typeOfExplo, memeActivation, memeProba);
		}
	}

	/** Passage de force au step suivant en ce qui
	 * concerne le fitting.
	 *
	 */
	public void fitNextStep(){
		goNextStepInManuelMode = true;
		autoPauseIfNexted = false;
	}

	/** avg;dispersion
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

	//endregion

	//region renvoie d'une exploration method
	/** lorsque le programme est appelé depuis java - et que l'on souhaite utiliser les modelParameter pour cycler.
	 *
	 *
	 * @return
	 */
	private IExplorationMethod callFromJava(){
		// Tout le trala des explorateurs qui enchaine les IModelParameter
		Hashtable<Meme, GenericBooleanParameter> memeDispo = new Hashtable<>();
		Hashtable<Integer, IModelParameter<?>>  providers = new Hashtable<>();

		// Rempli la liste des memes que l'on veut pour lancer le fitting.
		for (Meme meme : memeFactory.getMemes(Configurator.MemeList.FITTING,Configurator.ActionType.ANYTHING))
			memeDispo.put(meme, new GenericBooleanParameter());

		MemeAvailability memeProvider = new MemeAvailability(memeDispo);
		memeProvider.setEntiteHandler(entiteHandler);
		// providers.put(1,memeProvider);

		MemeDiffusionProba memeDiffu = new MemeDiffusionProba(memeFactory.getMemes(Configurator.MemeList.FITTING,Configurator.ActionType.ANYTHING),
				new GenericDoubleParameter(.1,.1,1.,.1));
		memeDiffu.setEntiteHandler(entiteHandler);
		providers.put(0,memeDiffu);

		memeProvider.addMemeListListener(memeDiffu);

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
		if(debugJarMode)
			writeNRead.writeSmallFile(toWrite, "RezTemp", activation.stream().map(e->e.toString()).collect(Collectors.toList()));

		if(debugJarMode)
			memesSelectionnes = new ArrayList<>();

		// Parcourt la liste des memes
		for (int i = 0; i < activation.size(); i++) {
			if(activation.get(i)) {

				//	memeAndProba.put(memeFactory.getMemeFromIndex(i), new GenericDoubleParameter(proba.get(i)));
				selectedMeme = memeFactory.getIemeMemeFromSpecList(MemeList.FITTING, i);
				if(selectedMeme != null) {
					memeAndProba.put(selectedMeme, new GenericDoubleParameter(proba.get(i)));
					if (debugJarMode)
						memesSelectionnes.add(";" + entiteHandler.translateMemeCombinaisonReadable(selectedMeme.toString()) + "-" + proba.get(i));
				}else {
					System.err.println("[StatAndPlotGeneric.CallFromJar]- Pas assez de meme dans la liste de fitting pour le nombre de param appelé");

				}
			}
		}
		if(debugJarMode)
			writeNRead.writeSmallFile(toWrite, "RezTemp", memeAndProba.keySet().stream().map(e->e.toString()).collect(Collectors.toList()));
		//	if(Configurator.debugJarMode)
//			System.out.println("Memes voulus "+memesSelectionnes.stream().reduce(String::concat));



		MemeDiffusionProba memeDiffu = new MemeDiffusionProba(memeAndProba);
		memeDiffu.setEntiteHandler(entiteHandler);
		providers.put(0,memeDiffu);

//		IModelParameter.ModelParamNbNode nodesChanging = new IModelParameter.ModelParamNbNode(100, 1000, 100);
//		providers.put(1, nodesChanging);
//
//		// l'ordre est important. Rapport au mise a jour de noeud etc dans les structures de données et graphstream
//		nodesChanging.addMemeListListener(networkConstructor);
//		nodesChanging.addMemeListListener(entiteHandler);
//		nodesChanging.addMemeListListener(fitter);

		return ExplorationMethod.getSpecificExplorator(EnumExplorationMethod.oneShot, providers);
	}

	/** Retourne une liste spécifique a explorer.
	 *
	 * @return
	 */
	private IExplorationMethod callSpecificParam(){
		// appelle sec avec un seul jeu de parametre, aux probas fixés
		Hashtable<Meme, GenericDoubleParameter> memeAndProba = new Hashtable<>();
		Hashtable<Integer, IModelParameter<?>>  providers = new Hashtable<>();
		List<Meme> existingMeme = memeFactory.getMemes(Configurator.MemeList.EXISTING, Configurator.ActionType.ANYTHING);

		for (int i = 0; i < existingMeme.size(); i++) {
			if(existingMeme.get(i).getName().compareToIgnoreCase("ADDØ") == 0 ){
				memeAndProba.put(existingMeme.get(i),new GenericDoubleParameter(.04,.04,.2,.04));
			}
			else if(existingMeme.get(i).getName().compareToIgnoreCase("RMVØ") == 0 ){
				memeAndProba.put(existingMeme.get(i),new GenericDoubleParameter(1.,1.,1.,1.));
			}
			else if(existingMeme.get(i).getName().compareToIgnoreCase("AddØ-Hop") == 0 ){
				memeAndProba.put(existingMeme.get(i),new GenericDoubleParameter(1.,1.,1.,1.));
			}
		}

		MemeDiffusionProba memeDiffu = new MemeDiffusionProba(memeAndProba);
		memeDiffu.setEntiteHandler(entiteHandler);
		providers.put(0,memeDiffu);

		IModelParameter.ModelParamNbNode nodesChanging = new IModelParameter.ModelParamNbNode(100, 2100, 400);
		providers.put(1, nodesChanging);

		return ExplorationMethod.getSpecificExplorator(Configurator.explorator, providers);
	}

	//endregion

	//region fitting
	/** Refact. Fonction commune à tous les appels, premiere de la série.
	 *
	 */
	public double fittingLauncherVersionClean(Configurator.EnumLauncher typeOfLaunch, Configurator.EnumExplorationMethod typeOfExplo,
											  Optional<List<Boolean>> memeActivationOpt, Optional<List<Double>> memeProbaOpt) {
		double resultat;

		List<Boolean> memeActivation = memeActivationOpt.orElse(new ArrayList<>());
		List<Double> memeProba = memeProbaOpt.orElse(new ArrayList<>());

		// Qui définira pour la classe de fitting l'espace de recherche
		IExplorationMethod explorator = null;

		FittingClass configuration = new FittingClass();

		// Dans le cas ou on veut un one shot de l'IHM il faut remplir les listes
		// En gros quand on lance depuis lIHM
		if(typeOfExplo == EnumExplorationMethod.oneShot && !memeActivationOpt.isPresent())
			//fitListEMManuel(memeActivation, memeProba);
			fitListAnotherHJ(memeActivation,memeProba);

		// Si appelle toutifruiti, explo full ou random, depuis IHM Besoin de cycler sur les config de IModel, etc etc.
		if(typeOfExplo == EnumExplorationMethod.exhaustive)
			explorator = callFromJava();

		// Si appelle oneShot, depuis IHM ou depuis JAR
		else if(typeOfExplo == EnumExplorationMethod.oneShot)
			explorator = callFromJar(configuration, memeActivation, memeProba);

		// NON FINI
		else if (typeOfExplo == EnumExplorationMethod.specific)
			explorator = callSpecificParam();

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
	 * @param config
	 * @return le score de la config testé
	 */
	private double factorisation(FittingClass config){
		config.init();
		int nbActionPassee;

		// boucle changement de config RUN++
		do {
			config.newRun();

			// On fait nbRunByConfig mesures par configuration pour étudier la variance des résultats REPETITION++
			for (int i = 0; i < config.nbRepetitionByConfig; i++)
			{
				config.newRepetition();
				cfqDensityOnFitting.clear();
				do
				{
					config.com.view.displayMessageOnFitPanel("Work In Progress");

					// TODO a voir pour rendre le truc un peu plus flexible sur la circular queue
					// qui prend boucleExterneSize en taille mais qui n'est reset que dans newRepetition().

					// On répète x fois
					for (int x = 0; x < config.boucleExterneSize; x++)
					{
						do{
							nbActionPassee  = getNbAction();
						}while(nbActionPassee <= config.nbActionByStep);

						NetworkProperties np = networkConstructor.updatePreciseNetworkProperties(Configurator.getIndicateur(NetworkAttribType.DENSITY));
						cfqDensityOnFitting.add(np.getDensity());
						config.computeMemeAppliance();
						resetNbAction();
					}

//					debugBeforeSkip = config.continuFittingSimpliestVersion();
					debugBeforeSkip =config.continuFitting(cfqDensityOnFitting);

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

				config.endRepetition();
			}

			config.endRun();

			// Configuration distribution suivante
		} while(config.explorator.gotoNext());

		if(!Configurator.jarMode)
			System.out.println(" - Fin de l'exploration - ");
		else {
			entiteHandler.stop();
			networkConstructor.stop();
			networkConstructor.resume();
		}
		return config.endSimu();
	}

	/** prends les listes vides en entrée et les remplis pour les faire correspondre a une
	 * entrée standard en appelle console.
	 *  Pour faire un oneshot depuis IHM
	 *  TODO [WayPoint] - ONESHOT CONFIGURATION
	 * @param activator
	 * @param proba
	 */
	private void fitListEMManuel(List<Boolean> activator, List<Double> proba){
		activator.clear(); proba.clear();
		List<Meme> existingMeme = memeFactory.getMemes(Configurator.MemeList.EXISTING, Configurator.ActionType.ANYTHING);

		for (int i = 0; i < existingMeme.size(); i++) {
			if(existingMeme.get(i).getName().compareToIgnoreCase("AddØ-Hop") == 0 ) {
				activator.add(true);proba.add(1.); }
			else
 				if(existingMeme.get(i).getName().compareToIgnoreCase("Add+") == 0 ){
				activator.add(true); proba.add(1.);}
			 else
			 	if(existingMeme.get(i).getName().compareToIgnoreCase("Add-") == 0 ){
				activator.add(true); proba.add(1.);}
			else
				if(existingMeme.get(i).getName().compareToIgnoreCase("Add∞") == 0 ){
					activator.add(true); proba.add(1.); }
			else
				if(existingMeme.get(i).getName().compareToIgnoreCase("AddØ") == 0 ){
					activator.add(true); proba.add(1.); }
			else
				if(existingMeme.get(i).getName().compareToIgnoreCase("RmvØ-2hop") == 0 ){
					activator.add(true); proba.add(1.); }
			else
				if(existingMeme.get(i).getName().compareToIgnoreCase("RMVØ") == 0 ){
					activator.add(true); proba.add(1.); }
			else
				if(existingMeme.get(i).getName().compareToIgnoreCase("Rmv+") == 0 ){
					activator.add(true); proba.add(1.); }
			else
				if(existingMeme.get(i).getName().compareToIgnoreCase("Rmv-") == 0 ){
					activator.add(true); proba.add(1.); }
		}

	}

	private void fitListAnotherHJ(List<Boolean> activator, List<Double> proba) {

	    // SCORING 55

        // small world? @ 100 noeuds
//		proba.addAll(Arrays.asList(0.041,0.618,.155,0.811,0.594,0.582,1.,.446,.557));
//		activator.addAll(Arrays.asList(true, false, false, true, false, true,true,true,true));
		// Jazz @ 194 noeud @ bad scoring
		// proba.addAll(Arrays.asList(0.4374,0.,.963,0.513,1.,0.388,.5914,.0,.989));
		// activator.addAll(Arrays.asList(false, true, false, true, false, true,true,false,true));
		// Jazz @ 194 noeud @  scoring 352
//		proba.addAll(Arrays.asList(0.7759,0.9701,.15157,0.6078,.69379,0.6541,.8387,.100,.8346));
//		activator.addAll(Arrays.asList(true, false, true, false, false, false,true,true,true));
		// Jazz @ 194 noeud @  scoring 174 20 min iteration marche pas mal
//		proba.addAll(Arrays.asList(0.8361,0.0587,0.,0.1333,.4851,0.35549,.08054,.17551,.24969));
//		activator.addAll(Arrays.asList(true, true, true, false, false, true,false,false,false));
		// Jazz @ 194 noeud @  scoring 174 10h itération
//		proba.addAll(Arrays.asList(0.36619,0.662239,0.2188339,0.7548696,.5282568,0.55852,.835418,.77056,.045));
//		activator.addAll(Arrays.asList(false, false, false, 	true, 	 false, 	true,  true, true,  false));
		// bof @ 1h de simu 3 repet par config
		//proba.addAll(Arrays.asList(0.36619,0.662239,0.2188339,0.5524068,.5282568,0.9388505,.835418,.77056,.549245149));
		//activator.addAll(Arrays.asList(false, false, false, 	true, 	 false, 	true,  false, false,  true));
         //Good @ 12h de simu avec
// 		proba.addAll(Arrays.asList(0.537244,0.1521694,0.2188339,0.5524068,.5282568,0.218909,.835418,.77056,.549245149));
//		activator.addAll(Arrays.asList(true, true, false, 	false, 	 false, 	true,  false, false,  false));

//		proba.addAll(Arrays.asList(0.39502254698724504,0.14734812316413426,0.1881433193671096,0.35610837754779356,0.8530818497900912,0.649708309830368,0.9434847536531004,0.5146669894328433,0.6188785584129679));
//		activator.addAll(Arrays.asList(false, true, true, 	false, 	 true, 	true,  true, true, true));

		proba.addAll(Arrays.asList(0.11728577951388841,0.24408400914825767,0.8181187158045617,0.9047304554969549,0.947883696331626,0.20749494856553974,0.23900466288876032,0.14446794654252515,0.5394101827842079));
		activator.addAll(Arrays.asList(false, false, false, 	true, 	 false, 	true,  true, true, false));


        // ENDSCORING 55
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

	protected int getNbAction() {
		synchronized(nbAction)
		{
			return nbAction;
		}
	}

	protected void resetNbAction(){
		synchronized(nbAction){
			nbAction = 0;
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
package giteri.network.networkStuff;

import giteri.run.interfaces.Interfaces.StatAndPlotInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Optional;

import giteri.tool.math.Toolz;
import giteri.tool.other.WriteNRead;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.network.NetworkProperties;
import giteri.network.network.TinyNetwork;
import giteri.network.network.TinyNetwork.TinyNode;
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
import giteri.meme.entite.EntiteHandler;
import giteri.meme.entite.Meme;

/** Classe commune à tous les statAndPlot
 *
 */
public abstract class StatAndPlotGeneric implements StatAndPlotInterface {

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
	// Mis au niveau du configuraztor.
	// Met en pause automatiquement quand l'algo essaye de passer au cran suivant
	public boolean autoPauseIfNexted = Configurator.autoPauseIfNexted;
	public boolean goNextStepInManuelMode = !Configurator.manuelNextStep;

	boolean debugBeforeSkip = true;
	boolean debug = Configurator.debugStatAndPlot;
	public ArrayList<Double> probaVoulu;

	/** Constructeur de cet élément de base.
	 * Ne peut etre appelé directement, classe abstract
	 */
	protected StatAndPlotGeneric( EntiteHandler entiteHandler, MemeFactory memeFactory, NetworkConstructor networkConstructor,
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

	/** Lancement de thread qui va comparer un réseau lu et le réseau en cours.
	 *
	 */
	public Double fitNetwork(){
		if(!Configurator.jarMode){
			(new Thread(){
				public void run() {
					 fittingLauncher();
				}
			}).start();
		return 0.;
		}else{
			return fittingLauncher();
		}
	}

	/** PREMIERE FONCTION APPELEE DANS LA LONGUE SERIE DES FITTING SEARCHING
	 *
	 */
	public double fittingLauncher(){
		double resultat;
		// Classe de configuration qui contient tout ce qu'il faut pour faire une simu
		FittingClass configuration = new FittingClass(writeNRead,communicationModel,
				memeFactory, networkFileLoader, workerFactory, entiteHandler, networkConstructor);

		// ajout de la fitting classe au listener
		entiteHandler.addEntityListener(configuration);

		// initialise les config de simulation genre répartition des comportments etc
		initializeConfigForStability(configuration);

		// Lancement d'une simulation
		resultat = factorisation(configuration);

		// retrait de la fitting classe des listener
		entiteHandler.removeEntityListener(configuration);

		return resultat;
	}

	/** Initialise et instancie pour préparer le fitting. Behavior présent sur la map,
	 * leur range de proba. de transmission. Le type d'exploration qu'on va faire de l'espace
	 * de param.
	 * TODO [WayPoint] - Mise en place de la configuration pour le fitting ( Step, providers )
	 * @param fitting
	 */
	protected void initializeConfigForStability(FittingClass fitting){
		Hashtable<Meme, GenericBooleanParameter> memeDispo = new Hashtable<>();
		Hashtable<Integer, IModelParameter<?>>  providers = new Hashtable<>();

		// Rempli la liste des memes que l'on veut pour lancer le fitting.
		for (Meme meme : memeFactory.getMemes(Configurator.MemeList.FITTING,Configurator.ActionType.ANYTHING))
			memeDispo.put(meme, new GenericBooleanParameter());

		MemeAvailability memeProvider = new MemeAvailability(memeDispo);
		memeProvider.setEntiteHandler(entiteHandler);
		providers.put(1,memeProvider);

		MemeDiffusionProba memeDiffu = new MemeDiffusionProba(memeFactory.getMemes(Configurator.MemeList.FITTING,Configurator.ActionType.ANYTHING),
				new GenericDoubleParameter(.2,.2,.4,.2));
		memeDiffu.setEntiteHandler(entiteHandler);
		providers.put(0,memeDiffu);

		memeProvider.addMemeListListener(memeDiffu);

		if(Configurator.explorator == EnumExplorationMethod.oneShot){
			ArrayList<Double> InOrderOfParameter = new ArrayList<>(Arrays.asList(1.,0.365018173274458,0.089130672733655,0.484877681454417,1.));

			ArrayList<Double> probaVoulu = new ArrayList<>();
			probaVoulu.add(InOrderOfParameter.get(3));
			probaVoulu.add(InOrderOfParameter.get(0));
			probaVoulu.add(InOrderOfParameter.get(1));
			probaVoulu.add(InOrderOfParameter.get(4));
			probaVoulu.add(InOrderOfParameter.get(2));
			setPreciseValue(probaVoulu, memeDiffu);
		}

		fitting.explorator = ExplorationMethod.getSpecificExplorator(Configurator.explorator, providers);

//		if(!Configurator.jarMode)
//			Configurator.methodOfGeneration = Configurator.MemeDistributionType.FollowingFitting;
	}

	/**
	 *
	 * @param probaVoulu
	 * @param memeDiffu
	 */
	private void setPreciseValue(ArrayList<Double> probaVoulu, MemeDiffusionProba memeDiffu){
		Double value;
		int i = 0;
		for (Meme meme :memeFactory.getMemes(Configurator.MemeList.FITTING,Configurator.ActionType.ANYTHING)){
			value = probaVoulu.get(i++);
			memeDiffu.getValue().put(meme, new GenericDoubleParameter(value,value, value,.1));
		}
	}

	/** Classe factorisée pour les traitements de fitting ou searching.
	 *
	 * @param config
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
						config.addDensity(np.getDensity());
						config.computeMemeAppliance();
						resetNbAction();
					}

//					debugBeforeSkip = config.continuFittingCleanVersion();
					debugBeforeSkip = config.continuFittingSimpleVersion();

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

	//endregion

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

	/** Mise a jour de la classe de propriété de réseau, de facon sélective en fonction du code d'activation.
	 * Mise à jour depuis le tinyNetwork en paramètre, et non le courant d'une classe.
	 * L'UUID du networkProperties est mis a jour ssi le code d'activation a demandé une mise a jour de tout
	 * les attributs.
	 *
	 */
	public void updateNetworkProperties(TinyNetwork net, NetworkProperties networkProp, int activationCode){
		double nbNodes, nbEdges, parcouru, firstQ, thirdQ;
		int index;
		// RP: résultats potentiels
		double density = -1, avgDegre = -1 ;
		int[] distrib = new int[0];
		double ddInterQrt = -1;
		@SuppressWarnings("unused")
		double avgClustering = 0;
		// Pas de passage par le gc pour "libérer" une variable simple, plus performant que d'appeler plusieurs fois le configurator.isattribActivated
		boolean avgClust = Configurator.isAttribActived(activationCode, NetworkAttribType.AVGCLUST);
		//RP: Concernant le calcul pour les clustering moyen
		Hashtable<Integer, ArrayList<Integer>> connectionsByNode = null;
		Hashtable<Integer, Double> clustByNode = null;
		ArrayList<Integer> connections = null;
		double nodeClustering = 0;
		double networkClustering = 0;
		double apl = 0;

		nbNodes = net.nbNodes;
		nbEdges = net.nbEdges;

		// Calcul de la densité
		if (Configurator.isAttribActived(activationCode, NetworkAttribType.DENSITY)) {
			density = (double) nbEdges / ( nbNodes * (nbNodes -1));
		}

		// degré moyen sur les nodes
		if (Configurator.isAttribActived(activationCode, NetworkAttribType.DDAVG)){
			avgDegre = (double)nbEdges / (nbNodes);
		}

		// DD
		if(	Configurator.isAttribActived(activationCode, NetworkAttribType.DDINTERQRT) ||
				Configurator.isAttribActived(activationCode, NetworkAttribType.DDARRAY) ||
				Configurator.isAttribActived(activationCode, NetworkAttribType.AVGCLUST) ) {

			distrib = new int[(int)nbNodes];
			// Hash qui sera utilisé pour trouver les amis des amis

			if(avgClust) {
				connectionsByNode = new Hashtable<Integer, ArrayList<Integer>>();
				connections =  new ArrayList<Integer>();
				clustByNode = new Hashtable<Integer, Double>();
			}

			for (int i : net.nodes.keySet()) {

				TinyNode node = net.nodes.get(i);
				distrib[node.connectedNodes.size()] = ++(distrib[node.connectedNodes.size()]);
				//	nbEdges += node.connectedNodes.size();

				// Dans le cas ou on souhaite avoir le clustering moyen
				if(avgClust){
					connections =  new ArrayList<Integer>();
					for (int j = 0; j< node.connectedNodes.size() ; j++) {
						connections.add(node.connectedNodes.get(j));
					}
					connectionsByNode.put(i, connections);
				}
			}

			// Si espace interquartile
			if(Configurator.isAttribActived(activationCode, NetworkAttribType.DDINTERQRT) ){
				// Ecart inter quartile
				parcouru = 0;
				index = -1;
				double temp = nbNodes * .25f;
				do
				{
					index++;
					parcouru += distrib[index];
				} while ( parcouru < temp);

				firstQ = index;

				// 3er quartile
				parcouru = 0;
				index = -1;
				temp = nbNodes * .75f;
				do
				{
					index++;
					parcouru += distrib[index];
				} while ( parcouru < temp);

				thirdQ = index;
				ddInterQrt = thirdQ - firstQ;
			}

			//si avgClustering
			if(avgClust){
				for (Integer nodeCentral : connectionsByNode.keySet()) {
					nodeClustering = 0;
					for (Integer neigthboor : connectionsByNode.get(nodeCentral)) {
						for (Integer neightOfNeight : connectionsByNode.get(neigthboor)) {
							if(connectionsByNode.get(nodeCentral).contains(neightOfNeight))
								nodeClustering++;
						}
					}

					nodeClustering /= connectionsByNode.get(nodeCentral).size() * ( connectionsByNode.get(nodeCentral).size() - 1 );
					clustByNode.put(nodeCentral, nodeClustering);
				}

				// on retire les cas ou les noeuds n'ont aucune connexion.
				for (Double clust : clustByNode.values())
					if(!clust.isNaN())
						networkClustering += clust;

				networkClustering /= clustByNode.values().size();

			}
		}

		if(Configurator.isAttribActived(activationCode, NetworkAttribType.APL)){
			apl= this.getAPL();
		}


		// mise à jour des données.
		synchronized(networkProp){

			if (Configurator.isAttribActived(activationCode, NetworkAttribType.DENSITY))
				networkProp.setValue(NetworkAttribType.DENSITY,density);
			if (Configurator.isAttribActived(activationCode, NetworkAttribType.DDAVG))
//				networkProp.setDd(distrib);
				networkProp.setValue(NetworkAttribType.DDAVG,avgDegre);
			if (Configurator.isAttribActived(activationCode, NetworkAttribType.DDINTERQRT))
				networkProp.setValue(NetworkAttribType.DDINTERQRT,ddInterQrt);
			if (Configurator.isAttribActived(activationCode, NetworkAttribType.DDARRAY))
				networkProp.setValue(NetworkAttribType.DDARRAY,distrib);
			if (Configurator.isAttribActived(activationCode, NetworkAttribType.NBEDGES))
				networkProp.setValue(NetworkAttribType.NBEDGES, nbEdges);
			if (Configurator.isAttribActived(activationCode, NetworkAttribType.NBNODES))
				networkProp.setValue(NetworkAttribType.NBNODES,nbNodes);
			if(avgClust)
				networkProp.setValue(NetworkAttribType.AVGCLUST, networkClustering);
			if(Configurator.isAttribActived(activationCode, NetworkAttribType.APL))
				networkProp.setValue(NetworkAttribType.APL, apl);
			if(activationCode == Configurator.activationCodeAllAttribExceptDD)
				// met a jour le uuid
				networkProp.setNetworkUuidInstance(net.networkUpdateVersion);
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

	protected int getNbAction() {
		synchronized(nbAction)
		{
			return nbAction;
		}
	}

	public void incrementNbAction(){
		synchronized(nbAction)
		{
			nbAction++;
		}
	}

	protected void resetNbAction(){
		synchronized(nbAction){
			nbAction = 0;
		}
	}

	//region GARBAGE

	/** lancement de thread pour la fonction de stabilité d'un réseau.
	 *
	 */
	public void testStability(){

	}

	//endregion

	//endregion
}
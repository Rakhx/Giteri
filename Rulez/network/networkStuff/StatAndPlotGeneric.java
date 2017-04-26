package networkStuff;

import interfaces.Interfaces.StatAndPlotInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Optional;

import math.Toolz;
import mecanisme.MemeFactory;
import network.NetworkProperties;
import network.TinyNetwork;
import network.TinyNetwork.TinyNode;
import parameters.FittingClass;
import parameters.IModelParameter;
import parameters.IModelParameter.GenericBooleanParameter;
import parameters.IModelParameter.GenericDoubleParameter;
import parameters.IModelParameter.MemeAvailability;
import parameters.IModelParameter.MemeDiffusionProba;
import algo.IExplorationMethod.ExplorationMethod;
import configurator.Configurator;
import configurator.Configurator.EnumExplorationMethod;
import configurator.Configurator.NetworkAttribType;
import entite.EntiteHandler;
import entite.Meme;

/** Classe commune à tous les statAndGraphe
 *
 */
public abstract class StatAndPlotGeneric implements StatAndPlotInterface {
	
	// Element autres
	private Integer nbActionRelative = 0;
	
	// Empeche l'algo de passer au step suivant de fitting sans le passage manuel
	// Mis au niveau du configuraztor.
	// public boolean manuelNextStep = false;
	// Met en pause automatiquement quand l'algo essaye de passer au cran suivant
	public boolean autoPauseIfNexted = Configurator.autoPauseIfNexted;
	
	public boolean goNextStepInManuelMode = !Configurator.manuelNextStep;
	
	boolean debugBeforeSkip = true;
	boolean debug = Configurator.debugStatAndPlot;
	
	/** Constructeur de cet élément de base.
	 * Ne peut etre appelé directement, classe abstract
	 */
	protected StatAndPlotGeneric(){
	
	}
		
	/** Lancement de thread qui va comparer un réseau lu et le réseau en cours.
	 * 
	 */
	public void fitNetwork(int activator){
		(new Thread(){
			  public void run() {
				  boolean stability = activator == 0? true: false;
				  callFactorisedFunction(stability);
			  }
		}).start();
	}
	
	/** PREMIERE FONCTION APPELEE DANS LA LONGUE SERIE DES FITTING SEARCHING
	 * 
	 */
	public void callFactorisedFunction(boolean stabilityRun){
		// Classe de configuration qui contient tout ce qu'il faut pour faire une simu
		FittingClass configuration = new FittingClass(stabilityRun);
		
		// ajout de la fitting classe au listener
		EntiteHandler.getInstance().addEntityListener(configuration);
//		EntiteHandler.getInstance().addMemeListener(configuration);
		
		// initialise les config de simulation genre répartition des comportments etc
		initializeConfigForStability(configuration);
		
		// Lancement d'une simulation
		factorisation(configuration);
		
		// retrait de la fitting classe des listener
		EntiteHandler.getInstance().removeEntityListener(configuration);
//		EntiteHandler.getInstance().removeMemeListener(configuration);
	}

	/** Initialise et instancie pour préparer le fitting. Behavior présent sur la map,
	 * leur range de proba. de transmission. Le type d'exploration qu'on va faire de l'espace 
	 * de param.
	 * 
	 * @param fitting
	 */
	protected void initializeConfigForStability(FittingClass fitting){
		Hashtable<Meme, GenericBooleanParameter> memeDispo = new Hashtable<Meme,GenericBooleanParameter>();
		Hashtable<Integer, IModelParameter<?>>  providers = new Hashtable<Integer, IModelParameter<?>>();
		
		for (Meme meme : MemeFactory.getInstance().getMemeAvailable(true)) 
			memeDispo.put(meme, new GenericBooleanParameter());

		// classe truqué pour tjrs renvoyer tous les behaviors. Trop chiant a refaire en l'enlevant et pas forcement
		// pertinent.
		MemeAvailability memeProvider = new MemeAvailability(memeDispo);
		providers.put(1,memeProvider);
		MemeDiffusionProba memeDiffu = new MemeDiffusionProba(memeProvider.availableMeme, new GenericDoubleParameter(.0,.0,.2,.1));
		providers.put(0,memeDiffu);
		
		if(Configurator.explorator == EnumExplorationMethod.oneShot){
			ArrayList<Double> probaVoulu = new ArrayList<Double>(Arrays.asList(0.788335449538696,0.373092466173732,0.292052580578804,0.438882845642738,0.109153677952613));
			setPreciseValue(probaVoulu, memeDiffu);
		}
		
		fitting.explorator = ExplorationMethod.getSpecificExplorator(Configurator.explorator, providers);
		
	}
	
	private void setPreciseValue(ArrayList<Double> probaVoulu,MemeDiffusionProba memeDiffu){
		Double value;
		int i = 0;
		for (Meme meme : MemeFactory.getInstance().getMemeAvailable(true)){
			value = probaVoulu.get(i);
			i++;
			memeDiffu.specifyMemeBound(meme, new GenericDoubleParameter(value,value, value,.1));
		}
	}
	
	/** Classe factorisée pour les traitements de fitting ou searching. 
	 * 
	 * @param config
	 */
	private void factorisation(FittingClass config){
		config.init();
		int nbActionPassee = 0;
		
		// boucle changement de config RUN++
		do {
			config.newRun();
			
			// On fait nbRunByConfig mesures par configuration pour étudier la variance des résultats REPETITION++ 
			for (int i = 0; i < config.nbRepetitionByConfig; i++) 
			{
				config.newTurn();
				do
				{
					config.com.view.displayMessageOnFitPanel("Work In Progress");
					
					// TODO a voir pour rendre le truc un peu plus flexible sur la circular queue
					// qui prend boucleExterneSize en taille mais qui n'est reset que dans newTurn().
					
					// On répète x fois
					for (int x = 0; x < config.boucleExterneSize; x++) 
					{
						do{
							nbActionPassee  = getNbActionRelative();
						}while(nbActionPassee <= config.nbActionByStep);
						
						NetworkProperties np = NetworkConstructor.getInstance().updatePreciseNetworkProperties(Configurator.getIndicateur(NetworkAttribType.DENSITY));
						config.addDensity(np.getDensity());
						config.computeMemeAppliance();
						resetNbActionRelative();
					}
				
					debugBeforeSkip = config.continuFittingCleanVersion();
					if(!debugBeforeSkip){
						if(debug) System.out.println("Voudrait passer au step suivant");
					}
					
				} while( debugBeforeSkip || !goNextStepInManuelMode );
				
				if(Configurator.manuelNextStep)
					goNextStepInManuelMode = false;
				
				if(autoPauseIfNexted)
				{
					CommunicationModel.getInstance().suspend();
					do{
						try {			Thread.sleep(10);			} catch (InterruptedException e) {e.printStackTrace();}
					}while(autoPauseIfNexted);
					
					autoPauseIfNexted = true;
					CommunicationModel.getInstance().resume();
				}
				
				config.endTurn();
			}
			
			config.endRun();
		
		// Configuration distribution suivante
		}while(config.explorator.gotoNext());
		
		if(!Configurator.jarMode)
			System.out.println(" - Fin de l'exploration - ");
		else {
			EntiteHandler.getInstance().stop();
			NetworkConstructor.getInstance().stop();
			NetworkConstructor.getInstance().resume();
		}
		config.endSimu();
	}
	
	// EndRegion
	
	/** avg;dispersion
	 * 
	 */
	public String getDDInfos(){

		Hashtable<Integer, Double> furDurchschnitt = new Hashtable<Integer, Double>();
		String total = "";
		NetworkConstructor.getInstance().updatePreciseNetworkProperties(Configurator.getIndicateur(NetworkAttribType.DDARRAY));
		int[] dd =NetworkConstructor.getInstance().getNetworkProperties().getDd();

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
		NetworkConstructor.getInstance().updatePreciseNetworkProperties(Configurator.getIndicateur(NetworkAttribType.DDARRAY));
		int[] dd =NetworkConstructor.getInstance().getNetworkProperties().getDd();

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
	
	protected int getNbActionRelative() {
		synchronized(nbActionRelative)
		{
			return nbActionRelative;
		}
	}
	
	public void incrementNbActionRelative(){
		synchronized(nbActionRelative)
		{
			nbActionRelative++;
		}
	}
	
	protected void resetNbActionRelative(){
		synchronized(nbActionRelative){
			nbActionRelative = 0;
		}
	}
	
	// Region GARBAGE
	
	/** lancement de thread pour la fonction de stabilité d'un réseau.
	 * 
	 */
	public void testStability(){

	}

	// EndRegion
	
	// EndRegion
}


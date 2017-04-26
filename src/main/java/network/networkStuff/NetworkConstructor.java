package networkStuff;

import interfaces.Interfaces.DrawerInterface;
import interfaces.Interfaces.StatAndPlotInterface;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;

import math.Toolz;
import network.Edge;
import network.INetworkRepresentations;
import network.INetworkRepresentations.TinyNetworks;
import network.Network;
import network.NetworkProperties;
import run.ThreadHandler;
import configurator.Configurator;

/** Prend en entrée un fichier texte, ou génére aléatoirement un réseau.
 * Va instancier des Node & Edge 
 */
public class NetworkConstructor extends ThreadHandler {

	// Region Properties
	
	DrawerInterface drawer;
	StatAndPlotInterface computer;
	final Network networkInstance;	
	INetworkRepresentations networkRepresentation;
	NetworkProperties networkInstanceProperties;
	
	boolean onceOneStep = true;
	int nbNodeInit = Configurator.nbNode;
	int cmp = 0;
	int activator;

	// EndRegion 	
	
	// Region Constructor

	private static NetworkConstructor INSTANCE = null;
		
	/** Constructeur sans paramètre.
	 * 
	 */
	private NetworkConstructor() {
		if(Configurator.DisplayLogdebugInstantiation)
			System.out.println("Network Constructor Initialisation");
		
		this.drawer = WorkerFactory.getInstance().getDrawer();	
		this.computer = WorkerFactory.getInstance().getCalculator();

		networkInstance = new Network();
		generateNodes();
		WorkerFactory.getInstance().setNetwork(networkInstance);

		if(Configurator.DisplayLogdebugInstantiation)
			System.out.println("Network Constructor Closing");
		
		networkInstanceProperties = new NetworkProperties("Courant");
		networkInstanceProperties.createStub();
		networkRepresentation = new TinyNetworks();
	}

	/** Fourni l'unique instance de la classe.
	 * 
	 */
	public static NetworkConstructor getInstance() {
		if( INSTANCE == null)
			INSTANCE = new NetworkConstructor();
		
		return INSTANCE;
	}
	
	// EndRegion
	
	// Region public methods
	
	/** Toutes les x secondes, lancement du thread. Si 1er lancement, création d'un réseau
	 * random. Affichage du réseau, et modification au fur et a mesure du temps qui passe.
	 * Modif. : Ajout de link. 
	 */
	public void doRun() {
		try {
			Thread.sleep(Configurator.getThreadSleepMultiplicateur());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		OneStep();
	}

	/** Version lancement manuelle des actions. 
	 * 
	 */
	public void OneStep(){
		if(onceOneStep){
			// TODO bon ici pour faire proprement il faudrait lancer cette fonction
			// depuis le thread de l'entité handler directement, apres s'etre assuré que
			// tout les éléments dans le réseau sont ok, peut etre a l'aide d'un join()
			// ou peut etre qu'ya rien a attendre et que c'est direcement passable dans 
			// la partie Entitehandler
			// EntiteHandler.getInstance().giveMemeToEntite();
			
			// drawer.drawThisNetwork(networkInstance);
			onceOneStep = false;
		}
		else
			suspend();
		
		// ICI 
		// Fonction de mise a jour des propriétés du réseau
		// this.computeNetworkProperties()
		// Avoir un object synchronized a lire et écrire
		// this.suspend();
		
//		cmp++;
//		if(cmp % Configurator.refreshInfoRate == 0){
//			displayNetworkInfo();
//			cmp = 0;
//		}
	}
	
	/** Renvoi, après avoir fait le lien entre entités et noeud du réseau,
	 * un thread de lui meme. 
	 */
	public Thread getThread(){		
		// Va créer des entitées pour faire une correspondance entitée / noeuds sur le réseau
		Thread returnThread = new Thread(NetworkConstructor.getInstance());
		returnThread.setName("NC");
		drawer.drawThisNetwork(networkInstance);
		return returnThread;
	}
	
	/** Remise a zero des variables etc pour pouvoir relancer une simulation
	 * Eventuellemen,t y appliquer ensuite les liens la création de l'état de base voulu
	 */
	public void resetStat(){
		onceOneStep = true;	
		networkInstance.resetStat();
//		tinyNetworkInstance.resetTiny();
		networkRepresentation.resetRepresentation();
		networkInstanceProperties.createStub();
	}	
	
	/** Met à jour l'ensemble des propriétés du réseau dans l'instance
	 * networkInstanceProperties dans le cas ou la source du calcul de ces valeurs, le tinyNetwork, 
	 * ait subit des modifications 
	 * 
	 */
	public void updateAllNetworkProperties(){
		if(!isNetPropUpToDate()){
			synchronized (networkInstance) {
				networkRepresentation.ConvertNetwork(networkInstance);
			}
			
			networkInstanceProperties = networkRepresentation.getNetworkProperties(Configurator.activationCodeAllAttribExceptDD);
		}
	}
	
	/** Dans le cas ou on ne les veux pas tous, on ne met pas a jour la valeur de tinynetwork changed.
	 * On recalcule ainsi que la valeur que l'on souhaite sans empecher la mise a jour des autres valeurs
	 * en changeant le boolean marqueur de calcul de tte les propriétés 
	 * 
	 * @param activator
	 * @return
	 */
	public NetworkProperties updatePreciseNetworkProperties(int activator){
		if(!isNetPropUpToDate()){
			synchronized (networkInstance) {
				networkRepresentation.ConvertNetwork(networkInstance);
			}
			
			networkInstanceProperties = networkRepresentation.getNetworkProperties(activator);
		}
		return networkInstanceProperties;
	}

	// Region Network modifications
	
	/** Méthode qui génère un réseau d'un certains type. 
	 * Va générer les liens, mais les noeuds doivent déjà etre présent.
	 * 0 empty,1 4%,  2 30%, 3 scalefree, 4 smallworld, 5 complet
	 * @param activator
	 */
	public void generateNetwork(int activator){
		int nbEdgeToAdd ;
		int choosenNodeToAdd;
		ArrayList<Integer> availableNodes = new ArrayList<Integer>();
		// 4% et 30% respectivement. 
		int pourcentageLow = 2;
		int pourcentageMiddle = 50;
		
		int nbEdgeToAddByNode = activator == 1 ? (nbNodeInit * pourcentageLow/200) : (nbNodeInit * pourcentageMiddle/200);
//					nbNodeInit * ((double)pourcentageLow/200) : nbNodeInit * ((double)pourcentageMiddle/200);
		Hashtable<Integer,Double> KVNodeDegree = new Hashtable<Integer, Double>();
		Integer target; 
		
		// Scale free
		if(activator == 3)
		{
			for (int i = 0; i < nbNodeInit; i++) 
			{
				availableNodes.clear();
				for (int j = 0; j < i; j++)
					if(i != j && !networkInstance.getNode(j).getConnectedNodes().contains(i))
						availableNodes.add(j);
				
				for (Integer integer : availableNodes) {
					KVNodeDegree.put(integer,
							Double.parseDouble("" + networkInstance.getNode(integer).getConnectedNodes().size()));
				}
				
				if(KVNodeDegree.size() > 0){
					target = Toolz.getElementByUniformProba(KVNodeDegree);
					networkInstance.addEdgeToNodes(i, target, false);
				}
			}
		}
		// Réseau complet
		else if (activator == 5){
			for (int i = 0; i < nbNodeInit; i++) 
				for (int j = 0; j < nbNodeInit; j++) 
					if(i != j && !networkInstance.getNode(j).getConnectedNodes().contains(i) )
						networkInstance.addEdgeToNodes(i, j, false);
			
		}
		
		// Small world
		else if (activator == 4){
			double probaRelink = .1;
			int newTarget;
			
			// pour chaque noeud le connecté au deux suivants modulo
			for (int i = 0; i < nbNodeInit; i++) {
				// Ajout de deux link
				for (int j = 1; j < 3; j++) {
					// Ca peut etre redirigé de suite
					if(Toolz.rollDice(probaRelink)){
						do 
						{
							newTarget = Toolz.getRandomNumber(nbNodeInit);
							networkInstance.addEdgeToNodes(i, newTarget, false);
						} while(newTarget == i||newTarget == (i+j)%nbNodeInit );
					} 
					else{
						networkInstance.addEdgeToNodes(i, (i + j) % nbNodeInit, false);
					}
				}
			}
		}
		
		else 
		{
			// ajout des liens entre les noeuds
			for (int i = 0; i < nbNodeInit; i++) 
			{
				// On prépare la liste des noeuds dispos pour linkage
				 availableNodes.clear();
				for (int j = 0; j < nbNodeInit; j++) 
					if(i != j && !networkInstance.getNode(j).getConnectedNodes().contains(i))
						availableNodes.add(j);
				
				// Ajoute X noeuds
				nbEdgeToAdd = 0;
				if(activator == 1 || activator == 2)
					nbEdgeToAdd = nbEdgeToAddByNode;
				if(activator == 5)
					nbEdgeToAdd = Toolz.getRandomNumber(nbNodeInit / 2);
				
				if(activator != 0){
					do 
					{
						if(availableNodes.size() > 0)
						{
							choosenNodeToAdd = Toolz.getRandomElementAndRemoveIt(availableNodes);
							networkInstance.addEdgeToNodes(i, choosenNodeToAdd, false);
						}
					}while (--nbEdgeToAdd > 0);
				}
			}
		}
	}

	/** NETWORK MODIFICATIONS. Ajout d'un noeud.
	 * 
	 */
	public void NMAddNode(){
		int index = networkInstance.addNode();
		drawer.addNode(index);
	}
	
	/** NETWORK MODIFICATIONS. ajout d'un lien
	 * 
	 * @param fromNodeIndex
	 * @param toNodeIndex
	 */
	public void NMAddLink(int fromNodeIndex,int toNodeIndex, boolean directed){
		synchronized(networkInstance)
		{
			networkInstance.addEdgeToNodes(fromNodeIndex, toNodeIndex, directed);
			drawer.addEdge(fromNodeIndex, toNodeIndex);
		}		
	}
	
	/** NETWORK MODIFICATIONS. Retrait d'un lien
	 * 
	 * @param from
	 * @param to
	 */
	public boolean NMRemoveLink(int from, int to, boolean directed){
		boolean sucess ;			
		sucess =  networkInstance.removeEdgeFromNodes(from, to, directed);
		if(sucess){
			drawer.removeEdge(from, to);
		}
		return sucess;
	}
	
	/** NETWORK MODIFICATIONS. Reroutage d'un lien
	 * 
	 * @param from
	 * @param oldTo
	 * @param newTo
	 */
	public void NMChangeLink(int from, int oldTo, int newTo){
		
	}
	
	/** NETWORK MODIFICATIONS. Changement d'une couleur
	 * 
	 * @param from
	 * @param to
	 * @param color
	 */
	public void NMChangeEdgeColor(int from, int to, Color color){
		networkInstance.setColorToEdge(from, to, color);
	}
	
	public void changeColorClass( Integer actingEntite ,ArrayList<Integer> nodeToDesignAsTarget){
		drawer.applyTargetColor(networkInstance, actingEntite, nodeToDesignAsTarget);
	}
	
	public void resetColorClass(){
		drawer.resetGoodColor(networkInstance);
	}
	
	/** Affichage dans la console du network
	 * 
	 */
	public void getNetworkOverview(){
		System.out.println("Network overview : Start");
		
		for (Edge edge : networkInstance.getEdges()) {
			System.out.println(edge.getNodeFrom().getIndex()+"-"+edge.getNodeTo().getIndex());
		}
		
		System.out.println("Network overview : Stop");
		// drawer.networkOverview();
	}
	
	/** Retourne le réseau
	 * 
	 * @return
	 */
	public Network getNetwork(){
		return networkInstance;
	}
	
	/** Retourne les noeuds connectés au noeud en parametre,
	 * null si le noeud en question n'existe pas.
	 * 
	 * @param nodeIndex
	 * @return
	 */
	public ArrayList<Integer> getConnectedNodes(int nodeIndex){
		return networkInstance.getConnectedNodes(nodeIndex);
	}
	
	/**
	 * 
	 * @return
	 */
	public NetworkProperties getNetworkProperties(){
		return this.networkInstanceProperties;
	}
	
	/**
	 * 
	 * @return
	 */
	public INetworkRepresentations getNetworkRepresentations(){
		return networkRepresentation;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isTinyUpToDate(){
		return networkInstance.getUpdateId() == networkRepresentation.getRepresentationUUID();
	}
	
	/** compare réseau réel au properties, sans passer par le tiny
	 * 
	 * @return
	 */
	public boolean isNetPropUpToDate(){
		return networkInstance.getUpdateId() == networkInstanceProperties.getNetworkInstance();
	}
	
	// EndRegion
	
	// EndRegion
	
	// Region Private
	
	/** Création des nodes pour le réseau.
	 * 
	 */
	private void generateNodes(){
		for (int i = 0; i < nbNodeInit; i++) {
			networkInstance.addNode(); 
		}
	}
	
	// EndRegion
	
}

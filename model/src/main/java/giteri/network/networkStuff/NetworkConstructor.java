package giteri.network.networkStuff;

import giteri.network.event.INbNodeChangedListener;
import giteri.network.event.NbNodeChangedEvent;
import giteri.run.interfaces.Interfaces;

import java.awt.Color;
import java.util.*;

import giteri.tool.math.Toolz;
import giteri.network.network.Edge;
import giteri.network.network.IInternalNetReprestn;
import giteri.network.network.IInternalNetReprestn.TinyNetworks;
import giteri.network.network.Network;
import giteri.network.network.NetworkProperties;
import giteri.run.ThreadHandler;
import giteri.run.configurator.Configurator;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.IdAlreadyInUseException;

/** Prend en entrée un fichier texte, ou génére aléatoirement un réseau.
 * Va instancier des Node & Edge
 * Mécanisme qui ne met pas à jour systématiquement la représentation du réseau a chaque action des entités.
 * Le fera si une demande de calcul ou d'affichage est faite.
 */
public class NetworkConstructor extends ThreadHandler implements INbNodeChangedListener {

	//region Properties
	Interfaces.DrawerNetworkInterface drawer;
	final Network networkInstance;
	IInternalNetReprestn networkRepresentation; // Pour l'instant le tiny n'est pas mis a jour en meme temps que les networks
	NetworkProperties networkInstanceProperties;
	// Liste des représentations a mettre a jour au fur et a mesure de la simultion. Network; Opt<matrice>
	Set<IInternalNetReprestn> netRepzsToUpdate;
	boolean onceOneStep = true;
	int nbNodeInit = Configurator.getNbNode();
	int cptSale = 0;

	//endregion

	/** Constructeur sans paramètre.
	 *
	 */
	public NetworkConstructor() {
		netRepzsToUpdate = new HashSet<>();
		networkInstance = new Network();
		netRepzsToUpdate.add(networkInstance);

		// Représentation matriciel qui double la rep. network, utilisé pour les filtes hop away
		IInternalNetReprestn.AdjacencyMatrixNetwork matrice = new IInternalNetReprestn.AdjacencyMatrixNetwork();
		netRepzsToUpdate.add(matrice);

		generateNodes();
		networkInstanceProperties = new NetworkProperties("Courant");
		networkInstanceProperties.createStub();
		networkRepresentation = new TinyNetworks();
		generateNetwork(Configurator.initialnetworkForBase);
	}

	public void setDrawer(Interfaces.DrawerNetworkInterface drawer){
		this.drawer = drawer;
	}

	//region public methods

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
	}

	/** Renvoi, après avoir fait le lien entre entités et noeud du réseau,
	 * un thread de lui meme.
	 */
	public Thread getThread(){
		// Va créer des entitées pour faire une correspondance entitée / noeuds sur le réseau
		Thread returnThread = new Thread(this);
		returnThread.setName("NC");
		drawer.drawThisNetwork(networkInstance, false);
		return returnThread;
	}

	/** Remise a zero des variables etc pour pouvoir relancer une simulation
	 * Eventuellement, y appliquer ensuite les liens la création de l'état de base voulu
	 */
	public void resetStat(){
		onceOneStep = true;
		for (IInternalNetReprestn netRepz : netRepzsToUpdate) {
			netRepz.resetRepresentation();
		}

//		networkInstance.resetRepresentation();
		networkRepresentation.resetRepresentation();
		networkInstanceProperties.createStub();
	}

	/** Dans le cas ou on ne les veux pas tous, on ne met pas a jour la valeur de tinynetwork changed.
	 * On recalcule ainsi que la valeur que l'on souhaite sans empecher la mise a jour des autres valeurs
	 * en changeant le boolean marqueur de calcul de tte les propriétés
	 *
	 * @param activator
	 * @return
	 */
	public NetworkProperties updatePreciseNetworkProperties(int activator){
		// Si le tiny n'es pas en accord avec le current
		if(!isNetPropUpToDate()) {
			synchronized (networkInstance) {
				networkRepresentation.convertNetwork(networkInstance);
			}
		}

		networkInstanceProperties = networkRepresentation.getNetworkProperties
				(Optional.of(networkInstanceProperties),"Courant",activator);

		return networkInstanceProperties;
	}

	//region Network modifications

	/** Méthode qui génère un réseau d'un certains type.
	 * Va générer les liens, mais les noeuds doivent déjà etre présent.
	 * 0 empty,1 4%,  2 30%, 3 scalefree, 4 smallworld, 5 complet, 6 custom random, 7 forest fire
	 * @param activator
	 */
	public void generateNetwork(int activator){
		int nbEdgeToAdd, choosenNodeToAdd;
		ArrayList<Integer> availableNodes = new ArrayList<>();
		Hashtable<Integer,Double> KVNodeDegree = new Hashtable<>();
		Integer target;
		// 4% et 30% respectivement.
		int pourcentageLow = 2;
		int pourcentageMiddle = 30;
		double firePropa = .2;
		int m = 2; // Parametre du nombre de connection pour un scale free
		int nbEdgeToAddByNode = 0;
		boolean makeMe = false;


		if(activator == 0){ // EMPTY

		}
		else if(activator == 1){ // 4%
			nbEdgeToAddByNode = (nbNodeInit * pourcentageLow/200);
			makeMe = true;
		}
		else if(activator == 2){ // 30%
			nbEdgeToAddByNode = nbNodeInit * pourcentageMiddle/200;
			makeMe = true;
		}
		else if(activator == 3){ // SCALE FREE
			// modification lazy sous opti pour prendre en compte le m dans la génération
			for (int i = 0; i < nbNodeInit; i++) // pour chaque noeud i
			{
				availableNodes.clear();
				for (int j = 0; j < i; j++) // // pour chaque noeud j
					if (i != j && !networkInstance.getNode(j).getConnectedNodes().contains(i))
						availableNodes.add(j); // Si i et j non connectés
				for (Integer integer : availableNodes) { // on trouve le degré des noeuds disponibles
					KVNodeDegree.put(integer, Double.parseDouble("" + networkInstance.getNode(integer).getConnectedNodes().size()));
				}
				for(int k = 0; k < m;k++) {
					if (KVNodeDegree.size() > 0) {
						target = Toolz.getElementByUniformProba(KVNodeDegree);
						for (IInternalNetReprestn iInternalNetReprestn : netRepzsToUpdate) {
							iInternalNetReprestn.addNodeWithEdge(i, target, false);
						}
//						networkInstance.addEdgeToNodes(i, target, false);
						KVNodeDegree.remove(target);
					}
				}
			}
		}
		else if(activator == 4){ // SMALL WORLD
			double probaRelink = 0.1;//.1;
			int newTarget;
			int nbNodeLattice = 11;

			// pour chaque noeud le connecter au deux suivants modulo
			for (int i = 0; i < nbNodeInit; i++) {
				// Ajout de deux link
				for (int j = 1; j < 1+nbNodeLattice; j++) {
					// Ca peut etre redirigé de suite
					if(Toolz.rollDice(probaRelink)){
						do
						{
							newTarget = Toolz.getRandomNumber(nbNodeInit);
						} while(newTarget == i || networkInstance.areNodesConnected(i,newTarget)
								|| networkInstance.areNodesConnected(newTarget,i));//newTarget == (i+j) % nbNodeInit );

						for (IInternalNetReprestn iInternalNetReprestn : netRepzsToUpdate) {
							iInternalNetReprestn.addNodeWithEdge(i, newTarget, false);
						}
//						networkInstance.addEdgeToNodes(i, newTarget, false);
					}
					else{
						for (IInternalNetReprestn iInternalNetReprestn : netRepzsToUpdate) {
							iInternalNetReprestn.addNodeWithEdge(i, (i + j) % nbNodeInit, false);
						}
//						networkInstance.addEdgeToNodes(i, (i + j) % nbNodeInit, false);
					}
				}
			}
		}
		else if(activator == 5){ // COMPLET
			for (int i = 0; i < nbNodeInit; i++)
				for (int j = 0; j < nbNodeInit; j++)
					if(i != j && !networkInstance.getNode(j).getConnectedNodes().contains(i) )
						for (IInternalNetReprestn iInternalNetReprestn : netRepzsToUpdate) {
							iInternalNetReprestn.addNodeWithEdge(i, j , false);
						}

//						networkInstance.addEdgeToNodes(i, j, false);
		}
		else if(activator == 6){ // CUSTOM RANDOM
			nbEdgeToAddByNode = 5614/500;
			makeMe = true;
		}
		else if(activator == 7){ // FOREST FIRE
			int choosed;
			int nbToAdd;
			List<Integer> linkWannaBeIn = new ArrayList<>();
			List<Integer> linkIn = new ArrayList<>();
			for (int i = 1; i < nbNodeInit; i++) { // pour chaque noeud du réseau pris un par un
				linkIn.clear();
				linkWannaBeIn.clear();
				choosed = Toolz.getRandomNumber(i); // on en choisit un d'index inférieur
				linkIn.add(choosed);
				linkWannaBeIn.addAll(networkInstance.getConnectedNodes(choosed)); // et on va regarder ses voisins
				recursiveFF(linkIn,linkWannaBeIn, firePropa);
				//System.out.println(linkIn.size());
				for (Integer integer : linkIn) {
					if(!networkInstance.areNodesConnected(i, integer))
						for (IInternalNetReprestn iInternalNetReprestn : netRepzsToUpdate) {
							iInternalNetReprestn.addNodeWithEdge(i, integer, false);
						}
				}
			}
		}

		if (  makeMe ) {
			// ajout des liens entre les noeuds
			for (int i = 0; i < nbNodeInit; i++)
			{
				// On prépare la liste des noeuds dispos pour linkage
				availableNodes.clear();
				for (int j = 0; j < nbNodeInit; j++)
					if(i != j && !networkInstance.getNode(j).getConnectedNodes().contains(i))
						availableNodes.add(j);
				nbEdgeToAdd = nbEdgeToAddByNode;
				do
				{
					if(availableNodes.size() > 0)
					{
						choosenNodeToAdd = Toolz.getRandomElementAndRemoveIt(availableNodes);
						for (IInternalNetReprestn iInternalNetReprestn : netRepzsToUpdate) {
							iInternalNetReprestn.addNodeWithEdge(i, choosenNodeToAdd, false);
						}
//						networkInstance.addEdgeToNodes(i, choosenNodeToAdd, false);
					}
				}while (--nbEdgeToAdd > 0);
			}
		}
	}

	private void recursiveFF(List<Integer> listToLink, List<Integer> listWannaBeIn, double addProba){
		listWannaBeIn.removeAll(listToLink);
		int nbToAdd = Toolz.jureDistrib(addProba)- 1 ;
		for (Integer integer : listWannaBeIn) {
			if(nbToAdd > 0)
			if(Toolz.rollDice(addProba)) {
				listToLink.add(integer);
				nbToAdd--;
				recursiveFF(listToLink, networkInstance.getConnectedNodes(integer), addProba);
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

			for (IInternalNetReprestn iInternalNetReprestn : netRepzsToUpdate) {
				iInternalNetReprestn.addNodeWithEdge(fromNodeIndex, toNodeIndex, directed);
			}

			try {
				drawer.addEdge(fromNodeIndex, toNodeIndex);
			}catch(EdgeRejectedException ere){
				System.out.println(ere.getMessage());
			}catch(IdAlreadyInUseException iaiue){
				System.out.println(iaiue.getMessage());

			}
		}

//		cptSale++;
//		if(cptSale% 10000 == 0){
//			synchronized (networkInstance) {
//				if(!tmp.checkConsistency(networkInstance)){
//					System.out.println("WOOWOWW");
//				}else
//					System.out.println("ALLFINE");
//			}
//		}
	}

	/** NETWORK MODIFICATIONS. Retrait d'un lien
	 *
	 * @param from
	 * @param to
	 */
	public boolean NMRemoveLink(int from, int to, boolean directed){
		for (IInternalNetReprestn iInternalNetReprestn : netRepzsToUpdate) {
			iInternalNetReprestn.removeEdgeFromNodes(from, to, directed);
		}
		drawer.removeEdge(from, to);
		return true;
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

	public void changeColorClass( Integer actingEntite , Set<Integer> nodeToDesignAsTarget){
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

	/** Retour la matrice d'adjacency
	 *
	 * @return
	 */
	public boolean[][] getNetworkMatrix(){

		for (IInternalNetReprestn iInternalNetReprestn : netRepzsToUpdate) {
			if (iInternalNetReprestn instanceof IInternalNetReprestn.AdjacencyMatrixNetwork) {
				return ((IInternalNetReprestn.AdjacencyMatrixNetwork) iInternalNetReprestn).getMatrix();
			}
		}

		return null;
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
	public IInternalNetReprestn getNetworkRepresentations(){
		return networkRepresentation;
	}

	/**
	 *
	 * @return
	 */
	public boolean isTinyUpToDate(){
		return networkInstance.getRepresentationUUID() == networkRepresentation.getRepresentationUUID();
	}

	public boolean isVersionCorrect(int version){
		return networkInstance.getRepresentationUUID() == version;
	}

	/** compare réseau réel au properties, sans passer par le tiny
	 *
	 * @return
	 */
	public boolean isNetPropUpToDate(){
		return (Integer.compare(networkInstance.getRepresentationUUID(),networkInstanceProperties.getNetworkInstance() ) == 0);
	}

	//endregion

	//endregion

	//region Private

	/** Création des nodes pour le réseau.
	 *
	 */
	private void generateNodes(){
		for (int i = 0; i < nbNodeInit; i++) {
			networkInstance.addNode();
		}
	}

	@Override
	public void handlerNbNodeChanged(NbNodeChangedEvent e) {
		nbNodeInit = e.nbNode;
		networkInstance.fullResetStat();
		generateNodes();

	}

	//endregion

}

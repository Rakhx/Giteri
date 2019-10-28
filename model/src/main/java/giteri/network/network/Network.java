package giteri.network.network;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

/** Objet réseau en lui giteri.meme. Des noeuds et des edges.
 * Pas de corrélation entre l'attribut index du noeud et sa place dans la liste
 */
public class Network {
	
	//region Properties
	
	private ArrayList<Node> nodes;
	private ArrayList<Edge> edges;
	private Integer updateId;

	//endregion

	// Constructeur

	public Network (){
		nodes = new ArrayList<>();
		edges = new ArrayList<>();
		updateId = -1;
	}

	//region Public methods

	/** On supprime tous les edges
	 *
	 */
	public synchronized void resetStat(){
		edges.clear();
	}

	/** On supprime tous les edges et nodes
	 *
	 */
	public synchronized void fullResetStat(){
		edges.clear();
		nodes.clear();
	}

	/** Méthode pour ajouter un edge 
	 * 
	 * @param from
	 * @param to
	 * @param directed
	 */
	public synchronized boolean addEdgeToNodes(int from, int to, boolean directed){
		boolean succes = false;
		Node nodeOne = getNode(from);
		Node nodeTwo = getNode(to);
		if(nodeOne != null && nodeTwo != null)
		{
			addEdge(new Edge(nodeOne, nodeTwo, directed));
			nodeOne.addConnectedNode(nodeTwo);
			if(!directed)
				nodeTwo.addConnectedNode(nodeOne);
			succes = true;
		}
		
		return succes;
	}
	
	/** Retrait d'un edge de la liste des edges dans le réseau
	 *  et dissociation
	 * des noeuds pour qu'ils ne se considérent pas comme connecté ensemble. 
	 * Singulier si l'edge est dirigé, pluriel si non dirigé.
	 * @param from
	 * @param to
	 * @return
	 */
	public synchronized boolean removeEdgeFromNodes(int from, int to, boolean directed){
		Edge toRemove = null;
		Node nodeOne, nodeTwo;
		boolean succes = false;
				
		// Trouver l'edge correspondant
		nodeOne = getNode(from);
		nodeTwo = getNode(to);
		if(nodeOne != null && nodeTwo != null)
			toRemove = getEdge(nodeOne, nodeTwo, directed);
		
		// Le supprimer
		if(toRemove != null)
		{
			removeEdgeFromNodes(toRemove, nodeOne, nodeTwo);	
			succes = true;	
		}		
		
		return succes;
	}
	
	/** Ajoute un noeud dans le réseau
	 * Choisit l'index du noeud a la taille du réseau,
	 * si cet index est déjé pris ajoute un, et réessaye
	 * @return l'index du noeud ajouté
	 */
	public synchronized int addNode(){
		int index = nodes.size();
		while(getNode(index) != null)
			index++;
		addNode(index);
		return index;
	}
	
	public synchronized Integer getUpdateId() {
		return updateId;
	}

	/** Obtient le node possédant l'index donné
	 * 	
	 * @param index du node
	 * @return le node possédant l'index, null sinon.
	 */
	public synchronized Node getNode(int index){
//		synchronized(lockNodeAndEdge){
		for (Node node : nodes) {
			if(node.getIndex() == index)
				return node;
		}
		return null;
//		}
	}
	
	/** Obtient la liste des nodes du réseau.
	 * 
	 * @return une arraylist de node.
	 */
	public synchronized ArrayList<Node> getNodes(){
		return  this.nodes;
	}
	
	/** Retourne les index des noeuds connectés au noeud dont l'index est en parametre, 
	 * null si le noeud en quesiton n'existe pas
	 * 
	 * @param nodeIndex
	 * @return
	 */
	public synchronized ArrayList<Integer> getConnectedNodes(int nodeIndex){
		ArrayList<Integer> nodes = null;
		if(getNode(nodeIndex) != null){
			nodes = new ArrayList<Integer>();
			for (Integer integer : getNode(nodeIndex).getConnectedNodes() ) {
				nodes.add(new Integer(integer));
			}
		}
		
		return nodes;
	}
	
	/** Obtient la liste des edges.
	 *
	 * @return la liste des edges
	 */
	public synchronized  ArrayList<Edge> getEdges(){
			return this.edges;
	}

	/** Alloue une couleur a un edge s'il existe
	 *
	 * @param from
	 * @param to
	 * @param color
	 */
	public synchronized void setColorToEdge(int from,int to, Color color){
		Edge edge = getEdge(from, to, false);
		if(edge != null)
			edge.setMyColor(color);
	}

	/** Return true si les noeuds représentés par leur index sont connectés.
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public boolean areNodesConnected(int from, int to){
		return nodes.get(from).getConnectedNodes().contains(to);
	}

	/** Retourne le réseau sous forme textuel.
	 *
	 */
	public synchronized String toString(){
		String resultat = "";
		Collections.sort(edges, new Edge());
		Edge currentEdge;

		for (int i = 0; i < edges.size(); i++) {
			currentEdge = edges.get(i);
			resultat += "Node["+currentEdge.from.getIndex()+"] <---> Node["+ currentEdge.to.getIndex() + "]";
			resultat += "\n";
		}

		return resultat;
	}

	//endregion

	//region Private Methode

	/** Ajoute un node a la liste des nodes,
	 * si il n'existe pas de node possédant cet index.
	 *
	 * @return renvoi le node ainsi créé
	 */
	private synchronized Node addNode(int index){
		Node added = null;
//		synchronized (lockNodeAndEdge) {
			if(getNode(index) == null){
				added = new Node(index);
				nodes.add(added);
			}
//		}
		updateId ++;
		return added;
	}
	/** Ajoute un edge a la liste d'edge
	 *
	 * @param edgeee
	 */
	private synchronized void addEdge(Edge edgeee) {
//		synchronized (lockNodeAndEdge) {
			edges.add(edgeee);
//		}
//
		updateId ++;
	}

	/** Retire de la liste l'edgee spécifié
	 *
	 * @param edgeee
	 */
	private synchronized void removeEdge(Edge edgeee){
//		synchronized (lockNodeAndEdge) {
			edges.remove(edgeee);
//		}

		updateId ++;
	}

	/** Trouve un edge en fonction de l'index de node in out et
	 * d'un boolean directed
	 * @param from
	 * @param to
	 * @param directed
	 * @return l'edge trouvé ou nul
	 */
	private synchronized Edge getEdge(int from, int to, boolean directed){
	    Edge edgeToReturn = null;
		Node nodeFrom, nodeTo;

		// Trouver l'edge correspondant
		nodeFrom = getNode(from);
		nodeTo = getNode(to);
		if(nodeFrom != null && nodeTo != null)
			for (Edge edge : getEdges()) {
				if(edge.getNodeFrom() == nodeFrom && edge.getNodeTo() == nodeTo)
					edgeToReturn = edge;

				if(!directed && edge.getNodeFrom() == nodeTo && edge.getNodeTo() == nodeFrom)
					edgeToReturn = edge;
			}

		return edgeToReturn;
	}

	/** Retrouve un edge qui a comme point de départ le node spéficié, de giteri.meme pour l'arrivé.
	 * Ou le contraire, si directed = false
	 *
	 * @param nodeFrom Noeud de départ pour l'edge
	 * @param nodeTo Noeufd d'arrivée pour l'edge
	 * @param directed efface la notion de départ et arrivé
	 * @return l'edge correspondant, null si pas trouvé.
	 */
	private synchronized Edge getEdge(Node nodeFrom, Node nodeTo, boolean directed){
		for (Edge edge : getEdges()) {
			if(edge.getNodeFrom() == nodeFrom && edge.getNodeTo() == nodeTo){
				return edge;
			}
			if(!directed && edge.getNodeFrom() == nodeTo && edge.getNodeTo() == nodeFrom){
				return edge;
			}
		}

		return null;
	}

	/** Retrait d'un edge de la liste des edges du réseau, et dissociation
	 * des noeuds pour qu'ils ne se considérent pas comme connecté ensemble.
	 * Singulier si l'edge est dirigé, pluriel si non dirigé.
	 *
	 * @param edgeee edge a retiré
	 * @param from depuis
	 * @param to vers.
	 */
	private synchronized void removeEdgeFromNodes(Edge edgeee, Node from, Node to){
		to.removeConnectedNode(from);
		if(!edgeee.isDirected())
			from.removeConnectedNode(to);
		this.removeEdge(edgeee);
	}

	//endregion

	//region Unused
	/** Donne le nombre de node dans le réseau
	 *
	 * @return la taille de la liste de node
	 */
	public synchronized int getNbNodes(){
		return this.nodes.size();
	}

	/** Obtient le nombre d'edge du réseau
	 *
	 * @return la size de la liste
	 */
	public synchronized int getNbEdges(){
		return this.edges.size();
	}

	/** Alloue une couleur a un node s'il existe
	 *
	 * @param index
	 * @param color
	 */
	public synchronized void setColorToNode(int index, Color color){
		Node node = getNode(index);
		if(node!= null)
			node.setMyColor(color);
	}

	//endregion
}

package giteri.network.network;

import java.util.ArrayList;
import java.util.Hashtable;

/** Petit giteri.network. Cute.
 * Réseau sous la forme Hash<Index, Node>
 * Et dans les Node : int[] index des nodes connectés.
 */
public class TinyNetwork {
	
	public Hashtable<Integer, TinyNode> nodes;
	public int nbEdges;
	public int nbNodes;
	public Integer networkUpdateVersion;
	
	public TinyNetwork(){
		nodes = new Hashtable<Integer, TinyNetwork.TinyNode>();
		networkUpdateVersion = -3;
	}
	
	public void resetTiny(){
		nodes.clear();
		nbEdges = 0;
		nbNodes = 0;
	}

	/** Ajout d'une occurence de connection entre un noeud src et un integer
	 * represetant l'index de la cible
	 * 
	 * @param nodeIndex
	 * @param connectedNode
	 */
	public void addConnection(int nodeIndex, int connectedNode ){
		TinyNode src = nodes.get(nodeIndex);
		src.connectedNodes.add(connectedNode);
	}
	
	/** Le réseau contient t-il un node?
	 * 
	 * @param index
	 * @return true si le contenant.
	 */
	public boolean isContainingNode(int index){
		return nodes.keySet().contains(index);
	}
	
	/** vérifie par rapport a un id en parametre la concordance
	 * des versions. 
	 * 
	 * @param networkId
	 * @return 
	 */
	public boolean isUpToDate(Integer networkId){
		return networkUpdateVersion == networkId;
	}
	
	/** Renvoi une string résumant le réseau.
	 * 
	 * @return
	 */
	public String getOverview(){
		String resultat = "";
		for (TinyNode node: nodes.values()) {
			for (int i = 0; i < node.connectedNodes.size(); i++) {
				resultat += node.index + ":" + node.connectedNodes.get(i) + "\n";
			}
		}
		return resultat;
	}
	
	/** Classe de node, réduite en espace mémoire
	 * 
	 */
	public class TinyNode{
		int index;
		public ArrayList<Integer> connectedNodes;
		
		public TinyNode(int ind){
			index = ind;
		}
		
		/** Constructeur d'un tinyNode
		 * 
		 * @param ind
		 * @param linked
		 */
		public TinyNode(int ind, ArrayList<Integer> linked){
			index = ind;
			connectedNodes =new ArrayList<Integer>();
			for (Integer integer : linked) {
				connectedNodes.add(integer);
			}
		}
		
		
	}
	
	/** Classe d'edge, réduite en espace mémoire.
	 *
	 */
	public class TinyEdge{
		int from, to;
		public TinyEdge(int from, int to){
			this.from = from;
			this.to = to;
		}
	}
	
}

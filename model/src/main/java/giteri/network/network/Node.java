package giteri.network.network;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

/** Noeud de la couche réseau
 *
 */
public class Node {
	
	Point position;
	// non utilisé dans la création d'un graphStream, peut etre source de confusion // non 
	// simularité entre deux états.
    private ArrayList<Integer> connectedNodes;
    int index;    
    Color myColor;
    // Info pour l'affichage on mouseOver de l'interface
    String displayInfo;
    
    // Info detaillé pour l'affichage dans la fenetre de texte sur le coté.
    String displayDetailledInfo;
    Object lockOnConnectedNodes;
	
	public Node(int indexxx){
		index = indexxx;
		connectedNodes = new ArrayList<Integer>();
		myColor = new Color(0, 1, 0);
		displayInfo = ""+index;
		lockOnConnectedNodes = new Object();
	}
	
	/** Reprend l'état de base, c'est a dire sans les connections
	 * 
	 */
	public void resetStat(){
		connectedNodes.clear();
		displayInfo = "";
		displayDetailledInfo ="";
	}

	//region Getter Setter
	
	public void addConnectedNode(Node toAdd){
		synchronized(lockOnConnectedNodes){
			connectedNodes.add(toAdd.getIndex());
		}
	}
	
	public void removeConnectedNode(Node toRemove){
		synchronized(lockOnConnectedNodes){
			connectedNodes.remove((Object)toRemove.getIndex());
		}
	}
	
	public final ArrayList<Integer> getConnectedNodes(){
		synchronized(lockOnConnectedNodes){
			return this.connectedNodes;
		}
	}
	
	public int getIndex()
	{return index;}

	public Color getColor() {
		return myColor;
	}
		
	public void setMyColor(Color myColor) {
		this.myColor = myColor;
	}
	
	public String toString(){
		return ""+index;
	}

	public void setDisplayInfo(String displayInfo) {
		this.displayInfo = displayInfo;
	}

	public int getDegree(){
		return this.getConnectedNodes().size();
	}
	//endregion

}	

package network;

import java.awt.Color;
import java.util.Comparator;

/**
 * Classe d'edge. des noeuds, dirigé ou non.
 *
 */
public class Edge implements Comparator<Edge>{
	boolean directed;
	Node from, to;
	Color myColor;
	
	public Edge(){
		
	}
	public Edge(Node fromParam, Node toParam, boolean directedParam){
		from = fromParam;
		to = toParam;
		directed = directedParam;
		myColor = new Color(0, 1, 0);
	}

	public int getFromNodeIndex(){
		return from.index; 
	}
	public int getToNodeIndex(){
		return to.index; 
	}
	
	public int compare(Edge arg0, Edge arg1) {
		if( arg0.from.getIndex() < arg1.from.getIndex()){
			return -1;
		}else if (arg0.from.getIndex() > arg1.from.getIndex()){
			return 1;
		}
		return 0;
	}
	
	public Node getNodeFrom(){
		return from;
	}
	public Node getNodeTo(){
		return to;
	}
	public Color getMyColor() {
		return myColor;
	}
	public void setMyColor(Color myColor) {
		this.myColor = myColor;
	}
	
	public String toString(){
		return "from:" + from + " to:" + to; 
	}
	public synchronized boolean isDirected() {
		return directed;
	}
	public synchronized void setDirected(boolean directed) {
		this.directed = directed;
	}
}

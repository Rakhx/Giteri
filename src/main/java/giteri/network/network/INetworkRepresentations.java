package giteri.network.network;

import giteri.run.interfaces.Interfaces.INetworkRepresentation;

import java.util.ArrayList;
import java.util.Hashtable;

import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.NetworkAttribType;

/** Interface et impplémentations des différentes représentation 
 * du reseau.
 * 
 */
public interface INetworkRepresentations extends INetworkRepresentation{
	
	/** Représentation direct du giteri.network. Utile pour copier le réseau
	 * courant sous une forme plus simple, et pouvoir ensuite calculer
	 * des propriétés sur ce réseau en laissant l'original se modifier. 
	 * 
	 *
	 */
	public class TinyNetworks implements INetworkRepresentations{
		private int networkVersion;
		private Hashtable<Integer, ArrayList<Integer>> nodesAndConnections;
		private int nbNodes, nbEdges;
		
		/** Constructeur sans param. 
		 * 
		 */
		public TinyNetworks(){
			networkVersion = -1;
			nodesAndConnections = new Hashtable<Integer, ArrayList<Integer>>();
			nbNodes = -1;
			nbEdges = -1;
		}
		
		@Override
		public void ConvertNetwork(Network toCopy) {
			ArrayList<Integer> linkOfANode;
			nbNodes = 0;
			nbEdges = 0;
			
			for (Node node : toCopy.getNodes()){
				nbNodes++;
				linkOfANode = new ArrayList<Integer>();
				for (Integer integer : node.getConnectedNodes()) 
					linkOfANode.add(new Integer(integer));
				nbEdges += node.getConnectedNodes().size();
				nodesAndConnections.put(node.index, linkOfANode);
			}
			
			networkVersion = toCopy.getUpdateId();
		}

		@Override
		public NetworkProperties getNetworkProperties(int activationCode) {

			int parcouru, index,firstQ, thirdQ;
			double density = -1, avgDegre = -1 ;
			int[] distrib = new int[0];
			int ddInterQrt = -1;
			@SuppressWarnings("unused")
			double avgClustering = 0;
			// Pas de passage par le gc pour "libérer" une variable simple, plus performant que d'appeler plusieurs fois le configurator.isattribActivated
			boolean avgClust = Configurator.isAttribActived(activationCode, NetworkAttribType.AVGCLUST);
			//RP: Concernant le calcul pour les clustering moyen
//			Hashtable<Integer, ArrayList<Integer>> connectionsByNode = null;
			Hashtable<Integer, Double> clustByNode = null; 
//			ArrayList<Integer> connections = null;
			double nodeClustering = 0;
			double networkClustering = 0;
			NetworkProperties networkPropertiesResulting = new NetworkProperties();
			networkPropertiesResulting.createStub();
			networkPropertiesResulting.setNetworkUuidInstance(networkVersion);
			
			networkPropertiesResulting.nbEdges = nbEdges;
			networkPropertiesResulting.nbNodes = nbNodes;
			
			// Calcul de la densité
			if (Configurator.isAttribActived(activationCode, NetworkAttribType.DENSITY)) {
				density = (double) nbEdges / ( nbNodes * (nbNodes -1));
				networkPropertiesResulting.density = density;
			}

			// degré moyen sur les nodes
			if (Configurator.isAttribActived(activationCode, NetworkAttribType.DDAVG)){
				avgDegre = (double)nbEdges / (nbNodes);
				networkPropertiesResulting.ddAvg = avgDegre;
			}
			
			// DD
			if(	Configurator.isAttribActived(activationCode, NetworkAttribType.DDINTERQRT) ||
				Configurator.isAttribActived(activationCode, NetworkAttribType.DDARRAY) ||
				Configurator.isAttribActived(activationCode, NetworkAttribType.AVGCLUST) ) {
				
				distrib = new int[nbNodes];
				
				if(avgClust) 
					clustByNode = new Hashtable<Integer, Double>();
				
				for (ArrayList<Integer> connections : nodesAndConnections.values()) 
		 			distrib[connections.size()] = ++(distrib[connections.size()]);
				
				networkPropertiesResulting.setDd(distrib);
				
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
					networkPropertiesResulting.ddInterQrt = ddInterQrt;
				}
				
				//si avgClustering
				if(avgClust){
					for (Integer nodeCentral : nodesAndConnections.keySet()) {
						nodeClustering = 0;
						for (Integer neigthboor : nodesAndConnections.get(nodeCentral)) {
							for (Integer neightOfNeight : nodesAndConnections.get(neigthboor)) {
								if(nodesAndConnections.get(nodeCentral).contains(neightOfNeight))
									nodeClustering++;
							}
						}
						
						nodeClustering /= nodesAndConnections.get(nodeCentral).size() * ( nodesAndConnections.get(nodeCentral).size() - 1 );
						clustByNode.put(nodeCentral, nodeClustering);
					}
					
					// on retire les cas ou les noeuds n'ont aucune connexion.
					for (Double clust : clustByNode.values()) 
						if(!clust.isNaN())
						networkClustering += clust;
					
					networkClustering /= clustByNode.values().size();
					networkPropertiesResulting.avgClust = networkClustering;
				}
			}
			
			return networkPropertiesResulting;
		}

		/** Obtient l'id du réseau représenté
		 * 
		 */
		public int getRepresentationUUID() {
			return networkVersion;
		}

		@Override
		public void resetRepresentation() {
			nbNodes = -1; 
			nbEdges = -1;
			networkVersion = -1;
			nodesAndConnections = new Hashtable<Integer, ArrayList<Integer>>();
		}
		
		@Override
		public synchronized ArrayList<String> getNetworkEdges(){
			ArrayList<String> edges = new ArrayList<String>();
			ArrayList<Integer> nodesIndex = new ArrayList<Integer>();
			ArrayList<Integer> nodeLinks  = new ArrayList<Integer>();
			nodesIndex.addAll(nodesAndConnections.keySet());
			nodesIndex.sort(null);
			
			for (Integer index : nodesIndex) {
	    		nodeLinks.clear();
	    		nodeLinks = nodesAndConnections.get(index);
	    		nodeLinks.sort(null);
	    		for (Integer integer : nodeLinks) {
	    			edges.add(index + " " + integer);
				}
				
			}
			
			return edges;
		}

	}
	
	/** Representation matricielle d'un réseau. Certains calcul sont plus
	 * rapide sur ce genre de représentation. 
	 *
	 */
	public class AdjacencyMatrixNetwork implements INetworkRepresentations {

		int[][] matrix;
		int nbNodes;
		
		@Override
		public void ConvertNetwork(Network toCopy) {
			nbNodes = toCopy.getNbNodes();
			matrix = new int[nbNodes][nbNodes];
			for (Edge edge : toCopy.getEdges()) 
				matrix[edge.from.getIndex()][edge.to.getIndex()] = matrix[edge.to.getIndex()][edge.from.getIndex()] = 1; 
		}

		@Override
		public NetworkProperties getNetworkProperties(int activator) {
			return null;
		}

		@Override
		public int getRepresentationUUID() {
			return 0;
		}

		@Override
		public void resetRepresentation() {
			matrix = null;
			
		}

		@Override
		public ArrayList<String> getNetworkEdges() {
			return null;
		}
	
		/**
		 * 
		 * @param distance
		 * @param nodeIndex
		 * @return
		 */
		public ArrayList<Integer> getNeighboor(int distance, int nodeIndex){
			ArrayList<Integer> neighboor = new ArrayList<Integer>();
//			ArrayList<Integer> lastAdd = new ArrayList<Integer>();
			while(distance > 0){
				for (int i = 0; i < nbNodes; i++) {
//					if()
				}
			}
			
			return neighboor;
		}
	}

}

package giteri.network.network;

import giteri.run.interfaces.Interfaces.INetworkRepresentation;

import java.util.*;

import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.NetworkAttribType;
import org.graphstream.algorithm.APSP;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

/** Interface et impplémentations des différentes représentation 
 * du reseau.
 * Pour garder les implémentations de network dans le meme fichier.
 * 
 */
public interface IInternalNetReprestn extends INetworkRepresentation{
	
	/** Représentation direct du giteri.network. Utile pour copier le réseau
	 * courant sous une forme plus simple, et pouvoir ensuite calculer
	 * des propriétés sur ce réseau en laissant l'original se modifier.
	 */
	public class TinyNetworks implements IInternalNetReprestn {
		public int networkVersion;
		public Map<Integer, ArrayList<Integer>> nodesAndConnections;
		private Object syncOnNodes;
		public int nbNodes, nbEdges;
		private Graph graphForApl;

		/** Constructeur sans param.
		 *
		 */
		public TinyNetworks(){
			syncOnNodes = new Object();
			networkVersion = -1;
			nodesAndConnections = new Hashtable<>();
			nbNodes = -1;
			nbEdges = -1;
			graphForApl = new SingleGraph("properties");
		}

		/** Update le tinyNetworks par rapport au réseau donné en param.
		 *
		 * @param toCopy Le réseau a copier.
		 */
		@Override
		public void ConvertNetwork(Network toCopy) {
			ArrayList<Integer> linkOfANode;
			nbNodes = 0;
			nbEdges = 0;

			synchronized (syncOnNodes) {
				for (Node node : toCopy.getNodes()) {
					nbNodes++;
					linkOfANode = new ArrayList<>();
					for (Integer integer : node.getConnectedNodes())
						linkOfANode.add(new Integer(integer));
					nbEdges += node.getConnectedNodes().size();
					nodesAndConnections.put(node.index, linkOfANode);
				}
			}
			networkVersion = toCopy.getUpdateId();
		}

		public Map<Integer, ArrayList<Integer>> getNetwork(){
			return nodesAndConnections;
		}

		/** TODO [Waypoint]- Calcul des propriétés du réseau courant.
		 *
		 */
		@Override
		public NetworkProperties getNetworkProperties(Optional<NetworkProperties> toModify, String networkName, int activationCode) {
			int parcouru, index,firstQ, thirdQ;
			double density = -1, avgDegre = -1 ;
			double apl = -1;
			int[] distrib = new int[0];
			int ddInterQrt = -1;
			@SuppressWarnings("unused")
			double avgClustering = 0;
			// Pas de passage par le gc pour "libérer" une variable simple, plus performant que d'appeler plusieurs fois le configurator.isattribActivated
			boolean avgClust = Configurator.isAttribActived(activationCode, NetworkAttribType.AVGCLUST);
			//RP: Concernant le calcul pour les clustering moyen
			Hashtable<Integer, Double> clustByNode = null;
			double nodeClustering = 0;
			double networkClustering = 0;
			Set<Integer> allrdyDoneNodes = new HashSet<>(nbNodes);
			NetworkProperties netPropDefault = new NetworkProperties(networkName);
			netPropDefault.createStub();
			netPropDefault.setNetworkUuidInstance(networkVersion);
			NetworkProperties netPropResult = toModify.orElse(netPropDefault);

			netPropResult.nbEdges = nbEdges;
			netPropResult.nbNodes = nbNodes;

			// Calcul de la densité
			if (Configurator.isAttribActived(activationCode, NetworkAttribType.DENSITY)) {
				density = (double) nbEdges / ( nbNodes * (nbNodes -1));
				netPropResult.setValue(NetworkAttribType.DENSITY,density);
			}
			// degré moyen sur les nodes
			if (Configurator.isAttribActived(activationCode, NetworkAttribType.DDAVG)){
				avgDegre = (double)nbEdges / (nbNodes);
				netPropResult.setValue(NetworkAttribType.DDAVG,avgDegre);
			}
			// region DD
			if(	Configurator.isAttribActived(activationCode, NetworkAttribType.DDINTERQRT) ||
				Configurator.isAttribActived(activationCode, NetworkAttribType.DDARRAY) ||
				Configurator.isAttribActived(activationCode, NetworkAttribType.AVGCLUST) ) {
				
				distrib = new int[nbNodes];
				
				if(avgClust) 
					clustByNode = new Hashtable<Integer, Double>();

				synchronized (syncOnNodes) {
					for (ArrayList<Integer> connections : nodesAndConnections.values()) {
						distrib[connections.size()] = ++(distrib[connections.size()]);
					}

					netPropResult.setDd(distrib);
					// Si espace interquartile
					if (Configurator.isAttribActived(activationCode, NetworkAttribType.DDINTERQRT)) {
						// Ecart inter quartile
						parcouru = 0;
						index = -1;
						double temp = nbNodes * .25f;
						do {
							index++;
							parcouru += distrib[index];
						} while (parcouru < temp);

						firstQ = index;

						// 3er quartile
						parcouru = 0;
						index = -1;
						temp = nbNodes * .75f;
						do {
							index++;
							parcouru += distrib[index];
						} while (parcouru < temp);

						thirdQ = index;
						ddInterQrt = thirdQ - firstQ;
						netPropResult.setValue(NetworkAttribType.DDINTERQRT,ddInterQrt);
					}

					//si avgClustering
					if (avgClust) {
						for (Integer nodeCentral : nodesAndConnections.keySet()) {
							nodeClustering = 0;
							for (Integer neigthboor : nodesAndConnections.get(nodeCentral)) {
								for (Integer neightOfNeight : nodesAndConnections.get(neigthboor)) {
									if (nodesAndConnections.get(nodeCentral).contains(neightOfNeight))
										nodeClustering++;
								}
							}

							nodeClustering /= nodesAndConnections.get(nodeCentral).size() * (nodesAndConnections.get(nodeCentral).size() - 1);
							clustByNode.put(nodeCentral, nodeClustering);
						}

						// on retire les cas ou les noeuds n'ont aucune connexion.
						for (Double clust : clustByNode.values())
							if (!clust.isNaN())
								networkClustering += clust;

						networkClustering /= clustByNode.values().size();
						netPropResult.setValue(NetworkAttribType.AVGCLUST, networkClustering);
					}
				}
			} // endregion

			// region APL
			if (Configurator.isAttribActived(activationCode, NetworkAttribType.APL)) {
				graphForApl.clear();
				synchronized (syncOnNodes) {
					for (Integer nodeIndex : this.nodesAndConnections.keySet()) {
						graphForApl.addNode("" + nodeIndex);
					}

					for (Integer nodeIndex : this.nodesAndConnections.keySet()) {
						allrdyDoneNodes.add(nodeIndex);
						for (Integer connectedNodeIndex : this.nodesAndConnections.get(nodeIndex)) {
							if(!allrdyDoneNodes.contains(connectedNodeIndex))
								graphForApl.addEdge(nodeIndex + "-" + connectedNodeIndex, nodeIndex, connectedNodeIndex, false);
						}
					}
				}

				APSP apsp = new APSP();
				apsp.init(graphForApl);
				apsp.setDirected(false);
				apsp.compute();
				APSP.APSPInfo info;// = graphForApl.getNode("10").getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
				double total = 0;
				int nbValue = 0;
				for (int i = 0; i < graphForApl.getNodeCount(); i++) {
					info =  graphForApl.getNode(""+i).getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
					for (String string : info.targets.keySet()) {
						total += info.targets.get(string).distance;
						nbValue++;
					}
				}

				apl = (double)total / nbValue;
				netPropResult.setValue(NetworkAttribType.APL,(double)total / nbValue);
			}
			// endregion

			return netPropResult;
		}

		/**
		 *
		 * @return
		 */
		public Map<Integer, ArrayList<Integer>> getNodesAndConnections() {
			synchronized (nodesAndConnections) {
				return nodesAndConnections;
			}
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
			ArrayList<String> edges = new ArrayList<>();
			ArrayList<Integer> nodesIndex = new ArrayList<>();
			ArrayList<Integer> nodeLinks  = new ArrayList<>();
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
	public class AdjacencyMatrixNetwork implements IInternalNetReprestn {

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
		public NetworkProperties getNetworkProperties(Optional<NetworkProperties> toModify,String netName,int activator) {
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

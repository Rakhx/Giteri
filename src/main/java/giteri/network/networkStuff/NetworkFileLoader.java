package giteri.network.networkStuff;

import giteri.run.interfaces.Interfaces.IReadNetwork;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JFrame;

import giteri.tool.math.Toolz;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.network.NetworkProperties;
import giteri.network.network.TinyNetwork;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.jfree.chart.ChartUtilities;

import giteri.tool.other.WriteNRead;
import giteri.run.configurator.Configurator;
import giteri.meme.entite.EntiteHandler;

/** classe pour loader un giteri.network depuis des lignes données en paramètres
 * Défini ce qu'il faut faire lors de la lecture d'une ligne d'un fichier. 
 * appelé dans la fonction what to do blabalbala
 * 
 */
public class NetworkFileLoader implements IReadNetwork {

	Hashtable<Integer, ArrayList<Integer>> nodesAndLinks = new Hashtable<Integer, ArrayList<Integer>>();
	boolean changed = true;
	private TinyNetwork net;
	int nombreScreen = 0;

	// Region Constructor
	
	private static NetworkFileLoader INSTANCE = null;
	
	/** Constructeur sans paramètre.
	 * 
	 */
	private NetworkFileLoader() {
	}

	/** Fourni l'unique instance de la classe.
	 * 
	 */
	public static NetworkFileLoader getInstance()
	{
		if( INSTANCE == null)
			INSTANCE = new NetworkFileLoader();
		
		return INSTANCE;
	}
	
	// EndRegion
	
	/** Appelé entre chaque lecture de réseau.
	 * 
	 */
	public void init(){
		nodesAndLinks.clear();
		changed = true;
		net = null;
	}
	
	/** Va lire la string donnée en paramètre, avec le séparateur
	 * spécifié. la string doit etre Int separator int. 
	 * conversion des strings en int directement sans try catch. 
	 * 
	 */
	public void whatToDoWithOneLine(String line, String separator) {
		changed = true;
		int key, value;
		String[] mots = line.split(separator);
		key = Integer.parseInt(mots[0]);
		value = Integer.parseInt(mots[1]);
		Toolz.addElementInHashArray(nodesAndLinks, key, value);
		// directed or not?
		// Toolz.addElementInHash(nodesAndLinks, value, key);
	}

	/** Renvoi les propriétés du réseau que le networkLoader a chargé.
	 * Si le réseau viens d'etre chargée, calcul le lu pour créer le tinyNetwork
	 * 
	 * @return
	 */
	public NetworkProperties getNetworkProperties(){
		if(changed){
			achieve(); 
			changed = false;
		}
		
		NetworkProperties netProp = new NetworkProperties("Lu");
		NetworkAnalyzer.getInstance().updateNetworkProperties(net, netProp, Configurator.activationCodeAllAttrib);
		
		// TODO Ici se passait le calcul lié au APL
//		Graph graph = getGraphFromdataRead();
//		netProp.computeAPL(graph);
		
		return netProp;
	}
	
	/** Sauvegarde des informations sur le réseau courant dans un dossier.
	 * 
	 */
	public void takeSnapshot(ArrayList<String> reps, long seed){
		// On détermine les reps dans lequel on souhaite enregistrer les données
		File repertoires = WriteNRead.getInstance().createAndGetDirFromString(reps);
		
		// Enregistrement des settings dans un fichier texte
		ArrayList<String> toWrite = new ArrayList<String>(Configurator.getConfig());
		toWrite.add("screenshot n°: " + nombreScreen++);
		toWrite.add("seed n°: " + seed);
		toWrite.add("Density: " + CommunicationModel.getInstance().getDensity());
		toWrite.add("Proportion: "+ MemeFactory.getInstance().getMemeAvailableAsString(false));
		toWrite.add("Meme possession: "+ EntiteHandler.getInstance().checkPropertiesByMemePossession());
		toWrite.add("DD stuff: "+ CommunicationModel.getInstance().getDDInfos());
		
		WriteNRead.getInstance().writeSmallFile2(repertoires, "config", toWrite);
		
		// Enregistrement du réseau dans un fichier texte.
		try {
			WriteNRead.getInstance().writeFileFromNetwork(repertoires, "giteri/network", CommunicationModel.getInstance().nc.getNetworkRepresentations());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// Enregistrement des screenshots de graphique
		try {
			ChartUtilities.saveChartAsPNG(new File(repertoires,CommunicationModel.getInstance().view.getDDChart().getTitle().getText()+".png"), CommunicationModel.getInstance().view.getDDChart(), 840, 680);
			ChartUtilities.saveChartAsPNG(new File(repertoires,CommunicationModel.getInstance().view.getDensityChart().getTitle().getText()+".png"), CommunicationModel.getInstance().view.getDensityChart(), 840, 680);
//			ChartUtilities.saveChartAsPNG(new File(repertoires,CommunicationModel.getInstance().view.getDensityOverProbaChart().getTitle().getText()+".png"), CommunicationModel.getInstance().view.getDensityOverProbaChart(), 840, 680);
			
		} catch (IOException e ) {
			e.printStackTrace();
		}catch (IndexOutOfBoundsException e2){
			e2.printStackTrace();
		}catch (NullPointerException e3){
//			e3.printStackTrace();
		}
	}
	
	/** Compile les données récupérées pour faire le réseau. 
	 * 
	 */
	private void achieve(){
		net = new TinyNetwork();
		int nbEdges= 0;
		for (Integer nodeIndex : nodesAndLinks.keySet()){ 
			if(!net.isContainingNode(nodeIndex)){
				net.nodes.put(nodeIndex, net.new TinyNode(nodeIndex, nodesAndLinks.get(nodeIndex)));
				nbEdges += nodesAndLinks.get(nodeIndex).size();
			} else
				System.out.println("Ca ne devrait pas arriver");
		}
		
		net.nbNodes = nodesAndLinks.keySet().size();
		net.nbEdges = nbEdges;
		net.networkUpdateVersion = -4;
	}
	
	
	public Graph getGraphFromdataRead(){
		JFrame popo = new JFrame("Lu");
		popo.setVisible(true);
		Graph graph = new SingleGraph("Lu");
		for (int i = 0; i < nodesAndLinks.keySet().size(); i++) {
			graph.addNode(""+i);
		}
		
		for (Integer i : nodesAndLinks.keySet()) {
			for (Integer j : nodesAndLinks.get(i)) {
				if(j > i)
					graph.addEdge(""+i+""+j, ""+i, ""+j);
			}
		}

		graph.display();
		return graph;
	}
}

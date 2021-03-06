package giteri.network.networkStuff;

import giteri.run.interfaces.Interfaces.IReadNetwork;

import java.io.*;
import java.util.*;

import giteri.tool.math.Toolz;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.network.NetworkProperties;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.jfree.chart.ChartUtilities;
import giteri.tool.other.WriteNRead;
import giteri.run.configurator.Configurator;
import giteri.network.network.IInternalNetReprestn.TinyNetworks;

/** classe pour loader un network depuis des lignes données en paramètres
 * Défini ce qu'il faut faire lors de la lecture d'une ligne d'un fichier.
 * appelé dans la fonction what to do blabalbala
 *
 */
public class NetworkFileLoader implements IReadNetwork {

	private CommunicationModel communicationModel;
	private MemeFactory memeFactory;
	private WriteNRead writeNRead;
	// KV nodeIndex:<nodeIndex>
	private Hashtable<Integer, ArrayList<Integer>> nodesAndLinks = new Hashtable<>();
	private boolean changed = true;
	private TinyNetworks net;
	private int nombreScreen = 0;

	//region constructeur et setter
	/** Constructeur sans paramètre.
	 *
	 */
	public NetworkFileLoader( MemeFactory mf, WriteNRead wnr) {
		memeFactory = mf;
		writeNRead = wnr;
	}

	public void setCommunicationModel(CommunicationModel cm){
		communicationModel = cm;
	}

	/** Appelé entre chaque lecture de réseau.
	 *
	 */
	public void init(){
		nodesAndLinks.clear();
		changed = true;
		net = null;
	}
	//endregion

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

	/** TODO [Waypoint]- Calcul des propriétés du réseau en fichier texte.
	 * Renvoi les propriétés du réseau que le networkLoader a chargé.
	 * Si le réseau viens d'etre chargée, calcul le lu pour créer le tinyNetwork
	 *
	 * @return
	 */
	public NetworkProperties getNetworkProperties(boolean fromSerialised, boolean andWriteThem){
		if(fromSerialised){
			return readSerializedPropertiesFromFile();
		}

		if(changed){
			achieve();
			changed = false;
		}

		NetworkProperties netProp;
		netProp = net.getNetworkProperties(Optional.empty(),"lu", Configurator.activationCodeAllAttrib);
		// writeSerializePropertiesIntoFile(netProp);
		// netProp = readSerializedPropertiesFromFile();

		if(andWriteThem)
			writeSerializePropertiesIntoFile(netProp);
		return netProp;
	}

	/** Sauvegarde des informations sur le réseau courant dans un dossier.
	 *
	 */
	public void takeSnapshot(ArrayList<String> reps, long seed){
		// On détermine les reps dans lequel on souhaite enregistrer les données
		File repertoires = writeNRead.createAndGetDirFromString(reps);

		// Enregistrement des settings dans un fichier texte
		ArrayList<String> toWrite = new ArrayList<>(Configurator.getConfig());
		toWrite.add("screenshot n°: " + ++nombreScreen);
		toWrite.add("seed n°: " + seed);
		toWrite.add("Density: " + communicationModel.getDensity());
		toWrite.add("CC: " + communicationModel.getCurrentNetProperties(16).avgClust);
		toWrite.add("Meme on map et propagation: "+ memeFactory.getMemeAvailableAsString(Configurator.MemeList.ONMAP));
		toWrite.add("Meme possession: "+ communicationModel.eh.checkPropertiesByMemePossession());
		toWrite.add("DD stuff: "+ communicationModel.getDDInfos());

		writeNRead.writeSmallFile(repertoires, "config", toWrite);

		// Enregistrement du réseau dans un fichier texte.
		try {
			writeNRead.writeFileFromNetwork(repertoires, "network", communicationModel.nc.getNetworkRepresentations());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Enregistrement des screenshots de graphique
		try {
			ChartUtilities.saveChartAsPNG(new File(repertoires,communicationModel.view.getDDChart().getTitle().getText()+".png"), communicationModel.view.getDDChart(), 840, 680);
			ChartUtilities.saveChartAsPNG(new File(repertoires,communicationModel.view.getDensityChart().getTitle().getText()+".png"), communicationModel.view.getDensityChart(), 840, 680);
//			ChartUtilities.saveChartAsPNG(new File(repertoires,CommunicationModel.getInstance().view.getDensityOverProbaChart().getTitle().getText()+".png"), CommunicationModel.getInstance().view.getDensityOverProbaChart(), 840, 680);

		} catch (IOException e ) {
			e.printStackTrace();
		}catch (IndexOutOfBoundsException e2){
			e2.printStackTrace();
		}catch (NullPointerException e3){
//			e3.printStackTrace();
		}
	}

	/** Affichage du graph depuis la structure interne a la classe.
	 *
	 * @return
	 */
	public Graph getGraphFromDataRead(){
		Graph graph = new SingleGraph("Lu");
		Integer maxNodes = nodesAndLinks.keySet().stream().max(Comparator.comparing(Integer::valueOf)).get();
		//Integer maxNode = nodesAndLinks.keySet().stream().max(Comparator.comparingInt(value -> value)).get();
		//Integer maxNodeValues =
			//	nodesAndLinks.values().stream().flatMap(value -> value.stream()).max(Comparator.comparingInt(value->value)).get();
		//.max(Comparator.comparingInt(value -> value)).get();

		for (int i = 0; i < maxNodes+1; i++) {
			graph.addNode(""+i);
		}

		for (Integer i : nodesAndLinks.keySet()) {
			for (Integer j : nodesAndLinks.get(i)) {
				if(j > i)
					try {
						graph.addEdge("" + i + "-" + j, "" + i, "" + j);
					}catch (org.graphstream.graph.IdAlreadyInUseException e){
						System.out.println(e.getMessage());
					}
			}
		}

		graph.display();
		return graph;
	}

	/** Serialisation d'un fichier de networkProperties dans le répertoire courant
	 *
	 * @param toSave
	 */
	public void writeSerializePropertiesIntoFile(NetworkProperties toSave){
		try {
			FileOutputStream file = new FileOutputStream(Configurator.fileNameSerialisation);
			ObjectOutputStream out = new ObjectOutputStream(file);
			out.writeObject(toSave);
			out.close();
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** De serialisation d'un fichier de networkProperties depuis le repertoire courant dont le nom
	 * est celui donné dans le fichier de Configurator.
	 *
	 * @return
	 */
	public NetworkProperties readSerializedPropertiesFromFile(){
		NetworkProperties netProp = null;
		// File repertoires = writeNRead.createAndGetDirFromString(new ArrayList<>(Arrays.asList(Configurator.fileNameSerialisation)));
		try
		{
			// Reading the object from a file
			if(!Configurator.fullSilent)
			System.out.println(System.getProperty("user.dir"));
			FileInputStream file;
			if(Configurator.typeOfConfig == Configurator.EnumLauncher.jarOpenMole)
			//	file = new FileInputStream(Configurator.fileNameSerialisation);
				file  = new FileInputStream(Configurator.fileNameSerialisationOpen);
			else
				 file = new FileInputStream(Configurator.fileNameSerialisation);

			ObjectInputStream in = new ObjectInputStream(file);
			// Method for deserialization of object
			netProp = (NetworkProperties)in.readObject();
			in.close();
			file.close();

		}
		catch(IOException ex)
		{
			System.out.println("[networkFileLoader.read...]- IOException is caught");
		}
		catch(ClassNotFoundException ex)
		{
			System.out.println("ClassNotFoundException is caught");
		}

		return netProp;
	}

	/** Compile les données récupérées pour faire le réseau.
	 *
	 */
	private void achieve(){
		net = new TinyNetworks();
		int nbEdges= 0;
		ArrayList<Integer> edges ;
		for (Integer nodeIndex : nodesAndLinks.keySet()) {
			edges = new ArrayList<>();
			for (Integer integer : nodesAndLinks.get(nodeIndex)) {
				edges.add(integer);
				nbEdges++;
			}

			net.addNodeWithEdge(nodeIndex, edges);
		}

		net.nbNodes = nodesAndLinks.keySet().size();
		net.nbEdges = nbEdges;
		net.networkVersion = -4;
	}
}

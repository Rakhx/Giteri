package giteri.fitting.algo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.*;

import javax.swing.JFrame;

import giteri.fitting.parameters.IModelParameter;
import giteri.tool.math.Toolz;
import giteri.network.network.NetworkProperties;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.PolarChartPanel;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import giteri.tool.other.WriteNRead;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.NetworkAttribType;
/** Classe de résultat des réseaux pendant l'étape de fitting.
 * les réseaux sont identifiés par un GUID, et des map <Guid, parametre> // Score>
 * sont la pour associer réseau, paramètre et résultat suivant des métriques calculées
 * pendant l'étape de fitting. blabl
 */
public class ResultSet {

	private WriteNRead writeNRead;

	// Structure de donnée

	// Identifiant et resultat d'une simulation
	private Map<Integer, Result> resultById;

//	// Identifiant et paramètre d'une config
//	private Hashtable<Integer, String> parameterSetById;
//	// Ensemble des scores calculés pour les différents runs d'une config
//	private Hashtable<Integer, ArrayList<Double>> scoreById;
//	// Ensemble des propriétés des réseaux obtenues pour différents run sur une meme config
//	private Hashtable<Integer, ArrayList<NetworkProperties>> networkPropertiesById;

	// Affichage stuff
	XYDataset dataset;
	JFreeChart chart;
	ChartPanel chartPanel;
	boolean debugMode = true;
//	int lastNetworkId;

	/** Constructeur.
	 *
	 */
	public ResultSet(WriteNRead wnr) {
		writeNRead = wnr;
		resultById = new Hashtable<>();
//		parameterSetById = new Hashtable<>();
//		scoreById = new Hashtable<>();
//		networkPropertiesById = new Hashtable<>();
	}

	// Region Public Method

	// TODO a refaire éventuellement.
	/** Parcourt tout les réseaux de la liste, et choisi ceux qui ont des valeurs
	 * extremes ( min ou max) dans au moins l'une des propriétés regardé.
	 * Va ensuite afficher le résultat.
	 *
	 */

	/*public void displayPolar(){
		int activationCode = 23;
		// choix de ceux qui ont une valeur min/max dans un domaine
		Hashtable<Integer, Integer> scoreMax = new Hashtable<>();
		Hashtable<Integer, Integer> scoreMin = new Hashtable<>();
		ArrayList<Integer> interestingNetwork = new ArrayList<>();

		int rangParameter = -1;
		int bitConcerne;
		double maxValue , minValue , currentValue;
		NetworkProperties netProp;

		// Initialisation des meilleurs score
		for (Integer id : parameterSetById.keySet()) {
			scoreMax.put(id, 0);
			scoreMin.put(id, 0);
		}

		// pour chaque type d'attribut
		for (NetworkAttribType attributTypeLooked : NetworkAttribType.values()) {
			if(!Configurator.isAttribActived(activationCode, attributTypeLooked))
				continue;

			maxValue = 0;
			minValue = Double.MAX_VALUE;
			rangParameter++;
			// Association d'un rang a une puissance de 2 dans le int de max et min score.
			bitConcerne = (int) Math.pow(2, rangParameter);

			// pour chaque réseau.
			for (Integer id : parameterSetById.keySet()) {

				// propriété du réseau étudié.
				netProp = networkPropertiesById.get(id).get(0);
				currentValue = Double.parseDouble(""+netProp.getValue(attributTypeLooked));

				// si on trouve un meilleur score que le score précédent
				if(currentValue > maxValue) {
					if(debugMode)
						System.out.println("Meileur score battu, ancien "+maxValue +" nouveau " + currentValue);
					// on efface le bit d'activation de meilleur score pour ce param de tous les UUID
					for (Integer concerne : scoreMax.keySet()) {
						scoreMax.put(concerne, (scoreMax.get(concerne) | bitConcerne) ^ bitConcerne);
					}
					// On place la nouvelle meilleur valeur
					scoreMax.put(id, scoreMax.get(id) | bitConcerne);
					maxValue = currentValue;
				}

				// Si le score est égal a la meilleure valeur, on ajoute cet id au meilleur valeur
				else if(currentValue == maxValue ){
					// On ajoute une nouvelle meilleur valeur
					scoreMax.put(id, scoreMax.get(id) | bitConcerne);
					if(debugMode)
						System.out.println("Score egal au meilleur score courant "+ maxValue);
				}

				// Si le score trouvé est inférieur a la plus petite des valeur min
				if(currentValue < minValue){
					if(debugMode)
						System.out.println("min score battu, ancien "+ minValue +" nouveau " + currentValue);
					for(Integer concerne : scoreMin.keySet()){
						scoreMin.put(concerne, (scoreMin.get(concerne) | bitConcerne) ^ bitConcerne);
					}
					scoreMin.put(id, scoreMin.get(id) | bitConcerne);
					minValue = currentValue;

					// si le socre trouvé est égal a la meilleur valeur inférieur
				}else if(currentValue == minValue){
					if(debugMode)
						System.out.println(" EGALITE DE MIN ");
					scoreMin.put(id, scoreMin.get(id) | bitConcerne);
				}
			}
		}

		// Initialisation des meilleurs score
		for (Integer id : parameterSetById.keySet()) {
			if(debugMode){
				System.out.println("UUID: " + id);
				System.out.println("Score " + scoreById.get(id));
				System.out.println("MAX " + scoreMax.get(id));
				System.out.println("MIN " + scoreMin.get(id));
				System.out.println("elements " + getConfigAsStringForId(id));
			}


			// Eventuellement rarifier la selection pour avoir moins de réseau qui ressorte.
			// genre savoir si on veut un min ou un max sur certaines propriétés ou encore
			// sélectionner les réseaux qui sont les meilleurs sur deux attrib ou qui sont meilleurs
			// de loin sur cette propriété. ( plus dur )
			if(scoreMax.get(id) != 0 || scoreMin.get(id) != 0)
				interestingNetwork.add(id);
		}

		for (Integer networkId : parameterSetById.keySet()) {
			System.out.println("_____________________________________");
			System.out.println(networkPropertiesById.get(networkId).get(0).toString() + " ◊score: "+scoreById.get(networkId));
		}

		processData(interestingNetwork, 23);
	}
	*/

	/** Renvoi les scores en fonction des ID en fonction des distributions
	 *
	 *
	 */
	public double displayResult(){
		double res = 0;
		if(Configurator.jarMode){
			for (Integer networkId : resultById.keySet())
				res = resultById.get(networkId).getAvgScore();
				System.out.println(res);
		}
		else {
			for (Integer networkId : resultById.keySet()) {
				System.out.println("_____________________________________");
				System.out.println("Configuration " + resultById.get(networkId).getCurrentConfig());
			}
		}
		return res;
	}

	// TODO A virer.
	/** Retourne a partir du UUID d'un réseau lae string des set de
	 * parameter ParameterSetOfValue.
	 *
	 * @param networkId
	 * @return
	 */
	public String getConfigAsStringForId(Integer networkId){
		return "";
//		String resultat="";
//		if(parameterSetById.containsKey(networkId)){
//			resultat = parameterSetById.get(networkId);
//			return resultat;
//		}
//		else{
//			System.err.println("[Result] L'id du network n'est pas trouvable dans les hashtable, ca ne devrait pas arriver");
//			return "FAIL";
//		}
	}

	public void addInitialConfigurationToResult(int networkId, Collection<IModelParameter<?>> parameters){
		resultById.put(networkId, new Result(parameters));
	}

	/**
	 *
	 * @param networkId
	 * @param value
	 */
	public void addScore(int networkId, double value, NetworkProperties properties) {
		Result result = resultById.get(networkId);
		result.addScore(value);
		result.addProperties(properties.toString());
//		lastNetworkId = networkId;
	}


	/** Ecriture dans le fichier détaillé des propriétés du dernier network.
	 * Ecrit ttes les propriétés, et laisse un espace pour leur SD qui sera écrit a la fin de
	 * chaque RUN
	 *
	 * @param rep
	 */
	public void writelastTurnOnDetailCSV(File rep, int networkId ,NetworkProperties properties){

		Result result = resultById.get(networkId);

		String toWrite = "";
		toWrite += networkId;
		toWrite += ";" + result.getCurrentConfig();
		toWrite += "; -";

		// On prend le dernier élément de la liste
		NetworkProperties lastProp = properties;

		// Prendre la moyenne a chaque fois?
		// à encadrer avec l'entete etc
		toWrite += lastProp.getCSVFormatDoubleColonne(null, Configurator.activationCodeAllAttribExceptDD);

		// Concernant les scores
		toWrite += ";" + result.getLastScore();
		toWrite += ";" + -1;

		writeNRead.writeSmallFile2(rep, "NetworkDetailsCSV", Arrays.asList(toWrite)) ;
	}

	/** Va écrire:
	 * Pour le CSV normal : les valeurs moyennes et écartType pour les attributs activés
	 * Pour le CSV détaillé : écartType et moyenne pour chaque attribut
	 *
	 */
	public void writeLastRunOnCSV(File rep,int networkId,List<NetworkProperties> ListOfproperties ,int activator){
		NetworkProperties netMean = new NetworkProperties(); netMean.createStub();
		NetworkProperties netSD = new NetworkProperties(); netSD.createStub();
		Result result = resultById.get(networkId);
		List<Double> scores = result.getScores();
		Double[] scoreMeanAndSd;
		String toWriteSimple = "", toWriteDetailled = "";
		String parameter = result.getCurrentConfig();

		updateMeanAndSD(ListOfproperties, Configurator.activationCodeAllAttribExceptDD, netMean, netSD);
		toWriteSimple += networkId;
		toWriteDetailled += networkId;

		toWriteSimple += ";" + parameter;
		toWriteDetailled += ";" + parameter;

		toWriteSimple += "; -"; //+ parameter;
		toWriteDetailled += "; -"; // + parameter;

		// STEP: Partie simple
		// Moyenne et ecart type sur les valeurs activées, et sur le score
		toWriteSimple += netMean.getCSVFormatDoubleColonne(netSD, activator);

		// STEP: Partie detaillé
		toWriteDetailled += netMean.getCSVFormatDoubleColonne(netSD, Configurator.activationCodeAllAttribExceptDD);

		// STEP: Commun
		// Dans tous les cas, ajouts du score et de son SD
		scoreMeanAndSd = Toolz.getDeviationAndMean(scores);
		// TODO [WayPoint]- Calcul du score moyenne x ecart type mais pas pris en compte pour le vrai score...
		toWriteSimple += ";" + scoreMeanAndSd[0] + ";" + scoreMeanAndSd[1] + ";"
				+ ((scoreMeanAndSd[0]*scoreMeanAndSd[0]) / (scoreMeanAndSd[0] - scoreMeanAndSd[1]) );

		toWriteDetailled += ";" + scoreMeanAndSd[0] + ";" + scoreMeanAndSd[1]
				+ ((scoreMeanAndSd[0]*scoreMeanAndSd[0]) / (scoreMeanAndSd[0] - scoreMeanAndSd[1]) );

		// TODO [WayPoint]- Ecriture dans les deux csv, normal et détaillé.
		writeNRead.writeSmallFile2(rep, "NetworkCSV", Arrays.asList(toWriteSimple)) ;
		writeNRead.writeSmallFile2(rep, "NetworkDetailsCSV", Arrays.asList(toWriteDetailled)) ;
	}

	// EndRegion

	// Region private

	// REGION SCORE ETC

	/** Va calculer la moyenne et l'écart type de la série de nework properties en parametre
	 * Dans le cas du fichier détaillé, tous les champs ont besoin d'etre calculer.
	 * Pour l'autre seulement les champs activé
	 * @param networksToRead IN
	 * @param activationCode IN
	 * @param netMean passage par "référence" IN/OUT
	 * @param netSD IN/OUT
	 * @return
	 */
	private void updateMeanAndSD(List<NetworkProperties> networksToRead, int activationCode, NetworkProperties netMean, NetworkProperties netSD){
		netMean.createStub();
		netSD.createStub();

		NetworkAttribType attribut;
		ArrayList<Double> netPropValues  = new ArrayList<Double>();

		// On regarde sur tous les attributs de réseau ceux qui ont été activé
		// pour le calcul de distance entre deux réseaux

		// For each Attribut existant
		for (int i = 0; i < Configurator.NetworkAttribType.values().length; i++) {
			attribut = NetworkAttribType.values()[i];
			// Si l'attribut est actif
			if(Configurator.isAttribActived(activationCode, attribut))
			{
				netPropValues.clear();
				// On regarde tous les réseaux en param
				for (NetworkProperties netProp : networksToRead)
					// Ajout de ces valeurs dans une liste
					netPropValues.add((Double)netProp.getValue(attribut));

				// calcul des moyennes et écart type sur les valeurs données dans la liste
				Double[] avgNSd = Toolz.getDeviationAndMean(netPropValues);

				// Ajout de ces valeurs dans les networkProperties contenant moyenne et écart type
				netMean.setValue(attribut, avgNSd[0]);
				netSD.setValue(attribut, avgNSd[1]);
			}
		}
	}

	// TODO a refaire.
	/** Va les mettres sous forme string:config du réseau arraydouble : valeur des attributs
	 * ordonnée
	 *
	 * @param bestNetwork
	 */
//	private void processData(ArrayList<Integer> bestNetwork, int activationCode){
//
//		double valeurMax, valeurCourante, valeurNormalisee;
//		String networkBlaze;
//
//		// Data formaté, sous forme nom du réseau :: list d'attribut & value
//		Hashtable<String, Hashtable<NetworkAttribType, Double>> dataFormat =
//				new Hashtable<String,Hashtable<NetworkAttribType, Double>>();
//
//		// attribut qui sont utilisé pour les réseaux
//		final ArrayList<NetworkAttribType> usedAttrib = new ArrayList<Configurator.NetworkAttribType>();
//
//		// Valeur max par type d'attrib
//		Hashtable<NetworkAttribType, Double> maxValues = new Hashtable<Configurator.NetworkAttribType, Double>();
//
//		// Renseigne les attributs effectivement utilisés
//		for (NetworkAttribType type : NetworkAttribType.values())
//			if(Configurator.isAttribActived(activationCode, type))
//				usedAttrib.add(type);
//
//		// Recherche des meilleurs valeurs pour tous les attribs
//		for (NetworkAttribType type : usedAttrib) {
//			valeurMax = 0;
//			for (Integer networkId : bestNetwork) {
//				valeurCourante = Double.parseDouble(""+networkPropertiesById.get(networkId).get(0).getValue(type));
//				if(valeurMax < valeurCourante)
//					valeurMax = valeurCourante;
//			}
//
//			maxValues.put(type, valeurMax);
//		}
//
//		// Création de valeur normalisée pour les valeurs de chacun de ces networks
//		for (Integer networkId : bestNetwork) {
//
//			// le nom du réseau par ses propriétés
//			networkBlaze = parameterSetById.get(networkId);
//			// sa liste de donnée normalisé, classé en hash par le type d'attribut du réseau
//			Hashtable<NetworkAttribType, Double> formatValues = new Hashtable<NetworkAttribType, Double>();
//			dataFormat.put(networkBlaze, formatValues);
//
//			// on rempli la structure de données normalisées
//			for (NetworkAttribType type : usedAttrib) {
//				valeurNormalisee = Double.parseDouble(""+networkPropertiesById.get(networkId).get(0).getValue(type)) / maxValues.get(type);
//				formatValues.put(type, valeurNormalisee);
//			}
//		}
//
//		displayShit(dataFormat, activationCode);
//	}

	// REGION DISPLAY ETC

//	/**  Affiche les données sélectionnées,
//	 *
//	 * @param dataFormat key::Nom du réseau, to string de ses propriétés
//	 * Value::Hashtable
//	 * 		Key:: Type de propriété considéré ( clustering, densité... )
//	 * 		Value:: Valeur de cette propriété pour le réseau en question.
//	 * @param activationCode
//	 */
//	private void displayShit(Hashtable<String, Hashtable<NetworkAttribType, Double>> dataFormat, int activationCode){
//		JFrame radar = new JFrame("Result");
//		final XYDataset dataset = createDataset(dataFormat, activationCode);
//		final JFreeChart chart = createChart(dataset);
//		final ChartPanel chartPanel = new PolarChartPanel(chart);
//		chartPanel.setPreferredSize(new Dimension(500, 270));
//		chartPanel.setEnforceFileExtensions(false);
//		radar.getContentPane().add(chartPanel, BorderLayout.CENTER);
//		radar.pack();
//		radar.setVisible(true);
//	}
//
//	/** création a partir d'un data set du polar.
//	 *
//	 * @param dataset
//	 * @return
//	 */
//	private JFreeChart createChart(final XYDataset dataset) {
//		final JFreeChart chart = ChartFactory.createPolarChart("Chart polar",
//				dataset, true, true, false);
//		final PolarPlot plot = (PolarPlot) chart.getPlot();
//		final DefaultPolarItemRenderer renderer = (DefaultPolarItemRenderer) plot
//				.getRenderer();
//		renderer.setSeriesFilled(2, true);
//		return chart;
//	}
//
//	/** Crée et retourne le dataset
//	 *
//	 * @return
//	 */
//	private XYDataset createDataset(Hashtable<String, Hashtable<NetworkAttribType, Double>> dataFormat, int activationCode){
//		final XYSeriesCollection data = new XYSeriesCollection();
//		Hashtable<NetworkAttribType, Double> values;
//
//		// On recherche les type d'attributs qui sont effectivement utilisés.
//		final ArrayList<NetworkAttribType> usedAttrib = new ArrayList<Configurator.NetworkAttribType>();
//
//		// Devrait ne pas etre obligatoire, redondance des données puisque déja présenter dnas les hashtables
//		for (NetworkAttribType type : NetworkAttribType.values())
//			if(Configurator.isAttribActived(activationCode, type))
//				usedAttrib.add(type);
//
//		for (String networkName : dataFormat.keySet()) {
//			values = dataFormat.get(networkName);
//			data.addSeries(createSeries(networkName, values,usedAttrib));
//		}
//
//		return data;
//	}
//
//	/** Renvoi une série correspondant a un NetworkProperties, sur les attributs de la liste.
//	 *
//	 * @param name
//
//	 * @param attribs
//	 * @return
//	 */
//	private XYSeries createSeries(String name, Hashtable<NetworkAttribType, Double> values, ArrayList<NetworkAttribType> attribs){
//		final XYSeries series = new XYSeries(name);
//		double placementRadius;
//		double value;
//		for (NetworkAttribType type : attribs) {
//			placementRadius = convertAttributeToRadial(attribs, type);
//			value = values.get(type);
//			series.add(placementRadius, value);
//		}
//
//		return series;
//	}
//
//	/** Obtient la division de 360 par l'emplacement dans la liste de l'attribut en question.
//	 *
//	 * @param attribType
//
//	 * @return
//	 */
//	private double convertAttributeToRadial(ArrayList<NetworkAttribType> usedAttrib ,NetworkAttribType attribType){
//		double tranche = Math.round(360 / usedAttrib.size());
//		int indexConcerne = usedAttrib.indexOf(attribType);
//		return tranche * indexConcerne;
//	}



	// EndRegion
}
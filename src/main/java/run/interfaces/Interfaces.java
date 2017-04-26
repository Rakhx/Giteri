package interfaces;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Optional;

import network.Network;
import network.NetworkProperties;
import network.TinyNetwork;

import org.graphstream.graph.Graph;
import org.jfree.chart.JFreeChart;

import configurator.Configurator.FittingBehavior;
import entite.Meme;
import event.ActionApplyListener;
import event.BehaviorTransmissionListener;

/** Ensemble des interfaces utilisées dans le programme.
 * 
 *
 */
public class Interfaces {
	
	/** Interface concernant 
	 *
	 */
	public interface ISetValues{
		void setValue(Double value);
	}

	/** Interface qui permet la lecture d'une ligne pour la création d'un
	 * réseau depuis plusieurs lignes lues.
	 *
	 */
	public interface IReadNetwork{
		void whatToDoWithOneLine(String line, String separator);
		void init();
		NetworkProperties getNetworkProperties();
		Graph getGraphFromdataRead();
	}
	
	/** Interface des classes qui s'occupe de rendre le network. 
	 *
	 */
	public interface DrawerInterface extends BehaviorTransmissionListener {

		public void resetDisplay();
		public void drawThisNetwork(Network net);
		public void networkOverview();
		public void screenshotDisplay(ArrayList<String> rep);
		
		public void applyTargetColor(Network net, Integer actingEntite ,ArrayList<Integer> nodeToDesignAsTarget);
		public void resetGoodColor(Network net);
		
		
		public void addEdge(int from, int to);
		public void addNode(int from);
		public void removeEdge(int from, int to);
	}
	
	/** Interface des classes permettant de calculer les différentes
	 * stats sur le graph.
	 */
	public interface StatAndPlotInterface extends ActionApplyListener{
		
		// Fonction d'obtention des stats
		public String getDDInfos();
		
		public void updateNetworkProperties(TinyNetwork net, NetworkProperties netProp, int activationCode);
		public double getAPL();
		public void fitNetwork(int activator);
//		public void searchStability();
		public void fitNextStep();
		public void testStability();
		public void incrementNbActionRelative();
	}
	
	/** Interface qui définit les actions possibles sur l'IHM
	 *
	 */
	public interface IView {
		void setDisplayNbAction(String message);
		void resetIHM();
		void resetDensityOverProbaChart();
		void toggleEnableInterface();
		void toggleWkProgress(String message);
		void addValueToDensityOverProbaSerie(double x, double y);
		public void addValueToApplianceSerie(double time,Hashtable<Integer, Double>  value);
		JFreeChart getDDChart();
		JFreeChart getDensityChart();
		JFreeChart getDensityOverProbaChart();
	}
	
	/** Interface qui définit les actions du model qui peuvent etre
	 * appelé depuis le controller, et donc depuis la vue. 
	 *
	 */
	public interface IModel {

		void stabilityResearch();
		void fittingNetworks();
		void displayPolar();
		void fittingNextStep();
		void toggleStep();
		void rdmConfig();
		void takeSnapshot(long seed, Optional<ArrayList<String>> simulationPath);
		
		void suspend();
		void resume();
		void oneStep();
		
		Hashtable<Integer, ArrayList<Meme>>  getMemesAvailables(FittingBehavior setAsked);
		double getDensity();
		String getDDInfos();
		NetworkProperties getCurrentNetProperties(int activator);
		IReadNetwork getReader();
		
		void generateGraph(int activator);
		void purgeLinks();
	}

	/** Interface pour un network.
	 * 
	 *
	 */
	public interface INetwork{
//		public void addNode();
		public void addEdge(int from, int to);
		public void removeEdge(int from, int to);
	}
	
	/** Interface qui définit une forme de représentation, ainsi que des
	 * calculs sur les propriétés de cette représentation
	 * 
	 */
	public interface INetworkRepresentation {
		/** Conversion d'un network vers la forme de représentation choisit.
		 * Doit etre fait avec un synchronized pour éviter la modification du 
		 * network pendant la copie. 
		 * 
		 * @param toCopy Le réseau a copier. 
		 */
		public void ConvertNetwork(Network toCopy);
		/** Obtenir les propriétés issu de la représentation obtenue, avec un activator
		 * définissant quel type de propriété on souhaite obtenir. 
		 * 
		 * @param activator
		 */
		public NetworkProperties getNetworkProperties(int activator);
		/** Obtenir l'UUID du network que la représentation copie. 
		 * {@inheritDoc}
		 * @return
		 */
		public int getRepresentationUUID();
		/** Reset des propriétés de la copie du réseau
		 * 
		 */
		public void resetRepresentation();
		/** Obtient la liste des edges du network sous la forme 
		 * IndexNode espace IndexNode, classé en ordre croissant. 
		 * 
		 * @return
		 */
		public ArrayList<String> getNetworkEdges();
	}
}

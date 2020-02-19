package giteri.run.interfaces;

import java.awt.*;
import java.util.*;
import java.util.List;

import giteri.meme.entite.CoupleMeme;
import giteri.network.network.Network;
import giteri.network.network.NetworkProperties;

import giteri.run.configurator.Configurator;
import org.graphstream.graph.Graph;
import org.jfree.chart.JFreeChart;

import giteri.meme.entite.Meme;
import giteri.meme.event.IActionApplyListener;
import giteri.meme.event.IBehaviorTransmissionListener;

import giteri.run.configurator.Configurator.ViewMessageType;

/** Ensemble des interfaces utilisées dans le programme.
 * 
 *
 *
 */
public class Interfaces {

	/** Interface qui permet la lecture d'une ligne pour la création d'un
	 * réseau depuis plusieurs lignes lues.
	 *
	 */
	public interface IReadNetwork{
		void whatToDoWithOneLine(String line, String separator);
		void init();
		NetworkProperties getNetworkProperties(boolean fromSerializedFile ,boolean andSerializIt);
		Graph getGraphFromDataRead();
	}
	
	/** Interface des classes qui s'occupe de rendre le giteri.network.
	 *
	 */
	public interface DrawerNetworkInterface extends IBehaviorTransmissionListener {

		void resetDisplay();
		void drawThisNetwork(Network net, boolean outsideView);
		void networkOverview();
		void screenshotDisplay(ArrayList<String> rep);

		void applyTargetColor(Network net, Integer actingEntite ,Set<Integer> nodeToDesignAsTarget);
		void resetGoodColor(Network net);
		Color getColorAsColor(int i);

		void addEdge(int from, int to);
		void addNode(int from);
		void removeEdge(int from, int to);
	}
	
	/** Interface des classes permettant de calculer les différentes
	 * stats sur le graph.
	 */
	public interface StatAndPlotInterface  {
		
		// Fonction d'obtention des stats
		 String getDDInfos();
		 Double fitNetwork(Configurator.EnumLauncher typeOfLaunch, Configurator.EnumExplorationMethod typeOfExplo,
						   Interfaces.IOpenMoleParameter param);
		 void fitNextStep();
		 void exploFitting();
		 void incrementNbAction();
	}
	
	/** Interface qui définit les actions possibles sur l'IHM ou autre
	 * type de vue. ( fichier, console, IHM )
	 *
	 */
	public interface IView {
		/** Affichage d'une information.
		 *
		 * @param type Le
		 * @param info
		 */
		void displayInfo(ViewMessageType type, List<String> info);
		void displayXLastAction(int nbAction, Map<String, Integer> nbActivByMeme, Map<String,Integer> nbLastActivByMeme, List<String> lastXMemeApplied);
		void setDisplayNbAction(String message);
		void resetIHM();
		void resetDensityOverProbaChart();
		void toggleEnableInterface();
		void toggleWkProgress(String message);
		void addValueToApplianceSerie(double time, Map<IUnitOfTransfer, Double> kvIndexValue);
		void setMemeAvailable(List<IUnitOfTransfer> memes);
		JFreeChart getDDChart(); // Pour prendre les screenshot. Pas propre.
		JFreeChart getDensityChart();
		JFreeChart getDensityOverProbaChart();
	}
	
	/** Interface qui définit les actions du model qui peuvent etre
	 * appelé depuis le controller, et donc depuis la vue. 
	 *
	 */
	public interface IModel {

		void fittingSpecificConfig();
		void fittingNetworks();
		void displayInput();
		void fittingNextStep();
		void fittingOnce();
		void exploFitting();
		void rdmConfig();
		void setViewMemeAvailable(List<IUnitOfTransfer> memes);
		void takeSnapshot(long seed, Optional<ArrayList<String>> simulationPath);

		void suspend();
		void resume();
		void oneStep();

		double getDensity();
		String getDDInfos();
		NetworkProperties getCurrentNetProperties(int activator);
		IReadNetwork getReader();
		
		void generateGraph(int activator);
		void purgeLinks();
	}
	
	/** Interface qui définit une forme de représentation, ainsi que des
	 * calculs sur les propriétés de cette représentation
	 * 
	 */
	public interface INetworkRepresentation {
		/** Conversion d'un giteri.network vers la forme de représentation choisit.
		 * Doit etre fait avec un synchronized pour éviter la modification du 
		 * giteri.network pendant la copie.
		 * 
		 * @param toCopy Le réseau a copier. 
		 */
		 void convertNetwork(Network toCopy);
		/** Obtenir les propriétés issu de la représentation obtenue, avec un activator
		 * définissant quel type de propriété on souhaite obtenir. 
		 * 
		 * @param activator
		 */
		 NetworkProperties getNetworkProperties(Optional<NetworkProperties> toModify,String netName,int activator);
		/** Obtenir l'UUID du giteri.network que la représentation copie.
		 * {@inheritDoc}
		 * @return
		 */
		 int getRepresentationUUID();

		 /** Reset des propriétés de la copie du réseau
		 * 
		 */
		 void resetRepresentation();

		/** Ajout d'un noeud et de son set de lien a la représentation du réseau
		 *
		 * @param nodeIndex
		 * @param edgesIndexes
		 */
		 void addNodeWithEdge(int nodeIndex, List<Integer> edgesIndexes);

		 void addNodeWithEdge(int nodeFrom, int nodeTo, boolean directed);

		 boolean removeEdgeFromNodes(int nodeFrom, int nodeTo, boolean directed);

		 /** Obtient la liste des edges du giteri.network sous la forme
		 * IndexNode espace IndexNode, classé en ordre croissant. 
		 *  TODO Si volonté de faire apparaitre les noeuds seuls, modifier l'implémentation de cette fonction.
		 * @return
		 */
		 ArrayList<String> getNetworkEdges();
	}

	/** Element qui sera passé d'entité en entité.
	 * Peut etre un meme ou un couple de meme.
	 * Extends comparable pour les sort, Iterable pour pouvoir appliquer les memes fonctions
	 * aux couples<Meme> ou aux Meme simple.
	 * @param <T>
	 */
	public interface IUnitOfTransfer <T extends IUnitOfTransfer> extends Comparable<T>, Iterable<Meme> {
		void setProbaPropagation(double p);
		double getProbaPropagation();
		Configurator.TypeOfUOT getActionType();
		boolean isFluide();

		/** ADLKRDM etc etc
		 *
		 * @return
		 */
		String toFourCharString();

		/** add+ pour les singles
		 * add+.rmv- pour les couples.
		 *
		 * @return
		 */
		String toNameString();
	}


	/** Interface qui contient les parametres donnés par l'utilisation d'openmole,
	 * donc on oneshot, couple version ou non.
	 *
	 */
	public interface IOpenMoleParameter {

	}
}

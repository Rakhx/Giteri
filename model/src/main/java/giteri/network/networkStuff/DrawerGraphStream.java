package giteri.network.networkStuff;

import giteri.meme.entite.CoupleMeme;
import giteri.meme.entite.Meme;
import giteri.run.interfaces.Interfaces;

import java.awt.Color;
import java.io.IOException;
import java.util.*;

import giteri.meme.mecanisme.MemeFactory;
import giteri.network.network.Edge;
import giteri.network.network.Network;

import giteri.tool.math.Toolz;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Resolutions;

import giteri.tool.other.WriteNRead;
import giteri.run.configurator.Configurator;
import giteri.meme.entite.Entite;
import giteri.meme.entite.EntiteHandler;
import giteri.meme.event.ActionApplyEvent;
import giteri.meme.event.BehavTransmEvent;

import static giteri.run.configurator.Configurator.coupleVersion;

/** Classe de dessin pour graphStream
 *
 */
public class DrawerGraphStream extends StatAndPlotGeneric implements Interfaces.DrawerNetworkInterface {

	/** Constructeur sans paramètre.
	 *
	 */
	public DrawerGraphStream(EntiteHandler entiteHandler, MemeFactory memeFactory, NetworkConstructor networkConstructor,
							 WriteNRead wnr, NetworkFileLoader nfl, WorkerFactory wf) {
		super(entiteHandler, memeFactory, networkConstructor, wnr, nfl, wf);
		colorPieAsString = new Hashtable<>();
		colorPieAsColor = new Hashtable<>();
		setColorPie();
		fs = new FileSinkImages("pre", FileSinkImages.OutputType.PNG, FileSinkImages.Resolutions.HD1080,
				FileSinkImages.OutputPolicy.NONE);
		fs.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
	}


	// Graphe associé
	public Graph graph;
	// x3
	private int nbColorWanted = 3;
	private Hashtable<Integer, String> colorPieAsString;
	private Hashtable<Integer, Color> colorPieAsColor;

	FileSinkImages fs;


	/** Permet de transmettre l'instance du graph.
	 *
	 * @param thisgraph
	 */
	public void setGraph(Graph thisgraph){
		graph = thisgraph;
		fs = new FileSinkImages(OutputType.PNG, Resolutions.VGA);
		fs.setLayoutPolicy(LayoutPolicy.COMPUTED_ONCE_AT_NEW_IMAGE);
		defineGraphAttributes(graph);
	}

	//region Interface MemeListener

	/** Lorsqu'un meme est ajouté ou retiré d'une entité
	 *
	 */
	public void handlerBehavTransm(BehavTransmEvent e) {
		if(Configurator.displayMemePosessionDuringSimulation)
		// A priori inutile de parler du cas de remplacement car la classe de node écrase
		// la précédente. A revoir si des memes peuvent etre spontanement perdu.
		if(e.message == Configurator.MemeActivityPossibility.AjoutMeme.toString()){
			// On cherche le noeud concerné
			Node noeud = graph.getNode(""+e.entite.getIndex());
			setNodeClass(noeud, e.entite.getGraphStreamClass());
			if(Configurator.displayLogMemeTransmission)
				System.out.println(noeud+" "+e.meme.toNameString());
		}
	}


	//endregion

	//region Interface DrawerNetworkInterface

	/** Dessin d'un graphe initial, va aussi regarder les memes possédé par les
	 * noeuds pour l'initialisation des couleurs.
	 *
	 * @param outsideView Défini si on doit afficher le graphe Net dans une nouvelle vue ou non
	 */
	public void drawThisNetwork(Network net, boolean outsideView) {
		Graph toDisplay;
		if(outsideView){
		}
		else{
			toDisplay = graph;
		}
		String attrib;
		for (giteri.network.network.Node node : net.getNodes())
		{
			this.addNode(node.getIndex());
			// Etape pour connaitre les memes initiaux
			Entite entity = entiteHandler.getEntityCorresponding(node);
			if (entity != null) {
				attrib = entity.getGraphStreamClass();
				Node noeud = graph.getNode("" + entity.getIndex());
				setNodeClass(noeud, attrib);
			}
		}

		for (Edge edge : net.getEdges())
		{
			this.addEdge(edge.getNodeFrom().getIndex(), edge.getNodeTo().getIndex());
		}
	}

	/** Associe aux noeuds du réseau des classes qui permettront de changer leur couleurs d'affichage.
	 * Prend un network et des noeuds sur lesquels il faut appliquer la couleur propore au target
	 * Sur les autres noeuds, les passer en noirs. Le noeud qui fait l'action rester dans sa couleur
	 * d'origine.
	 *
	 * @param net
	 * @param nodeToDesignAsTarget
	 */
	public void applyTargetColor(Network net, Integer actingEntite, Set<Integer> nodeToDesignAsTarget){

		String attrib;
		boolean found = false;
		// On regarde tous les nodes du réseau
		for (giteri.network.network.Node node : net.getNodes()){
			// on regarde dans la liste des entités de target
			for (Integer integer : nodeToDesignAsTarget) {
				// Si l'entité qu'on regarde est aussi une target possible de l'action
				if(integer.intValue() == node.getIndex()){
					Node noeud = graph.getNode("" + node.getIndex());
					setNodeClass(noeud, "TARGET");
					found = true;
					break;
				}
			}
			// Dans le cas contraire; ca n'est pas une target
			if(!found){
				// Et qu'il ne s'agit pas de l'entité agissant
				if(node.getIndex() != actingEntite.intValue()){
					Node noeud = graph.getNode("" + node.getIndex());
					setNodeClass(noeud, "NOTTARGET");
				}
			}

			found = false;
		}
	}

	/** Semi progression stuff.
	 * Rend aux noeuds du réseau les classes de couleurs correspondant a leur comportements.
	 *
	 * @param net
	 */
	public void resetGoodColor(Network net){
		String attrib;
		for (giteri.network.network.Node node : net.getNodes())
		{
			// Etape pour connaitre les memes initiaux
			Entite entity = entiteHandler.getEntityCorresponding(node);
			if (entity != null) {
				attrib = entity.getGraphStreamClass();
				Node noeud = graph.getNode("" + entity.getIndex());
				setNodeClass(noeud, attrib);
			}
		}
	}

	/** Print un apercu du graphe dans la console.
	 *
	 */
	public void networkOverview() {
		System.out.println("GraphStream view");
		for(org.graphstream.graph.Edge e:graph.getEachEdge()) {
			System.out.println(e.getId());
		}
		System.out.println("GraphStreamVeiw : stop");
	}

	/** Faire une capture d'écran du réseau en cours.
	 *
	 */
	public void screenshotDisplay(ArrayList<String> rep){
		try {
			fs.writeAll(graph, writeNRead.createAndGetDirFromString(rep).getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e2){
			e2.printStackTrace();
		}
	}

	/** Reset les states du graph
	 *
	 */
	public void resetDisplay(){
		graph.clear();
//		seriesDensity.clear();
//		indexDensityAdded = 0;
		defineGraphAttributes(graph);
	}

	/** Ajout d'un edge dans le graphe visuel
	 *
	 */
	public void addEdge(int from, int to) {
		graph.addEdge(""+from+"-"+to, from, to, false);
	}

	/** Ajout d'un node dans le graphe visuel
	 *
	 */
	public void addNode(int nod) {
		graph.addNode(""+ nod);
	}

	/** Suppression d'un edge du graphe.
	 *
	 */
	public void removeEdge(int from, int to) {
		graph.removeEdge(from, to);
	}

	//endregion interface DrawerNetworkInterface

	//region Fonction propre à ce moteur graphique

	/** va associer une classe au node en fonction des memes qu'il contient
	 *
	 * @param node
	 * @param attribut
	 */
	private void setNodeClass(Node node, String attribut){
		if(node != null)
			node.addAttribute("ui.class", attribut);
	}

	/** Défini la correspondance Combinaison de meme <-> couleur pour Graphstream.
	 * Parcourt les combinaisons de meme existants dispo sur la map.
	 * @param graph
	 */
	// TODO [CV] -

	private void defineGraphAttributes(Graph graph){

		String attribut = "";
		Integer officialIndex;
		int nbMemeSolo = memeFactory.getMemes(Configurator.MemeList.ONMAP, Configurator.TypeOfUOT.BASIC).size();
		nbMemeSolo--;
		int aAppliquer;
		List<String> combinaisons = coupleVersion? entiteHandler.getMemeAvailableAsString(Configurator.FittingBehavior.onlyComplex) :
				entiteHandler.getMemeAvailableAsString(Configurator.FittingBehavior.simpleAndComplex);

		for (String combi : combinaisons) {
			officialIndex = memeFactory.getIndexFromMemeFourChar(combi);

			if (officialIndex != null)
				aAppliquer = officialIndex;
			else
				aAppliquer = ++nbMemeSolo;

			attribut += "node. " + combi + " {fill-color: " + colorPieAsString.get(aAppliquer) + ";}";
			if (Configurator.DisplayLogBehaviorColors) {
				System.out.println(memeFactory.translateMemeCombinaisonReadable(combi) + ":" + colorPieAsString.get(aAppliquer));
			}
		}

		// utilisé pour le step by step
		attribut += "node.TARGET {fill-color: " + colorPieAsString.get(100) + ";}";
		attribut += "node.NONTARGET {fill-color: " + colorPieAsString.get(101) + ";}";

		graph.addAttribute("ui.stylesheet", attribut);
	}

	/** Retourne la couleur correspondant a l'index en param
	 *
	 * @param index
	 * @return une java.awt.color
	 */
	public Color getColorAsColor(int index){
		synchronized(this.colorPieAsColor){
			return colorPieAsColor.get(index);
		}
	}
	/** Retourne la couleur correspondant a l'index en param
	 *
	 * @param index
	 * @return une java.awt.color
	 */
	public String getColorAsString(int index){
		synchronized(this.colorPieAsString){
			return colorPieAsString.get(index);
		}
	}

	/** TODO [WayPoint]- Initialisation des couleurs pour le graphe
	 *
	 */
	private void setColorPie(){

		int i= 0;
		for (int r = 0; r <= 250; r += 250/nbColorWanted)
			for (int g = 0; g <= 250; g += 250/nbColorWanted)
				for (int b = 0; b <= 250; b += 250/nbColorWanted)
				{
					if(r == g && r == b)
						continue;
					colorPieAsColor.put(i, new Color(r,g,b));
					i++;
				}

		colorPieAsColor = Toolz.shuffleHashmap(colorPieAsColor, false);
		for (Integer index: colorPieAsColor.keySet()) {
			colorPieAsString.put(index, "rgb("
					+ colorPieAsColor.get(index).getRed()+","
					+ colorPieAsColor.get(index).getGreen()+","
					+ colorPieAsColor.get(index).getBlue()+")");

		}

		if(Configurator.semiStepProgression){
			colorPieAsString.put(100, "rgb(204,0,0)");
			colorPieAsColor. put(100, new Color(204,0,0));
			colorPieAsString.put(101, "rgb(0,0,0)");
			colorPieAsColor. put(101, new Color(0,0,0));
			new Color(180,180,180);
		}
	}

	//endregion

	//region fonction pour obtenir des mesures sur le graph
	/** obtient la densité du graph au moment de l'appel 
	 * de la fonction
	 */
	private double getDensity() {
		return Toolkit.density(graph);
	}

	/** Obtient un tableau du degree de distribution. 
	 * [index] = n. N noeud possède un degré d'index. 
	 *
	 */
	private int[] getDegreeDistribution(){
		return Toolkit.degreeDistribution(graph);
	}

	/** Renvoi le degre moyen de?? densité?
	 *
	 */
	private double getAvgDegree() {
		Toolkit.averageDegree(graph);
		return 0;
	}

	/** TODO PAS EXACT DU TOUT DU TOUT
	 *
	 */
	private double getDDInterQrt() {
		Toolkit.degreeAverageDeviation(graph);
		return 0;
	}

	/**
	 *
	 * @return
	 */
	private int getNbEdges(){
		return graph.getEdgeCount();
	}

	/** Calcul du chemin le plus court moyen
	 *
	 */
//	public double getAPL(){
//		APSP apsp = new APSP();
//		apsp.init(graph);
//		apsp.setDirected(false);
//		apsp.compute();
//		APSPInfo info = graph.getNode("10").getAttribute(APSPInfo.ATTRIBUTE_NAME);
//		double total = 0;
//		int nbValue = 0;
//		for (int i = 0; i < graph.getNodeCount(); i++) {
//			info =  graph.getNode(""+i).getAttribute(APSPInfo.ATTRIBUTE_NAME);
//			for (String string : info.targets.keySet()) {
//				total += info.targets.get(string).distance;
//				nbValue++;
//			}
//		}
//
//		return  total / nbValue;
//	}

	//endregion
}

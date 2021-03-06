package giteri.network.networkStuff;

import giteri.meme.entite.EntiteHandler;
import giteri.meme.entite.Meme;
import giteri.network.network.NetworkProperties;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.FittingBehavior;
import giteri.run.configurator.Configurator.NetworkAttribType;
import giteri.run.controller.Controller.VueController;
import giteri.run.interfaces.Interfaces.IModel;
import giteri.run.interfaces.Interfaces.IReadNetwork;
import giteri.run.interfaces.Interfaces.StatAndPlotInterface;
import giteri.tool.other.WriteNRead;

import java.io.IOException;
import java.util.*;

/** Classe de représentation du modèle proposé via un controller
 * à l'IHM. 
 *
 */
public class CommunicationModel implements IModel {

	EntiteHandler eh;
	NetworkConstructor nc;
	private StatAndPlotInterface calculator;
	private NetworkFileLoader nl;
	public VueController view;
	private WorkerFactory wf;

	//region constructeur
	/** Constructeur sans paramètre.
	 *
	 */
	public CommunicationModel(EntiteHandler eh,
							  NetworkConstructor nc,
							  // TODO pas propre la facon dont la lecture d'un file est réalisée
							  NetworkFileLoader ln,
							  WorkerFactory wf,
							  StatAndPlotInterface calculator ) {
		this.eh = eh;
		this.nc = nc;
		this.wf = wf;
		this.calculator = calculator;
		this.nl = ln;

	}

	public void setViewController(VueController vue){
		view = vue;
	}
	//endregion

	//region getting data

	/** Obtenir la liste des behaviors disponibles
	 *
	 */
	public Hashtable<Integer, ArrayList<Meme>>  getMemesAvailables(FittingBehavior setAsked){
		return eh.getMemeAvailable(setAsked, Optional.empty());
	}

	/** Obtenir la valeur de densité du graphe.
	 * Comprise entre 0 et 1.
	 *
	 */
	public double getDensity() {
		nc.updatePreciseNetworkProperties(Configurator.getIndicateur(NetworkAttribType.DENSITY));
		return nc.getNetworkProperties().getDensity();
	}

	/** Obtenir la distribution de degrée du
	 *
	 */
	public String getDDInfos() {
		return calculator.getDDInfos();
	}

	/** Obtient les propriétés du réseau courant, en fonction
	 * des activations // LES ACTIVATIONS NE SONT PAS PRISES EN COMPTE
	 *
	 */
	public NetworkProperties getCurrentNetProperties(int activator){
		nc.updatePreciseNetworkProperties(activator);
		return nc.getNetworkProperties();
	}

	/** Renvoi un reader de fichier texte implémentant l'interface.
	 *
	 */
	public IReadNetwork getReader() {
		return nl;
	}

	//endregion

	//region asking updates & lancement processus

	/**
	 *
	 */
	public void fittingSpecificConfig() {
		System.out.println("Pouet");
//		this.calculator.fitNetwork(Configurator.EnumLauncher.ihm, Configurator.EnumExplorationMethod.specific, Optional.empty(), Optional.empty());
	}

	/** Plus de fonctionnalité
	 *
	 */
	public void fittingOnce(){
		Configurator.explorator = Configurator.EnumExplorationMethod.oneShot;
		Configurator.typeOfMemeUseForFitting = Configurator.MemeList.FITTING;
		this.calculator.fitNetwork(Configurator.EnumLauncher.ihm, Configurator.EnumExplorationMethod.oneShot,Optional.empty(),Optional.empty());
	}

	/** Lancement du processus de fittage du réseau courant a celui
	 * en paramètre.
	 *
	 */
	public void fittingNetworks(){
		this.calculator.fitNetwork(Configurator.EnumLauncher.ihm, Configurator.EnumExplorationMethod.exhaustive, Optional.empty(), Optional.empty());
	}

	/** Lorsqu'on fait du step by step pour le fitting.
	 *
	 */
	public void fittingNextStep(){
		calculator.fitNextStep();
	}

	public void exploFitting(){
		this.calculator.exploFitting();
	}

	/** Affichage d'un graphe représentant le fichier donné en input.
	 *
	 */
	public void displayInput(){


		WriteNRead wr = new WriteNRead();
		try {
			wr.readAndCreateNetwork(Configurator.defaultPathForReadingNetwork,nl," ","#");
		} catch (IOException e) {
			e.printStackTrace();
		}
		nl.getGraphFromDataRead();
	}

	/** Indique aux vues les memes disponibles sur la map.
	 *
	 * @param memes
	 */
	public void setViewMemeAvailable(List<Meme> memes){
		view.setMemeAvailable(memes);
	}

	public void rdmConfig(){
	}

	/** Sauvegarde des informations sur le réseau courant dans un dossier.
	 * TODO repetition avec la fonction de takesnapeshot de nl. enlever l'optional?
	 */
	public void takeSnapshot(long seed, Optional<ArrayList<String>> simulationPath){
		String screen = "Screenshot.png";
		ArrayList<String> path = simulationPath.orElse(new ArrayList<>
				(Arrays.asList(Configurator.repByDefault,""+Configurator.getDateFormat().format(new Date()))));
		nl.takeSnapshot(path, seed);
		path.add(screen);
		wf.getDrawer().screenshotDisplay(path);
		path.remove(screen);
	}

	//endregion

	//region Generation Graphe

	/**
	 *
	 */
	public void generateGraph(int activator) {
		eh.suspend();
		nc.suspend();

		eh.resetStat();
		nc.resetStat();
		wf.getDrawer().resetDisplay();
		view.resetIHM();

		eh.generateNetwork(activator);
		eh.synchronizeNodeConnectionWithEntiteConnection();

		wf.getDrawer().drawThisNetwork(nc.networkInstance, false);
		// eh.giveMemeToEntite(Configurator.methodOfGeneration);

		// Reste en pause après avoir générer les graphes depuis l'interface
//		synchronized (nc) {				nc.resume();			}
//		synchronized (eh) {				eh.resume();			}
	}

	/**
	 *
	 */
	public void purgeLinks(){
		eh.purgeLink();
	}

	//endregion

	//region thread stuff

	/** Reset tout les éléments nécessaires pour relancer une simulation
	 * du réseau ou de fitting ou autre.
	 */
	public void resetStuff(){
		eh.resetStat();
		nc.resetStat();
		wf.getDrawer().resetDisplay();
		view.resetIHM();
		generateGraph(0);
	}

	public void suspend() {
		eh.suspend();
		nc.suspend();
	}

	public void resume() {
		eh.resume();
		nc.resume();
	}

	public void oneStep() {
		eh.OneStep();
//		nc.OneStep();
	}

	//endregion
}

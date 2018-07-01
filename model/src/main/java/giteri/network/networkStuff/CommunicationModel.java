package giteri.network.networkStuff;

import giteri.run.interfaces.Interfaces.IModel;
import giteri.run.interfaces.Interfaces.IReadNetwork;
import giteri.run.interfaces.Interfaces.StatAndPlotInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Optional;

import giteri.network.network.NetworkProperties;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.FittingBehavior;
import giteri.run.configurator.Configurator.NetworkAttribType;
import giteri.run.controller.Controller.VueController;
import giteri.meme.entite.EntiteHandler;
import giteri.meme.entite.Meme;

/** Classe de représentation du modèle proposé via un controller
 * à l'IHM. 
 *
 */
public class CommunicationModel implements IModel {

	public EntiteHandler eh;
	public NetworkConstructor nc;
	public StatAndPlotInterface calculator;
	public NetworkFileLoader nl;
	public VueController view;
	public WorkerFactory wf;

	// Region SINGLETON

	/** Constructeur sans paramètre.
	 *
	 */
	public CommunicationModel(EntiteHandler eh,
							  NetworkConstructor nc,
							  NetworkFileLoader ln,
							  WorkerFactory wf,
							  StatAndPlotInterface calculator ) {
		this.eh = eh;
		this.nc = nc;
		this.wf = wf;
		this.calculator = calculator;
		this.nl = ln;

	}

	// EndRegion

	public void setCalculator(StatAndPlotInterface stat){
		this.calculator = stat;
	}


	/** Constructeur.
	 *
	 */
	public void setViewController(VueController vue){
		view = vue;
	}

	// Region getting data

	/** Obtenir la liste des behaviors disponibles
	 *
	 */
	public Hashtable<Integer, ArrayList<Meme>>  getMemesAvailables(FittingBehavior setAsked){
		return eh.getMemeAvailable(setAsked);
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
		if(activator != Configurator.activationCodeAllAttribExceptDD)
			nc.updatePreciseNetworkProperties(activator);
		else
			nc.updateAllNetworkProperties();
		return nc.getNetworkProperties();
	}

	/** Renvoi un reader de fichier texte implémentant l'interface.
	 *
	 */
	public IReadNetwork getReader() {
		return nl;
	}

	// EndRegion

	// Region asking updates & lancement processus

	/**
	 *
	 */
	public void stabilityResearch() {
		this.calculator.fitNetwork(0);
	}

	/** Lancement du processus de fittage du réseau courant a celui
	 * en paramètre.
	 *
	 */
	public void fittingNetworks(){
		this.calculator.fitNetwork(1);
	}

	public void displayPolar(){

	}

	/** Plus de fonctionnalité
	 *
	 */
	public void toggleStep(){
		// Configurator.fittingStepByStep  = !Configurator.fittingStepByStep ;
	}

	public void rdmConfig(){
//		calculator.testStability();
	}

	/** Sauvegarde des informations sur le réseau courant dans un dossier.
	 * TODO repetition avec la fonction de takesnapeshot de nl. enlever l'optional?
	 */
	public void takeSnapshot(long seed, Optional<ArrayList<String>> simulationPath){
		String screen = "Screenshot.png";
		ArrayList<String> path = simulationPath.orElse(new ArrayList<String>
				(Arrays.asList("defaultRep",""+Configurator.getDateFormat().format(new Date()))));
		nl.takeSnapshot(path, seed);
		path.add(screen);
		wf.getDrawer().screenshotDisplay(path);
		path.remove(screen);

	}

	/** Lorsqu'on fait du step by step pour le fitting.
	 *
	 */
	public void fittingNextStep(){
		calculator.fitNextStep();
	}

//	public void
	// EndRegion

	// Region Generation Graphe

	/**
	 *
	 */
	public void generateGraph(int activator) {
		eh.suspend();
		nc.suspend();

		eh.resetStat(false);
		nc.resetStat();
		wf.getDrawer().resetDisplay();
		view.resetIHM();

		eh.generateNetwork(activator);

		wf.getDrawer().drawThisNetwork(nc.networkInstance);
		eh.synchronizeNodeConnectionWithEntiteConnection(nc.networkInstance);

		eh.giveMemeToEntite(Configurator.methodOfGeneration);

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

	// EndRegion

	// Region thread stuff

	/** Reset tout les éléments nécessaires pour relancer une simulation
	 * du réseau ou de fitting ou autre.
	 */
	public void resetStuff(){
		eh.resetStat(true);
		nc.resetStat();
		wf.getDrawer().resetDisplay();
		view.resetIHM();
		wf.getDrawer().drawThisNetwork(nc.networkInstance);
		generateGraph(2);
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


	// EndRegion

	// Region private method

	// EndRegion
}

package giteri.network.networkStuff;

import giteri.meme.entite.EntiteHandler;
import giteri.network.network.NetworkProperties;
import giteri.run.configurator.CasteOpenMoleParameter;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.NetworkAttribType;
import giteri.run.controller.Controller.VueController;
import giteri.run.interfaces.Interfaces.IModel;
import giteri.run.interfaces.Interfaces.IReadNetwork;
import giteri.run.interfaces.Interfaces.StatAndPlotInterface;
import giteri.tool.math.Toolz;
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
	}

	/** Plus de fonctionnalité
	 * TODO [WayPoint] Couple selection C/C fichier openMole
	 */
	public void fittingOnce(){
		Configurator.explorator = Configurator.EnumExplorationMethod.oneShot;
		List<Double> param = new ArrayList<>(Arrays.asList(



				//RDM 2% not fixed
				// 2.,8136.160881859491,0.4,0.1,0.30000000000000004,0.1
				// 2.,8469.551490382237,0.7,0.0,0.1,0.5

				// rdm 30% not fixed
				// Reseau a densité .08
			 	//4.,8915.621431168347,0.5,0.5,0.5,0.5
				// Réseau ou le couple ne se propage pas, donc double random
			//	1.,9018.693376428437,0.2,0.4,0.8999999999999999,0.7999999999999999
				//réseau 0.05
				// 3.,7270.658561195815,0.2,0.9999999999999999,0.4,0.30000000000000004

				// rdm 30% fixed // plateau sur le 30% puis monte
				//4.,8736.998931322976,0.7,0.7999999999999999,0.8999999999999999,0.8999999999999999
				// idem
			//	4.,6660.851730329648,0.4,0.7,0.2,0.7
				// bloqué a 10%
			//	4.,7656.443397177638,0.5,0.0,0.5,0.6

				// Scale free FIXED
			//	4.,2601.0493688534166,0.9999999999999999,0.8999999999999999,0.7,0.8999999999999999
			//	4.,2601.049368853417,0.9999999999999999,0.8999999999999999,0.7,0.8999999999999999
			//  ! fixed

			//	4.,6810.241829328059,0.7,0.30000000000000004,0.0,0.8999999999999999
			//	1.,1632.354391137285,0.0,0.0,0.4,0.5
			//	1.,10000.0,0.0,0.0,0.0,0.5
				//4.,6598.278719720552,0.0,0.8999999999999999,0.5,0.8999999999999999
			//	1.,7517.289043657569,0.5,0.30000000000000004,0.0,0.6



				// SW FIXED
				//4.,1682.1578236601042,0.8999999999999999,0.30000000000000004,0.0,0.9999999999999999
				//4.,244.402235453462,0.7,0.0,0.7,0.0
				// zolie
				4.,8210.98736073283,0.2,0.6,0.5,0.5

				// Jazz
				//4.,4208.258271016998,0.7,0.7,0.0,0.9999999999999999
				//4.,0.0,0.0,0.7999999999999999,0.6,0.9999999999999999
				//4.,7996.092504983751,0.7,0.9999999999999999,0.9999999999999999,0.9999999999999999
			//	4.,4495.600396080507,0.7999999999999999,0.9999999999999999,0.7,0.8999999999999999
			//	4.,5297.973178956758,0.5,0.6,0.1,0.2


		));

		int combinaison = Toolz.combinatoire((int)Math.floor(param.get(0)), 121 );
		double activation = param.get(1) * combinaison / 10000;

		CasteOpenMoleParameter comp = new CasteOpenMoleParameter(((int)Math.floor(activation)),(int)Math.floor(param.get(0)), 121,
				param.subList(2,6));
		this.calculator.fitNetwork(Configurator.EnumLauncher.ihm, Configurator.EnumExplorationMethod.oneShot,comp);
	}

	/** Lancement du processus de fittage du réseau courant à celui
	 * en paramètre.
	 *
	 */
	public void fittingNetworks(){

		this.calculator.fitNetwork(Configurator.EnumLauncher.ihm, Configurator.EnumExplorationMethod.exhaustive, null);
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

package giteri.network.networkStuff;

import giteri.run.interfaces.Interfaces.DrawerInterface;
import giteri.run.interfaces.Interfaces.StatAndPlotInterface;
import giteri.run.jarVersion.WorkerFactoryJarVersion;
import giteri.network.network.Network;
import giteri.run.configurator.Configurator;



/** Classe permettant d'accéder aux classes de dessin et de calcul
 * associé au réseau etc.
 *  TODO possibilité de merger ca dans communicationModel et faire passer
 *  ce dernier en singleton?
 */
public class WorkerFactory {

	protected StatAndPlotInterface calculator;
	protected DrawerInterface drawer;
	public Object waitingForReset;

	public WorkerFactory(){
		waitingForReset = new Object();
	}

	public void setNecessary(StatAndPlotInterface statAnd, DrawerInterface dra ){
		calculator = statAnd;
		drawer = dra;
	}

	/** Constructeur sans paramètre.
	 *
	 */
	public WorkerFactory(DrawerGraphStream dra) {
		waitingForReset = new Object();
		calculator = dra;
		drawer = dra;
	}

	public WorkerFactory(NetworkAnalyzer anal ) {
		waitingForReset = new Object();
		calculator = anal;
		drawer = new DrawerStub();
	}

	/** Bouger la condition du boolean an l'appel du worker factory.
	 *
	 * @param statAnd
	 * @param dra
	 */
//	public WorkerFactory(StatAndPlotInterface statAnd, DrawerInterface dra ) {
//		waitingForReset = new Object();
//		calculator = statAnd;
//		drawer = dra;
//	}

//	public static WorkerFactory getInstance()
//	{
//		if( INSTANCE == null)
//			if(!Configurator.jarMode)
//				INSTANCE = new WorkerFactory();
//			else
//				INSTANCE = WorkerFactoryJarVersion.getInstance();
//
//		return INSTANCE;
//	}

	public StatAndPlotInterface getCalculator(){
		return calculator;
	}

	public DrawerInterface getDrawer(){
		return drawer;
	}

}

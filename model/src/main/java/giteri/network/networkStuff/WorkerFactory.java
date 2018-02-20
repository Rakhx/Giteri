package giteri.network.networkStuff;

import giteri.run.interfaces.Interfaces.DrawerInterface;
import giteri.run.interfaces.Interfaces.StatAndPlotInterface;
import giteri.run.jarVersion.WorkerFactoryJarVersion;
import giteri.network.network.Network;
import giteri.run.configurator.Configurator;



/** Classe permettant d'accéder aux classes de dessin et de calcul
 * associé au réseau etc.
 */
public class WorkerFactory {

	protected StatAndPlotInterface calculator;
	protected DrawerInterface drawer;
	public final Object waitingForReset = new Object();

	public WorkerFactory(){

	}

	public void setNecessary(StatAndPlotInterface statAnd, DrawerInterface dra ){
		calculator = statAnd;
		drawer = dra;
	}

	/** Constructeur sans paramètre.
	 *
	 */
	public WorkerFactory(DrawerGraphStream dra) {
		calculator = dra;
		drawer = dra;
	}

	public WorkerFactory(NetworkAnalyzer anal ) {
		calculator = anal;
		drawer = new DrawerStub();
	}

	public StatAndPlotInterface getCalculator(){
		return calculator;
	}

	public DrawerInterface getDrawer(){
		return drawer;
	}

}

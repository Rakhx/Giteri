package giteri.network.networkStuff;

import giteri.run.interfaces.Interfaces.DrawerNetworkInterface;
import giteri.run.interfaces.Interfaces.StatAndPlotInterface;
import giteri.run.jarVersion.StatAndPlotJarVersion;


/** Classe permettant d'accéder aux classes de dessin et de calcul
 * associé au réseau etc.
 */
public class WorkerFactory {

	protected StatAndPlotInterface calculator;
	protected DrawerNetworkInterface drawer;
	public final Object waitingForReset = new Object();

	public WorkerFactory(){

	}

	public void setNecessary(StatAndPlotInterface statAnd, DrawerNetworkInterface dra ){
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

	public WorkerFactory(StatAndPlotJarVersion anal ) {
		calculator = anal;
		drawer = new DrawerStub();
	}

	public StatAndPlotInterface getCalculator(){
		return calculator;
	}

	public DrawerNetworkInterface getDrawer(){
		return drawer;
	}

}

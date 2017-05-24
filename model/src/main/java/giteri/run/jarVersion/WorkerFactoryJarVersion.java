package giteri.run.jarVersion;

import giteri.network.networkStuff.DrawerStub;
import giteri.network.networkStuff.WorkerFactory;
import giteri.run.interfaces.Interfaces;

public class WorkerFactoryJarVersion extends WorkerFactory {

	/** Constructeur sans paramètre.
	 *
	 */
	public WorkerFactoryJarVersion() {
	}

	public void setNecessary(Interfaces.StatAndPlotInterface statAnd, Interfaces.DrawerInterface dra ){
		calculator = statAnd;
		drawer = dra;
	}

}

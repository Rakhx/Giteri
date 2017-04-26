package jarVersion;

import networkStuff.DrawerStub;
import networkStuff.WorkerFactory;

public class WorkerFactoryJarVersion extends WorkerFactory {

	
	// Region singleton Stuff
	private static WorkerFactoryJarVersion INSTANCE = null;
	
	/** Constructeur sans paramètre.
	 * 
	 */
	protected WorkerFactoryJarVersion() {
		waitingForReset = true;
	
		calculator = StatAndPlotJarVersion.getInstance();
		drawer = new DrawerStub();
		
	}

	/** Fourni l'unique instance de la classe.
	 * 
	 */
	public static WorkerFactoryJarVersion getInstance()
	{
		
		if( INSTANCE == null)
			INSTANCE = new WorkerFactoryJarVersion();
		
		return INSTANCE;
	}
}

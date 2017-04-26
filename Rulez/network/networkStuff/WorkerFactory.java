package networkStuff;

import interfaces.Interfaces.DrawerInterface;
import interfaces.Interfaces.StatAndPlotInterface;
import jarVersion.WorkerFactoryJarVersion;
import network.Network;
import configurator.Configurator;


/** Classe permettant d'accéder aux classes de dessin et de calcul
 * associé au réseau etc. 
 *  TODO possibilité de merger ca dans communicationModel et faire passer
 *  ce dernier en singleton? 
 */
public class WorkerFactory {

	protected StatAndPlotInterface calculator;
	protected DrawerInterface drawer;
	public Object waitingForReset;
	
	// Region singleton Stuff
	private static WorkerFactory INSTANCE = null;
	
	/** Constructeur sans paramètre.
	 * 
	 */
	protected WorkerFactory() {
		waitingForReset = new Object();
		if(Configurator.withGraphicalDisplay)
		{
			calculator = DrawerGraphStream.getInstance();
			drawer = DrawerGraphStream.getInstance();
		}else {
			calculator = NetworkAnalyzer.getInstance();
			drawer = new DrawerStub();
		}
	}

	/** Fourni l'unique instance de la classe.
	 * 
	 */
	public static WorkerFactory getInstance()
	{
		if( INSTANCE == null)
			if(!Configurator.jarMode)
				INSTANCE = new WorkerFactory();
			else
				INSTANCE = WorkerFactoryJarVersion.getInstance();
		
		return INSTANCE;
	}
	// EndRegion
	
	public StatAndPlotInterface getCalculator(){
		return calculator;
	}
	
	public DrawerInterface getDrawer(){
		return drawer;
	}
	
	public void setNetwork(Network net){
//		calculator.init(net);
	}
}

package giteri.network.networkStuff;

import giteri.meme.entite.EntiteHandler;
import giteri.meme.mecanisme.MemeFactory;
import giteri.run.configurator.Configurator;
import giteri.meme.event.ActionApplyEvent;
import giteri.tool.other.WriteNRead;
//import giteri

/** fait des calculs pour analyser un réseau, deux modes disponibles.
 * - Association d'un réseau a la classe, et renvoi les propriétés après recalcul
 * si necessaire. Le réseau associé est censé être le courant. 
 * - Obtenir les propriétés d'un réseau donné en paramètre.
 *
 */
public class NetworkAnalyzer extends StatAndPlotGeneric {

	// Region Properties 



	// Le réseau

	// Attribut propre a la classe
	private int module = 0;

	// EndRegion

	// Region Constructor

	private static NetworkAnalyzer INSTANCE = null;

	/** Constructeur sans paramètre.
	 *
	 */
	public NetworkAnalyzer(EntiteHandler entiteHandler, MemeFactory memeFactory, NetworkConstructor networkConstructor,
						   WriteNRead wnr, NetworkFileLoader nfl, WorkerFactory wf) {
		super(entiteHandler, memeFactory, networkConstructor, wnr, nfl,wf);
	}

//	/** Fourni l'unique instance de la classe.
//	 *
//	 */
//	public static NetworkAnalyzer getInstance()
//	{
//		if( INSTANCE == null)
//			INSTANCE = new NetworkAnalyzer();
//
//		return INSTANCE;
//	}

	// EndRegion

	// Region Public methods

	// Region StatAndPlotInterface

	/** lors d'un changement de type ajout ou retrait de lien
	 *
	 */
	public void handlerActionApply(ActionApplyEvent e) {
		if(++module%Configurator.refreshInfoRate == 0){
			module = 0;
			// mise a jour d'un boolean 
		}
	}

	@Override
	public double getAPL() {
		return 0;
	}

	// EndRegion

	// EndRegion

}

package giteri.run.jarVersion;

import giteri.meme.entite.EntiteHandler;
import giteri.meme.event.ActionApplyEvent;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.networkStuff.*;
import giteri.run.configurator.Configurator;
import giteri.tool.other.WriteNRead;

public class StatAndPlotJarVersion extends StatAndPlotGeneric {

	// Attribut propre à la classe
	// private int module = 0;

	/**
	 *
	 */
	public StatAndPlotJarVersion(EntiteHandler entiteHandler, MemeFactory memeFactory, NetworkConstructor networkConstructor,
								 WriteNRead wnr, NetworkFileLoader nfl, WorkerFactory wf) {
		super(entiteHandler, memeFactory, networkConstructor, wnr, nfl, wf);
	}

}

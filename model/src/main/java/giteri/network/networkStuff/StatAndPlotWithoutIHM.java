package giteri.network.networkStuff;

import giteri.meme.entite.EntiteHandler;
import giteri.meme.event.ActionApplyEvent;
import giteri.meme.mecanisme.MemeFactory;
import giteri.tool.other.WriteNRead;

public class StatAndPlotWithoutIHM extends StatAndPlotGeneric {

    /**
     *
     * @param entiteHandler
     * @param memeFactory
     * @param networkConstructor
     * @param wnr
     * @param nfl
     * @param wf
     */
    public StatAndPlotWithoutIHM(EntiteHandler entiteHandler, MemeFactory memeFactory, NetworkConstructor networkConstructor,
                                 WriteNRead wnr, NetworkFileLoader nfl, WorkerFactory wf){
        super(entiteHandler, memeFactory, networkConstructor, wnr, nfl, wf);
    }

}

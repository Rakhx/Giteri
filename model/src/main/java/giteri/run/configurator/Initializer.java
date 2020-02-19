package giteri.run.configurator;

import giteri.meme.entite.EntiteHandler;
import giteri.meme.mecanisme.ActionFactory;
import giteri.meme.mecanisme.AttributFactory;
import giteri.meme.mecanisme.FilterFactory;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.networkStuff.*;
import giteri.run.controller.Controller;
import giteri.run.displaysStuff.ConsoleView;
import giteri.run.displaysStuff.FileView;
import giteri.run.displaysStuff.IHM;
import giteri.run.interfaces.Interfaces;
import giteri.tool.math.Toolz;
import giteri.tool.other.StopWatchFactory;
import giteri.tool.other.WriteNRead;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static giteri.run.configurator.Configurator.fullSilent;
import static giteri.run.configurator.Configurator.withGraphicalDisplay;



/** Classe d'initialisation des objets nécessaires à l'utilisation du framework
 * Commun à ttes les classes.
 * idée :
 *                                                          -> IHM ( Set des meme utilisés, des probas =)
 * Lancement de l'appli => initializer ( Meme existant )    -> JAR ( lecture des memes et probas )
 *
 */
public class Initializer {
    public static Double initialize(Configurator.EnumLauncher launcher, Interfaces.IOpenMoleParameter parameters) {

        if(Configurator.timeEfficiency) {
            StopWatchFactory.getInstance().addWatch("", "perf");
            StopWatchFactory.getInstance().startWatch("perf");
        }

        Configurator.typeOfConfig = launcher;
        boolean ihmLauncher = (launcher == Configurator.EnumLauncher.ihm) ;

        if(ihmLauncher){
            Configurator.displayPlotWhileSimulation = true;
            Configurator.withGraphicalDisplay = true;
            Configurator.jarMode = false;
            Configurator.systemPaused = true;
            Configurator.writeNetworkResultOnFitting = !fullSilent;
            Configurator.writeMemeResultOnFitting = !fullSilent;
            Configurator.explorator = Configurator.EnumExplorationMethod.exhaustive;
            Configurator.limitlessAction = true;
        }
        else{
            // La configuration de base correspond a OpenMole, car histoire de multi acces a des variables
            // depuis la meme JVM donc ne pas modifier du static. Les launchers pour autres usages changent cette configuration initiale
            Configurator.withGraphicalDisplay = false;
            Configurator.jarMode = true;
            Configurator.systemPaused = false;
            Configurator.writeNetworkResultOnFitting = !fullSilent;
            Configurator.writeMemeResultOnFitting = !fullSilent;
            Configurator.nbRepetitionbyRun = Configurator.nbRepetitionForJar;
            Configurator.limitlessAction = false;
        }

        WriteNRead writeNRead = new WriteNRead();
        AttributFactory attributFactory = new AttributFactory();
        FilterFactory filterFactory = new FilterFactory();
        ActionFactory actionFactory = new ActionFactory() ;
        MemeFactory memeFactory = new MemeFactory(actionFactory, filterFactory, attributFactory);
        WorkerFactory workerFactory = new WorkerFactory();
        NetworkConstructor networkConstructor = new NetworkConstructor();
        EntiteHandler entiteHandler = new EntiteHandler(networkConstructor, memeFactory, workerFactory);
        NetworkFileLoader networkFileLoader = new NetworkFileLoader(memeFactory, writeNRead);
        DrawerGraphStream drawerGraphStream = null;
        StatAndPlotGeneric stat = null;

        // Communication model
        CommunicationModel communicationModel ;
        if(Configurator.withGraphicalDisplay) {
            drawerGraphStream = new DrawerGraphStream(entiteHandler, memeFactory, networkConstructor, writeNRead, networkFileLoader, workerFactory);
            communicationModel = new CommunicationModel(entiteHandler, networkConstructor, networkFileLoader, workerFactory, drawerGraphStream);
            drawerGraphStream.setCommunicationModel(communicationModel);
        } else {
            stat = new StatAndPlotWithoutIHM(entiteHandler, memeFactory, networkConstructor, writeNRead, networkFileLoader, workerFactory);
            communicationModel = new CommunicationModel(entiteHandler, networkConstructor, networkFileLoader, workerFactory, stat);
            stat.setCommunicationModel(communicationModel);
        }

        networkFileLoader.setCommunicationModel(communicationModel);
        actionFactory.setEntiteHandler(entiteHandler);
        filterFactory.setEntiteHandler(entiteHandler);

        if( withGraphicalDisplay ){
            workerFactory.setNecessary(drawerGraphStream, drawerGraphStream);
            networkConstructor.setDrawer(drawerGraphStream);
        }else {
            workerFactory.setNecessary(stat, new DrawerStub());
            networkConstructor.setDrawer(new DrawerStub());
        }

        // Controller
        Controller c = new Controller();
        Controller.VueController vControl = c.new VueController();
        Controller.ModelController mControl = c.new ModelController(vControl, communicationModel);

        if (ihmLauncher) {
            entiteHandler.initialisation();
            IHM fenetre;
            // La fenetre en elle meme Controller de Model donné a l'IHM
            if(withGraphicalDisplay)
                fenetre = new IHM(mControl,
                        memeFactory,
                        entiteHandler,
                        drawerGraphStream,
                        writeNRead);
            else
                fenetre = new IHM(mControl,
                        memeFactory,
                        entiteHandler,
                        new DrawerStub(),
                        writeNRead);

            // le gestionnaire de multiple vue possible.
            vControl.addView(fenetre);
            if((Configurator.activationCodeForView & 4) == 4)
                vControl.addView(new FileView(false));
            if((Configurator.activationCodeForView & 2) == 2)
                vControl.addView(new ConsoleView());

            // Le graph associé lors de l'affichage avec graphstream
            if (Configurator.withGraphicalDisplay) {
                Graph graph = new SingleGraph("Embedded");
                drawerGraphStream.setGraph(graph);
                graph.display();
            }

            Interfaces.IReadNetwork nl = mControl.getReader();
            fenetre.setVisible(true);
            entiteHandler.setVueController(vControl);

            // fait le lien entre les entités d'action et de transmission de meme
            // avec l'IHM, pour permettre la mise a jour des affichages etc
//            entiteHandler.addEntityListener(fenetre);
            entiteHandler.addMemeListener(fenetre);

            // De meme, le dessinateur graphique s'abonne aux évènements de type transmission de meme
            // Dans le but de faire changer la couleur du noeud en fonction des memes possédés par ce dernier
            entiteHandler.addMemeListener(workerFactory.getDrawer());
           // entiteHandler.addEntityListener(workerFactory.getCalculator());

            networkConstructor.start();
            if (!Configurator.isSystemPaused()) {
                networkConstructor.start();
                entiteHandler.start();
            } else {
                networkConstructor.start();
                networkConstructor.suspend();
                entiteHandler.start();
                entiteHandler.suspend();
            }

            //huhum
//            if(Configurator.coupleVersion)
//                entiteHandler.giveCoupleMemeToEntite(memeFactory.getCoupleMemes());
            return 0.;
        }

        // Crée une fenetre stub
       else  {
            if((Configurator.activationCodeForView & 4) == 4)
                vControl.addView(new FileView(false));
            if((Configurator.activationCodeForView & 2) == 2)
                vControl.addView(new ConsoleView());
            entiteHandler.setVueController(vControl);
            entiteHandler.initialisation();
            entiteHandler.addMemeListener(workerFactory.getDrawer());
          //  entiteHandler.addEntityListener(workerFactory.getCalculator());

            Interfaces.IReadNetwork nl = mControl.getReader();

            entiteHandler.suspend();
            networkConstructor.suspend();
            networkConstructor.start();
            entiteHandler.start();


            return launchForClassic(stat, parameters);

       }
    }

    /** méthode pour lancement spécifique au cas classique.
     *
     * @param stat
     * @param parameters
     * @return
     */
    public static Double launchForClassic(StatAndPlotGeneric stat, Interfaces.IOpenMoleParameter parameters){
        return stat.fitNetwork(Configurator.EnumLauncher.jarC,
                Configurator.EnumExplorationMethod.oneShot,
                parameters);

    }


}

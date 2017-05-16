package giteri.run;

import giteri.fitting.algo.IExplorationMethod;
import giteri.fitting.parameters.IModelParameter;
import giteri.meme.entite.EntiteHandler;
import giteri.meme.entite.Meme;
import giteri.meme.mecanisme.ActionFactory;
import giteri.meme.mecanisme.AgregatorFactory;
import giteri.meme.mecanisme.AttributFactory;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.networkStuff.*;
import giteri.run.configurator.Configurator;
import giteri.run.controller.Controller;
import giteri.run.interfaces.Interfaces;
import giteri.run.jarVersion.StatAndPlotJarVersion;
import giteri.run.jarVersion.WorkerFactoryJarVersion;
import giteri.test.TestProvider;
import giteri.tool.other.WriteNRead;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

/** Classe d'initialisation des objets nécessaires a l'utilisation du framework
 * Commun a ttes les classes.
 */
public class Initializer {
    public static Double initialize(Configurator.EnumLauncher launcher, File fileInput, ArrayList<Double> probaBehavior) {

        // A instancier dans les if. a lancer dans tous les cas a la fin?
        Runnable willBeRun;

        if(launcher == Configurator.EnumLauncher.jar){
//            Configurator.methodOfGeneration = Configurator.MemeDistributionType.FollowingFitting;
//            Configurator.displayPlotWhileSimulation = false;
//            Configurator.withGraphicalDisplay = false;
//            Configurator.systemPaused = false;
//            Configurator.jarMode = true;
        }else if(launcher == Configurator.EnumLauncher.ihm){
            Configurator.methodOfGeneration = Configurator.MemeDistributionType.SingleBasic;
            Configurator.displayPlotWhileSimulation = true;
            Configurator.withGraphicalDisplay = true;
            Configurator.jarMode = false;
            Configurator.systemPaused = true;
            Configurator.writeNetworkResultOnFitting = true;
            Configurator.explorator = Configurator.EnumExplorationMethod.exhaustive;
        }else if(launcher == Configurator.EnumLauncher.test){
            Configurator.methodOfGeneration = Configurator.MemeDistributionType.SingleBasic;
            Configurator.displayPlotWhileSimulation = false;
            Configurator.withGraphicalDisplay = false;
            Configurator.jarMode = false;
            Configurator.systemPaused = false;
            Configurator.writeNetworkResultOnFitting = false;
            Configurator.explorator = Configurator.EnumExplorationMethod.exhaustive;
        }

        WriteNRead writeNRead = new WriteNRead();
        AttributFactory attributFactory = new AttributFactory();
        AgregatorFactory agregatorFactory = new AgregatorFactory();
        ActionFactory actionFactory = new ActionFactory() ;
        MemeFactory memeFactory = new MemeFactory(actionFactory, agregatorFactory, attributFactory);

        WorkerFactory workerFactory = null ;
        WorkerFactoryJarVersion workerFactoryJar = null;

        if(launcher == Configurator.EnumLauncher.jar|| launcher == Configurator.EnumLauncher.test){
            workerFactoryJar = new WorkerFactoryJarVersion();
        }else if(launcher == Configurator.EnumLauncher.ihm ){
            workerFactory = new WorkerFactory();
        }

        NetworkConstructor networkConstructor = new NetworkConstructor();
        EntiteHandler entiteHandler  = null;

        if(launcher == Configurator.EnumLauncher.jar|| launcher == Configurator.EnumLauncher.test){
            entiteHandler = new EntiteHandler(networkConstructor, memeFactory, workerFactoryJar);
        }else if(launcher == Configurator.EnumLauncher.ihm ){
            entiteHandler = new EntiteHandler(networkConstructor, memeFactory, workerFactory);
        }

        NetworkFileLoader networkFileLoader = new NetworkFileLoader(memeFactory, writeNRead);
        DrawerGraphStream drawerGraphStream = null;
        StatAndPlotJarVersion stat = null;

        if(launcher == Configurator.EnumLauncher.jar|| launcher == Configurator.EnumLauncher.test){
             stat = new StatAndPlotJarVersion(entiteHandler, memeFactory, networkConstructor,
                     writeNRead, networkFileLoader, workerFactoryJar);
        }else if(launcher == Configurator.EnumLauncher.ihm){
            drawerGraphStream =  new DrawerGraphStream(entiteHandler, memeFactory,
                    networkConstructor, writeNRead, networkFileLoader, workerFactory);
        }

        // Communication model
        CommunicationModel communicationModel = null;

        if(launcher == Configurator.EnumLauncher.jar|| launcher == Configurator.EnumLauncher.test) {
            communicationModel = new CommunicationModel(entiteHandler, networkConstructor, networkFileLoader, workerFactoryJar, stat);
            stat.setCommunicationModel(communicationModel);
        }else if (launcher == Configurator.EnumLauncher.ihm) {
            communicationModel = new CommunicationModel(entiteHandler, networkConstructor, networkFileLoader, workerFactory, drawerGraphStream);
            drawerGraphStream.setCommunicationModel(communicationModel);
        }

        networkFileLoader.setCommunicationModel(communicationModel);
        actionFactory.setEntiteHandler(entiteHandler);
        agregatorFactory.setEntiteHandler(entiteHandler);

        if(launcher == Configurator.EnumLauncher.jar|| launcher == Configurator.EnumLauncher.test)  {
            workerFactoryJar.setNecessary(stat, new DrawerStub());
            networkConstructor.setDrawer(new DrawerStub());
        }else if (launcher == Configurator.EnumLauncher.ihm) {
            workerFactory.setNecessary(drawerGraphStream, drawerGraphStream);
            networkConstructor.setDrawer(drawerGraphStream);
        }

        // Controller
        Controller c = new Controller();
        Controller.VueController vControl = c.new VueController();
        Controller.ModelController mControl = c.new ModelController(vControl, communicationModel);

        if(launcher == Configurator.EnumLauncher.jar)  {
            // La fenetre en elle meme Controller de Model donné a l'IHM
            IHMStub fenetre = new IHMStub();

            vControl.setView((Interfaces.IView)fenetre);

            entiteHandler.initialisation();

            entiteHandler.addMemeListener(workerFactory.getDrawer());
            entiteHandler.addEntityListener(workerFactory.getCalculator());

            Interfaces.IReadNetwork nl = mControl.getReader();
            try {
                writeNRead.readAndCreateNetwork(fileInput, nl," ","#");
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            stat.probaVoulu = probaBehavior;
            entiteHandler.suspend();
            networkConstructor.suspend();
            networkConstructor.start();
            entiteHandler.start();

            return stat.fitNetwork(0);
        }else if (launcher == Configurator.EnumLauncher.ihm) {
            entiteHandler.initialisation();

            // La fenetre en elle meme Controller de Model donné a l'IHM
            IHM fenetre = new IHM(mControl,
                    networkConstructor,
                    memeFactory,
                    workerFactory,
                    entiteHandler,
                    actionFactory,
                    drawerGraphStream,
                    writeNRead);

            vControl.setView((Interfaces.IView) fenetre);

            // Le graph associé lors de l'affichage avec graphstream
            if (Configurator.withGraphicalDisplay) {
                Graph graph = new SingleGraph("Embedded");
                drawerGraphStream.setGraph(graph);
                Viewer lol = graph.display();
            }

            Interfaces.IReadNetwork nl = mControl.getReader();
            try {
                writeNRead.readAndCreateNetwork("" + Configurator.defaultPathForReadingNetwork, nl, " ", "#");
            } catch (IOException e1) {
                e1.printStackTrace();
            }


            fenetre.setVisible(true);
            entiteHandler.setIHMController(vControl);

            // fait le lien entre les entités d'action et de transmission de meme
            // avec l'IHM, pour permettre la mise a jour des affichages etc
            entiteHandler.addEntityListener(fenetre);
            entiteHandler.addMemeListener(fenetre);

            // De meme, le dessinateur graphique s'abonne aux évènements de type transmission de meme
            // Dans le but de faire changer la couleur du noeud en fonction des memes possédés par ce dernier
            entiteHandler.addMemeListener(workerFactory.getDrawer());
            entiteHandler.addEntityListener(workerFactory.getCalculator());

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

            return 0.;
        }
        else if(launcher == Configurator.EnumLauncher.test){
            entiteHandler.initialisation();

            Hashtable<Meme, IModelParameter.GenericBooleanParameter> memeDispo = new Hashtable<Meme,IModelParameter.GenericBooleanParameter>();
            ArrayList<Meme> memeOnMap = new ArrayList<Meme>();
            Hashtable<Integer, IModelParameter<?>>  providers = new Hashtable<Integer, IModelParameter<?>>();

            for (Meme meme : memeFactory.getMemeAvailable(true)) {
                memeDispo.put(meme, new IModelParameter.GenericBooleanParameter());
            }

            IModelParameter.MemeAvailability memeProvider = new IModelParameter.MemeAvailability(memeDispo);
            memeProvider.setEntiteHandler(entiteHandler);
            providers.put(1,memeProvider);

            IModelParameter.MemeDiffusionProba memeDiffu = new IModelParameter.MemeDiffusionProba(memeFactory.getMemeAvailable(true), new IModelParameter.GenericDoubleParameter(.0,.0,.2,.1));
            memeDiffu.setEntiteHandler(entiteHandler);
            providers.put(0,memeDiffu);

            memeProvider.addMemeListListener(memeDiffu);

            willBeRun = new TestProvider.Companion(IExplorationMethod.ExplorationMethod.getSpecificExplorator(Configurator.EnumExplorationMethod.exhaustive, providers)).giveMyself();
            willBeRun.run();

            return 0.;
        }

        return 0.;
    }
}

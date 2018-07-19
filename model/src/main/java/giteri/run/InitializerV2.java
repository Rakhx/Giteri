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

import static giteri.run.configurator.Configurator.debugOpenMole;

/** Classe d'initialisation des objets nécessaires à l'utilisation du framework
 * Commun à ttes les classes.
 * idée :
 *                                                          -> IHM ( Set des meme utilisés, des probas =)
 * Lancement de l'appli => initializer ( Meme existant )    -> JAR ( lecture des memes et probas )
 *
 */
public class InitializerV2 {
    public static Double initialize(Configurator.EnumLauncher launcher, File fileInput, ArrayList<Double> probaBehavior) {

        // A instancier dans les if. a lancer dans tous les cas a la fin?
        Runnable willBeRun;
        boolean ihmLauncher = launcher == Configurator.EnumLauncher.ihm ;

        if(launcher == Configurator.EnumLauncher.jarC || launcher == Configurator.EnumLauncher.jarOpenMole){
//            Configurator.methodOfGeneration = Configurator.MemeDistributionType.FollowingFitting;
        }
        else if(launcher == Configurator.EnumLauncher.ihm){
            Configurator.methodOfGeneration = Configurator.MemeDistributionType.SingleBasic;
            Configurator.displayPlotWhileSimulation = true;
            Configurator.withGraphicalDisplay = true;
            Configurator.jarMode = false;
            Configurator.systemPaused = true;
            Configurator.writeNetworkResultOnFitting = true;
            Configurator.explorator = Configurator.EnumExplorationMethod.exhaustive;
//            Configurator.explorator = Configurator.EnumExplorationMethod.oneShot;
        }
        else if(launcher == Configurator.EnumLauncher.testProvider){
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
        WorkerFactory workerFactory = new WorkerFactory();
        NetworkConstructor networkConstructor = new NetworkConstructor();
        EntiteHandler entiteHandler = new EntiteHandler(networkConstructor, memeFactory, workerFactory);
        NetworkFileLoader networkFileLoader = new NetworkFileLoader(memeFactory, writeNRead);
        DrawerGraphStream drawerGraphStream = null;
        StatAndPlotJarVersion stat = null;

        if(!ihmLauncher) {
             stat = new StatAndPlotJarVersion(entiteHandler, memeFactory, networkConstructor, writeNRead, networkFileLoader, workerFactory);
        }else{
            drawerGraphStream =  new DrawerGraphStream(entiteHandler, memeFactory, networkConstructor, writeNRead, networkFileLoader, workerFactory);
        }

        // Communication model
        CommunicationModel communicationModel = null;

        if(!ihmLauncher) {
            communicationModel = new CommunicationModel(entiteHandler, networkConstructor, networkFileLoader, workerFactory, stat);
            stat.setCommunicationModel(communicationModel);
        }else {
            communicationModel = new CommunicationModel(entiteHandler, networkConstructor, networkFileLoader, workerFactory, drawerGraphStream);
            drawerGraphStream.setCommunicationModel(communicationModel);
        }

        networkFileLoader.setCommunicationModel(communicationModel);
        actionFactory.setEntiteHandler(entiteHandler);
        agregatorFactory.setEntiteHandler(entiteHandler);

        if(!ihmLauncher)  {
            workerFactory.setNecessary(stat, new DrawerStub());
            networkConstructor.setDrawer(new DrawerStub());
        }else {
            workerFactory.setNecessary(drawerGraphStream, drawerGraphStream);
            networkConstructor.setDrawer(drawerGraphStream);
        }

        // Controller
        Controller c = new Controller();
        Controller.VueController vControl = c.new VueController();
        Controller.ModelController mControl = c.new ModelController(vControl, communicationModel);

        if(launcher == Configurator.EnumLauncher.jarC || launcher == Configurator.EnumLauncher.jarOpenMole)  {
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
            if(debugOpenMole) System.out.println("Proba donnée au launcher:" + probaBehavior);
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
        else if(launcher == Configurator.EnumLauncher.testProvider){
            entiteHandler.initialisation();

            Hashtable<Meme, IModelParameter.GenericBooleanParameter> memeDispo = new Hashtable<>();
            Hashtable<Integer, IModelParameter<?>> providers = new Hashtable<>();

            for (Meme meme : memeFactory.getMemes(Configurator.MemeList.FITTING, Configurator.ActionType.ANYTHING)) {
                memeDispo.put(meme, new IModelParameter.GenericBooleanParameter());
            }

            IModelParameter.MemeAvailability memeProvider = new IModelParameter.MemeAvailability(memeDispo);
            memeProvider.setEntiteHandler(entiteHandler);
            providers.put(1,memeProvider);

            IModelParameter.MemeDiffusionProba memeDiffu = new IModelParameter.MemeDiffusionProba(memeFactory.getMemes(Configurator.MemeList.FITTING,Configurator.ActionType.ANYTHING),
                    new IModelParameter.GenericDoubleParameter(.2,.2,.6,.2));
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

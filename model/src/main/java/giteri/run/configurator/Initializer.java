package giteri.run.configurator;

import giteri.fitting.algo.IExplorationMethod;
import giteri.fitting.parameters.IModelParameter;
import giteri.meme.entite.EntiteHandler;
import giteri.meme.entite.Meme;
import giteri.meme.mecanisme.ActionFactory;
import giteri.meme.mecanisme.FilterFactory;
import giteri.meme.mecanisme.AttributFactory;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.networkStuff.*;
import giteri.run.controller.Controller;
import giteri.run.displaysStuff.ConsoleView;
import giteri.run.displaysStuff.FileView;
import giteri.run.displaysStuff.IHM;
import giteri.run.displaysStuff.IHMStub;
import giteri.run.interfaces.Interfaces;
import giteri.run.jarVersion.StatAndPlotJarVersion;
import giteri.test.TestProvider;
import giteri.tool.other.WriteNRead;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    public static Double initialize(Configurator.EnumLauncher launcher, File fileInput, List<Boolean> memeActication , List<Double> memeProba) {

        // A instancier dans les if. à lancer dans tous les cas a la fin?
        Runnable willBeRun;
        boolean ihmLauncher = (launcher == Configurator.EnumLauncher.ihm) ;
        if(launcher == Configurator.EnumLauncher.jarC || launcher == Configurator.EnumLauncher.jarOpenMole){
            // La configuration de base correspond a OpenMole, car histoire de multi acces a des variables
	        // depuis la meme JVM donc ne pas modifier du static. Les launchers pour autres usages changent cette configuration initiale
            Configurator.methodOfGeneration = Configurator.MemeDistributionType.Nothing;
            Configurator.withGraphicalDisplay = false;
            Configurator.jarMode = true;
            Configurator.systemPaused = false;
            Configurator.writeNetworkResultOnFitting = !fullSilent;
            Configurator.nbRepetitionbyRun = Configurator.nbRepetitionForJar;
            Configurator.isFitting = true;
        }

        else if(launcher == Configurator.EnumLauncher.ihm){
            Configurator.methodOfGeneration = Configurator.MemeDistributionType.SingleBasic;
            Configurator.displayPlotWhileSimulation = true;
            // Configurator.withGraphicalDisplay = true; // On le laisse a la valeur donné dans le configurator
            Configurator.jarMode = false;
            Configurator.systemPaused = true;
            Configurator.writeNetworkResultOnFitting = !fullSilent;
            Configurator.explorator = Configurator.EnumExplorationMethod.exhaustive;
            Configurator.isFitting = false;
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
        FilterFactory filterFactory = new FilterFactory();
        ActionFactory actionFactory = new ActionFactory() ;
        MemeFactory memeFactory = new MemeFactory(actionFactory, filterFactory, attributFactory);
        WorkerFactory workerFactory = new WorkerFactory();
        NetworkConstructor networkConstructor = new NetworkConstructor();
        EntiteHandler entiteHandler = new EntiteHandler(networkConstructor, memeFactory, workerFactory);
        NetworkFileLoader networkFileLoader = new NetworkFileLoader(memeFactory, writeNRead);
        DrawerGraphStream drawerGraphStream = null;
        StatAndPlotGeneric stat = null;

        if(ihmLauncher)
        {
            if(Configurator.withGraphicalDisplay)
                drawerGraphStream =  new DrawerGraphStream(entiteHandler, memeFactory, networkConstructor, writeNRead, networkFileLoader, workerFactory);
            else
                stat = new StatAndPlotWithoutIHM(entiteHandler, memeFactory, networkConstructor, writeNRead, networkFileLoader, workerFactory);
        }
        else
            stat = new StatAndPlotJarVersion(entiteHandler, memeFactory, networkConstructor, writeNRead, networkFileLoader, workerFactory);

        // Communication model
        CommunicationModel communicationModel ;

        if(ihmLauncher && Configurator.withGraphicalDisplay){
            communicationModel = new CommunicationModel(entiteHandler, networkConstructor, networkFileLoader, workerFactory, drawerGraphStream);
            drawerGraphStream.setCommunicationModel(communicationModel);
        }else {
            communicationModel = new CommunicationModel(entiteHandler, networkConstructor, networkFileLoader, workerFactory, stat);
            stat.setCommunicationModel(communicationModel);
        }

        networkFileLoader.setCommunicationModel(communicationModel);
        actionFactory.setEntiteHandler(entiteHandler);

        if(ihmLauncher && withGraphicalDisplay ){
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
        // Crée une fenetre stub
        if(launcher == Configurator.EnumLauncher.jarC || launcher == Configurator.EnumLauncher.jarOpenMole) {
            // La fenetre en elle meme Controller de Model donné a l'IHM
            IHM fenetre = new IHM(mControl,
                    memeFactory,
                    entiteHandler,
                    new DrawerStub(),
                    writeNRead);

            vControl.addView(fenetre);
            if((Configurator.activationCodeForView & 4) == 4)
                vControl.addView(new FileView(false));
            if((Configurator.activationCodeForView & 2) == 2)
                vControl.addView(new ConsoleView());
            entiteHandler.setVueController(vControl);
            entiteHandler.initialisation();
            entiteHandler.addMemeListener(workerFactory.getDrawer());
            entiteHandler.addEntityListener(workerFactory.getCalculator());

            Interfaces.IReadNetwork nl = mControl.getReader();
            // Pour pouvoir lancer direct le fitting.
            try {
                writeNRead.readAndCreateNetwork(fileInput, nl," ","#");
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            entiteHandler.giveMemeToEntite(Configurator.methodOfGeneration);

            entiteHandler.suspend();
            networkConstructor.suspend();
            networkConstructor.start();
            entiteHandler.start();

            return stat.fitNetwork(Configurator.EnumLauncher.jarC,
                    Configurator.EnumExplorationMethod.oneShot,
                    Optional.of(memeActication),
                    Optional.of(memeProba));

        }else if (launcher == Configurator.EnumLauncher.ihm) {
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
            // Permet d'éviter d'avoir besoin de cliquer sur analyse avant de fitté
            try {
                writeNRead.readAndCreateNetwork("" + Configurator.defaultPathForReadingNetwork, nl, " ", "#");
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            fenetre.setVisible(true);
            entiteHandler.setVueController(vControl);

            // fait le lien entre les entités d'action et de transmission de meme
            // avec l'IHM, pour permettre la mise a jour des affichages etc
            entiteHandler.addEntityListener(fenetre);
            entiteHandler.addMemeListener(fenetre);

            // De meme, le dessinateur graphique s'abonne aux évènements de type transmission de meme
            // Dans le but de faire changer la couleur du noeud en fonction des memes possédés par ce dernier
            entiteHandler.addMemeListener(workerFactory.getDrawer());
            entiteHandler.addEntityListener(workerFactory.getCalculator());
            entiteHandler.giveMemeToEntite(Configurator.methodOfGeneration);

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
            entiteHandler.giveMemeToEntite(Configurator.methodOfGeneration);

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

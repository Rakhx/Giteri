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

            //huhum
            if(Configurator.coupleVersion)
                entiteHandler.giveCoupleMemeToEntite(memeFactory.getCoupleMemes());
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
            entiteHandler.addEntityListener(workerFactory.getCalculator());

            Interfaces.IReadNetwork nl = mControl.getReader();

            entiteHandler.suspend();
            networkConstructor.suspend();
            networkConstructor.start();
            entiteHandler.start();

            if(Configurator.coupleVersion){
                return launchForCaste(parameters);
            }else {
                return launchForClassic(stat, parameters);
            }
       }
    }

    /** entier d'activation pour choisir les memes présents sur la simulation. Sur un total de
     *
     * @param addActivator
     * @param rmvActivator
     */
    public static void setBooleanActivation(int addActivator, int rmvActivator){
        int nbAdd = 3, nbRmv = 3, tailleTotal= 10;
        addActivation = getActionActivation(addActivator, nbAdd, tailleTotal);
        rmvActivation = getActionActivation(rmvActivator, nbRmv, tailleTotal);

    }

    // méthode sale mais osef
    public static boolean[] addActivation = new boolean[10];
    public static boolean[] rmvActivation = new boolean[10];

    /** méthode pour lancement spécifique au cas classique.
     *
     * @param stat
     * @param parameters
     * @return
     */
    public static Double launchForClassic(StatAndPlotGeneric stat, Interfaces.IOpenMoleParameter parameters){
        ClassicOpenMoleParameter comp = (ClassicOpenMoleParameter)parameters;
        return stat.fitNetwork(Configurator.EnumLauncher.jarC,
                Configurator.EnumExplorationMethod.oneShot,
                Optional.of(comp.memeActication),
                Optional.of(comp.memeProba));

    }

    public static Double launchForCaste(Interfaces.IOpenMoleParameter parameters){
        CasteOpenMoleParameter comp = (CasteOpenMoleParameter)parameters;

        return .0;
    }




    /** Le numero d'activator. Le Max défini le nombre de combinaison max, et le Nb le nombre
     * d'élément qui constitue la combinaison considérée.
     * Si choix de 3 meme sur 10 max possible, le nombre de combinaison est de 10!/7!
     *
     *
     * @param activator Xeme  combinaison" effectivement choisi. i.e. la 56eme
     * @param nbActivator 3
     * @param maxactivator 10
     * @return
     */
    public static boolean[] getActionActivation(int activator, int nbActivator, int maxactivator){
        System.out.println("CALL: " + activator);
        boolean[] resultat = new boolean[maxactivator];
        boolean again = true;
        int nbCombinaison = Toolz.getLittleFactorial(maxactivator) / (Toolz.getLittleFactorial(maxactivator-nbActivator)
        *Toolz.getLittleFactorial(nbActivator));
        // position de base des activators, i<j<k
       // int i= 1, j=2,k = 3;
        for (int i = 0; i < maxactivator; i++) {
            for (int j = i+1; j < maxactivator; j++) {
                for (int k = j+1; k < maxactivator ; k++) {
                    if(activator > 0) {
                        activator--;
                    }
                    else if(activator == 0){
                        System.out.println("ijk- " + i + j + k);
                        for (int i1 = 0; i1 < maxactivator; i1++) {
                            if(( i == i1) || (j == i1) || (k==i1))
                                resultat[i1] = true;
                            else
                                resultat[i1] = false;
                        }

                        again = false;
                    }

                    if(!again)
                        break;

                }
                if(!again)
                    break;
            }

            if(!again)
                break;
        }

        String res = "[";
        for (int i = 0; i < resultat.length; i++) {
            res += ";";
            res += resultat[i] ? "1" : "0";
        }
        res += "]";

        System.out.println(res);
        return resultat;
    }

}

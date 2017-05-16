package giteri.run.jarVersion;

import giteri.meme.mecanisme.ActionFactory;
import giteri.meme.mecanisme.AgregatorFactory;
import giteri.meme.mecanisme.AttributFactory;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.networkStuff.CommunicationModel;
import giteri.network.networkStuff.DrawerStub;
import giteri.network.networkStuff.NetworkFileLoader;
import giteri.run.IHMStub;
import giteri.run.Initializer;
import giteri.run.configurator.Configurator;
import giteri.run.interfaces.Interfaces.IReadNetwork;
import giteri.run.interfaces.Interfaces.IView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import giteri.network.networkStuff.NetworkConstructor;
import giteri.tool.other.WriteNRead;
import giteri.run.controller.Controller;
import giteri.run.controller.Controller.ModelController;
import giteri.run.controller.Controller.VueController;
import giteri.meme.entite.EntiteHandler;

/** Version qui prend en paramètre des doubles représentants les probas, et un path d'un file
 * contenant le file contenant le réseau à atteindre.
 * Ordre .AddØ-Hop:0.2 .Add∞:0.1 .RmvØ-2hop:0.5 .Rmv+:0.4 .Rmv-:0.3
 *
 */
public class JarVersion {

    public static boolean fromMain = false;

    public static void main(String[] args) {

        fromMain = true;

		// Region Param
		ArrayList<Double> probaBehavior = new ArrayList<Double>();
		String filePath ;
		File inputFile;
		boolean debug = true;

		// STEP: Récupérer les probas
		if(args.length != 6){
			System.err.println("Pas le bon nombre de paramètres");
			return;
		}

		filePath = args[0];
        inputFile = new File(filePath);
        if(debug)System.out.print("Fichier d'input: " + (inputFile.exists()? "exist" : "does not exist"));

		for (int i = 1; i < args.length; i++) {
			probaBehavior.add(Double.parseDouble(args[i]));
		}

		if(debug) System.out.println("Proba Recup "+ probaBehavior);

		run(inputFile, probaBehavior.get(0),probaBehavior.get(1),probaBehavior.get(2),probaBehavior.get(3),probaBehavior.get(4));
    }

    /** Run lancé depuis openMole, ou depuis le main.
     *
     * @param fileInput
     * @param param1
     * @param param2
     * @param param3
     * @param param4
     * @param param5
     * @return
     */
    public static Double run(File fileInput, double param1, double param2, double param3, double param4, double param5) {

        ArrayList<Double> probaBehavior = new ArrayList<Double>();
        probaBehavior.addAll(Arrays.asList(param1,param2,param3,param4,param5));
        return Initializer.initialize(Configurator.EnumLauncher.jar, fileInput, probaBehavior);


//        if (!fromMain) {
//            Configurator.methodOfGeneration = Configurator.MemeDistributionType.FollowingFitting;
//            Configurator.displayPlotWhileSimulation = false;
//            Configurator.withGraphicalDisplay = false;
//            Configurator.systemPaused = false;
//            Configurator.jarMode = true;
//        }
//
//        boolean debug = false;
//        if(debug) System.out.println("Proba Recup "+ probaBehavior);
//
//
//        WriteNRead writeNRead = new WriteNRead();
//        AttributFactory attributFactory = new AttributFactory();
//        AgregatorFactory agregatorFactory = new AgregatorFactory();
//        ActionFactory actionFactory = new ActionFactory() ;
//        MemeFactory memeFactory = new MemeFactory(actionFactory, agregatorFactory, attributFactory);
//        WorkerFactoryJarVersion workerFactory = new WorkerFactoryJarVersion();
//        NetworkConstructor networkConstructor = new NetworkConstructor();
//        EntiteHandler entiteHandler = new EntiteHandler(networkConstructor, memeFactory, workerFactory);
//        NetworkFileLoader networkFileLoader = new NetworkFileLoader( memeFactory, writeNRead);
//        StatAndPlotJarVersion stat = new StatAndPlotJarVersion(entiteHandler, memeFactory, networkConstructor,
//                writeNRead, networkFileLoader, workerFactory);
//
//        // Communication model
//
//        CommunicationModel communicationModel = new CommunicationModel(entiteHandler, networkConstructor,
//                networkFileLoader, workerFactory,stat);
//        networkFileLoader.setCommunicationModel(communicationModel);
//        stat.setCommunicationModel(communicationModel);
//        actionFactory.setEntiteHandler(entiteHandler);
//        agregatorFactory.setEntiteHandler(entiteHandler);
//        workerFactory.setNecessary(stat, new DrawerStub());
//        networkConstructor.setDrawer(new DrawerStub());
//
//        // Controller
//        Controller c = new Controller();
//        VueController vControl = c.new VueController();
//        ModelController mControl = c.new ModelController(vControl, communicationModel);
//
//        // La fenetre en elle meme Controller de Model donné a l'IHM
//        IHMStub fenetre = new IHMStub();
//
//        vControl.setView((IView)fenetre);
//
//        entiteHandler.initialisation();
//
//        entiteHandler.addMemeListener(workerFactory.getDrawer());
//        entiteHandler.addEntityListener(workerFactory.getCalculator());
//
//        IReadNetwork nl = mControl.getReader();
//        try {
//            writeNRead.readAndCreateNetwork(fileInput, nl," ","#");
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
//
//        stat.probaVoulu = probaBehavior;
//        entiteHandler.suspend();
//        networkConstructor.suspend();
//        networkConstructor.start();
//        entiteHandler.start();
//
//        return stat.fitNetwork(0);
    }


}

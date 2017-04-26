package giteri.run.jarVersion;

import giteri.run.IHMStub;
import giteri.run.configurator.Configurator;
import giteri.run.interfaces.Interfaces.IReadNetwork;
import giteri.run.interfaces.Interfaces.IView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import giteri.network.networkStuff.NetworkConstructor;
import giteri.tool.other.WriteNRead;
import giteri.run.controller.Controller;
import giteri.run.controller.Controller.ModelController;
import giteri.run.controller.Controller.VueController;
import giteri.meme.entite.EntiteHandler;

public class JarVersion {

	
	public static double run(double one, double two, double three, double four, double five){
		// Region Param
		ArrayList<Double> probaBehavior = new ArrayList<Double>();
		boolean debug = Configurator.overallDebug;
		probaBehavior.addAll(Arrays.asList(one, two, three, four, five));
		
		
		// EndRegion
		Configurator.methodOfGeneration = Configurator.MemeDistributionType.FollowingFitting;
		Configurator.displayPlotWhileSimulation = false;
		Configurator.withGraphicalDisplay = false;
		Configurator.turboMode = true;
		Configurator.systemPaused = false;
		Configurator.jarMode = true;
		Configurator.setThreadSleepMultiplicateur(0);
		
		NetworkConstructor nc = NetworkConstructor.getInstance();
		EntiteHandler eh = EntiteHandler.getInstance();
		
		Controller c = new Controller();
		VueController vControl = c.new VueController();
		ModelController mControl = c.new ModelController(vControl);
		
		IHMStub fenetre = new IHMStub();
		vControl.setView((IView)fenetre);
		eh.setIHMController(vControl);
		
		eh.addMemeListener(WorkerFactoryJarVersion.getInstance().getDrawer());
		eh.addEntityListener(WorkerFactoryJarVersion.getInstance().getCalculator());

		IReadNetwork nl = mControl.getReader();
		try {
			WriteNRead.getInstance().readAndCreateNetwork("" + Configurator.rightHerePathForReadingNetwork, nl, " ", "#");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		StatAndPlotJarVersion stat = StatAndPlotJarVersion.getInstance();
		stat.probaVoulu = probaBehavior;
		
		eh.suspend();
		nc.suspend();
		nc.start();
		eh.start();
		
		stat.fitNetwork(0);
		
		return -1	;
	}
	
	
	public static void main(String[] args) {
		
		// Region Param
		ArrayList<Double> probaBehavior = new ArrayList<Double>();
		boolean debug = Configurator.overallDebug;
		
		// STEP: Récupérer les probas
		if(args.length < 2){
			System.err.println("Pas assez de paramètre");
			return;
		}
		
		for (int i = 0; i < args.length; i++) {
			probaBehavior.add(Double.parseDouble(args[i]));
		}
		
		if(debug) System.out.println("Proba Recup "+ probaBehavior);
		
		
		// EndRegion
		Configurator.methodOfGeneration = Configurator.MemeDistributionType.FollowingFitting;
		Configurator.displayPlotWhileSimulation = false;
		Configurator.withGraphicalDisplay = false;
		Configurator.turboMode = true;
		Configurator.systemPaused = false;
		Configurator.jarMode = true;
		Configurator.setThreadSleepMultiplicateur(0);
		
		NetworkConstructor nc = NetworkConstructor.getInstance();
		EntiteHandler eh = EntiteHandler.getInstance();
		
		Controller c = new Controller();
		VueController vControl = c.new VueController();
		ModelController mControl = c.new ModelController(vControl);
		
		IHMStub fenetre = new IHMStub();
		vControl.setView((IView)fenetre);
		eh.setIHMController(vControl);
		
		eh.addMemeListener(WorkerFactoryJarVersion.getInstance().getDrawer());
		eh.addEntityListener(WorkerFactoryJarVersion.getInstance().getCalculator());

		IReadNetwork nl = mControl.getReader();
		try {
			WriteNRead.getInstance().readAndCreateNetwork("" + Configurator.rightHerePathForReadingNetwork, nl, " ", "#");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		StatAndPlotJarVersion stat = StatAndPlotJarVersion.getInstance();
		stat.probaVoulu = probaBehavior;
		
		eh.suspend();
		nc.suspend();
		nc.start();
		eh.start();
		
		stat.fitNetwork(0);
	}

}

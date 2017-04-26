package controller;

import interfaces.Interfaces.IModel;
import interfaces.Interfaces.IReadNetwork;
import interfaces.Interfaces.IView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Optional;

import math.Toolz;
import network.NetworkProperties;
import networkStuff.CommunicationModel;

import org.jfree.chart.JFreeChart;

import configurator.Configurator;

/** Controller de la vue, dans le sens vue -> model, ainsi que dans 
 * l'autre sens. 
 * 
 */
public class Controller {

	/** Classe qui permet l'appel de fonction depuis
	 * l'extérieur concernant l' interface.
	 *
	 */
	public class VueController{
		IView view;
		
		public VueController(){
			
		}
		
		public void setView(IView viewParam){
			view = viewParam;
		}
		
		public void resetIHM(){
			view.resetIHM();
		}

		public void resetPlotDensity(){
			view.resetDensityOverProbaChart();
		}
		
		/** Ajoute une valeur dans la chart de density over proba.
		 * 
		 * @param x
		 * @param y
		 */
		public void addDensityOverProbaValue(double x, double y){
			view.addValueToDensityOverProbaSerie(x, y);
		}
		
		/** ajoute une série de valeur pour le time T
		 * 
		 * @param time
		 * @param values
		 */
		public void addValueToApplianceSerie(double time, Hashtable<Integer, Double>  values){
			view.addValueToApplianceSerie( time, values);
		}
		
		
		/** Affiche le nombre d'action par seconde
		 * 
		 * @param message
		 */
		public void setDisplayNbAction(String message){
			view.setDisplayNbAction(message);
		}
		
		/** Permet d'afficher un nouveau message ou de toggle le 
		 * déjà en place
		 * @param message
		 */
		public void displayMessageOnFitPanel(String message){
			view.toggleWkProgress(message);
		}
		
		public JFreeChart getDDChart(){
			return view.getDDChart();
		}
		public JFreeChart getDensityChart(){
			return view.getDensityChart();
		}
		public JFreeChart getDensityOverProbaChart(){
			return view.getDensityOverProbaChart();
		}
	}
	
	/** Classe qui permet l'appel de fonction depusi l'extérieur
	 * vers le modèle. 
	 *
	 */
	public class ModelController {
		IModel model;
		
		/** Constructeur
		 * 
		 */
		public ModelController(VueController vueC){
			model = CommunicationModel.getInstance();
			CommunicationModel.getInstance().setViewController(vueC);
		}

		// Region Demande d'informations
		
		public String getDDInfos(){
			return model.getDDInfos();
		}
		
		public double getDensity(){
			return model.getDensity();
		}
		
		public IReadNetwork getReader(){
			return model.getReader();
		}
		
		public NetworkProperties getCurrentNetProperties(int activator){
			return model.getCurrentNetProperties(activator);
		}
		
		public void takeScreenshot(Optional<ArrayList<String>> simulationPath){
			model.takeSnapshot(Toolz.getLastSeed(), simulationPath);
		}
		
		// EndRegion
		
		// Region calcul pour la mise à jour d'éléments de l'interface
	
		public void stabilityResearch(){
			model.stabilityResearch();
		}
		
		public void fittingNetworks(){
			model.fittingNetworks();
		}
		
		public void displayPolar(){
			model.displayPolar();
		}
		
		public void fittingNextStep(){
			model.fittingNextStep();
		}
		
		public void toggleStep(){
			//model.toggleStep();
		}
		
		public void rdmConfig(){
			model.rdmConfig();
		}
		
		public void toggleActionSemiAuto(){
			Configurator.semiStepProgression = !Configurator.semiStepProgression;
		}
		
		// EndRegion
		
		// Region generation de graphe.
		
		/**
		 * 
		 */
		public void generateEmptyGraph(){
			model.generateGraph(0);
		}
		
		/**
		 * 
		 */
		public void generateTwentyEightGraph(){
			model.generateGraph(2);
		}
		
		/**
		 * 
		 */
		public void generateFiveGraph(){
			model.generateGraph(1);
		}
		
		/**
		 * 
		 */
		public void generateScaleFreeGraph(){
			model.generateGraph(3);
		}
		
		/**
		 * 
		 */
		public void generateSmallWorldGraph(){
			model.generateGraph(4);
		}
		
		/**
		 * 
		 */
		public void generateFullGraph(){
			model.generateGraph(5);
		}
		
		/**
		 * 
		 */
		public void purgeLinks(){
			model.purgeLinks();
		}
		
		// EndRegion
		
		// Region thread and co
	
		public void suspend(){
			model.suspend();
		}
		
		public void resume(){
			model.resume();
		}
		
		public void oneStep(){
			model.oneStep();
		}
		
		// EndRegion
	}
}

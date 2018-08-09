package giteri.run.controller;

import giteri.run.configurator.Configurator;
import giteri.run.interfaces.Interfaces;
import giteri.run.interfaces.Interfaces.IModel;
import giteri.run.interfaces.Interfaces.IReadNetwork;
import giteri.run.interfaces.Interfaces.IView;

import java.util.*;

import giteri.tool.math.Toolz;
import giteri.network.network.NetworkProperties;
import giteri.network.networkStuff.CommunicationModel;

import org.jfree.chart.JFreeChart;

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
		Set<IView> vues;
		public VueController(){
			vues = new HashSet<>();
		}

		/** Ajout d'une vue a la liste.
		 *
		 * @param toAdd
		 */
		public void addView(Interfaces.IView toAdd){
			vues.add(toAdd);
		}


		//region iview part

		public void displayInfo(String type, String info) {
			for (Interfaces.IView vue:  vues) {
				vue.displayInfo(type,info);
			}
		}

		public void resetIHM(){
			for (Interfaces.IView vue:  vues) {
				vue.resetIHM();
			}
		}

		public void resetPlotDensity(){
			for (Interfaces.IView vue:  vues) {
				vue.resetDensityOverProbaChart();
			}
		}

		/** Ajoute une valeur dans la chart de density over proba.
		 *
		 * @param x
		 * @param y
		 */
		public void addDensityOverProbaValue(double x, double y)
		{
			for (Interfaces.IView vue:  vues) {
				vue.addValueToDensityOverProbaSerie(x,y);
			}
		}

		/** ajoute une série de valeur pour le time T
		 *
		 * @param time
		 * @param values
		 */
		public void addValueToApplianceSerie(double time, Hashtable<Integer, Double>  values){
			for (Interfaces.IView vue:  vues) {
				vue.addValueToApplianceSerie(time, values);
			}
		}

		/** Affiche le nombre d'action par seconde
		 *
		 * @param message
		 */
		public void setDisplayNbAction(String message){
			for (Interfaces.IView vue:  vues) {
				vue.setDisplayNbAction(message);
			}
		}

		/** Permet d'afficher un nouveau message ou de toggle le 
		 * déjà en place
		 * @param message
		 */
		public void displayMessageOnFitPanel(String message){
			for (Interfaces.IView vue:  vues) {
				vue.toggleWkProgress(message);
			}
		}

		public JFreeChart getDDChart(){
			for (Interfaces.IView vue:  vues) {
				if(vue.getDDChart() != null)
					return vue.getDDChart();
			}
			return null;
		}

		public JFreeChart getDensityChart(){
			for (Interfaces.IView vue:  vues) {
				if(vue.getDensityChart() != null)
					return vue.getDensityChart();
			}
			return null;
		}
		public JFreeChart getDensityOverProbaChart(){
			for (Interfaces.IView vue:  vues) {
				if(vue.getDensityOverProbaChart() != null)
					return vue.getDensityOverProbaChart();
			}
			return null;
		}

		// endregion
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
		public ModelController(VueController vueC, CommunicationModel com){
			model = com;
			com.setViewController(vueC);
		}

		//region Demande d'informations

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

		//endregion

		//region calcul pour la mise à jour d'éléments de l'interface

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

		public void fittingOnce(){
			model.fittingOnce();
		}

		public void rdmConfig(){
			model.rdmConfig();
		}

		public void toggleActionSemiAuto(){
			Configurator.semiStepProgression = !Configurator.semiStepProgression;
		}

		//endregion

		//region generation de graphe.

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

		//endregion

		//region thread and co

		public void suspend(){
			model.suspend();
		}

		public void resume(){
			model.resume();
		}

		public void oneStep(){
			model.oneStep();
		}

		//endregion
	}
}

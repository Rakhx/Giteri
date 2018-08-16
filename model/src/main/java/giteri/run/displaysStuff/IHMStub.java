package giteri.run.displaysStuff;

import giteri.run.interfaces.Interfaces.IView;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;

public class IHMStub implements IView {

	@Override
	public void displayInfo(String type, List<String> info) {

	}
	public void displayXLastAction(int nbAction,Map<String, Integer> nbActivByMeme, Map<String,Integer> nbLastActivByMeme, List<String> lastXMemeApplied){

	}

	@Override
	public void setDisplayNbAction(String message) {
		
	}

	@Override
	public void resetIHM() {
		
	}

	@Override
	public void resetDensityOverProbaChart() {
		
	}

	@Override
	public void toggleEnableInterface() {
		
	}

	@Override
	public void toggleWkProgress(String message) {
		
	}

	@Override
	public void addValueToDensityOverProbaSerie(double x, double y) {
		
	}

	@Override
	public void addValueToApplianceSerie(double time, Hashtable<Integer, Double> value) {
		
	}

	@Override
	public JFreeChart getDDChart() {
		return null;
	}

	@Override
	public JFreeChart getDensityChart() {
		return null;
	}

	@Override
	public JFreeChart getDensityOverProbaChart() {
		return null;
	}

}

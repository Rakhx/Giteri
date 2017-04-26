package run;

import interfaces.Interfaces.IView;

import java.util.Hashtable;

import org.jfree.chart.JFreeChart;

public class IHMStub implements IView {

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

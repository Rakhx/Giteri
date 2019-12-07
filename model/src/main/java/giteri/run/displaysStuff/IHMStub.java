package giteri.run.displaysStuff;

import giteri.meme.entite.CoupleMeme;
import giteri.meme.entite.Meme;
import giteri.run.configurator.Configurator;
import giteri.run.interfaces.Interfaces.IView;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;

public class IHMStub implements IView {

	@Override
	public void displayInfo(Configurator.ViewMessageType type, List<String> info) {

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
	public void addValueToApplianceSerie(double time, Map<Meme, Double> value) {
		
	}

	@Override
	public void setMemeAvailable(List<Meme> memes) {

	}

	@Override
	public void setCoupleMemeAvailable(List<CoupleMeme> cMemes) {

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

package giteri.run.displaysStuff;

import giteri.run.interfaces.Interfaces;
import org.jfree.chart.JFreeChart;

import java.util.*;

public class ConsoleView implements Interfaces.IView {


    // region IVIEW

    @Override
    public void displayInfo(String type, List<String> info){
        System.out.println("["+type+"] - " + info);
    }

    public void displayXLastAction(int nbAction, Map<String, Integer> nbActivByMeme, Map<String,Integer> nbLastActivByMeme, List<String> lastXMemeApplied){
        List<String> nbActiv = new ArrayList<>(Arrays.asList(""+nbAction));
        nbActivByMeme.entrySet().stream().forEach(k -> nbActiv.add("meme "+k.getKey()+" - "+ k.getValue().toString()));
        displayInfo("NbActivByMeme", nbActiv);

        nbActiv.clear();
        nbActiv.add(""+nbAction);
        nbLastActivByMeme.entrySet().stream().forEach(k -> nbActiv.add("meme "+k.getKey()+" - "+ k.getValue().toString()));
        displayInfo("LastMemeActif", nbActiv);
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
    public void addValueToApplianceSerie(double time, Map<Integer, Double> value) {

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

    //endregion
}

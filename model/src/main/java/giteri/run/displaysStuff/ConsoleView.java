package giteri.run.displaysStuff;

import giteri.meme.entite.Meme;
import giteri.run.configurator.Configurator;
import giteri.run.interfaces.Interfaces;
import org.jfree.chart.JFreeChart;

import java.util.*;

import giteri.run.configurator.Configurator.ViewMessageType;

public class ConsoleView implements Interfaces.IView {

    // region IVIEW

    @Override
    public void displayInfo(ViewMessageType type, List<String> info){
        System.out.println("["+type+"] - " + info);
    }

    public void displayXLastAction(int nbAction, Map<String, Integer> nbActivByMeme, Map<String,Integer> nbLastActivByMeme, List<String> lastXMemeApplied){
        List<String> nbActiv = new ArrayList<>(Arrays.asList(""+nbAction));
        nbActivByMeme.entrySet().stream().forEach(k -> nbActiv.add("meme "+k.getKey()+" - "+ k.getValue().toString()));
        displayInfo(ViewMessageType.NBACTIVBYMEME, nbActiv);

        nbActiv.clear();
        nbActiv.add(""+nbAction);
        nbLastActivByMeme.entrySet().stream().forEach(k -> nbActiv.add("meme "+k.getKey()+" - "+ k.getValue().toString()));
        displayInfo(ViewMessageType.LASTMEMEACTIF, nbActiv);
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
        System.out.println("Cht de liste de memes: " + memes.stream().map(e -> e.toFourCharString()).reduce("", String::concat));
    }

    @Override
    public void screenshotMemeUsed() {

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

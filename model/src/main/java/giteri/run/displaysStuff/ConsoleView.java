package giteri.run.displaysStuff;

import giteri.run.interfaces.Interfaces;
import org.jfree.chart.JFreeChart;

import java.util.Hashtable;

public class ConsoleView implements Interfaces.IView {


    // region IVIEW

    @Override
    public void displayInfo(String type, String info) {
        System.out.println("["+type+"] - " + info);
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

    //endregion
}

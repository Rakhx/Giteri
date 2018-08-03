package giteri.run.displaysStuff;

import giteri.run.interfaces.Interfaces;
import org.jfree.chart.JFreeChart;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/** Classe qui gère tout les affichages.
 *
 */
public class DisplaysMng implements Interfaces.IView {
    // Les vues a gérer.
    Set<Interfaces.IView> vues;

    /** Constructeur sans param.
     *
     */
    public DisplaysMng(){
        vues = new HashSet<>();
    }

    /** Ajout d'une vue a la liste.
     *
     * @param toAdd
     */
    public void addView(Interfaces.IView toAdd){
        vues.add(toAdd);
    }

    // region implémentation d'interface IView

    @Override
    public void setDisplayNbAction(String message) {
        for (Interfaces.IView vue:  vues) {
            vue.setDisplayNbAction(message);
        }
    }

    @Override
    public void resetIHM() {
        for (Interfaces.IView vue:  vues) {
            vue.resetIHM();
        }
    }

    @Override
    public void resetDensityOverProbaChart() {
        for (Interfaces.IView vue:  vues) {
            vue.resetDensityOverProbaChart();
        }
    }

    @Override
    public void toggleEnableInterface() {
        for (Interfaces.IView vue:  vues) {
            vue.toggleEnableInterface();
        }
    }

    @Override
    public void toggleWkProgress(String message) {
        for (Interfaces.IView vue:  vues) {
            vue.toggleWkProgress(message);
        }
    }

    @Override
    public void addValueToDensityOverProbaSerie(double x, double y) {
        for (Interfaces.IView vue:  vues) {
            vue.addValueToDensityOverProbaSerie(x,y);
        }
    }

    @Override
    public void addValueToApplianceSerie(double time, Hashtable<Integer, Double> value) {
        for (Interfaces.IView vue:  vues) {
            vue.addValueToApplianceSerie(time, value);
        }
    }

    @Override
    public JFreeChart getDDChart() {
        for (Interfaces.IView vue:  vues) {
            if(vue.getDDChart() != null)
                return vue.getDDChart();
        }
        return null;
    }

    @Override
    public JFreeChart getDensityChart() {
        for (Interfaces.IView vue:  vues) {
            if(vue.getDensityChart() != null)
                return vue.getDensityChart();
        }
        return null;
    }

    @Override
    public JFreeChart getDensityOverProbaChart() {
        for (Interfaces.IView vue:  vues) {
            if(vue.getDensityOverProbaChart() != null)
                return vue.getDensityOverProbaChart();
        }
        return null;
    }

    // endregion
}

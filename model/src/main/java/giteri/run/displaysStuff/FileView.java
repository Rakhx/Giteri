package giteri.run.displaysStuff;

import giteri.run.configurator.Configurator;
import giteri.run.interfaces.Interfaces;
import giteri.tool.other.WriteNRead;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.util.*;

/** Implémentation pour écrire dans les fichiers textes.
 * ? utilisation d'un buffer?
 *
 */
public class FileView implements Interfaces.IView {
    // utilisé pour les opération d'écriture
    WriteNRead writer;

    // Détermine si on fait la différence entre les fichiers
    boolean oneFilePerSubject = false;
    // Si on utiliser un seul fichier pour tout les logs
    String defaultName = "fullLog";

    // Liste de rep. de base default rep + date
    List<String> repForWritting;

    // Séparator pour fichier csv
    String separator = ";";

    // pour le test.
    int cpt = 0;

    // chemin pour l'écriture si non fitting. Peut eventullement set ce truc la sur fitting.
    File chemin;

    /** Constructeur sans param.
     * Va écrire par défault dans le rep par défault + date de création de la view.
     * TODO Comportement a modifier si on veut lors d'un fitting avoir les infos dans le bon rep.
     * méthode probable: un set() depuis la classe de fitting sur la view ? Sale.
     */
    public FileView(boolean onlyOneFile){
        oneFilePerSubject = !onlyOneFile;
        repForWritting = new ArrayList<>(
                Arrays.asList(Configurator.repByDefault,""+Configurator.getDateFormat().format(new Date())));
        writer = new WriteNRead();
        chemin = writer.createAndGetDirFromString(repForWritting);
    }

    /** Ajoute une ligne à un fichier, donné par subject.
     *
     * @param subject
     * @param oneLine
     */
    public void addLine(String subject, List<String> oneLine) {
        // si on veut pas de multiple fichiers écritures
        if (!oneFilePerSubject)
            subject = defaultName;
        String toWrite = "";
        for (String string : oneLine)
            toWrite += string + separator;

        writer.writeSmallFile(chemin, subject, Arrays.asList(toWrite));
    }

    // region implémentation d'interface

    @Override
    public void setDisplayNbAction(String message) {
        addLine("nbline", new ArrayList<>(Arrays.asList(message, "" + cpt++)));
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

    // endregion
}

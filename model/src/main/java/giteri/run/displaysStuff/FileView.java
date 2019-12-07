package giteri.run.displaysStuff;

import giteri.meme.entite.CoupleMeme;
import giteri.meme.entite.Meme;
import giteri.run.configurator.Configurator;
import giteri.run.interfaces.Interfaces;
import giteri.tool.other.WriteNRead;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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
    public void displayInfo(Configurator.ViewMessageType type, List<String> info) {
        if (!oneFilePerSubject)
            info.add(0, type.toString());
        addLine(type.toString(), info);
    }

    public void displayXLastAction(int nbAction, Map<String, Integer> nbActivByMeme, Map<String,Integer> nbLastActivByMeme, List<String> lastXMemeApplied){
       List<String> nbActiv = new ArrayList<>(Arrays.asList(""+nbAction));
       List<String> nbActiv2;
       nbActivByMeme.entrySet().stream().forEach(k -> nbActiv.add("meme "+k.getKey()+" - "+ k.getValue().toString()));
       addLine("MemeActif", nbActiv);
       nbActiv.clear();
       nbActiv.add(""+nbAction);
       nbLastActivByMeme.entrySet().stream().forEach(k -> nbActiv.add("meme "+k.getKey()+" - "+ k.getValue().toString()));
       addLine("LastMemeActif", nbActiv);
       nbActiv2 = lastXMemeApplied.stream().collect(
                Collectors.groupingBy(String::toString, Collectors.counting())).
                entrySet().stream().map(Object::toString).collect(Collectors.toList());
       nbActiv2.add(0,""+ nbAction);
       addLine("LastMXMemeApplied",nbActiv2 );
    }

    @Override
    public void setDisplayNbAction(String message) {
        if(Configurator.writeNbActionPerSec)
            addLine("nbline", new ArrayList<>(Arrays.asList(message, "&" + cpt++)));
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
        addLine("Configuration", Collections.singletonList(memes.stream().map(e -> e.toFourCharString()).reduce("", String::concat)));
    }

    @Override
    public void setCoupleMemeAvailable(List<CoupleMeme> cMemes) {
        addLine("Configuration", Collections.singletonList(cMemes.stream().map(e -> e.getName()).reduce("", String::concat)));

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

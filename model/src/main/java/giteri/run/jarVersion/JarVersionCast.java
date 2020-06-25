package giteri.run.jarVersion;

import giteri.run.configurator.CasteOpenMoleParameter;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Initializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Jar version pour l'utilisation cast
 *
 */
public class JarVersionCast {

    public static Configurator.EnumLauncher launcher = Configurator.EnumLauncher.jarOpenMole;

    /** appelle depuis les lignes de commande java, non openmole
     * transforme les données des lignes de commande pour appeler fonction run(), fonction appelée par
     * openmole directement.
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        launcher = Configurator.EnumLauncher.jarC;
        // entre 1 et 8
        int nbMeme = 4;
        // selection quel position
        Double selector =  1648.1167465125636;
        // total des memes dispo
        int maxMemes =  121;

        // proba de propa
        List<Double> proba = new ArrayList<>(Arrays.asList(0.3527163423273064,0.18011838476725225,0.2314149150643855,0.0,0.08945316611411025,0.340427695791489,0.33728830652508085,0.9145513055291642));
        run(selector,nbMeme,maxMemes,proba.get(0), proba.get(1), proba.get(2), proba.get(3));
    }

    /** fonction appelée depuis openmole ou depuis le main de jarversioncast
     *
     *
     * @param activationCode entre 0 et 10000
     * @param nbMeme entre 1 et 4
     * @param maxCombinaison 11*11
     * @param proba
     * @return
     */
    public static Double run(Double activationCode, int nbMeme, int maxCombinaison, Double... proba){

        List<Double> probas = new ArrayList<>();
        probas.addAll(Arrays.asList(proba));
        int activatio = (int)Math.floor(activationCode);
        CasteOpenMoleParameter comp = new CasteOpenMoleParameter(activatio,nbMeme,maxCombinaison,probas);
        return Initializer.initialize(launcher, comp);
    }

}

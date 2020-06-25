package giteri.run.jarVersion;

import giteri.run.configurator.CasteOpenMoleParameter;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Initializer;
import scala.Array;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Jar version pour l'utilisation cast
 *
 */
public class JarVersionCast {

    public static Configurator.EnumLauncher launcher;

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
        Double selector = 8066026.655799285;
        // total des memes dispo
        int maxMemes = 121;

        // proba de propa
        List<Double> proba = new ArrayList<>(Arrays.asList(.1,.2,.3,.4,.5,.6,.7,.8));
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
        // Concernant
        for (int i = 0; i < proba.length; i++) {
            probas.add(proba[i]);
        }


        int activatio = (int)Math.floor(activationCode);
        CasteOpenMoleParameter comp = new CasteOpenMoleParameter(activatio,nbMeme,maxCombinaison,probas);
        return Initializer.initialize(launcher, comp);
    }


    /**
     *
     * @param jpp
     * @return
     */
    public static List<Double> convert(List<scala.Double> jpp){
        List<Double> res = new ArrayList<>();
        for (scala.Double aDouble : jpp) {
            res.add(scala.Predef.double2Double(aDouble.toDouble()));
        }

        return res;
    }




}

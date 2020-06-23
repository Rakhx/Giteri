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
        int selector = 458;
        // total des memes dispo
        int maxMemes = 100;

        // proba de propa
        List<Double> proba = new ArrayList<>(Arrays.asList(.1,.2,.3,.4,.5,.6,.7,.8));
        run(selector,nbMeme,maxMemes,proba);
    }

    /** fonction appelée depuis openmole ou depuis le main de jarversioncast
     *
     *
     * @param activationCode
     * @param nbMeme
     * @param maxCombinaison
     * @param proba
     * @return
     */
    public static Double run(int activationCode, int nbMeme, int maxCombinaison, List<Double> proba){
        CasteOpenMoleParameter comp = new CasteOpenMoleParameter(activationCode,nbMeme,maxCombinaison,proba);
        Initializer.initialize(launcher, comp);
        return 0.;
    }

    // Pour tester depuis openMole.
    public static int runlol(int... args){
        return args.length;
    }

    // Pour tester depuis openMole.
    public static int runDouble(scala.Double... args){
        return args.length;
    }
    // Pour tester depuis openMole.
    public static int runDouble1(scala.Double args){
        return 1;
    }

    public static int runDouble2(scala.Array<scala.Double> args){  return 1;
    }
    public static int runDoubleThree(Array<Double> args){  return 1;
    }
    public static int runDoubleFour(Array<scala.Double> args){  return 1;
    }


}

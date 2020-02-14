package giteri.run.jarVersion;

import giteri.run.configurator.CasteOpenMoleParameter;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Initializer;

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
     * Paramètres: filePath, Code d'activation ajout, nb meme Ajout, total Ajout, retrait, nb meme retrait, total retrait,
     * memeAjout x memeRetrait proba
     *
     * @param args
     */
    public static void main(String[] args) {
        boolean debug = true;

        String filePath;
        File inputFile;
        launcher = Configurator.EnumLauncher.jarC;

        filePath = args[0];
        inputFile = new File(filePath);
        if(debug)System.out.print("Fichier d'input: " + (inputFile.exists()? "exist" : "does not exist"));
        int decalage = 7;

        Double[] proba = new Double[args.length -decalage];
        for (int i = decalage; i < args.length; i++) {
            proba[i-decalage] = Double.parseDouble(args[i]);
        }


        run(Integer.parseInt(args[1]),Integer.parseInt(args[2]),Integer.parseInt(args[3]),
                Integer.parseInt(args[4]),Integer.parseInt(args[5]),Integer.parseInt(args[6]) , proba);

    }

    /** Fonction appelée depuis openMole. Nombre de parametre en double étant addActi x rmvActi
     *
     */
    public static Double run(int addActi, int addNb, int addTotal, int rmvActi, int rmvNb, int rmvTotal,
                               Double... args){

        CasteOpenMoleParameter comp = new CasteOpenMoleParameter(addActi,addNb,addTotal,rmvActi,rmvNb,rmvTotal);
        comp.probaPropa = new ArrayList<>(Arrays.asList(args));
        assert(comp.probaPropa.size() == addNb * rmvNb);

        Initializer.initialize(launcher, comp);
        return 0.;
    }



    // Pour tester depuis openMole.
    public static int runlol(int... args){
        return args.length;
    }
}

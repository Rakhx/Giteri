package giteri.run.jarVersion;

import giteri.run.configurator.Configurator;

import java.io.File;
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
        run(inputFile,0,0,0,0,0,0, Double.parseDouble(args[8])
                , Double.parseDouble(args[7]) , Double.parseDouble(args[7]));


    }

    /** Fonction appelée depuis openMole. Nombre de parametre en double étant addActi x rmvActi
     *
     */
    public static Double run(File fileInput, int addActi, int addNb, int addTotal, int rmvActi, int rmvNb, int rmvTotal,
                               Double... args){


        return 0.;
    }



    // Pour tester depuis openMole.
    public static int runlol(int... args){
        return args.length;
    }
}

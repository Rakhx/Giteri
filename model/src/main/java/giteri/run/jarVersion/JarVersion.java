package giteri.run.jarVersion;

import giteri.run.Initializer;
import giteri.run.configurator.Configurator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/** Version qui prend en paramètre des doubles représentants les probas, et un path d'un file
 * contenant le file contenant le réseau à atteindre.
 * Ordre .AddØ-Hop:0.2 .Add∞:0.1 .RmvØ-2hop:0.5 .Rmv+:0.4 .Rmv-:0.3
 *
 */
public class JarVersion {

	public static Configurator.EnumLauncher launcher = Configurator.EnumLauncher.jarOpenMole;

	/** Main utilisé par la version Jar en cas de lancement depuis les lignes de commandes ou depuis
	 * l'algo Genetique C++. OpenMole appelle directement run().
	 * --------------------------------------------------------
	 * Prend les paramètres en entrée, correspondant au filePath du fichier de réseau a copier
	 * et aux doubles de transmission des memes. Pour l'instant version static en nombre de comportement.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		// Region Param
		ArrayList<Double> probaBehavior = new ArrayList<Double>();
		String filePath ;
		File inputFile;
		boolean debug = false;

		launcher = Configurator.EnumLauncher.jarC;

		// STEP: Récupérer les probas et du filepath
		if(args.length != 6){
			System.err.println("Pas le bon nombre de paramètres");
			return;
		}

		filePath = args[0];
        inputFile = new File(filePath);
        if(debug)System.out.print("Fichier d'input: " + (inputFile.exists()? "exist" : "does not exist"));

		for (int i = 1; i < args.length; i++) {
			probaBehavior.add(Double.parseDouble(args[i]));
		}

		if(debug) System.out.println("Proba Recup "+ probaBehavior);

		run(inputFile, probaBehavior.get(0),probaBehavior.get(1),probaBehavior.get(2),probaBehavior.get(3),probaBehavior.get(4));
    }

    /** Run lancé depuis openMole, ou depuis le main@JarVersion.
     *
     * @param fileInput
     * @param param1
     * @param param2
     * @param param3
     * @param param4
     * @param param5
     * @return
     */
    public static Double run(File fileInput, double param1, double param2, double param3, double param4, double param5) {
        ArrayList<Double> probaBehavior = new ArrayList<Double>();
        probaBehavior.addAll(Arrays.asList(param1,param2,param3,param4,param5));
        return Initializer.initialize(launcher, fileInput, probaBehavior);
    }

}

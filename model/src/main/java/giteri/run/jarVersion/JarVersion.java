package giteri.run.jarVersion;

import giteri.run.configurator.Initializer;
import giteri.run.configurator.Configurator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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

		//region Param
		ArrayList<Double> memeProba = new ArrayList<>();
		ArrayList<Boolean> memeActi = new ArrayList<>();
		String filePath;
		File inputFile;
		boolean debug = false;
		launcher = Configurator.EnumLauncher.jarC;

		filePath = args[0];
		inputFile = new File(filePath);
		if(debug)System.out.print("Fichier d'input: " + (inputFile.exists()? "exist" : "does not exist"));


		for (int i = 1; i <= args.length/2; i++) {
			memeActi.add(args[i].compareToIgnoreCase("0") != 0);
		}

		for (int i = args.length/2 + 1; i < args.length; i++) {
			memeProba.add(Double.parseDouble(args[i]));
		}

		if(debug) System.out.println("Activation recup " + memeActi);
		if(debug) System.out.println("Proba Recup "+ memeProba);
		run(inputFile, memeActi, memeProba);

		System.exit(0);
	}

	/** Run lancé depuis openMole passant par Scala, ou . Ordre au 4/12/19
	 * [;.AddØ-Hop-0.0;.Add+-0.1;.Add--0.2;.Add∞-0.3;.AddØ-0.4;.RmvØ-2hop-0.5;.RmvØ-0.6;.Rmv+-0.7;.Rmv--0.8]
	 */
	public static Double run(File fileInput,
							 boolean acti1,boolean acti2,boolean acti3,boolean acti4,boolean acti5,
							 boolean acti6,boolean acti7,boolean acti8,boolean acti9, boolean acti10, boolean acti11, boolean acti12, boolean acti13,
							 double param1, double param2, double param3, double param4, double param5,
							 double param6, double param7, double param8, double param9, double param10, double param11, double param12, double param13)
	{
		ArrayList<Double> probaBehavior = new ArrayList<>();
		ArrayList<Boolean> memeAtivation = new ArrayList<>();
		memeAtivation.addAll(Arrays.asList(acti1,acti2,acti3,acti4,acti5, acti6, acti7, acti8, acti9, acti10, acti11, acti12, acti13));
		probaBehavior.addAll(Arrays.asList(param1,param2,param3,param4,param5, param6, param7, param8, param9, param10, param11, param12, param13));
		ClassicOpenMoleParameter comp = new ClassicOpenMoleParameter();
		comp.memeActication = memeAtivation;
		comp.memeProba = probaBehavior;
		return Initializer.initialize(launcher, comp);
	}

	public static Double run(File fileInput, List<Boolean> activation, List<Double> proba){
		return Initializer.initialize(launcher, fileInput, activation , proba);
	}

}

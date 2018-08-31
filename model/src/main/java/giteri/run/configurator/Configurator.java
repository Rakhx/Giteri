package giteri.run.configurator;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import giteri.tool.math.Toolz;

public final class Configurator {

	// VALEURS DONNEES A TITRE INDICATIF, set définitif dans l'initializer
	// La configuration de base correspond a OpenMole, car histoire de multi acces a des variables
	// depuis la meme JVM donc ne pas modifier du static. Les launchers pour autres usages changent
	// cette configuration initiale
	public static boolean withGraphicalDisplay = false;
	public static boolean jarMode = true;
	public static boolean systemPaused = false;
	public static boolean writeNetworkResultOnFitting = true;
	public static boolean writeMemeResultOnFitting = writeNetworkResultOnFitting || true;
	public static MemeDistributionType methodOfGeneration = MemeDistributionType.Nothing;

	// FITTING

	public static EnumExplorationMethod explorator = EnumExplorationMethod.exhaustive;
	public static FittingBehavior memeCombinaisonOnMap = FittingBehavior.simpleAndComplex;

	// TODO [Refact 6]- pour cohérence avec les méthodes, supprimé ce boolean et avoir la stabilitySearch pour le jar qui utilise direct les ftting
	public static MemeList typeOfMemeUseForFitting = MemeList.FITTING;
	// affiche dans la console "param en cours"
	public static boolean displayFittingProviderApplied = true;
	// pas de passage au run suivant, il faut appuyer sur next
	public static boolean manuelNextStep = false;
	// mise en pause automatique avant un changement de run. Il faut appuyer sur next
	public static boolean autoPauseIfNexted = false;

	// PROPAGATION DE MEME

	public static boolean usePropagation = true;
	// transmet l'un des memes possédés par l'acteur
	public static boolean usePropagationSecondGeneration = true;
	public static boolean memeCanBeReplaceByCategory = true;
	// les possesseurs initiaux des memes ne peuvent pas les perdre
	public static boolean fixedSlotForBreeder = true;
	// Donne des random add et rmv a tt le monde pour (?) gagner du temps
	// A tester les résultats

	//fluidité
	public static boolean initializeDefaultBehavior = true;

	public static boolean checkWhenFullPropagate = true;


	public static int activationCodeForScore = 55;
	public static int activationCodeAllAttribExceptDD = 247;
	public static int activationCodeAllAttrib = 255;

	// applique les filtres tour a tour
	public static boolean semiStepProgression = false;
	public final static int semiAutoWaitingTime = 3000;

	public static boolean useMemePropagationProba = true;
	public final static double probaEntiteLearning = 0;

	// Affichage log
	public static boolean DisplayLogBehaviorColors = false;

	public static boolean displayLogMemeApplication = false;
	public static boolean displayLogAvgDegreeByMeme = false;
	public static boolean displayLogMemeTransmission = false;

	private static boolean faster = false;
	public static boolean displayLogRatioLogFailOverFail = faster;
	public static boolean displayLogRatioLogFailOverSuccess = faster;
	public static boolean displayLogRatioTryAddOverTryRmv = faster;

	public static boolean DisplayLogdebugInstantiation = false;
	public static boolean DisplayLogGetAvailableMeme = false;

	// I/O
	// Défini si on écrit les détails des actions dans un fichier texte.
	//public static boolean writeHeavyDetails = true;
	public static String repByDefault = "defaultRep";
	// 1 = ihm, 2 = console, 4 = file; Et combinaison. 3 = ihm + console
	// 5 = file + ihm, 6 = console + file, 7 tout le tralal.
	public static int activationCodeForView = 1;

	public static String fileNameCsvSimple = "NetworkCSV";
	public static String fileNameCsvDetail = "NetworkDetailsCSV";
	public static String fileNameMeme = "memeCSV";


	// affichage de debug
	// Affiche le fait de vouloir passer au step suivant
	public static boolean debugStatAndPlot = true;
	public static boolean debugFittingClassFast = true;
	public static boolean debugFittingClass = true;
	public static boolean debugEntite = false;
	public static boolean debugEntiteHandler = false;

	public static boolean overallDebug = !jarMode;
	public static boolean debugHopAway = false;
	public static boolean autrucheMode = false;
	public static boolean debugOpenMole = false;

	// Configuration Modèle
	public static final boolean lotOfNodes = false;
	public final static int nbNode = lotOfNodes ? 1000 : 100;
	public static int refreshInfoRate = 500;
	public final static boolean autoRedoActionIfNoAction = false;

	// Affichage
	public static boolean displayPlotWhileSimulation = !jarMode;
	public static boolean displayMemePosessionDuringSimulation = true;
	
	// Config Temporaire

	//region ancien boolean, osef, etc
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-d_HH'h'mm'm'ss's'");
	public static File defaultPathForReadingNetwork = new File("model\\default.txt");
	public static Integer baseSleepTimeMulti = 0;
	private static Integer threadSleepMultiplicateur = baseSleepTimeMulti;

	// Construction aléatoire du réseau ou non // systeme en pause au lancement
	private static double probaRetraitLien = 0.;
	private static Object lockOnPause = new Object();

	// Ancien mecanismes
	public static boolean desgressiveLearningProba = false;
	public static boolean useEntitePropagationProba = false;
	public static boolean learningOnlyOnce = false;

//	public static long randomSeed;

	//endregion

	public enum EnumLauncher
	{
		ihm,
		jarC,
		jarOpenMole,
		testProvider
	}

	//region Concernant Meme and co

	/** Exhaustive..
	 *
	 */
	public enum EnumExplorationMethod
	{
		exhaustive,
		random,
		genetique,
		oneShot,
		specific
	}

	/** Basic Combinaison..
	 *
	 */
	public enum MemeDistributionType {
		SingleBasic,
		SingleCombinaison,
		AllCombinaison,
		AllSingle,
		specificDistrib,
		Nothing
	}

	public enum MemeActivityPossibility{
		AjoutMeme,
		RetraitMeme
	}

	/**
	 *
	 */
	public enum FittingBehavior {
		onlySimple,
		onlyComplex,
		simpleAndComplex
	}

	/** Retourne quelques éléments de la configuration pour les fichiers
	 * de log
	 * @return une arraylist, chaque élément sur un point particulier
	 */
	public static ArrayList<String> getConfig(){
		ArrayList<String> elements = new ArrayList<String>();
		elements.add("Nombre de noeuds: "+ nbNode);
		elements.add("Méthode de propagation" + (usePropagationSecondGeneration ?
				"Transmet l'un des meme portée":"Transmission direct du meme joué"));
		return elements;
	}

	/** Density..
	 *
	 */
	public enum NetworkAttribType{
		DENSITY,
		DDAVG,
		DDINTERQRT,
		DDARRAY,
		AVGCLUST,
		NBEDGES,
		NBNODES,
		APL,
		nbEdgesOnNbNodes
	}

	public static boolean isAttribActived(int activationCode, NetworkAttribType attribType){
		int puissance = (int) Math.pow(2, Configurator.getIndicateur(attribType)-1);
		return (activationCode & puissance) == puissance;
	}

	/** Fait le lien entre l'attribut et son bit d'activation / d'identification
	 *
	 * @param attributType
	 * @return
	 */
	public static int getIndicateur(NetworkAttribType attributType){
		switch (attributType) {
			case DENSITY:
				return 1;
			case DDAVG:
				return 2;
			case DDINTERQRT:
				return 3;
			case DDARRAY:
				return 4;
			case AVGCLUST:
				return 5;
			case NBEDGES:
				return 6;
			case NBNODES:
				return 7;
			case APL:
				return 8;
			case nbEdgesOnNbNodes:
				return 9;
			default:
				return -1;
		}
	}

	/**
	 *
	 * @param attributActive
	 * @return
	 */
	public static int getIndicateur(ArrayList<NetworkAttribType> attributActive){
		int activator = 0, puissance;
		for (NetworkAttribType networkAttribType : attributActive) {
			puissance = (int) Math.pow(2, Configurator.getIndicateur(networkAttribType)-1);
			activator += puissance;
		}

		return activator;
	}

	/** The most...
	 *
	 */
	public enum AgregatorType{
		THEMOST,
		THELEAST,
//		THEMOSTLINKED,
//		THELEASTLINKED,
		MINESUP,
		MINEINF,
		MINEDIF,
		MINEEQUAL,
		NOTLINKED,
		LINKED,
		RANDOM,
		HOPAWAY
	}

	/** Ajout lien...
	 *
	 */
	public enum ActionType {
		AJOUTLIEN,
		RETRAITLIEN,
		COPIERANDOMMEME,
		EVAPORATION,
		REFRESH,
		PURIFY,
		ANYTHING
	}

	/** degree
	 *
	 */
	public enum AttributType {
		DEGREE
	}

	public enum MemeList {
		EXISTING,
		ONMAP,
		FITTING
	}

	@SuppressWarnings("unused")
	public static double getProbaLearning(double max){
		if(probaEntiteLearning <= 1 && probaEntiteLearning >= 0)
			return probaEntiteLearning;
		else
			return Toolz.getProba() * max;
	}

	public static void setThreadSpeed(int speedToSet){
		synchronized(threadSleepMultiplicateur){
			threadSleepMultiplicateur = speedToSet;
		}
	}

	public static int getThreadSleepMultiplicateur(){
		synchronized(threadSleepMultiplicateur){
			return threadSleepMultiplicateur;
		}
	}

	public static boolean isSystemPaused() {
		synchronized(lockOnPause){
			return systemPaused;
		}
	}

	public static void setSystemPaused(boolean systemPaused) {
		synchronized(lockOnPause){
			Configurator.systemPaused = systemPaused;
		}
	}

	public static synchronized DateFormat getDateFormat() {
		return dateFormat;
	}

	//endregion
}


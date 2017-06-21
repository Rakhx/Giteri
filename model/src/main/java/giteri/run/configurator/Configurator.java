package giteri.run.configurator;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import giteri.tool.math.Toolz;

public final class Configurator {
	// La configuration de base correspond a OpenMole, car histoire de multi acces a des variables
	// depuis la meme JVM donc ne pas modifier du static. Les launchers pour autres usages changent
	// cette configuration initiale
	public static boolean withGraphicalDisplay = false;
	public static boolean jarMode = true;
	public static boolean systemPaused = false;
	public static boolean writeNetworkResultOnFitting = false;
	public static boolean doNotApplyMemeAvailability = true;
	public static MemeDistributionType methodOfGeneration = MemeDistributionType.FollowingFitting;

	// Fitting
	public static EnumExplorationMethod explorator = EnumExplorationMethod.oneShot;
	public static FittingBehavior memeCombinaisonOnMap = FittingBehavior.simpleAndComplex;
	public static boolean displayFittingProviderApplied = true;
	public static boolean manuelNextStep = false;
	public static boolean autoPauseIfNexted = false;

	// Propagation de meme
	public static boolean usePropagation = true;
	public static boolean usePropagationSecondGeneration = true;
	public static boolean memeCanBeReplaceByCategory = true;

	public static boolean fixedSlotForBreeder = true;
	public static boolean initializeDefaultBehavior = true;
	public static int activationCodeForScore = 55;
	public static int activationCodeAllAttribExceptDD = 247;
	public static int activationCodeAllAttrib = 255;

	public static boolean semiStepProgression = false;
	public final static int semiAutoWaitingTime = 3000;

	public static boolean useMemePropagationProba = true;
	public final static double probaEntiteLearning = 0;

	// Affichage log
	public static boolean DisplayLogBehaviorColors = false;
	public static boolean displayLogTimeEating = false;

	public static boolean displayLogMemeApplication = false;
	public static boolean displayLogAvgDegreeByMeme = false;
	public static boolean displayLogMemeTransmission = false;

	public static boolean displayLogRatioLogFailOverFail = false;
	public static boolean displayLogRatioLogFailOverSuccess= false;
	public static boolean displayLogRatioTryAddOverTryRmv = false;

	public static boolean DisplayLogdebugInstantiation = false;
	public static boolean DisplayLogGetAvailableMeme = false;

	// affichage de debug
	// Affiche le fait de vouloir passer au step suivant
	public static boolean debugStatAndPlot = true;
	public static boolean debugFittingClassFast = true;
	public static boolean debugFittingClass = true;
	public static boolean debugEntite = true;
	public static boolean debugEntiteHandler = false;

	public static boolean overallDebug = !jarMode;
	public static boolean debugHopAway = false;
	public static boolean autrucheMode = false;
	public static boolean debugOpenMole = false;

	// Configuration Modèle
	public static final boolean lotOfNodes = true;
	public final static boolean autoRedoActionIfNoAction = false;
	public final static int nbNode = lotOfNodes? 1000 : 100;
	public static int refreshInfoRate = 500;

	// Affichage
	public static boolean displayPlotWhileSimulation = !jarMode;
	public static boolean displayMemePosessionDuringSimulation = true;
	
	// Config Temporaire

	// Region ancien boolean, osef, etc
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-d_HH'h'mm'm'ss's'");
	public static File defaultPathForReadingNetwork = new File("/Users/Felix/Documents/W/S/Giteri/Rulez/default.txt");
	public static File rightHerePathForReadingNetwork = new File("default.txt");
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

	// EndRegion

	public enum EnumLauncher
	{
		ihm,
		jarC,
		jarOpenMole,
		testProvider
	}

	// Region Concernant Meme and co

	/** Exhaustive..
	 *
	 */
	public enum EnumExplorationMethod
	{
		exhaustive,
		random,
		genetique,
		oneShot
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
		FollowingFitting
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
		elements.add("Proba d'évaporation: "+ probaRetraitLien);
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
		THEMOSTLINKED,
		THELEASTLINKED,
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
		PURIFY
	}

	/** degree
	 *
	 */
	public enum AttributType {
		DEGREE
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

	// EndRegion
}


package configurator;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import math.Toolz;

public final class Configurator {
	
	public static boolean doNotApplyMemeAvailability = false;
	
	// Comportement modèle
//	public final static MemeDistributionType methodOfGeneration = MemeDistributionType.AllCombinaison;
//	public final static MemeDistributionType methodOfGeneration = MemeDistributionType.FollowingFitting;
//	public static MemeDistributionType methodOfGeneration = MemeDistributionType.SingleBasic;
	
	public static MemeDistributionType methodOfGeneration = MemeDistributionType.AllCombinaison;
	
//	public final static MemeDistributionType methodOfGeneration = MemeDistributionType.FollowingFitting;
	
//	public  static MemeDistributionType methodOfGeneration = MemeDistributionType.specificDistrib;
	
	public final static EnumExplorationMethod explorator = EnumExplorationMethod.exhaustive;
	
	// Fitting
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
	
	public static boolean displayLogAvgDegreeByMeme = false;
	public static boolean displayLogMemeTransmission = false;
	public static boolean displayLogMemeApplication = false;
	
	public static boolean displayLogRatioLogFailOverFail = false;
	public static boolean displayLogRatioLogFailOverSuccess= false;
	public static boolean displayLogRatioTryAddOverTryRmv = false;
	
	public static boolean DisplayLogdebugInstantiation = false;
	public static boolean DisplayLogGetAvailableMeme = false;

	// affichage de debug
	// Affiche le fait de vouloir passer au step suivant
	public static boolean debugStatAndPlot = false;
	public static boolean debugFittingClassFast = false;
	public static boolean debugFittingClass = false;
	
	// Configuration Modèle
	public static boolean jarMode = false;
	public static boolean turboMode = jarMode ? true : false;
	public static boolean overallDebug = jarMode ? false : true;
	
	public static final boolean lotOfNodes = false;
	
	public final static boolean autoRedoActionIfNoAction = false;
	public final static int nbNode = lotOfNodes? 10000 : 100;
	public static boolean withGraphicalDisplay = lotOfNodes? false: true;
	public static boolean resetDensityOverTimeBetweenRun = true; 
	public static int refreshInfoRate = turboMode? 3000 : 100;
	public final static int nbSimulationByStep = lotOfNodes? 1: 3;
	
	public final static boolean osefResultat = false;
	
	// Affichage
	public static boolean displayPlotWhileSimulation = lotOfNodes ? false: true;
	public static boolean displayMemePosessionDuringSimulation = lotOfNodes ? false: true;
	public static boolean writeNetworkResultOnFitting = true;
	
	
	// Config Temporaire
	public static boolean debugHopAway = false;
	public static boolean autrucheMode = true;
	
	// Region ancien boolean, osef, etc
	
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-d_HH'h'mm'm'ss's'");
	public static File defaultPathForReadingNetwork = new File("/Users/Felix/Documents/W/S/Giteri/Rulez/default.txt");
	public static File rightHerePathForReadingNetwork = new File("default.txt");
//	public static File defaultPathForConfig = new File("/Users/Felix/Documents/W/S/Giteri/Rulez/config.txt");
	
	public static Integer baseSleepTimeMulti = 0;//turboMode ? 0 : 1;
	private static Integer threadSleepMultiplicateur = baseSleepTimeMulti;
	
	// Construction aléatoire du réseau ou non // systeme en pause au lancement	
	public static boolean systemPaused = turboMode ? false :true;
	private static double probaRetraitLien = 0.;
	private static Object lockOnPause = new Object();		
	
	// Ancien mecanismes
	public static boolean desgressiveLearningProba = false;
	public final static double reLearningThreshold = 0.001;
	public static boolean useEntitePropagationProba = false;
	public static boolean learningOnlyOnce = false;
	
	public static long randomSeed; 
	
	// EndRegion

	// Region Concernant Meme and co
	
	/** Exhaustive..
	 *
	 */
	public static enum EnumExplorationMethod
	{
		exhaustive,
		random,
		genetique,
		oneShot
	}
	
	/** Basic Combinaison..
	 * 
	 */
	public static enum MemeDistributionType {
		SingleBasic,
		SingleCombinaison,
		AllCombinaison,
		AllSingle,
		FourWithInverted,
		OnlyOneAgent,
		specificDistrib,
		FollowingFitting
	}
	
	public static enum MemeActivityPossibility{
		AjoutMeme,
		RetraitMeme	
	}
	
	/**
	 * 
	 */
	public static enum FittingBehavior {
		onlySimple,
		onlyComplex,
	    simpleAndComplex
	}
	
	/** Distribution de cmpt, boolean relearn..
	 * 
	 */
	public static enum FittingParamType{
		LEARNINGPROBA,
		DISTRIB,
		DIFFUSIONPROBA,
		RELEARN,
		TRANSMITPROBA
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
	public static enum NetworkAttribType{
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
	public static enum AgregatorType{
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
	public static enum ActionType {
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
	public static enum AttributType {
		DEGREE
	}

	@SuppressWarnings("unused")
	public static double getProbaLearning(double max){
		if(probaEntiteLearning <= 1 && probaEntiteLearning >= 0)
			return probaEntiteLearning;
		else
			return Toolz.getProba() * max;
	}
	
	public static double getProbaRetraitLien() {
		return probaRetraitLien;
	}

	public static synchronized void setProbaRetraitLien(double probaEvaporation) {
		Configurator.probaRetraitLien = probaEvaporation;
	}

	public static void setThreadSpeed(int speedToSet){
			synchronized(threadSleepMultiplicateur){
				threadSleepMultiplicateur = speedToSet;
			}
	}
	
	public static Integer getBaseThreadSleepMulti() {
		return baseSleepTimeMulti;
	}

	public static int getThreadSleepMultiplicateur(){
		synchronized(threadSleepMultiplicateur){
			return threadSleepMultiplicateur;
		}
	}
	
	public static void setThreadSleepMultiplicateur(Integer threadSpeedMultiplicateur) {
		synchronized(threadSpeedMultiplicateur){
			Configurator.threadSleepMultiplicateur = threadSpeedMultiplicateur;
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

	public static synchronized void setDateFormat(DateFormat dateFormat) {
		Configurator.dateFormat = dateFormat;
	}

	public static boolean toggleSystemPause(){
		synchronized(lockOnPause){
			systemPaused = !systemPaused;
			return systemPaused;
		}
	}
	
	// EndRegion
}


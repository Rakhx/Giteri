package giteri.run.configurator;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import giteri.tool.math.Toolz;

public final class Configurator {

	// region iniitalizer stuff
	// VALEURS DONNEES A TITRE INDICATIF, set définitif dans l'initializer
	// La configuration de base correspond a OpenMole, car histoire de multi acces a des variables
	// depuis la meme JVM donc ne pas modifier du static. Les launchers pour autres usages changent
	// cette configuration initiale
	public static boolean withGraphicalDisplay = true;
	public static boolean jarMode = true;
	public static boolean systemPaused = false;
	public static boolean writeNetworkResultOnFitting = true; // Screenshot, network.csv...
	public static boolean writeMemeResultOnFitting = writeNetworkResultOnFitting || true; // NetworkDetails.csv
	public static MemeDistributionType methodOfGeneration = MemeDistributionType.Nothing;
	// endregion

	// region Modèle

	// FONCTIONNEMENT
	public static boolean manuelNextStep = false; // pas de passage au run suivant, il faut appuyer sur next
	public static boolean autoPauseIfNexted = false; // mise en pause automatique avant un changement de run. Il faut appuyer sur next
	public static boolean displayPlotWhileSimulation = !jarMode; // Affichage des DD et densité
	public static boolean initializeDefaultBehavior = false;	//fluidité
	public static boolean oneAddForOneRmv = false; // Joue tour a tour un ajout d'un retrait
	public static boolean onlyOneOfEachAction = false; // Réduit le nombre d'action a

	// MEME
	public static boolean strictEqualityInComparaison = false; // FALSE : >= || TRUE : >

	// PROPAGATION
	public static boolean fixedSlotForBreeder = true;	// les possesseurs initiaux des memes ne peuvent pas les perdre
	public static boolean checkWhenFullPropagate = true; 	// All action spread? affiche en combien d'action
	public static int checkFullProRefreshRate = 75; // every X step vérification du full propagate

	// SCORE
	public static int activationCodeForScore = 55;
	public static int activationCodeAllAttribExceptDD = 247;
	public static int activationCodeAllAttrib = 255;

	// endregion

	// region Fitting

	public static EnumExplorationMethod explorator = EnumExplorationMethod.exhaustive; // Type d'exploration de fitting
	public static MemeList typeOfMemeUseForFitting = MemeList.FITTING; // Peut etre ONMAP, EXISTING, FITTING
	public static int initialNetworkForFitting = 0; // code pour le network en fitting. 0:empty 1:4% 2:50% 3:PA 4:SW
	public static int nbRepetitionbyRun = 100;
	public static boolean fixedNbAction  = false; //  ne pas augmenter le nombre d'action max en fonction du nombre de noeud


	// endregion

	// region I/O
	public static boolean displayFittingProviderApplied = true;	// affiche dans la console apprlications des params:
	// 1 = ihm, 2 = console, 4 = file; Et combinaison. 3 = ihm + console
	// 5 = file + ihm, 6 = console + file, 7 tout le tralal.
	public static int activationCodeForView = 5;
	public static boolean displayMemePosessionDuringSimulation = true; // Affiche réparition des memes [NbActivByMeme] - [37500, meme ADLKDGRDMMNSPNTLK - 13528, meme RMLKDGRDMMNIFLK - 18132,
	public static boolean writeNbActionPerSec = false; // pas de fichier nbline

	public static boolean writeFailDensityLink = false; // fichier d'info sur les Fails X density variation

	public static String repByDefault = "DefaultRep";
	public static String repForFitting = "Stability";
	public static String fileNameCsvSimple = "NetworkCSV";
	public static String fileNameCsvDetail = "NetworkDetailsCSV";
	public static String fileNameMeme = "memeCSV";

	public static boolean displayOnIHMDensitySD = false;
	// endregion

	// region Affichage log
	public static boolean DisplayLogBehaviorColors = false; // correspondance meme <=> code couleur

	public static boolean displayLogMemeApplication = false; // Chaque application de meme
	public static boolean displayLogAvgDegreeByMeme = false; // combinaisons de meme et leur degré + derniere application + application from start
	public static boolean displayLogMemeTransmission = false; // qui recoit quel meme

	private static boolean faster = false; // les rations d'echecs sur echec, echec sur réussite...
	public static boolean displayLogRatioLogFailOverFail = faster;
	public static boolean displayLogRatioLogFailOverSuccess = faster;
	public static boolean displayLogRatioTryAddOverTryRmv = faster;

	public static boolean DisplayLogdebugInstantiation = false;
	public static boolean DisplayLogGetAvailableMeme = false;

	//endregion

	// region affichage de debug
	public static boolean debugStatAndPlot = true;
	public static boolean debugFittingClassFast = true;
	public static boolean debugFittingClass = true;
	public static boolean debugEntite = false;
	public static boolean debugEntiteHandler = false;

	public static boolean overallDebug = !jarMode;
	public static boolean debugHopAway = false;
	public static boolean autrucheMode = false;
	public static boolean debugOpenMole = false;

	// endregion

	//region ancien boolean, osef, etc
	// moyen osef
	public static final boolean lotOfNodes = false;

	private static int nbNode = lotOfNodes ? 1000 : 100;
	public static int refreshInfoRate = 10;
	public final static boolean autoRedoActionIfNoAction = false;
	public static boolean semiStepProgression = false;	// applique les filtres tour a tour
	public static boolean useMemePropagationProba = true;
	public final static double probaEntiteLearning = 0;
	public static boolean memeCanBeReplaceByCategory = true;
	public final static int semiAutoWaitingTime = 3000;
	public static boolean usePropagation = true;
	public static boolean usePropagationSecondGeneration = false; // transmet un des memes du porteur, pas forcement celui applied


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

	//region Enum & co

	public enum EnumLauncher
	{
		ihm,
		jarC,
		jarOpenMole,
		testProvider
	}

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
		nbEdgesOnNbNodes;
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
		HOPAWAY,
		TRIANGLE
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
		ANYTHING;
	}


	/** degree
	 *
	 */
	public enum AttributType {
		DEGREE;
	}


	public enum MemeList {
		EXISTING,
		ONMAP,
		FITTING;
	}

	//endregion

	// region getter Setter

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

	private static Object syncNode = new Object();
	public static int getNbNode() {
		synchronized (syncNode) {
			return nbNode;
		}
	}
	public static void setNbNode(int nbnode) {
		synchronized (syncNode) {
			nbNode = nbnode;
		}
	}

	//endregion
}


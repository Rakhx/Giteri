package giteri.run.configurator;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public final class Configurator {

	public static boolean fullSilent = false; // Aucun affichage, aucun fichier output

	// region initializer stuff
	// VALEURS DONNEES A TITRE INDICATIF, set définitif dans l'initializer
	// La configuration de base correspond a OpenMole, car histoire de multi acces a des variables
	// depuis la meme JVM donc ne pas modifier du static. Les launchers pour autres usages changent
	// cette configuration initiale
	public static boolean withGraphicalDisplay = true;
	public static boolean jarMode = true; // Si vrai, affiche le score resultat de simu
	public static boolean systemPaused = false;
	public static boolean writeNetworkResultOnFitting = !fullSilent; // Screenshot, network.csv...
	public static boolean writeMemeResultOnFitting = writeNetworkResultOnFitting; // NetworkDetails.csv
	public static MemeDistributionType methodOfGeneration = MemeDistributionType.Nothing;
	// endregion

	// region Modèle

	// FONCTIONNEMENT
	public static boolean manuelNextStep = false; // NO-AUTOSKIP pas de passage au run suivant, il faut appuyer sur next
	public static boolean autoPauseIfNexted = false; // AUTOPAUSE mise en pause automatique avant un changement de run. Il faut appuyer sur next
	@toOutput ( yes = true )
	public static boolean initializeDefaultBehavior = true	;	// ----FLUIDITE----
	@toOutput ( yes = true )
	public static boolean rebranchementAction = false; // REWIRE Faire l'ajout et le retrait dans le meme temps

	// MEME
	@toOutput ( yes = true )
	public static boolean strictEqualityInComparaison = true; // FALSE : >= || TRUE : >

	// PROPAGATION
	public static boolean usePropagation = true; // utilisation de la propagation
	public static boolean fixedSlotForBreeder = true;	// les possesseurs initiaux des memes ne peuvent pas les perdre
	@toOutput ( yes = true )
	public static boolean autoMemeForBreeder = false;	// Les breeder ont associé un meme complémement, rmd ajout ou retrait.
	public static boolean onlyOneToPropagate = true; // Dans le cas ou une action s'applique sur plusieurs entités
	public static boolean usePropagationSecondGeneration = false; // transmet un des memes du porteur, pas forcement celui applied

	public static boolean useEntitySuccesProba = false; // Prend en compte la proba porté pour l'entité pour APPLY a meme. Actuellement l'index
	public static boolean useMemePropagationProba = true; // utilise la proba de propagation portée par le meme

	// SCORE
	public static int activationCodeForScore = 202; // 153: APL(128)+avgClust(16)+DDArray(8)+Density(1)
	public static int activationCodeAllAttrib = 255;
	// public static int activationCodeActual = 255;

	// endregion

	// region Fitting

	public static EnumExplorationMethod explorator = EnumExplorationMethod.exhaustive; // Type d'exploration de fitting
	public static MemeList typeOfMemeUseForFitting = MemeList.FITTING; // Peut etre ONMAP, EXISTING, FITTING

	public static int initialNetworkForFitting = 0; // code pour le network en fitting. 0:empty 1:4% 2:50% 3:PA 4:SW
	@toOutput ( yes = true )
	public static int nbRepetitionbyRun = 2;
	@toOutput ( yes = true )
	public static int nbRepetitionForJar = 1;

	@toOutput ( yes = true )
	public static boolean fixedNbAction  = false; //  ne pas augmenter le nombre d'action max en fonction du nombre de noeud
	@toOutput ( yes = true )
	public static int multiplicatorNbAction  = 2500; //  Par combein on multiplie le nombdre de noeud sur la simulation

	// endregion

	// region I/O

	public static boolean displayFittingProviderApplied = fullSilent ? false : true;	// affiche dans la console apprlications des params:
	// 1 = ihm, 2 = console, 4 = file; Et combinaison. 3 = ihm + console
	// 5 = file + ihm, 6 = console + file, 7 tout le tralal.
	public static int activationCodeForView = fullSilent? 0 : 5;

	public static boolean displayMemePossessionEvolution = false && !fullSilent; // Affiche dans l'IHM la possession des meme au fur et a mesure

	public static boolean displayPlotWhileSimulation = true && !fullSilent; // Affichage des DD et densité
	public static boolean displayMemePosessionDuringSimulation = true && !fullSilent; // Affiche réparition des memes [NbActivByMeme] - [37500, meme ADLKDGRDMMNSPNTLK - 13528, meme RMLKDGRDMMNIFLK - 18132,
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

	public static boolean checkWhenFullPropagate = false; 	// All action spread? affiche en combien d'action
	public static int checkFullProRefreshRate = 75; // every X step vérification du full propagate

	//endregion

	// region affichage de debug
	public static boolean debugStatAndPlot = false;
	public static boolean debugFittingClassFast = false;
	public static boolean debugFittingClass = false;
	public static boolean debugEntite = false;
	public static boolean debugEntiteHandler = false;


	public static boolean overallDebug = !jarMode;
	public static boolean debugHopAway = false;
	public static boolean debugJarMode = false;

	// endregion

	//region ancien boolean, osef, etc
	// moyen osef
	public static final boolean lotOfNodes = false;
	private static int nbNode = lotOfNodes ? 500 : 200;
	public static int refreshInfoRate = 10;
	public final static boolean autoRedoActionIfNoAction = false;
	public static boolean semiStepProgression = false;	// applique les filtres tour a tour
	public static boolean memeCanBeReplaceByCategory = true;
	public final static int semiAutoWaitingTime = 3000;

	public static boolean oneAddForOneRmv = false; // ONEforONE Joue tour a tour un ajout d'un retrait
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-d_HH'h'mm'm'ss's'");
	public static File defaultPathForReadingNetwork = new File("model"+File.separator+"default.txt");
	public static Integer baseSleepTimeMulti = 0;
	private static Integer threadSleepMultiplicateur = baseSleepTimeMulti;


	public static int sizeOfCircularForLastActionDone = 100;

	// Construction aléatoire du réseau ou non // systeme en pause au lancement
	private static Object lockOnPause = new Object();

	// Ancien mecanismes
	public static boolean desgressiveLearningProba = false;
	public static boolean useEntitePropagationProba = false;
	public static boolean learningOnlyOnce = false;

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
		DENSITY, // Density du réseau
		DDAVG, //
		DDINTERQRT, // la distance interquartile du réseau
		DDARRAY, // le tableau de DD
		AVGCLUST, // Clustering moyen
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
		TRIANGLE,
		THEIRSUP
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

	public enum ViewMessageType {
		PROPAGATION,
		AVGDGRBYMEME,
		FAILXDENSITY,
		ECHECS,
		FITTINGSKIP,
		MEMEAPPLICATION,
		NBACTIVBYMEME,
		LASTMEMEACTIF

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
	@Retention(RetentionPolicy.RUNTIME)
	public @interface toOutput {
		boolean yes();
	}

}


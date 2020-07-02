package giteri.run.configurator;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public final class Configurator {

	// si true, openmole mode activé
	private static boolean fastSwitchOpen = false;
	public static boolean quickScore = !fastSwitchOpen; // Aucun affichage, aucun fichier output
	public static boolean fullSilent = fastSwitchOpen; // Aucun affichage, aucun fichier output

	// Fast debuguer & I/O
	public static boolean displayLogAvgDegreeByMeme = true; // combinaisons de meme et leur degré + derniere application + application from start

	// region Modèle

	// MODELE II param
	public static boolean coupleSingleTransmission = true; // Transmet le couple à l'entité ayant recu l'action et pas plus
	public static boolean useMemePropagationProba = true; // utilise la proba de propagation portée par le meme
	// TODO apply whole couple or only one adction?

	// FONCTIONNEMENT
	public static boolean manuelNextStep = false; // NO-AUTOSKIP pas de passage au run suivant, il faut appuyer sur next
	public static boolean autoPauseIfNexted = false; // AUTOPAUSE mise en pause automatique avant un changement de run. Il faut appuyer sur next
	@toOutput ( yes = true )
	public static boolean initializeDefaultBehavior = true;	// ----FLUIDITE----
	public static boolean initializeDefaultBehaviorToBreeder = false;	// ----FLUIDITE BREEDER----
	@toOutput ( yes = true )
	public static boolean rebranchementAction = false; // REWIRE Faire l'ajout et le retrait dans le meme temps
	public static boolean limitlessAction;

	// MEME
	@toOutput ( yes = true )
	public static boolean strictEqualityInComparaison = true; // FALSE : >= || TRUE : >

	// PROPAGATION
	public static boolean fixedSlotForBreeder = true;	// les possesseurs initiaux des memes ne peuvent pas les perdre
	@toOutput ( yes = true )
	public static boolean autoMemeForBreeder = false;	// Les breeder ont associé un meme complémement, rmd ajout ou retrait.
	public static boolean onlyOneToPropagate = true; // Dans le cas ou une action s'applique sur plusieurs entités


	// SCORE

	/** 1:density - 2:ddavg - 4:ddInterQuart - 8:ddArray 16:avgClust
	 * 32: nbEdges 64: nbNodes 128:APL 256:Edges/nodes 512:thirdMoment
	 *
	 */
	@toOutput ( yes = true )
	public static int activationCodeForScore = 157;
	//public static int activationCodeForScore = 17;
	//157 = 1 density + 4 ddinter + 8 DDarray + 16 avg Clust + 128 APL
	//190= 2 DDAVG + 4 DDINTER + 8 DDArray + 16 avg clust + 32 nbedge + 128 APL 
		// 446= 2 DDAVG + 4 DDINTER + 8 DDArray + 16 avg clust + 32 nbedge + 128 APL + 256 third moment

			//17; // Config "is max CC possible?"
	// 153;// SCORE POUR SMALLWORLD DENSITY - DDARRAY - DDAVG - APL

	 // 170+512; // 170+512 153: APL(128)+avgClust(16)+DDArray(8)+Density(1)+ third(512)
	// 16 + 512 Clust + third
	// 170+512 = THIRD APL EDGES ARRAY DDAVG

	// distance linéaire ou carré (augmente l'importance des 1er point perdu )
	public static boolean onlyLinear = true;
	// decote de score par rapport au noeud non connecté'
	public static boolean considereNodeAlone = true;
	public static int activationCodeAllAttrib = 1023;
	public static int initialnetworkForBase = 0; // Réseau tout initial tout au début 0-Vide 1-4% 2-30% 3- SF 4-SW

	// endregion

	// region Fitting

	public static EnumExplorationMethod explorator = EnumExplorationMethod.exhaustive; // Type d'exploration de fitting
	public static MemeList typeOfMemeUseForFitting = MemeList.FITTING; // Peut etre ONMAP, EXISTING, FITTING

	@toOutput ( yes = true )
	public static int initialNetworkForFitting = 0; // code pour le network en fitting. 0:empty 1:4% 2:50% 3:PA 4:SW
	@toOutput ( yes = true )
	public static int nbRepetitionbyRun = 2;
	@toOutput ( yes = true )
	public static int nbRepetitionForJar = 2;

	@toOutput ( yes = true )
	public static boolean fixedNbAction  = false; //  ne pas augmenter le nombre d'action max en fonction du nombre de noeud
	@toOutput ( yes = true )
	public static int multiplicatorNbAction  = 3000; //  Par combein on multiplie le nombdre de noeud sur la simulation de base 3000

	// endregion

	// region I/O

	public static boolean displayFittingProviderApplied = fullSilent ? false : true;	// affiche dans la console apprlications des params:
	// 1 = ihm, 2 = console, 4 = file; Et combinaison. 3 = ihm + console
	// 5 = file + ihm, 6 = console + file, 7 tout le tralal.
	//public static int activationCodeForView = fullSilent? 0 : 5;
	public static int activationCodeForView = fullSilent? 0 : 3;

	public static boolean displayMemePossessionEvolution = true && !fullSilent; // Affiche dans l'IHM la possession des meme au fur et a mesure
	public static boolean displayPlotWhileSimulation = true && !fullSilent; // Affichage des DD et densité
	public static boolean displayMemePosessionDuringSimulation = true && !fullSilent; // Affiche réparition des memes [NbActivByMeme] - [37500, meme ADLKDGRDMMNSPNTLK - 13528, meme RMLKDGRDMMNIFLK - 18132,
	public static boolean writeNbActionPerSec = false; // pas de fichier nbline
	public static boolean writeFailDensityLink = false; // fichier d'info sur les Fails X density variation
	public static boolean writeFailMemeApply = false; // fichier d'info sur les Fails X density variation

	public static String repByDefault = "DefaultRep";
	public static String repForFitting = "Stability";
	public static String fileNameCsvSimple = "NetworkCSV";
	public static String fileNameCsvDetail = "NetworkDetailsCSV";
	public static String fileNameMeme = "memeCSV";
	public static String fileNameSerialisation = "serialization.se";
	public static String fileNameSerialisationOpen = "../../../../../../../serialization.se";

	// endregion

	// region Affichage log

	public static boolean DisplayLogBehaviorColors = false; // correspondance meme <=> code couleur
	public static boolean displayLogMemeApplication = false; // Chaque application de meme
	public static boolean displayLogMemeTransmission = false; // qui recoit quel meme

	private static boolean faster = false; // les rations d'echecs sur echec, echec sur réussite...
	public static boolean displayLogRatioLogFailOverFail = faster;
	public static boolean displayLogRatioLogFailOverSuccess = faster;
	public static boolean displayLogRatioTryAddOverTryRmv = faster;

	public static boolean checkWhenFullPropagate = false; 	// All action spread? affiche en combien d'action
	public static int checkFullProRefreshRate = 75; // every X step vérification du full propagate

	public static boolean prepareTheOpti = false; // usage de stopwatch
	//endregion

	// region affichage de debug


	public static boolean debugCouple = true;

	public static boolean debugStatAndPlot = false;
	public static boolean debugFittingClass = false;
	public static boolean debugEntite = false;
	public static boolean debugEntiteHandler = false;

	public static boolean debugIHM = false;
	public static boolean debugHopAway = false;

	public static boolean debugJarMode = false;
	// endregion

	// region initializer stuff
	// VALEURS DONNEES A TITRE INDICATIF, set définitif dans l'initializer
	// La configuration de base correspond a OpenMole, car histoire de multi acces a des variables
	// depuis la meme JVM donc ne pas modifier du static. Les launchers pour autres usages changent
	// cette configuration initiale
	public static boolean withGraphicalDisplay;// = true;
	public static boolean jarMode; // = true; // Si vrai, affiche le score resultat de simu
	public static boolean systemPaused;// = false;
	public static boolean writeNetworkResultOnFitting ; //= !fullSilent; // Screenshot, network.csv...
	public static boolean writeMemeResultOnFitting ; //= writeNetworkResultOnFitting; // NetworkDetails.csv
	public static EnumLauncher typeOfConfig ;
	// endregion
	//region ancien boolean, osef, etc
	// moyen osef
	public static final boolean lotOfNodes = true;
	private static int nbNode = lotOfNodes ? 300 : 40;
	public static int refreshInfoRate = 10;
	public static boolean semiStepProgression = false;	// applique les filtres tour a tour
	public static boolean memeCanBeReplaceByCategory = true;
	public final static int semiAutoWaitingTime = 3000;

	public static boolean oneAddForOneRmv = false; // ONEforONE Joue tour a tour un ajout d'un retrait
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-d_HH'h'mm'm'ss's'");
	public static File defaultPathForReadingNetwork = new File("model"+File.separator+"network.txt");
	public static Integer baseSleepTimeMulti = 0;
	private static Integer threadSleepMultiplicateur = baseSleepTimeMulti;

	public static int sizeOfCircularForLastActionDone = 100;

	// Construction aléatoire du réseau ou non // systeme en pause au lancement
	private static Object lockOnPause = new Object();

	// Ancien mecanismes
	public static boolean useEntitePropagationProba = false;

	//endregion

	//region Enum & co

	public enum EnumLauncher
	{
		ihm,
		jarC,
		jarOpenMole
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
		DENSITY, // 1 - Density du réseau
		DDAVG, // 2
		DDINTERQRT, // 4 - la distance interquartile du réseau
		DDARRAY, // 8 - le tableau de DD
		AVGCLUST, // 16 - Clustering moyen
		NBEDGES, // 32
		NBNODES, // 64
		APL, // 128
		nbEdgesOnNbNodes, // 256
		thirdMoment; // 512
	}


	/** The most...
	 *
	 */
	public enum AgregatorType{
		THEMOST,
		THELEAST,
		MINESUP,
		MINEINF,
		MINEDIF,
		MINEEQUAL,
		NOTLINKED,
		LINKED,
		RANDOM,
		HOPAWAY,
		HOPAWAY3,
		TRIANGLE,
		THEIRSUP,
		THEIRSUPSIX,
		THEIREQUAL,
		SELFSUP,
		BLANK
	}

	/** Ajout lien...
	 *
	 */
	public enum TypeOfUOT {
		AJOUTLIEN, // ajout d'un lien
		RETRAITLIEN, // retrait d'un lien
		BASIC, // ajout ou retrait
		COUPLE, // couple d'action
		ANYTHING; // tout confondu
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
			case thirdMoment:
				return 10;
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

	// Diviseur appliqué a la propagation d'un couple meme ( un couple meme peut se proposer a tout les cibles? )
	public static double coupleDividerTransmission = nbNode; //
}


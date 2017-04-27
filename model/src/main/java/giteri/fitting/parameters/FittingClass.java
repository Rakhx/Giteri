package giteri.fitting.parameters;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Optional;

import giteri.tool.math.Toolz;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.network.NetworkProperties;
import giteri.network.networkStuff.CommunicationModel;
import giteri.network.networkStuff.NetworkConstructor;
import giteri.network.networkStuff.NetworkFileLoader;
import giteri.network.networkStuff.WorkerFactory;
import giteri.tool.objects.ObjectRef;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import giteri.tool.other.WriteNRead;
import scala.util.Random;
import giteri.fitting.algo.IExplorationMethod;
import giteri.fitting.algo.Result;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.NetworkAttribType;
import giteri.meme.entite.EntiteHandler;
import giteri.meme.entite.Meme;
import giteri.meme.event.ActionApplyEvent;
import giteri.meme.event.ActionApplyListener;
import giteri.meme.event.BehavTransmEvent;
import giteri.meme.event.BehaviorTransmissionListener;

/** Classe de configuration pour lancer un fitting
 * ou autre recherche de stabilité.
 *
 * Run: Lancement d'un simulation en partant de zero niveau
 * meme, état du réseau, etc
 * Step: Pendant un run, interval de temps entre deux mesures
 * Config // set de parametre : valeur des paramètres de configuration
 * de la simulation. Ne change pas pendant un run.
 *
 */
public class FittingClass implements BehaviorTransmissionListener, ActionApplyListener {

	// Region Variables

	private EntiteHandler entiteHandler;
	private MemeFactory memeFactory;
	private NetworkFileLoader networkFileLoader;
	private WriteNRead writeNRead;
	private WorkerFactory workerFactory;
	private NetworkConstructor networkConstructor;


	public boolean debug = false;
	public CommunicationModel com ;
	public IExplorationMethod explorator;

	// Region CONFIGURATION INITIALE

	// Nombre de fois ou on lance un run pour une meme config.
	public int nbRepetitionByConfig;

	// Nombre d'action réalisé par les entités avant une collecte de données
	public int nbActionByStep = 500;
	public int boucleExterneSize = 30;


	// EndRegion

	// Region VARIABLES DE FONCTIONNEMENT

	// numero du run en cours
	public int numeroRun = -1;
	public String numeroRunAsString = "-1";

	// numéro de répétition dans le run
	public int numeroTurn = -1;
	public String numeroTurnAsString = "-1";

	// Compteur du nombre de relevé fait pour un meme run
	public int turnCount = 0;

	File repOfTheSearch ;

	private Integer nbActionCount = 0;
	private Integer nbTransmissionCount = 0;

	public int nbActionLocal = 0;

	public long currentSeed ;


	public int circularSize = 200;
	// Cette liste est changée pendant le fitting a chaque event action
	public CircularFifoQueue<Meme> cqLastXActionDone;
	// Ces deux listes sont utilisées dans la boucle la plus interne du fitting, donc chaque nbActionByStep
	private Hashtable<Meme, Integer> kvLastActionDone;
	private Hashtable<Meme, Double> kvOverallProportionActionDone;
	private Hashtable<Meme, Integer> kvOverallNumberOfActionDone;


	// Liste qui sont utlisées pour définir si on s'arrete de fitter sur une configuration ou non
	private CircularFifoQueue<Hashtable<Meme, Double>> cfqMemeAppliancePropOnFitting;
	private CircularFifoQueue<Hashtable<Meme, Integer>> cfqMemeApplianceNumberOnFitting;
	private int nbEltCircularQFitting = 15;

	private ArrayList<Meme> memesAvailables;
	private int nbCallContinuOnThisConfig = 0;


	private double threshSeDensity, threshSeCoeff, threshSeAppliance;
	private double threshHeDensity, threshHeAppliance;
//	private double threshHeCoeff;

	// every nbActionThreshrising rise threshold of risingPourcentage

	private double risingPourcentage = .1;
	private int nbActionThreshRising = 7;

	// EndRegion

	// Region RESULTATS DE SIMULATION

	// relevés des différentes densité pour une meme configuration
	public ArrayList<NetworkProperties> networksSameTurn = new ArrayList<NetworkProperties>();

	public CircularFifoQueue<Double> cfqDensityValuesOnOneRun;
//	public CircularFifoQueue<Double> meanDensityValuesOnOneRun;

	// Variance sur ces densité
//	public double currentDensity;

	NetworkProperties targetNetProperties;
	NetworkProperties currentNetProperties = new NetworkProperties();

	public double currentNetworkScore;
	Result resultNetwork ;

	ArrayList<String> repertoires ;

	// EndRegion

	// EndRegion

	/**	Constructeur.
	 *
	 * @param stabilityRun
	 */
	public FittingClass(boolean stabilityRun, WriteNRead wnr, CommunicationModel com, MemeFactory memeF,
						NetworkFileLoader nfl, WorkerFactory wf, EntiteHandler eh, NetworkConstructor nc){
		resultNetwork = new Result(wnr);
		this.com = com;
		this.memeFactory = memeF;
		this.writeNRead = wnr;
		this.networkFileLoader = nfl;
		this.workerFactory = wf;
		this.entiteHandler = eh;
		this.networkConstructor = nc;
		setDefaultValue();
	}

	/**
	 *
	 */
	private void setDefaultValue(){
		nbRepetitionByConfig = 3;
		nbActionByStep = 50;
		cfqDensityValuesOnOneRun = new CircularFifoQueue<Double>(boucleExterneSize);
		cqLastXActionDone = new CircularFifoQueue<Meme>(circularSize);
		kvLastActionDone = new Hashtable<Meme, Integer>();
		kvOverallProportionActionDone = new Hashtable<Meme, Double>();
		kvOverallNumberOfActionDone = new Hashtable<Meme, Integer>();
		cfqMemeAppliancePropOnFitting = new CircularFifoQueue<Hashtable<Meme,Double>>(nbEltCircularQFitting);
		cfqMemeApplianceNumberOnFitting = new CircularFifoQueue<Hashtable<Meme,Integer>>(nbEltCircularQFitting);
		memesAvailables = memeFactory.getMemeAvailable(false);
		currentNetProperties.createStub();
	}

	// Region Fitting running

	/** Initialisation des variables nécessaire a un fitting.
	 * Ecriture dans les fichiers, ouverture des répertoires.
	 *
	 */
	public void init(){

		targetNetProperties = networkFileLoader.getNetworkProperties();

		// ECRITURE
		repertoires = new ArrayList<String>(Arrays.asList("Stability"));
		DateFormat dateFormat = Configurator.getDateFormat();
		repertoires.add(dateFormat.format(new Date()));
		repOfTheSearch = null;
		String toWriteNormalCSV = "";
		String toWriteDetailCSV = "";

		if(Configurator.writeNetworkResultOnFitting)
		{
			// STEP: HEADER
			repOfTheSearch = writeNRead.createAndGetDirFromString(repertoires);
			String header = "Name";
			for (IModelParameter<?> model : explorator.getModelParameterSet())
				header += ";" + model.nameString();

			// STEP: NORMAL
			toWriteNormalCSV += header;
			toWriteNormalCSV += currentNetProperties.getCsvHeader(Configurator.activationCodeForScore);
			toWriteNormalCSV += ";moyenne des scores";
			toWriteNormalCSV += ";Variance des scores";
			writeNRead.writeSmallFile2(repOfTheSearch, "NetworkCSV", Arrays.asList(toWriteNormalCSV));

			// STEP: DETAILLED
			toWriteDetailCSV += header;
			for (Configurator.NetworkAttribType attrib : Configurator.NetworkAttribType.values()) {
				if(Configurator.isAttribActived(Configurator.activationCodeAllAttribExceptDD, attrib))
				{
					toWriteDetailCSV += ";" + attrib.toString();
					toWriteDetailCSV += ";" + "SD " + attrib.toString();
				}
			}


			toWriteDetailCSV += ";moyenne des scores";
			toWriteDetailCSV += ";Variance des scores";
			writeNRead.writeSmallFile2(repOfTheSearch, "NetworkDetailsCSV", Arrays.asList(toWriteDetailCSV));
		}
	}

	/** Nouveau tour. C a d nouvelle série de Run dans une configuration du modèle donnée.
	 *
	 */
	public void newRun(){
		numeroRun++;
		numeroTurn = 0;
		networksSameTurn.clear();

		numeroRunAsString = "Run#" + numeroRun;
		if(debug){
			System.out.println(numeroRunAsString + " applications des parametres: ");
			System.out.println(explorator.toString());
		}
		repertoires.add(numeroRunAsString);
	}

	/** Nouveau run, donc reset du network et pas de chg sur la config. courante du modèle.
	 *
	 */
	public void newTurn(){
		resetSeuilValue();
		numeroTurnAsString = "Repetition#" + ++numeroTurn;
		repertoires.add(numeroTurnAsString);
		com.suspend();
		synchronized(workerFactory.waitingForReset)
		{
			com.resetStuff();
		}

		currentSeed = new Random().nextLong();
		Toolz.setSeed(currentSeed);

		explorator.apply();
		// & (3) Application de ces paramètres
		if(Configurator.displayFittingProviderApplied && numeroTurn == 1 && !Configurator.jarMode) {
			System.out.println(numeroRunAsString + " applications des parametres: ");
			System.out.println(explorator.toString());
		}

		if(debug) System.out.println(numeroRunAsString + " at " + numeroTurnAsString  );
		entiteHandler.resetProba();
		turnCount = 0;
		cqLastXActionDone.clear();
		cfqMemeAppliancePropOnFitting.clear();
		cfqMemeApplianceNumberOnFitting.clear();

		cfqDensityValuesOnOneRun.clear();
		kvOverallProportionActionDone.clear();
		kvOverallNumberOfActionDone.clear();
		com.resume();
	}

	/** Fin du run, une fois que le réseau a atteint un cycle//stabilité // chaos.
	 * Enregistre les valeurs, sauvegarde et snapshot.
	 *
	 */
	public void endTurn(){
		turnCount = 0;
		// STEP: Concernant la continuité du fitting sur meme config.
		nbCallContinuOnThisConfig = 0;
		resetSeuilValue();

		// STEP: On prend les properties courantes pour calculer une distance avec le réseau cible
		networkConstructor.updateAllNetworkProperties();
		// passe par le tinyNetwork
		currentNetProperties = networkConstructor.getNetworkProperties();

		// TODO [WayPoint]- Score distance entre deux network
		currentNetworkScore = getNetworksDistanceDumb(Configurator.activationCodeForScore, targetNetProperties, currentNetProperties);

		// Sauvegarde des propriétés courantes du réseau TODO [refact2.0] quelque chose a voir par ici pour le score total
		// idée: distance entre les score des différents run exponentiel
		networksSameTurn.add(currentNetProperties);

		// STEP: Remplissage de la classe résultat
		String configAsString = "";
		for (IModelParameter<?> model : explorator.getModelParameterSet()){
			configAsString += model.valueString();
		}

		resultNetwork.addScore(numeroRun, configAsString, currentNetworkScore, networkConstructor.getNetworkProperties().getCopyMe());
		com.takeSnapshot(currentSeed, Optional.ofNullable(repertoires));
		repertoires.remove(numeroTurnAsString);

		if(Configurator.writeNetworkResultOnFitting)
			// Va écrire les résultats détaillés dans le CSV correspondant
			resultNetwork.writelastTurnOnDetailCSV(repOfTheSearch);
	}

	/** Fin du tour, enregistre les variances sur les résultats des différents run sur meme config,
	 *
	 *
	 */
	public void endRun(){
		repertoires.remove(numeroRunAsString);
		if(Configurator.writeNetworkResultOnFitting)
			resultNetwork.writeLastRunOnCSV(repOfTheSearch, Configurator.activationCodeForScore);

		// ici, ecrire: La chart de density sur les 4 runs, le fichier de config des runs?
//		explorator.rememberNetwork(currentNetworkId);
		com.view.resetPlotDensity();
	}

	/** Fin de la simulation, affiche le résultat final.
	 *
	 */
	public void endSimu(){
		resultNetwork.displayResult();
		if(!Configurator.jarMode)
			resultNetwork.displayPolar();
	}

	/** Ajout d'une densité à l'issu d'un relevé. C'est avec l'ensemble des densités
	 * récoltés ici qu'on définit la stabilité d'un réseau ou non.
	 *
	 * @param density
	 */
	public void addDensity(double density){
		cfqDensityValuesOnOneRun.add(density);
	}

	/** appelée régulièrement pour vérifier l'avancé de l'usage des memes sur la simulation.
	 * cqLastXActionDone est mise a jour a chaque action effectivement appliquée. Size de
	 * circularSize, 200
	 */
	public void computeMemeAppliance(){
		int nbAction = 0;
		synchronized(cqLastXActionDone){
			for (Meme meme : cqLastXActionDone) {
				Toolz.addCountToElementInHashArray(kvLastActionDone, meme, 1);
				nbAction++;
			}

			// Si pas mise a zero, et que les 500 tentatives d'action dans la boucle principale
			// echoue, le prochain relevé de proportion restera identique. TODO[Refact3.0] nouveau, à bien tester
			cqLastXActionDone.clear();
		}

		for (Meme meme : kvLastActionDone.keySet()) {
			kvOverallProportionActionDone.put(meme, (double)kvLastActionDone.get(meme) / nbAction);
			kvOverallNumberOfActionDone.put(meme, kvLastActionDone.get(meme));
		}

		kvLastActionDone.clear();
	}

	// EndRegion

	// Region Fitting hardstuff

	/** Version utilisée pour savoir s'il faut continuer a faire du fitting ou si on a
	 * attend une stabilité.
	 *
	 * @return
	 */
	public boolean continuFittingCleanVersion(){

		boolean useDensity = true;
		boolean useMemeAppliance = true;

		boolean stepByStep = false;
		boolean debug = Configurator.debugFittingClass && !Configurator.jarMode;
		boolean fastDebug = Configurator.debugFittingClassFast && !Configurator.jarMode;


		boolean oneMoreTurn = true;
		boolean canStillApplyAction;
		boolean seDensity = false, heDensity = false, seCoeff = false, heCoeff = false, seAppliance = false, heAppliance = false;
		double scoreEcartDensite = 100, scoreCoeffDirDensite = 100, scoreTotal = 0, scoreMemeAppliance = 100, seuilScore = 0;

		ObjectRef<String> message = new ObjectRef<String>("");
		String resume = "";

		nbCallContinuOnThisConfig++;
		if(nbCallContinuOnThisConfig % nbActionThreshRising == 0)
			riseThresholdValue(nbCallContinuOnThisConfig);

		// REGION Vérification état du network

		// STEP Verification de la possibilité de jouer des actions
		canStillApplyAction = readingActionCanContinue(message);
		resume += message.getValue();
		// TODO Refact[3.0] que se passe t'il pour le score quand aucune action ne peut etre appliquée?

		// Si: pas de probleme avec les actions
		if(canStillApplyAction){

			// STEP Variation de la densité.
			// Dans le cas ou il est possible de jouer des actions & assez de donnée on regarde la variation de densité
			if(useDensity)
			{
				// meanDensityValueOnOneRun est mise a jour dans la boucle du fitting avant l'appel de cette fonction
				scoreEcartDensite = readingDensityVariation(new ArrayList<Double>(Arrays.asList(cfqDensityValuesOnOneRun.toArray(new Double[1]))), message) ;
				resume += message.getValue();
				scoreTotal += scoreEcartDensite;
				scoreCoeffDirDensite = readingDensityCoeffDir(new ArrayList<Double>(Arrays.asList(cfqDensityValuesOnOneRun.toArray(new Double[1]))), message) ;
				resume += message.getValue();
				scoreTotal += scoreCoeffDirDensite;
			}

			// STEP Variation de l'application des memes.
			if(useMemeAppliance)
			{
				scoreMemeAppliance = readingMemeAppliance(false, message);
				resume += message.getValue();
				scoreTotal += scoreMemeAppliance;
			}
		}

		// EndRegion

		// Region STEP Utilisation des scores

		if(canStillApplyAction)
		{
			// Density
			if(useDensity)
			{

				if(debug) System.out.println("Ecart densité " + scoreEcartDensite + " Coeff directeur " + scoreCoeffDirDensite );

				// Définition du hard et soft end pour l'écart a la densité
				if(scoreEcartDensite < threshSeDensity)
					seDensity = true;
				if(scoreEcartDensite < threshHeDensity)
					heDensity = true;

				// Définition du hard et soft pour le coeff directeur
				if(scoreCoeffDirDensite < threshSeCoeff)
					seCoeff = true;
			}

			// Appliance
			if(useMemeAppliance)
			{
				if(debug) System.out.println("Meme appliance: " + scoreMemeAppliance);
				// Définition du hard et soft pour le meme appliance
				if(scoreMemeAppliance < (memesAvailables.size() * threshSeAppliance))
					seAppliance = true;
				if(scoreMemeAppliance < (memesAvailables.size() * threshHeAppliance))
					heAppliance = true;
			}
		}

		// EndRegion

		// Region STEP Return

		// HARDEND
		if(!canStillApplyAction)
		{
			if(fastDebug) System.out.println("[ContinuFitting] Plus d'action réalisable");
			if(!stepByStep) oneMoreTurn = false;
		}

		if(useDensity)
			if(heCoeff || heDensity)
			{
				if(!stepByStep) oneMoreTurn = false;
				if(fastDebug) System.out.println("Hard End sur density " + (heCoeff ? "coeff" : "ecart type"));
			}

		if(useMemeAppliance)
			if(heAppliance)
			{
				if(!stepByStep) oneMoreTurn = false;
				if(fastDebug) System.out.println("Hard End sur appliance ");
			}

		// SOFTEND
		if( (useDensity ? (seDensity && seCoeff) : true) && ( useMemeAppliance ? (seAppliance) : true)  ){
			if(!stepByStep) oneMoreTurn = false;
			if(fastDebug) System.out.println("Soft end sur tout le monde");
		}

		// SOMME DES SCORES
		seuilScore += (useDensity ? threshHeDensity + threshHeAppliance : 0) + (useMemeAppliance ? threshHeAppliance: 0 );
		if(scoreTotal < seuilScore){
			if(!stepByStep) oneMoreTurn = false;
			if(fastDebug) System.out.println("Somme des scores inférieur a somme des SE");
		}

		if(debug && !oneMoreTurn)  System.out.println( resume );

		// EndRegion

		return oneMoreTurn;
	}

	/** Verification de la possibilité de jouer des actions valeur pas relevé de
	 *  facon synchronisé, le but étant de savoir si une action est encore possible ou si le systeme est bloqué.
	 *
	 * @param message
	 * @return
	 */
	private boolean readingActionCanContinue(ObjectRef<String> message){
		String resume = "\n [ReadingActionCanContinu]- ";
		boolean resultat = true;
		nbActionLocal = getNbAction();
		if(nbActionLocal == 0){
			// Et si aucune action n'a pu etre forcée a être faite
			if(!entiteHandler.forceAction()){
				resultat = false;
				resume += "[ActionReading] - Plus d'action possible dans ce réseau";
			}
		} // Si on a réussi a faire faire une action
		else{
			resume += "[ActionReading] - passé par un soft end d'actions réalisées";
			resetAction();
		}

		message.setValue(message.getValue() + resume);
		return resultat;
	}

	/** utilise l'écart type sur la moyenne a la densité comme indicateur.
	 *
	 * @param densities
	 * @param message
	 * @return
	 */
	private double readingDensityVariation(ArrayList<Double> densities,	ObjectRef<String> message){
		String resume = "\n [ReadingDensityVariation]- ";

		// Calcul de la moyenne des densités de de l'écart type à la moyenne
		double variationOnDensity = Toolz.getDeviation(densities, Optional.ofNullable(null));

		resume += "\n [Density] - Density ecart type " + variationOnDensity + " sur les values suivantes \n";
		resume += densities;

		message.setValue(message.getValue() + resume );
		// Le score max. sur l'écart type a la densité est .5. on veut un score sur 100.
		return variationOnDensity * 200;
	}

	/** utilisation de la variation des coefficients directeurs sur des plages de données
	 *
	 * @param densities
	 * @param message
	 * @return
	 */
	private double readingDensityCoeffDir(ArrayList<Double> densities, ObjectRef<String> message){
		String resume = "\n [ReadingDensityCoeff]- ";
		int sizeChunk = 1;
		int nbChunk = (int)(densities.size() / sizeChunk);
		double firstValue = densities.get(0);
		double valueOne, valueTwo;
		valueOne = firstValue;
		ArrayList<Double> fenetreGlissante = new ArrayList<Double>();
		ArrayList<Double> fenetreAgrandissante = new ArrayList<Double>();
		double distanceTotale = 0;
		double score; //, scoreOne, scoreTwo, scoreThree;

		for (int i = 1; i < nbChunk + 1 ; i++) {
			valueTwo = densities.get((i * sizeChunk)-1);

			// Part [T0;T1] U [T1;T2]
			fenetreGlissante.add((valueTwo - valueOne) / valueOne);

			// Part [T0;T1] U [T0;T2]
			fenetreAgrandissante.add((valueTwo - firstValue) / firstValue);

			// Part somme des distance pour voir a quel point on fait des zig zag
			distanceTotale += Math.abs(valueTwo - valueOne);

			valueOne = valueTwo;
		}

		resume += "Glissante " + fenetreGlissante;
		resume += "Agrandissante " + fenetreAgrandissante;
		resume += "Chemin court " + Math.abs((densities.get(densities.size() - 1) - densities.get(0))) + "Distance Totale " + distanceTotale ;
		message.setValue(message.getValue() + resume);

		// On compute les scores. On utilise le rapport entre l'élévation de densité entre le premier relevé et le dernier,
		// diviser par le nombre d'élévation cumulé step apres step, en valeur absolu.  i.e. la taille parcouru par un stylo si il
		// suit la courbe ou si il va directement du minimum au maximum
		score = (1 + Math.abs((densities.get(densities.size() - 1) - densities.get(0)))) / (1 + distanceTotale);

		// Le score obtenu est forcement inférieur a 1, les cas limite étant : une chart rectiligne, auquel cas l'élévation
		// cumulé et celle direct est égal, donc score = 1. autre cas limite : variation qui tend vers l'infini ( comme une
		// courbe a haute fréquence), mais une variation moyenne nul. le score tend alors vers 0.
		// on veut un score entre 0 et 100. on l'inverse pour avoir le score max dans le plus mauvais cas, c a d grosse variation
		score = (1 - score)  * 100;
		// on fait un score en cuvette, centré sur 0.03.
		// Si trop bas, on est dans le cas ou l'evolution moyenne et partie par partie est quasiement équivalent,
		// donc on est dans un cas de croissance ou decroissance monotone, on ne veut pas arreter la simulation, donc il faut
		// renvoyer un gros score. L'autre extreme est quand il y a de grosse variation de densité partie par partie. On ne veut pas
		// non plus arreter la simulation car situation trop chaotique. Le cas "moyen" semble etre centré pour un score à .3 ( ou
		// .03?) donc on veut renvoyer un score final minimum a ce score la, donc utilisation d'une fonction un peu compliquée
		// pour obtenir l'effet cuvette
		score = 1 / Math.pow(score+.0998, 2) + Math.exp(score / 21.7) - 1.25;

		// Le score ne prend donc en compte que la variation moyenne et la somme des variations partie sur partie
		// pas de fenetre glissante etc
		return score;
	}

	/** Utilisation des données sur les actions jouées par les entitées.
	 * Fonction écrite pour pouvoir utiliser les proportions d'applications OU l'écart type d'application d'un meme
	 * sur les X derniers appels. Aucune fonction de score pour l'utilisation de nombre de meme appliqué
	 * Entre chaque appel de cette fonction 500 actions sont lancé dans le fitting ( pas necessairement
	 * sucessful )
	 * @param displayEcartTypeOnGraph si true display l'écart type, sinon display les proportions d'application par meme
	 * @param message
	 * @return
	 */
	private double readingMemeAppliance(boolean displayEcartTypeOnGraph, ObjectRef<String> message){

		// Le relevé des memes appliances est fait dans la boucle de fitting donc pas de risque qu'elle soit modifier
		// pendant l'execution de la fonction en cours.
		String resume = "\n [ReadingMemeAppliance] - ";
		double score = 0;

		// on releve des données sur la proportion joué des actions, zero si pas sur la map.
		int numberCall;
		double propCall ;

		// Proportion des memes joués sur un step de lecture TODO [Refact.2] tte ne sont pas forcement nécessaires
		Hashtable<Meme, Double> kvMemePropForOneStep = new Hashtable<Meme, Double>(memesAvailables.size());
		Hashtable<Meme, Integer> kvMemeNumberForOneStep = new Hashtable<Meme, Integer>(memesAvailables.size());
		Hashtable<Meme, ArrayList<Double>> kvMemePropForNStep = new Hashtable<Meme, ArrayList<Double>>(memesAvailables.size());
		Hashtable<Meme, ArrayList<Integer>> kvMemeNumberForNStep = new Hashtable<Meme, ArrayList<Integer>>(memesAvailables.size());

		// On regarde sur les CircularSize dernieres actions jouées la proportion pour un meme, copie en local
		// et mis a zero si le meme n'est jamais joué. // TODO [Refact.2] DES STREAMS
		// valeur des kv mise a jour régulièrement par la fonction computeMemeAppliance()
		// qui vide
		for (Meme meme : memesAvailables) {
			if(kvOverallProportionActionDone.containsKey(meme)){
				propCall = kvOverallProportionActionDone.get(meme);
				numberCall = kvOverallNumberOfActionDone.get(meme);
			}
			else{
				propCall = 0;
				numberCall = 0;
			}

			kvMemeNumberForOneStep.put(meme, numberCall);
			kvMemePropForOneStep.put(meme, propCall);
		}

		// Ajout dans une circular queue qui sera rempli pendant les appel de cette fonction sur le meme run
		// size : @nbEltCircularQFitting, 15
		cfqMemeAppliancePropOnFitting.add(kvMemePropForOneStep);
		cfqMemeApplianceNumberOnFitting.add(kvMemeNumberForOneStep);

		// contient les prop. d'application ou l'écart type de l'application d'un meme
		Hashtable<Meme, Double> kvEcartTypeOrPropMemeAppliance = new Hashtable<Meme, Double>();

		// Dans le cas ou on veut l'écart type de l'application d'un meme par rapport à sa propre moyenne
		// prends les sizeCircular derniers
		if(displayEcartTypeOnGraph){
			// Pour chaque hashset d'un step || on regarde le nombre d'applications
			for (Hashtable<Meme,Integer> setOfMemeNumberForOneStep : cfqMemeApplianceNumberOnFitting){
				// on regarde chaque meme a chaque step le nombre de fois ou il a été appelé
				for (Meme meme : setOfMemeNumberForOneStep.keySet()){
					Toolz.addElementInHashArray(kvMemeNumberForNStep, meme, setOfMemeNumberForOneStep.get(meme));
					kvEcartTypeOrPropMemeAppliance.put(meme, Toolz.getDeviation(kvMemeNumberForNStep.get(meme), Optional.ofNullable(null)));
				}
			}
		}

		// Lecture des N dernier prop de meme et placement dans une liste pour analyser les écarts
		else {
			for (Hashtable<Meme,Double> hashtable : cfqMemeAppliancePropOnFitting) {
				for (Meme meme : hashtable.keySet()) {
					Toolz.addElementInHashArray(kvMemePropForNStep, meme, hashtable.get(meme));
				}
			}

			for (Meme meme : kvMemePropForNStep.keySet()) {
				kvEcartTypeOrPropMemeAppliance.put(meme, kvMemePropForOneStep.get(meme));
			}
		}

		// STEP Qualification de la stabilité. Utilise la somme en valeur non absolue de la différente entre la moyenne
		// et les éléments qui la composent afin de déterminer la stabilite. SUR LES proportions d'applications
		Hashtable<Meme, Double> stabilityByMeme = new Hashtable<Meme, Double>(memesAvailables.size());
		for (Meme meme : memesAvailables) {
			if(kvMemePropForNStep.get(meme) != null)
			{
				stabilityByMeme.put(meme, getIndiceEvolutionForMemeAppliance(kvMemePropForNStep.get(meme), .3));
			}
		}

		// Verification des facteurs de stabilité. Un score entre 0 et 100 doit etre renvoyer
		// 4 score empirique qui dénote une grosse instabilité de variation de prop. d'application
		// de meme. la fonction getIndiceEvolution renvoie 4 si pas assez de valeur pour regarder
		// l'évolution
		double maxValue = 4.;
		double localScore ;

		for (Meme meme : stabilityByMeme.keySet()) {
			localScore = Math.abs(stabilityByMeme.get(meme)) < maxValue ? Math.abs(stabilityByMeme.get(meme)) : maxValue;
			// la valeur maximum d'un score pour un meme est (100 / nbMeme) pour que le score final en cas d'instabilité
			// maximum pour tous les memes a été sommé à 100.
			localScore = localScore * (100 / (memesAvailables.size() * maxValue)) ;
			resume  += ("Meme "+ entiteHandler.translateMemeCombinaisonReadable(meme.toFourCharString())
					+ " score normalisé entre 0 et "+ 100 / memesAvailables.size() +" = " + localScore);
			score += localScore;
		}

		// Affichage dans la fenetre de l'évolution des meme appliances
		Hashtable<Integer, Double>  kvMemeValue = new Hashtable<Integer, Double>() ;
		for (Meme meme : kvEcartTypeOrPropMemeAppliance.keySet())
			kvMemeValue.put(memeFactory.getColorIndex(meme), kvEcartTypeOrPropMemeAppliance.get(meme));
		com.view.addValueToApplianceSerie(nbCallContinuOnThisConfig, kvMemeValue);

		message.setValue(message.getValue() + resume);
		return score;
	}

	/** Renvoi un indice d'évolution d'un coefficient directeur dont le départ se fait sur la moyenne des
	 * @pourcentageFirstTerm premiers éléments de la série.
	 *
	 * @param listValue
	 * @param pourcentageFirstTerm pourcentage, borné a 50%
	 * @return 0 si la courbe est également réparti de part et d'autre de la moyenne des x premiers points
	 * un abs(score) qui augmente si la courbe monte ou descend par rapport a sa moyenne. Score peu etre négatif.
	 */
	private double getIndiceEvolutionForMemeAppliance(ArrayList<Double> listValue, double pourcentageFirstTerm){
		double score = 4;
		if(listValue.size() > 3){
			score = 0;
			int indexBorne;
			double mean = 0;
			if(pourcentageFirstTerm > .5)
				pourcentageFirstTerm = .5;

			indexBorne = (int)(listValue.size() * pourcentageFirstTerm);
			for (int i = 0; i < indexBorne; i++) {
				mean += listValue.get(i);
			}

			mean /= (indexBorne);

			for (Double value : listValue) {
				score += value - mean;
			}
		}
		return score;
	}

	// EndRegion

	/** Methode la plus simple pour trouver la distance entre deux réseaux.
	 *
	 * @param activationCode
	 * @param targetNetworkProperty
	 */
	public static double getNetworksDistanceDumb(int activationCode, NetworkProperties targetNetworkProperty, NetworkProperties currentNetworkProperty) {
		// variation sur nombre le nombre de temps un lien dur, et sur le
		// pourcentage d'evap necessité :augmenter le nombre de paramètre regardé.
		double currentDistance = 0;
		double totalDistance = 0;
		NetworkAttribType attribut;

		// On regarde sur tout les attributs de réseau ceux qui ont été activé
		// pour le calcul de distance entre deux réseaux
		for (int i = 0; i < Configurator.NetworkAttribType.values().length; i++) {
			attribut = NetworkAttribType.values()[i];
			if(Configurator.isAttribActived(activationCode, attribut))
			{
				// Calcul de la distance entre deux network sur ce critère
				Object attributValueForCurrentNetwork = currentNetworkProperty.getValue(attribut);
				Object attributValueForTargetNetwork = targetNetworkProperty.getValue(attribut);
				currentDistance = getAttributDistance(attribut,	attributValueForCurrentNetwork,	attributValueForTargetNetwork);
				totalDistance += currentDistance;
			}
		}

		return totalDistance;
	}

	/** retourne une distance entre deux attributs
	 * Comprise entre 0 et 100
	 * @param type
	 * @param valueFrom
	 * @param valueTarget
	 * @return
	 */
	private static double getAttributDistance(NetworkAttribType type, Object valueFrom, Object valueTarget){
		double distance = 0;double valueOne;double valueTwo;
		switch (type) {
			case DENSITY:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, 1);
				break;
			case DDAVG:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, Configurator.nbNode - 1);
				break;
			case DDINTERQRT:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, Configurator.nbNode - 1);
				break;
			case DDARRAY:
				break;
			case AVGCLUST:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, 1);
				break;

			case NBEDGES:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, (Configurator.nbNode - 1) * Configurator.nbNode);
				break;
			case NBNODES:
				break;
			case APL:
				break;
			case nbEdgesOnNbNodes:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, 1);
				break;
			default:
				break;
		}

		return distance;
	}

	/** Donne la distance entre deux éléments, relative au max des ces éléments
	 *
	 * @param valueOne
	 * @param valueTwo
	 * @param max
	 * @return
	 */
	private static double distance(double valueOne, double valueTwo, double max){
		return (Math.abs(valueOne - valueTwo) * 100) / max;
	}

	/** Reset des seuils qui définissent la fin d'un round de fitting
	 *
	 */
	private void resetSeuilValue(){
		threshSeDensity = 0.5 ;
		threshHeDensity = 0.001;
		threshSeCoeff = 0.01;
//		threshHeCoeff = 0.001;
		threshSeAppliance = 0.3;
		threshHeAppliance = 0.1;
	}

	/** Augmente le treshold des valeurs pour les soft et hard fin de run
	 *
	 */
	private void riseThresholdValue(int nbStepDoneInSameRun){
		threshSeDensity *= (1 + risingPourcentage) ;
		threshHeDensity *= (1 + risingPourcentage) ;
		threshSeCoeff *= (1 + risingPourcentage) ;
//		threshHeCoeff *= (1 + risingPourcentage) ;
		threshSeAppliance *= (1 + risingPourcentage) ;
		threshHeAppliance *= (1 + risingPourcentage) ;
		if(debug) System.out.println("Rise des threshold");
	}

	// Region Ensemble de méthode pour compter le nombre d'action réalisée dans le network.

	/** Implemente ces handler d'entités pour pouvoir compter le nombre
	 * d'actions réalisées afin d'etre en mesure de connaitre la progression du network.
	 *
	 */
	public void handlerActionApply(ActionApplyEvent e) {
		if(!e.message.contains("NOACTION") ){
			if(!e.message.contains("Nope")){
				newAction();
				synchronized(cqLastXActionDone){ cqLastXActionDone.add(e.memeApply); }
			}
		}
	}
	public void handlerBehavTransm(BehavTransmEvent e) {
		newBehaviorTransmitted();
	}
	public void newAction(){
		synchronized(nbActionCount){
			nbActionCount++;
		}
	}
	public void resetAction(){
		synchronized(nbActionCount){
			nbActionCount = 0;
		}
	}
	public int getNbAction(){
		synchronized(nbTransmissionCount){
			return nbActionCount;
		}
	}
	public void newBehaviorTransmitted(){
		synchronized(nbTransmissionCount){
			nbTransmissionCount++;
		}
	}
	public void newresetTransmitted(){
		synchronized(nbTransmissionCount){
			nbTransmissionCount = 0;
		}
	}
	public int getNbBehaviorTransmitted(){
		synchronized(nbTransmissionCount){
			return nbTransmissionCount;
		}
	}

	// EndRegion

}
	
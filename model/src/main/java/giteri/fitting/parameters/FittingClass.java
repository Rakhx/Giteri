package giteri.fitting.parameters;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.*;

import giteri.fitting.algo.Result;
import giteri.fitting.algo.ResultSet;
import giteri.meme.event.*;
import giteri.network.event.INbNodeChangedListener;
import giteri.network.event.NbNodeChangedEvent;
import giteri.run.interfaces.Interfaces;
import giteri.run.interfaces.Interfaces.IUnitOfTransfer;
import giteri.tool.math.Toolz;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.network.NetworkProperties;
import giteri.network.networkStuff.CommunicationModel;
import giteri.network.networkStuff.NetworkConstructor;
import giteri.network.networkStuff.NetworkFileLoader;
import giteri.network.networkStuff.WorkerFactory;
import giteri.tool.objects.ObjectRef;

import giteri.tool.other.StopWatchFactory;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import giteri.tool.other.WriteNRead;
import giteri.fitting.algo.IExplorationMethod;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.NetworkAttribType;
import giteri.meme.entite.EntiteHandler;
import giteri.meme.entite.Meme;


/**
 * Classe de fonctionnement pour lancer un fitting ou autre recherche de stabilité.
 * Simu, composé de Run aux configurations changeante, composé de repétition sur un set de parametre fixe.
 * Run: Lancement d'un simulation en partant de zero niveau
 * meme, état du réseau, etc
 * Repetition
 * Step: Pendant un run, interval de temps entre deux mesures
 * Config // set de parametre : valeur des paramètres de configuration
 * de la simulation. Ne change pas pendant un run.
 */
public class FittingClass implements IBehaviorTransmissionListener, IActionApplyListener,
		INbNodeChangedListener, IMemeAvailableListener {

	//region Variables diverses
	private EntiteHandler entiteHandler;
	private MemeFactory memeFactory;
	private NetworkFileLoader networkFileLoader;
	private WriteNRead writeNRead;
	private WorkerFactory workerFactory;
	private NetworkConstructor networkConstructor;
	public CommunicationModel com ;
	public IExplorationMethod explorator;

	// CONFIGURATION INITIALE

	// Nombre de fois ou on lance un run pour une meme config.
	public int nbRepetitionByConfig;
	// Nombre d'action réalisé par les entités avant une collecte de données
	public int nbActionByStep = Configurator.getNbNode() * 10;
	public int boucleExterneSize = 30;
	public int nbActionBeforeQuit = Configurator.fixedNbAction ? 100000 :
			Configurator.multiplicatorNbAction * Configurator.getNbNode();

	// VARIABLES DE FONCTIONNEMENT

	// numero du run en cours
	public int numeroRun = -1;
	public String numeroRunAsString = "-1";

	// numéro de répétition dans le run
	public int numeroRepetition = -1;
	public String numeroRepetitionAsString = "-1";

	// Compteur du nombre de relevé fait pour un meme run
	public int turnCount = 0;
	File repOfTheSearch ;
	private Integer nbActionCount = 0; // Nombre d'action sur la répétition
	private Integer nbActionCountLocal = 0; // Nombre d'action depuis le dernier reset; plusieurs reset par répétition
	private Integer nbTransmissionCount = 0;
	public long currentSeed ;

	public int circularSize = 200;
	// Cette liste est changée pendant le fitting à chaque event action
	public CircularFifoQueue<IUnitOfTransfer> cqLastXActionDone;

	// Ces deux listes sont utilisées dans la boucle la plus interne du fitting, donc chaque nbActionByStep
	private Hashtable<IUnitOfTransfer, Integer> kvLastActionDone;
	private Hashtable<IUnitOfTransfer, Double> kvOverallProportionActionDone;
	private Hashtable<IUnitOfTransfer, Integer> kvOverallNumberOfActionDone;

	// Liste qui sont utilisées pour définir si on s'arrete de fitter sur une configuration ou non
	private CircularFifoQueue<Hashtable<IUnitOfTransfer, Double>> cfqMemeAppliancePropOnFitting;
	private CircularFifoQueue<Hashtable<IUnitOfTransfer, Integer>> cfqMemeApplianceNumberOnFitting;
	private int nbEltCircularQFitting = 15;

	private ArrayList<IUnitOfTransfer> memesAvailables;
	private int nbCallContinuOnThisConfig = 0;

	// RESULTATS DE SIMULATION

	// relevés des différentes densité pour une meme configuration
	public List<NetworkProperties> networksSameTurn = new ArrayList<>();

	NetworkProperties targetNetProperties;
	NetworkProperties currentNetProperties = new NetworkProperties();

	public double currentNetworkScore;
	ArrayList<String> repertoires ;
	ResultSet resultNetwork;

	//endregion

	//region constructeur et init

	/**	Constructeur.
	 *
	 */
	public FittingClass(){
	}

	/**
	 * Besoin de la référence de la fitting avant pour les listeners...
	 *
	 * @param wnr
	 * @param com
	 * @param memeF
	 * @param nfl
	 * @param wf
	 * @param eh
	 * @param nc
	 * @param explorator
	 */
	public void KindaConstructor(WriteNRead wnr, CommunicationModel com, MemeFactory memeF,
							NetworkFileLoader nfl, WorkerFactory wf, EntiteHandler eh, NetworkConstructor nc, IExplorationMethod explorator){
		resultNetwork = new ResultSet(wnr);
		this.com = com;
		this.memeFactory = memeF;
		this.writeNRead = wnr;
		this.networkFileLoader = nfl;
		this.workerFactory = wf;
		this.entiteHandler = eh;
		this.networkConstructor = nc;
		this.explorator = explorator;
		setDefaultValue();
	}

	/**
	 * Mise en place des valeurs par défault pour les variables d'utilisation
	 *
	 */
	private void setDefaultValue(){
		nbRepetitionByConfig = Configurator.nbRepetitionbyRun;
		nbActionByStep = Configurator.getNbNode() * 10;

		cqLastXActionDone = new CircularFifoQueue<>(circularSize);
		kvLastActionDone = new Hashtable<>();
		kvOverallProportionActionDone = new Hashtable<>();
		kvOverallNumberOfActionDone = new Hashtable<>();
		cfqMemeAppliancePropOnFitting = new CircularFifoQueue<>(nbEltCircularQFitting);
		cfqMemeApplianceNumberOnFitting = new CircularFifoQueue<>(nbEltCircularQFitting);
		currentNetProperties.createStub();
	}

	//endregion

	//region Fitting turn & run

	/**
	 * Initialisation des variables nécessaire à un fitting.
	 * Ecriture dans les fichiers, ouverture des répertoires.
	 *
	 */
	public void init(){
		// Si openMole, lire les propriétés du réseau cible depuis le fichier serialisé
		if(Configurator.typeOfConfig == Configurator.EnumLauncher.jarOpenMole ||
		Configurator.typeOfConfig == Configurator.EnumLauncher.jarC)
			targetNetProperties = networkFileLoader.getNetworkProperties(true,false);

		// CHEAT CODE
		else // Sinon, les lire depuis le fichier donné en paramètre dans l'interface
			targetNetProperties = networkFileLoader.getNetworkProperties(true,false);

		if(Configurator.prepareTheOpti){
			boolean dog = (Configurator.typeOfConfig == Configurator.EnumLauncher.jarOpenMole) || (Configurator.typeOfConfig == Configurator.EnumLauncher.jarC);
			System.out.println("FittingClass.Init() - Fin de lecture du fichier cible " + (dog? "serialise" : "non sériealisé"));
		}

		boolean doTheWrite = !Configurator.fullSilent;

		// ECRITURE
		repertoires = new ArrayList<>(Arrays.asList(Configurator.repForFitting));
		DateFormat dateFormat = Configurator.getDateFormat();
		repertoires.add(dateFormat.format(new Date()));
		repOfTheSearch = null;
		String toWriteNormalCSV = "";
		StringBuilder toWriteDetailCSV = new StringBuilder();
		String toWriteMemeCSV = "";
        String toWriteConfiguratorFile = "";

		// region Result
        // Creation des fichiers CSV avec entete
		if(Configurator.writeNetworkResultOnFitting)
		{
			// STEP: HEADER
			repOfTheSearch = writeNRead.createAndGetDirFromString(repertoires);
			StringBuilder header = new StringBuilder("Name");
			for (IModelParameter<?> model : explorator.getModelParameterSet())
				header.append(";").append(model.nameString());

			// STEP: NORMAL
			toWriteNormalCSV += header;
			toWriteNormalCSV += currentNetProperties.getCsvHeader(Configurator.activationCodeForScore);
			toWriteNormalCSV += ";moyenne des scores";
			toWriteNormalCSV += ";Variance des scores";
			if(doTheWrite)
			writeNRead.writeSmallFile(repOfTheSearch, Configurator.fileNameCsvSimple,
					Collections.singletonList(toWriteNormalCSV));

			// STEP: DETAILLED
			toWriteDetailCSV.append(header);
			for (Configurator.NetworkAttribType attrib :
					currentNetProperties.getActivatedAttrib(Configurator.activationCodeAllAttrib)) {
					toWriteDetailCSV.append(";").append(attrib.toString());
					toWriteDetailCSV.append(";" + "SD ").append(attrib.toString());
			}

			toWriteDetailCSV.append(";scores");
			toWriteDetailCSV.append(";moyenne des scores");
			toWriteDetailCSV.append(";Variance des scores");
			if(doTheWrite)
			writeNRead.writeSmallFile(repOfTheSearch, Configurator.fileNameCsvDetail,
					Collections.singletonList(toWriteDetailCSV.toString()));
		}
		// endregion

        //region fichier configurator
		toWriteConfiguratorFile = "Champs;Valeurs";
		if(doTheWrite)
			writeNRead.writeSmallFile(repOfTheSearch,"configurator",
					Collections.singletonList(toWriteConfiguratorFile));

		Class<?> conf = new Configurator().getClass();
        if(doTheWrite)
        for (int i = 0; i < conf.getFields().length; i++) {
            Field fieldOne = conf.getFields()[i];
            Annotation ae = fieldOne.getAnnotation(Configurator.toOutput.class);
            if(ae != null && ((Configurator.toOutput) ae).yes() == true ){
                try {
                    writeNRead.writeSmallFile(repOfTheSearch,"configurator",
                            Collections.singletonList(fieldOne.getName()+";"+fieldOne.get(fieldOne)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        //endregion
	}

	/**
	 * Nouveau tour. C a d nouvelle série de Run dans une configuration du modèle donnée.
	 *
	 */
	public void newRun(){
		numeroRun++;
		numeroRepetition = 0;
		networksSameTurn.clear();
		numeroRunAsString = "Run#" + numeroRun;
		repertoires.add(numeroRunAsString);
		resultNetwork.put(numeroRun, new Result(explorator.getModelParameterSet()));
		com.view.resetPlotDensity();

	}

	/**
	 * Nouvelle répétition d'une configuration donnée.
	 *
	 *
	 */
	public void newRepetition(){
		resetAction();
		resetActionLocal();
		numeroRepetitionAsString = "Repetition#" + ++numeroRepetition;
		repertoires.add(numeroRepetitionAsString);

		synchronized(workerFactory.waitingForReset)
		{
			com.resetStuff();
		}

		currentSeed = new Random().nextLong();
		Toolz.setSeed(currentSeed);

		com.generateGraph(Configurator.initialNetworkForFitting);
		explorator.apply();

		// Creation du fichier de detail de meme, HEADER. Avant l'ajout du repertoire de repetition
		if(Configurator.writeMemeResultOnFitting && numeroRepetition == 1&& !Configurator.jarMode)
			writeNRead.writeSmallFile(repOfTheSearch, Configurator.fileNameMeme,
					entiteHandler.memeProperties.getHeaderToWriteMemeDetails(
							entiteHandler.getKVMemeTranslate(), numeroRun, numeroRepetition, explorator));

		//repertoires.add(numeroRepetitionAsString);

		// Mise à jour des indicateurs de l'interface ( meme disponible a afficher )
		// com.setViewMemeAvailable(memesAvailables);

		entiteHandler.updateMemeAvailableForProperties();

		// & (3) Application de ces paramètres
		if(Configurator.displayFittingProviderApplied && numeroRepetition == 1 && !Configurator.jarMode) {
			System.out.println(numeroRunAsString + " applications des parametres: ");
			System.out.println(explorator.toString());
		}

		if(Configurator.debugFittingClass) System.out.println(numeroRunAsString + " at " + numeroRepetitionAsString);

		entiteHandler.resetProba();
		// STEP: Concernant la continuité du fitting sur meme config.
		turnCount = 0;
		nbCallContinuOnThisConfig = 0;
		cqLastXActionDone.clear();
		cfqMemeAppliancePropOnFitting.clear();
		cfqMemeApplianceNumberOnFitting.clear();
		kvOverallProportionActionDone.clear();
		kvOverallNumberOfActionDone.clear();

		com.resume();
	}

	/** Fin du répétition, une fois que le réseau a atteint un cycle//stabilité // chaos.
	 * Enregistre les valeurs, sauvegarde et snapshot.
	 *
	 */
	public void endRepetition(){
		com.suspend();

		// STEP: On prend les properties courantes pour calculer une distance avec le réseau cible
		networkConstructor.updatePreciseNetworkProperties(Configurator.activationCodeAllAttrib);

		// passe par le tinyNetwork
		currentNetProperties = networkConstructor.getNetworkProperties();
		currentNetProperties.name = "Construit";
		// Sauvegarde des propriétés courantes du réseau
		// TODO [refact2.0] quelque chose a voir par ici pour le score total
		// idée: distance entre les score des différents run exponentiel
		networksSameTurn.add(currentNetProperties.Clone());

		// TODO [WayPoint]- Score distance entre deux network
		currentNetworkScore = getNetworksDistanceDumb(resultNetwork, Configurator.activationCodeForScore, numeroRun,
				numeroRepetition,targetNetProperties, currentNetProperties);

		// LA FLEMME // TODO [WayPoint]- Prise en compte des noeuds seuls
		currentNetworkScore += networkConstructor.getNbNodeAlone();

		if(!Configurator.fullSilent)
			System.out.println("current score this conf:" + currentNetworkScore);

		// Ajout a la classe des resultSet un score et propriété d'un réseau
		resultNetwork.addScore(numeroRun, currentNetworkScore, currentNetProperties);

		if(Configurator.writeNetworkResultOnFitting)
			com.takeSnapshot(currentSeed, Optional.ofNullable(repertoires));

		repertoires.remove(numeroRepetitionAsString);

		// Va écrire les résultats détaillés dans le CSV correspondant
		if(Configurator.writeNetworkResultOnFitting)
			resultNetwork.writelastRepDetailCSV(repOfTheSearch,
					numeroRun, currentNetProperties, explorator);

		if(Configurator.writeMemeResultOnFitting) {
			writeNRead.writeSmallFile(repOfTheSearch, Configurator.fileNameMeme, Arrays.asList(
					entiteHandler.memeProperties.getStringToWriteMemeDetails(entiteHandler.getKVUOTTranslate())));
		}
	}

	/**
	 * Fin du tour, enregistre les variances sur les résultats des différents run sur meme config,
	 *
	 *
	 */
	public void endRun(){
		repertoires.remove(numeroRunAsString);
		if(Configurator.writeNetworkResultOnFitting)
			resultNetwork.writeLastRunOnCSV(repOfTheSearch, numeroRun,
					networksSameTurn, Configurator.activationCodeForScore);
		if(Configurator.writeNetworkResultOnFitting){
			writeNRead.writeSmallFile(repOfTheSearch,Configurator.fileNameMeme,
					entiteHandler.memeProperties.getCloserToWriteMemeDetails(entiteHandler.getKVMemeTranslate()));

		}


		// ici, ecrire: La chart de density sur les 4 runs, le fichier de config des runs?
//		explorator.rememberNetwork(currentNetworkId);

		// com.view.resetPlotDensity();
	}

	/**
	 * Fin de la simulation, affiche le résultat final.
	 *
	 */
	public double endSimu(){
		double res = resultNetwork.displayResult();
		return res;
	}

	/** Condition d'arrêt pour une itération d'une configuration.
	 * En nombre d'action, sans autre paramètre...
	 *
	 * @return
	 */
	public boolean continuFittingSimpliestVersion(){
		boolean oneMoreTurn = true;
	//	ObjectRef<String> message = new ObjectRef<>("");
		// oneMoreTurn = readingActionCanContinue(message);
		if(getNbAction() > nbActionBeforeQuit )
			oneMoreTurn /*&*/= false;
		return oneMoreTurn;


	}

	/**
	 * Fonction qui va déterminer si on doit continuer ou arrêter une itération de fitting.
	 *
	 * On veut passer rapidement les situations réseaux pleins ou vides.
	 * @return
	 */
	public boolean continuFitting(CircularFifoQueue<Double> densites){

		// Verif. de la valeur de densité.
		Double[] avgNSqrt;
		List<Double> bla = new ArrayList<>(densites);
		avgNSqrt = Toolz.getMeanAndSd(bla);
		if (avgNSqrt[0] > .9 && avgNSqrt[1] < .001) {
			if(Configurator.debugFittingClass)
				com.view.displayInfo(Configurator.ViewMessageType.FITTINGSKIP, new ArrayList<String>(Arrays.asList("Full Network")));
			return false;
		}
		if (avgNSqrt[0] < .001 && avgNSqrt[1] < .0001) {
			if(Configurator.debugFittingClass)
				com.view.displayInfo(Configurator.ViewMessageType.FITTINGSKIP, new ArrayList<String>(Arrays.asList("Empty Network")));
			return false;
		}

		return continuFittingSimpliestVersion();
	}

	//endregion

	//region Fitting hardstuff

	/**
	 * utilisation de la variation des coefficients directeurs sur des plages de données
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

	/** appelée régulièrement pour vérifier l'avancé de l'usage des memes sur la simulation.
	 * cqLastXActionDone est mise a jour a chaque action effectivement appliquée. Size de
	 * circularSize, 200
	 */
	public void computeMemeAppliance(){
		int nbAction = 0;
		synchronized(cqLastXActionDone){
			for (IUnitOfTransfer meme : cqLastXActionDone) {
				Toolz.addCountToElementInHashArray(kvLastActionDone, meme, 1);
				nbAction++;
			}

			// Si pas mise a zero, et que les 500 tentatives d'action dans la boucle principale
			// echoue, le prochain relevé de proportion restera identique. TODO[Refact3.0] nouveau, à bien tester
			cqLastXActionDone.clear();
		}

		for (IUnitOfTransfer meme : kvLastActionDone.keySet()) {
			kvOverallProportionActionDone.put(meme, (double)kvLastActionDone.get(meme) / nbAction);
			kvOverallNumberOfActionDone.put(meme, kvLastActionDone.get(meme));
		}

		kvLastActionDone.clear();
	}

	/** TODO reunir les deux? la flemme
	 * Methode la plus simple pour calculer la distance entre deux réseaux.
	 *
	 * @param activationCode
	 * @param targetNetworkProperty
	 */
	public static double getNetworksDistanceDumb(ResultSet result, int activationCode, int numeroRun, int numeroRepetition,
												 NetworkProperties targetNetworkProperty,
												 NetworkProperties currentNetworkProperty) {
		// variation sur nombre le nombre de temps un lien dur, et sur le
		// pourcentage d'evap necessité :augmenter le nombre de paramètre regardé.
		double currentDistance = 0;
		double totalDistance = 0;
		double currentValue;
		NetworkAttribType attribut;
		int nbAttribActivated = 0;

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
				if(attributValueForCurrentNetwork instanceof Double)
					currentValue = (Double)attributValueForCurrentNetwork;
				else
					currentValue= -1; // cas distrib degree
				result.addDetail(numeroRun, numeroRepetition, attribut, currentValue,currentDistance);
				if(Configurator.quickScore){
					System.out.println(attribut + "-t:"+attributValueForTargetNetwork +"; c:"+attributValueForCurrentNetwork
					+"; s:"+currentDistance);

				}

				totalDistance += currentDistance;
				nbAttribActivated++;
			}
		}

		// Normalise par rapport aux nombres d'éléments pris en compte pour renvoyer un pourcentage
		return totalDistance / nbAttribActivated;
	}
	/**
	 * Methode la plus simple pour calculer la distance entre deux réseaux.
	 *
	 * @param activationCode
	 * @param targetNetworkProperty
	 */
	public static double getNetworksDistanceDumb(int activationCode,
												 NetworkProperties targetNetworkProperty,
												 NetworkProperties currentNetworkProperty) {
		// variation sur nombre le nombre de temps un lien dur, et sur le
		// pourcentage d'evap necessité :augmenter le nombre de paramètre regardé.
		double currentDistance = 0;
		double totalDistance = 0;
		NetworkAttribType attribut;
		int nbAttribActivated = 0;

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
				//result.addDetail(numeroRun,attribut,currentDistance);
				totalDistance += currentDistance;
				nbAttribActivated++;
			}
		}

		// Normalise par rapport aux nombres d'éléments pris en compte pour renvoyer un pourcentage
		return totalDistance / nbAttribActivated;
	}

	/**
	 * Méthode qui se concentre sur trouver des réseaux particulier, non moyen.
	 * @param currentNetProperties
	 * @return
	 */
	public static double getNetworkScoreExplo(NetworkProperties currentNetProperties){
		double score = 0;
		double third, cc, density;
		double pallierInf = .02, pallierSup = .1;

		// on veut la plus grande skeness possible
		third = currentNetProperties.getThirdMoment();

		// avec haut clustering
		cc = currentNetProperties.getAvgClust();

		// mais éloigné des réseaux pleins. ( et vide )
		density = currentNetProperties.getDensity();
		if(density < pallierInf){
			score = pallierInf -  (density / pallierInf ) * 100;
		}
		if(density > pallierSup){
			score = (density / (1-pallierSup)) * 100 ;
		}

		score += (2 - Math.abs(third))*100  + (1.-cc)*100;

		return score;
	}

	/** retourne une distance entre deux attributs
	 * Comprise entre 0 et 100
	 * TODO [CheckPoint] calcul du score propriété par propriété
	 * @param type
	 * @param valueFrom
	 * @param valueTarget
	 * @return
	 */
	private static double getAttributDistance(NetworkAttribType type, Object valueFrom, Object valueTarget){
		double distance = 0;double valueOne;double valueTwo;
		int one, two;
		int[] ddOne, ddTwo;

		switch (type) {
			case DENSITY:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = Configurator.onlyLinear? linearDistance(valueOne, valueTwo, 1):
						squareDistance(valueOne, valueTwo, 1);
				break;
			case DDAVG:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance =  Configurator.onlyLinear? linearDistance(valueOne, valueTwo, Configurator.getNbNode() - 1):
						squareDistance(valueOne, valueTwo, Configurator.getNbNode() - 1);
				break;
			case DDINTERQRT:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = Configurator.onlyLinear?linearDistance(valueOne, valueTwo, Configurator.getNbNode() - 1):
						squareDistance(valueOne, valueTwo, Configurator.getNbNode() - 1);
				break;

			case DDARRAY:
				one = Integer.max(((int[]) valueFrom).length,((int[]) valueTarget).length);
				// doubler pour la taille max d'offset que subira l'une des deux
				ddOne = Arrays.copyOf((int[]) valueFrom,one*2);
				ddTwo = Arrays.copyOf((int[]) valueTarget,one*2);

				// on recupere la moyenne des arrays
				int somme1 = 0, deno1 = 0;
				int somme2 = 0, deno2 = 0;
				int offset1=0, offset2=0;
				for (int i = 0; i < one; i++) {
					somme1 += ddOne[i]*i;
					somme2 += ddTwo[i]*i;
					deno1 += ddOne[i];
					deno2 += ddTwo[i];
				}
				somme1 /= deno1;
				somme2 /= deno2;

				// si 1>2, quand on lit 1[0] on se réfère a 2[0-offset]
				if(somme1>somme2)
					offset2 = somme1-somme2;
				if(somme2>somme1)
					offset1 = somme2-somme1;

				// on compare deosrmais les dd avec la plus petite qui est decalé de la differente des moyennes vers
				//la plus grandes

				distance = 0;
				int nbElem = 0;
				int value1,value2;

				for (int i = 0; i < one; i++) {
					value1 = (i-offset1) >=0 ? ddOne[i-offset1]:0;
					value2 = (i-offset2) >=0 ? ddTwo[i-offset2]:0;
					// l'un des deux offwset est nul
					distance += Math.abs(value1-value2);
					nbElem++;
				}

				// cas max: deux distirb avec aucun pt en commun
				distance /= nbElem*2;
				distance *= 100;


				break;
			case AVGCLUST:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = Configurator.onlyLinear?linearDistance(valueOne, valueTwo, 1):
						squareDistance(valueOne, valueTwo, 1);
				break;
			case NBEDGES:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = Configurator.onlyLinear? linearDistance(valueOne, valueTwo, (Configurator.getNbNode() - 1) * Configurator.getNbNode()):
						squareDistance(valueOne, valueTwo, (Configurator.getNbNode() - 1) * Configurator.getNbNode());
				break;
			case NBNODES:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance =  Configurator.onlyLinear?linearDistance(valueOne, valueTwo, Configurator.getNbNode()-1):
						squareDistance(valueOne, valueTwo, Configurator.getNbNode()-1);
				break;
			case APL:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance =  Configurator.onlyLinear?linearDistance(valueOne, valueTwo, (double)(Configurator.getNbNode()+1)/3):
						squareDistance(valueOne, valueTwo, (double)(Configurator.getNbNode()+1)/3);
				break;
			case thirdMoment:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = squareDistance(valueOne, valueTwo, 3);
				if(distance>100) distance = 100;
				break;

			default:
				break;
		}

		// TODO[checkPoint]-  Full silent ?
		if(!Configurator.fullSilent)
			System.out.println("Score " + type.toString() + ": " + distance);

		return distance;
	}

	/**
	 * Donne la distance entre deux éléments ( en pourcentage ), relative au max des ces éléments
	 *
	 * @param valueOne
	 * @param valueTwo
	 * @param max
	 * @return
	 */
	private static double linearDistance(double valueOne, double valueTwo, double max){
		return (Math.abs(valueOne - valueTwo) * 100) / max;
	}

	/**
	 * Donne la distance entre deux éléments ( en pourcentage ), relative au max des ces éléments
	 * L'importance des premiers écarts sont plus important que les derniers
	 * @param valueOne
	 * @param valueTwo
	 * @param max
	 * @return
	 */
	private static double squareDistance(double valueOne, double valueTwo, double max){
		return Math.sqrt(linearDistance(valueOne,valueTwo,max)) * Math.sqrt(100);
	}
	//endregion

	//region Event et nb action

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

	/** Handler sur le changement de nombre de noeud disponibles sur la simulation.
	 *
	 * @param e
	 */
	@Override
	public void handlerNbNodeChanged(NbNodeChangedEvent e) {
		// Changement des steps etc
		nbActionByStep = e.nbNode * 10;
		nbActionBeforeQuit = Configurator.multiplicatorNbAction * e.nbNode;
		com.generateGraph(Configurator.initialNetworkForFitting);
	}

	/** En cas de changement des memes disponibles sur la map.
	 *
	 * @param e
	 */
	@Override
	public void handlerMemeAvailable(MemeAvailableEvent e) {
		this.memesAvailables = new ArrayList<>(e.listOfMeme);
	}
	public void handlerBehavTransm(BehavTransmEvent e) {
		newBehaviorTransmitted();
	}
	public void newAction(){
		synchronized(nbActionCount){
			nbActionCount++;
			nbActionCountLocal++;
		}
	}
	public void resetAction(){
		synchronized(nbActionCount){
			nbActionCount = 0;
		}
	}

	public void resetActionLocal(){
		synchronized(nbActionCountLocal){
			nbActionCountLocal = 0;
		}
	}
	public int getNbAction(){
		synchronized(nbActionCount){
			return nbActionCount;
		}
	}

	public int getNbActionLocal(){
		synchronized(nbActionCountLocal){
			return nbActionCountLocal;
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

	//endregion

}
	
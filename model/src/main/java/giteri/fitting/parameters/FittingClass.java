package giteri.fitting.parameters;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.*;

import giteri.fitting.algo.Result;
import giteri.fitting.algo.ResultSet;
import giteri.network.event.INbNodeChangedListener;
import giteri.network.event.NbNodeChangedEvent;
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
import giteri.fitting.algo.IExplorationMethod;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.NetworkAttribType;
import giteri.meme.entite.EntiteHandler;
import giteri.meme.entite.Meme;
import giteri.meme.event.ActionApplyEvent;
import giteri.meme.event.IActionApplyListener;
import giteri.meme.event.BehavTransmEvent;
import giteri.meme.event.IBehaviorTransmissionListener;


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
		INbNodeChangedListener {

	//region Variables diverses

	private boolean debug = Configurator.debugFittingClass;
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
	// Cette liste est changée pendant le fitting a chaque event action
	public CircularFifoQueue<Meme> cqLastXActionDone;
	// Ces deux listes sont utilisées dans la boucle la plus interne du fitting, donc chaque nbActionByStep
	private Hashtable<Meme, Integer> kvLastActionDone;
	private Hashtable<Meme, Double> kvOverallProportionActionDone;
	private Hashtable<Meme, Integer> kvOverallNumberOfActionDone;

	// Liste qui sont utilisées pour définir si on s'arrete de fitter sur une configuration ou non
	private CircularFifoQueue<Hashtable<Meme, Double>> cfqMemeAppliancePropOnFitting;
	private CircularFifoQueue<Hashtable<Meme, Integer>> cfqMemeApplianceNumberOnFitting;
	private int nbEltCircularQFitting = 15;

	private ArrayList<Meme> memesAvailables;
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
		memesAvailables = memeFactory.getMemes(Configurator.MemeList.ONMAP,Configurator.ActionType.ANYTHING);
		currentNetProperties.createStub();
	}

	//endregion

	//region Fitting turn & run

	/**
	 * Initialisation des variables nécessaire a un fitting.
	 * Ecriture dans les fichiers, ouverture des répertoires.
	 *
	 */
	public void init(){
		Configurator.isFitting = true;
		Configurator.methodOfGeneration = Configurator.MemeDistributionType.Nothing;
		targetNetProperties = networkFileLoader.getNetworkProperties();
		boolean doTheWrite = !Configurator.fullSilent;

		// ECRITURE
		repertoires = new ArrayList<>(Arrays.asList("Stability"));
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

        toWriteConfiguratorFile = "Champs;Valeurs";
        if(doTheWrite)
		writeNRead.writeSmallFile(repOfTheSearch,"configurator",
                Collections.singletonList(toWriteConfiguratorFile));

        // region fichier configurator
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
//                System.out.println("fiesta " + fieldOne.getName());
            }
        }
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
	}

	/**
	 * Nouvelle répétition d'une configuration donnée.
	 *
	 *
	 */
	public void newRepetition(){
		resetAction();
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

		entiteHandler.updateMemeAvailableForProperties();

		// & (3) Application de ces paramètres
		if(Configurator.displayFittingProviderApplied && numeroRepetition == 1 && !Configurator.jarMode) {
			System.out.println(numeroRunAsString + " applications des parametres: ");
			System.out.println(explorator.toString());

			// Creation du fichier de detail de meme, HEADER
			if(Configurator.writeMemeResultOnFitting)
				writeNRead.writeSmallFile(writeNRead.createAndGetDirFromString(repertoires), Configurator.fileNameMeme,
						Collections.singletonList(
						entiteHandler.memeProperties.getStringHeaderMemeDetail(explorator.getModelParameterSet(), true)));
		}
		if(debug) System.out.println(numeroRunAsString + " at " + numeroRepetitionAsString);

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
		networksSameTurn.add(currentNetProperties);

		// TODO [WayPoint]- Score distance entre deux network
		currentNetworkScore = Configurator.exploreSpecialNetworks?
			getNetworkScoreExplo(currentNetProperties)
			:getNetworksDistanceDumb(Configurator.activationCodeForScore, targetNetProperties, currentNetProperties);

		// Ajout a la classe des resultSet un score et propriété d'un réseau
		resultNetwork.addScore(numeroRun, currentNetworkScore, currentNetProperties);

		if(Configurator.writeNetworkResultOnFitting)
			com.takeSnapshot(currentSeed, Optional.ofNullable(repertoires));

		repertoires.remove(numeroRepetitionAsString);

		// Va écrire les résultats détaillés dans le CSV correspondant
		if(Configurator.writeNetworkResultOnFitting)
			resultNetwork.writelastRepDetailCSV(repOfTheSearch,
					numeroRun, currentNetProperties, explorator);

		if(Configurator.writeMemeResultOnFitting)
			writeNRead.writeSmallFile(repOfTheSearch, Configurator.fileNameMeme, Arrays.asList(
			entiteHandler.memeProperties.getStringToWriteMemeDetails(
				entiteHandler.getEntitesActive(), numeroRun, numeroRepetition, explorator)));

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

		// ici, ecrire: La chart de density sur les 4 runs, le fichier de config des runs?
//		explorator.rememberNetwork(currentNetworkId);
		com.view.resetPlotDensity();
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
			com.view.displayInfo(Configurator.ViewMessageType.FITTINGSKIP, new ArrayList<String>(Arrays.asList("Full Network")));
			return false;
		}
		if (avgNSqrt[0] < .001 && avgNSqrt[1] < .0001) {
			com.view.displayInfo(Configurator.ViewMessageType.FITTINGSKIP, new ArrayList<String>(Arrays.asList("Empty Network")));
			return false;
		}

		return continuFittingSimpliestVersion();
	}

	//endregion

	//region Fitting hardstuff

	/**
	 * Si la densité du network est quasiment au max avec peu de variation.
	 *
	 * @return
	 */
	private boolean heuristNetFull(){
		boolean isFull = false;

		return isFull;
	}

	/**
	 *
	 * @return
	 */
	private boolean heuristNetEmpty(){
		boolean isEmpty = false;

		return isEmpty;
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
		if(getNbActionLocal() == 0){
			// Et si aucune action n'a pu etre forcée a être faite
			if(!entiteHandler.forceAction()){
				resultat = false;
				resume += "[ActionReading] - Plus d'action possible dans ce réseau";
			}else {
				System.out.println("On a réussi a forcer");
			}
		} // Si on a réussi a faire faire une action
		else{
			resume += "[ActionReading] - passé par un soft end d'actions réalisées";
			resetActionLocal();
		}

		message.setValue(message.getValue() + resume);
		return resultat;
	}

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

	/**
	 * Methode la plus simple pour calculer la distance entre deux réseaux.
	 *
	 * @param activationCode
	 * @param targetNetworkProperty
	 */
	public static double getNetworksDistanceDumb(int activationCode, NetworkProperties targetNetworkProperty,
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
	 * @param type
	 * @param valueFrom
	 * @param valueTarget
	 * @return
	 */
	private static double getAttributDistance(NetworkAttribType type, Object valueFrom, Object valueTarget){
		double distance = 0;double valueOne;double valueTwo;
		int one, two;
		int[] ddOne, ddTwo;
		ArrayList<Double> temps = new ArrayList();

		switch (type) {
			case DENSITY:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, 1);
				break;
			case DDAVG:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, Configurator.getNbNode() - 1);
				break;
			case DDINTERQRT:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, Configurator.getNbNode() - 1);
				break;
			case DDARRAY:
				one = ((int[]) valueFrom).length;
				two = ((int[]) valueTarget).length;
				one = Integer.max(one,two);
				ddOne = Arrays.copyOf((int[]) valueFrom,one);
				ddTwo = Arrays.copyOf((int[]) valueTarget,one);

				distance = 0;
				for (int i = 0; i < ((int[]) valueFrom).length; i++) {
					distance += distance(ddOne[i], ddTwo[i], Configurator.getNbNode() - 1 );
					temps.add(distance(ddOne[i], ddTwo[i], Configurator.getNbNode() - 1 ));
				}

				distance /= Configurator.getNbNode();
				break;
			case AVGCLUST:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, 1);
				break;
			case NBEDGES:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, (Configurator.getNbNode() - 1) * Configurator.getNbNode());
				break;
			case NBNODES:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, Configurator.getNbNode()-1);
				break;
			case APL:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, (double)(Configurator.getNbNode()+1)/3);
				break;
			case nbEdgesOnNbNodes:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, 1);
				break;
			case thirdMoment:
				valueOne = (double) valueFrom;
				valueTwo = (double) valueTarget;
				distance = distance(valueOne, valueTwo, 1);
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
	private static double distance(double valueOne, double valueTwo, double max){
		return (Math.abs(valueOne - valueTwo) * 100) / max;
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

	@Override
	public void handlerNbNodeChanged(NbNodeChangedEvent e) {
		// Changement des steps etc
		nbActionByStep = e.nbNode * 10;
		nbActionBeforeQuit = 20 * e.nbNode;
		com.generateGraph(Configurator.initialNetworkForFitting);
	}

	//endregion

}
	
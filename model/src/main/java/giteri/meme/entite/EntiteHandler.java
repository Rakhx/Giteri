package giteri.meme.entite;

import giteri.meme.event.ActionApplyEvent;
import giteri.meme.event.BehavTransmEvent;
import giteri.meme.event.IActionApplyListener;
import giteri.meme.event.IBehaviorTransmissionListener;
import giteri.meme.mecanisme.FilterFactory.IFilter;
import giteri.meme.mecanisme.AttributFactory.IAttribut;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.event.INbNodeChangedListener;
import giteri.network.event.NbNodeChangedEvent;
import giteri.network.network.Network;
import giteri.network.network.Node;
import giteri.network.networkStuff.NetworkConstructor;
import giteri.network.networkStuff.WorkerFactory;
import giteri.run.ThreadHandler;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.*;
import giteri.run.controller.Controller;
import giteri.run.controller.Controller.VueController;
import giteri.tool.math.Toolz;
import giteri.tool.objects.ObjectRef;

import java.util.*;

/**
 * Classe qui gère les entités du réseau.
 *
 */
public class EntiteHandler extends ThreadHandler implements INbNodeChangedListener {

	//region properties & constructeur

	private VueController vueController;
	private NetworkConstructor networkConstruct;
	private MemeFactory memeFactory;
	private WorkerFactory workerFactory;

	// les entités du réseau
	protected Set<Entite> entites;

	// Entite possedant des actions
	private ArrayList<Entite> entitesActive;
	private Hashtable<String, String> memeTranslationReadable;
	private List<Meme> memeFittingApplied;
	private boolean allTransmitted = false;
	private boolean allAddTransmitted = false;
	private boolean allrmvTransmitted = false;


	public MemeProperties memeProperties;

	// Listener pour les évènements issu de l'obtention de meme ou application
	// de ce dernier.
	private ArrayList<IActionApplyListener> entityListeners;
	private ArrayList<IBehaviorTransmissionListener> memeListeners;

	private Meme addRandom = null, removeRandom = null;

	// Variable d'utilisation
	private static int indexOfMemesCombinaisonRecursion;
	private long atmLastTime;
	private int cptModulo;
	private int cptActionAddTried = 1, cptActionRmvTried = 1,cptActionAddFail = 1, cptActionRmvFail = 1;
	private ActionType lastAction = ActionType.RETRAITLIEN;
	private int nbActionBySecond;
	private List<String> toDisplayForRatio; // String pour affichage en utilisant la vueManager
	private double lastDensity;
	private int sumFailAction;
	private ObjectRef<Integer> nbFail = new ObjectRef<>(0);

	/** Constructeur sans param.
	 *
	 */
	public EntiteHandler(NetworkConstructor networkC, MemeFactory memeF, WorkerFactory workF) {

		if (Configurator.DisplayLogdebugInstantiation)
			System.out.println("EntiteHandler constructor Initialisation");

		networkConstruct = networkC;
		memeFactory = memeF;
		workerFactory = workF;

		entites = new HashSet<>();
		entitesActive = new ArrayList<>();
		entityListeners = new ArrayList<>();
		memeListeners = new ArrayList<>();
		memeTranslationReadable = new Hashtable<>();
		toDisplayForRatio = new ArrayList<>();
		memeProperties = new MemeProperties();

		if (Configurator.DisplayLogdebugInstantiation)
			System.out.println("EntiteHandler constructor Closing");
	}

	public void initialisation(){
		generateMemeAvailableForMap();
		bindNodeWithEntite(networkConstruct.getNetwork());
		//giveMemeToEntite(Configurator.methodOfGeneration);
	}

	public void updateMemeAvailableForProperties(){
		memeProperties.memeOnMap = this.memeFittingApplied;

		//combinaison de meme présent sur le run, classé par type d'action
		Hashtable<ActionType, ArrayList<Meme>> memesByCategory = new Hashtable<>();
		for (Meme meme: this.memeFittingApplied)
			Toolz.addElementInHashArray(memesByCategory,meme.getAction().getActionType(),meme);
		//memeCombinaisonFittingAvailable = this.getMemeAvailable(FittingBehavior.simpleAndComplex,Optional.of(memesByCategory));

		memeProperties.memeCombinaisonFittingAvailable =
				this.getMemeAvailable(FittingBehavior.simpleAndComplex, Optional.of(memesByCategory));
	}


	//endregion

	//region Thread

	/** Les joies des constructeurs nécessitant l'un et l'autre
	 *
	 * @param control
	 */
	public void setIHMController(VueController control) {
		vueController = control;
	}

	/** Obtenir un thread a partir de la classe
	 *
	 */
	public Thread getThread() {
		Thread returnThread = new Thread(this);
		returnThread.setName("EH");
		return returnThread;
	}

	/** Run pour la création de lien, passage de meme, etc.
	 *
	 */
	public void doRun() {
		try
		{
			Thread.sleep(Configurator.getThreadSleepMultiplicateur());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		OneStep();
	}

	/**
	 * Fait ce qui correspond à une execution du thread.
	 *
	 * @return les actions qui ont été réalisé
	 */
	public void OneStep() {
		if (Configurator.autoRedoActionIfNoAction)
			while (runEntite().contains("Nope"));
		else
			runEntite();

		if(!Configurator.jarMode){
			checkAPM();
		}
		cptModulo++;

		// Indicateur meme repartition, etc.
		if (cptModulo % (Configurator.refreshInfoRate * 25) == 0) {
			if (Configurator.displayMemePosessionDuringSimulation) {
				vueController.displayMemeUsage(cptModulo,
						memeProperties.getNbActivationByMemes(),
						memeProperties.countOfLastMemeActivation,
						memeProperties.lastHundredActionDone); }

			if (Configurator.displayLogAvgDegreeByMeme)
				vueController.displayInfo("AvgDgrByMeme", Arrays.asList(checkPropertiesByMemePossession()));
		}

		// Verification de la propagation totale des memes initiaux
		if(!allTransmitted && Configurator.checkWhenFullPropagate && cptModulo % Configurator.checkFullProRefreshRate == 0) {
			if(areAllMemeTransmitted()) {
                allTransmitted = true;
                vueController.displayInfo("Propagation", Arrays.asList("ALL TRANSMISTED IN ;" + cptModulo));
			}

			int resDetail = areAllMemeTransmittedDetails();
			if ((resDetail & 1) == 1){
				System.out.println("all add transmitted");
				vueController.displayInfo("Propagation", Arrays.asList("ALL ADD TRANSMISTED IN ;" + cptModulo));
				allAddTransmitted =true;
			}
			if((resDetail & 2) == 2) {
				System.out.println("all rmv transmitted");
				vueController.displayInfo("Propagation", Arrays.asList("ALL RMV TRANSMISTED IN ;" + cptModulo));
				allrmvTransmitted = true;
			}
			if((resDetail & 3) == 3){
				 allTransmitted = true;
			}
		}
	}

	//endregion

	//region Entité

	/**
	 * Méthode de génération de network. A placer dans le NC? Va générer les
	 * liens, mais les noeuds doivent déjà etre présent. 0 empty, 1 30%, 2 full,
	 * 3 scalefree, 4 smallworld, 5 complet
	 *
	 * @param activator
	 */
	public void generateNetwork(int activator) {
		networkConstruct.generateNetwork(activator);
	}

	/**
	 * fonction qui va forcer chaque entité a essayer chacune des actions de son
	 * panel. La fonction s'arrête si une action est effectivement réalisée. Si
	 * aucune action n'a pu effectivement etre réalisée, la fonction renvoi
	 * false. Permet de débloquer des situations. l'ordre d'appel des agents est
	 * volontairement aléatoire.
	 *
	 */
	public boolean forceAction() {
		boolean actionDone = false;
		ArrayList<Integer> nodeIndex = new ArrayList<Integer>();
		for (int i = 0; i < entites.size(); i++)
			nodeIndex.add(i);
		Toolz.unsortArray(nodeIndex);
		Entite acting;
		String resultat;

		List<Meme> myMemes ;

		for (Entite entite : entites) {
			// copie la liste pour éviter des deadlocks avec la fonction redefineProba
			// si on arrive a forcer une action et cette action se transmettre.
			myMemes = entite.getMyMemes();
			for (Meme action : myMemes) {
				resultat = doAction(entite, action);
				if (!resultat.contains("Nope") && !resultat.contains("NOACTION")) {
					actionDone = true;
					return actionDone;
				}
			}
		}

		return actionDone;
	}

	// TODO REFACTORING A voir pour le positionnement de l'appel à cette fonction.
	/** Rappelle pour chaque entité la fonction qui définit les probabilités de
	 * choix des règles
	 */
	public void resetProba() {
		for (Entite entite : entites) {
			entite.defineRulesProbaLimited();
		}
	}

	/** Ajout d'un lien entre deux entitées, dans la couche network.
	 *
	 * @param from
	 *            depuis cette entitées
	 * @param to
	 *            jusqu'a celle la
	 */
	public boolean addLink(Entite from, Entite to) {
		if (!isEntiteLinked(from, to, false)) {
			// ajout dans la couche network; Ajout dans la couche meme
			// automatique car les entitées ont leur edges en passant par la
			// propriété dans les nodes.
			networkConstruct.NMAddLink(
					from.getNode().getIndex(), to.getNode().getIndex(), false);
			// Ajout dans la liste des entités pour faire la correspondance
			// lien, temps de connection
			from.addConnectedEntite(to);
			to.addConnectedEntite(from);
			return true;
		}
		return false;
	}

	/**
	 * Retrait d'un lien, s'il existe, entre deux nodes.
	 *
	 * @param from
	 *            Depuis le noeud
	 * @param to
	 *            vers le noeud
	 * @return vrai si le retrait a bien eu lieu.
	 */
	public boolean removeLink(Entite from, Entite to) {
		if (isEntiteLinked(from, to, false)) {
			from.removeConnectedEntite(to);
			to.removeConnectedEntite(from);
			return networkConstruct.NMRemoveLink(
					from.getIndex(), to.getIndex(), false);
		}

		return false;
	}

	/** Renvoi une sous liste des agents auxquels est connecté l'asker
	 *
	 * @param asker
	 * @return une sous liste d'entité
	 */
	public Set<Entite> getLinkedEntite(Entite asker) {
		return asker.getConnectedEntite();
	}

	/** Retourne l'entite correspondant au node en parametre, null sinon.
	 *
	 * @param node
	 * @return
	 */
	public Entite getEntityCorresponding(Node node) {
		for (Entite entite : entites) {
			if (entite.getNode().getIndex() == node.getIndex()) {
				return entite;
			}
		}

		return null;
	}

	/** Retourne l'entite correspondant au node en parametre, null sinon.
	 *
	 * @param nodeIndex
	 * @return
	 */
	public Entite getEntityCorresponding(int nodeIndex) {
		for (Entite entite : entites) {
			if (entite.getNode().getIndex() == nodeIndex) {
				return entite;
			}
		}

		return null;
	}

	/** Replace en situation de base les éléments, c'est a dire des noeuds sans
	 * lien et sans meme.
	 *
	 */
	public void resetStat() {
		for (Entite entite : entites) {
			entite.resetStat();
		}

		lastDensity = 0;
		cptModulo = 0;
		cptActionAddTried = 1;
		cptActionRmvTried = 1;
		cptActionAddFail = 1;
		cptActionRmvFail = 1;
		allTransmitted = false;
		allAddTransmitted = false;
		allrmvTransmitted = false;
		memeProperties.clear();
		entitesActive.clear();
	}

	//endregion

	//region Event

	/** Pour lancer les évènements de type meme transmis
	 * crée un evenement pour la mise a jour de l'interface Il s'agit d'un
	 * evenement de type acquisition ou perte d'un meme.
	 */
	public void eventMemeChanged(Entite entiteConcernee, Meme ajoutOrR, String message) {

	    if(!Configurator.rebranchementAction || entiteConcernee.getMyMemes().size() > 1)
            synchronized (entitesActive) {
                if(!entitesActive.contains(entiteConcernee))
                    entitesActive.add(entiteConcernee);
            }

		BehavTransmEvent myEvent = new BehavTransmEvent(this, entiteConcernee, ajoutOrR, message);

		// On prévient les entites qui listen
		synchronized (memeListeners) {
			for (IBehaviorTransmissionListener memeListener : memeListeners) {
				memeListener.handlerBehavTransm(myEvent);
			}
		}
	}

	/** Pour lancer les évènements de type action réalisée. Application d'un meme // d'une action
	 *
	 * @param entiteConcernee
	 * @param memeRealisee
	 * @param message
	 */
	public synchronized void eventActionDone(Entite entiteConcernee, Meme memeRealisee, String message) {

		// On crée un événement rappelant l'état courant concernant les memes;
		ActionApplyEvent myEvent = new ActionApplyEvent(this, entiteConcernee, memeRealisee, message);

		// ON PREVIENT LES ENTITES QUI LISTEN
		synchronized (entityListeners) {
		for (IActionApplyListener entityListener : entityListeners) {
			entityListener.handlerActionApply(myEvent);
		}}
	}

	/** Ajout d'un listener a la liste des listeners a prévenir en cas d'event de
	 * type entity
	 *
	 * @param myListener
	 */
	public void addEntityListener(IActionApplyListener myListener) {
		synchronized (entityListeners) {

			if (!entityListeners.contains(myListener)) {
				entityListeners.add(myListener);
			}
		}
	}

	/** Retrait d'un listener depuis la liste des listeners
	 *
	 * @param myListener
	 */
	public void removeEntityListener(IActionApplyListener myListener) {
		synchronized (entityListeners) {
			if (entityListeners.contains(myListener)) {
				entityListeners.remove(myListener);
			}
		}
	}

	/** Ajout d'un listener a la liste des listeners a prévenir en cas d'event de
	 * type meme
	 *
	 * @param myListener
	 */
	public void addMemeListener(IBehaviorTransmissionListener myListener) {
		synchronized (memeListeners) {
			if (!memeListeners.contains(myListener)) {
				memeListeners.add(myListener);
			}
		}
	}

	/** Retrait d'un listener depuis la liste des listeners
	 *
	 * @param myListener
	 */
	public void removeMemeListener(IBehaviorTransmissionListener myListener) {
		synchronized (memeListeners) {
			if (memeListeners.contains(myListener)) {
				memeListeners.remove(myListener);
			}
		}
	}


	//endregion

	//region Meme

	/** Distribut les memes aux entités suivant certains mode.
	 * Utilisé pour les lancement manuels. l'apply du IModelParameter DiffuProba
	 * fourni une liste et appelle giveMemeToEntiteFitting
	 *
	 * @param distrib
	 */
	public void giveMemeToEntite(MemeDistributionType distrib) {
		switch (distrib) {
			case SingleBasic:
				giveMemeToEntiteOnlyBasis();
				break;
			case SingleCombinaison:
				giveMemeToEntiteOnlyCombinaison();
				break;
			case AllCombinaison:
				giveMemeToEntiteGeneric();
				break;
			case AllSingle:
				giveMemeBasicAllNodes();
				break;
			case specificDistrib:
				giveMemeToEntiteSpecific();
				break;
			default:
				break;
		}
	}

	/** Applique directement depuis une liste de meme au X premier agents pour les
	 * recevoir. Si option de défault behavior, donne des memes fluides aux autres agent.
	 * Utilisé par l'apply du IModelParameter
	 * @param memes
	 */
	public void giveMemeToEntiteFitting(List<Meme> memes) {
		ArrayList<Entite> entiteContente = new ArrayList<>();
		Iterator<Entite> entitees = entites.iterator();
		Entite actual;
		ArrayList<Entite> others = new ArrayList<>(entites);
		memeFittingApplied = memes;

		for (Meme meme : memes) {
			actual = entitees.next();
			eventMemeChanged(actual, actual.addMeme(meme, true), Configurator.MemeActivityPossibility.AjoutMeme.toString());
			entiteContente.add(actual);
		}

		// On file des memes d'ajout et retrait random aux entités qui n'ont pas eu de comportement de base a l'initialisation
		// TODO a deplacer avnt l'appel de cette fonction pour que tout les giveMeme puisse faire de la fluidité?
		if(Configurator.initializeDefaultBehavior) {
			others.removeAll(entiteContente);
			giveFluideMeme(others);
		}
	}

	/** Obtention de la liste des memes disponibles sur la map, soit les simples,
	 * soit les combinaisons de deux memes existantes, en fonction du param de configuration.
	 *
	 *
	 * @param setAsked
	 *            Défini si on veut les comportements simples, leur combinaison
	 *            ou les deux.
	 * @return
	 */
	public Hashtable<Integer, ArrayList<Meme>> getMemeAvailable(FittingBehavior setAsked,
																Optional<Hashtable<ActionType, ArrayList<Meme>>> memeByC) {
		Hashtable<Integer, ArrayList<Meme>> memes = new Hashtable<>();
		if (setAsked == FittingBehavior.onlyComplex || setAsked == FittingBehavior.simpleAndComplex) {
			memes = getMemeCombinaisonAvailable(memeByC);
		}
		if (setAsked == FittingBehavior.onlySimple || setAsked == FittingBehavior.simpleAndComplex) {
			int lastIndex = memes.size();
			for (Meme meme : memeFactory.getMemes(Configurator.MemeList.ONMAP,Configurator.ActionType.ANYTHING))
				Toolz.addElementInHashArray(memes, ++lastIndex, meme);
		}

		return memes;
	}
	/** Renvoi la liste des memes dispo sur la map en liste de string.
	 * Utilisé pour les couleurs sur le graphe
	 *
	 * @param setAsked Voir @getMemeAvailable
	 * @return
	 */
	public ArrayList<String> getMemeAvailableAsString(FittingBehavior setAsked) {
		ArrayList<String> memesAsString = new ArrayList<String>();
		Hashtable<Integer, ArrayList<Meme>> memes = getMemeAvailable(setAsked, Optional.empty());
		ArrayList<String> classes = new ArrayList<String>();
		String classe;

		for (ArrayList<Meme> combinaison : memes.values()) {
			classes.clear();
			for (Meme meme : combinaison)
				classes.add(meme.toFourCharString());

			classes.sort(null);
			classe = "";
			for (String string : classes)
				classe += string;

			memesAsString.add(classe);
		}

		if (Configurator.DisplayLogGetAvailableMeme)
			for (String string : memesAsString) {
				System.out.println(string);
			}

		return memesAsString;
	}

	/** Transforme les adlkminesup en add+;
	 * TODO [WayPoint]- traduction .add+ <= ADLKMTNTSPMN etc
	 * @param memeCombinaison
	 * @return un truc plus clair a lire.
	 */
	public String translateMemeCombinaisonReadable(String memeCombinaison) {
		String compo = "";
		String[] combinaison = memeCombinaison.contains(".")? memeCombinaison.split("\\."):new String[]{memeCombinaison};
		for (String oneName:
			 memeTranslationReadable.keySet()) {
			for (String combi:combinaison) {
				if(combi.compareToIgnoreCase(oneName) == 0)
					compo += "." + memeTranslationReadable.get(oneName);
			}
		}

		return compo;
	}

	/** Regarde le propriété des éléments du réseau, en fonction de la
	 * combinaison de meme qu'ils possedent.
	 *
	 */
	public String checkPropertiesByMemePossession() {
		Hashtable<String, ArrayList<Entite>> entiteByMemePossession = new Hashtable<>();
		//@SuppressWarnings("unchecked")
		HashSet<Entite> toExamine = new HashSet<>(entites);
		ArrayList<Integer> SelfDegrees = new ArrayList<>();
		ArrayList<Integer> othersDegrees = new ArrayList<>();
		String memes = "";
		Entite nodeConnected;
		String resultat = "";
		try {
			// On classe les entités par combinaison de memes qu'elles possedent
			for (Entite entite : toExamine) {
				memes = "";
				for (Meme meme : entite.getMyMemes())
					memes += "." + meme.toFourCharString();
				if (memes == "")
					memes = "AUCUN";

				Toolz.addElementInHashArray(entiteByMemePossession, memes, entite);
			}
		} catch (ConcurrentModificationException e) {
			System.err.println(e.getStackTrace());
		}

		resultat += "-----------------------------------------------------\n";
		for (String memeCombinaison : entiteByMemePossession.keySet()) {
			SelfDegrees.clear();
			othersDegrees.clear();

			for (Entite entite : entiteByMemePossession.get(memeCombinaison)) {

				SelfDegrees.add(entite.getDegree());
				for (Integer nodeIndex : networkConstruct
						.getConnectedNodes(entite.getIndex())) {
					nodeConnected = this.getEntityCorresponding(nodeIndex);
					if (nodeConnected != null)
						othersDegrees.add(nodeConnected.getDegree());
					else
						System.err.println("Ne DEVRAIT PAS ETRE POSSIBLE EH CHECKPROPERTIESBY...");
				}
			}

			resultat += "("
					+ entiteByMemePossession.get(memeCombinaison).size()
					+ ") - "
					+ "Degree moyen pour la combinaison de meme "
					+ translateMemeCombinaisonReadable(memeCombinaison)
					+ ": "
					+ Toolz.getAvg(SelfDegrees)
					+ " values: "
					+ Toolz.getNumberCutToPrecision(
					Toolz.getAvg(othersDegrees), 2);
			resultat += "\n";
		}

		return resultat;
	}

//	/**
//	 *
//	 * @param param
//	 * @param forRepetition si pour répetition, valeur de la répétition, sinon, celui des répétitions du runs
//	 * @return
//	 */
//	public String getStringHeaderMemeDetail(List<IModelParameter<?>> param, boolean forRepetition){
//		String header = "Run-Rep";
//		for (IModelParameter<?> model :param)
//			header += ";" + model.nameString();
//
//		// CALCUL DES INDICATEURS A ECRIRE
//
//		// La liste des memes courant devrait etre a jour, apply fait avant dans la fitting classe
//		for (Meme meme:memeFittingApplied) {
//			header += ";Meme[";
//			header += meme.toFourCharString() +":";
//			header += "]-nbEntiteOwning";
//			if(forRepetition)
//				header += ";SD";
//			header += ";last X appli."; // Nombre / X + pourcentage
//			if(forRepetition)
//				header += ";SD";
//		}
//
//		return header;
//	}

//	public String getStringHeaderCombinaison(List<IModelParameter<?>> param, boolean forRepetition){
//		String header = "Run-Rep";
//		for (IModelParameter<?> model :param)
//			header += ";" + model.nameString();
//
//		// combinaison de meme présent sur le run, classé par type d'action
//		Hashtable<ActionType, ArrayList<Meme>> memesByCategory = new Hashtable<>();
//		for (Meme meme: this.memeFittingApplied)
//			Toolz.addElementInHashArray(memesByCategory,meme.getAction().getActionType(),meme);
//		memeCombinaisonFittingAvailable = this.getMemeAvailable(FittingBehavior.simpleAndComplex,Optional.of(memesByCategory));
//
//		// La liste des memes courant devrait etre a jour, apply fait avant dans la fitting classe
//		for (Integer i:memeCombinaisonFittingAvailable.keySet()) {
//			header += ";Meme[";
//			for (Meme meme:memeCombinaisonFittingAvailable.get(i)) {
//				header += meme.toFourCharString() +":";
//			}
//
//			header += "]-nbEntiteOwning";
//			if(forRepetition)
//				header += ";SD";
//			header += ";last X appli."; // Nombre / X + pourcentage
//
//		}
//
//		return header;
//	}
//
//	public String getStringToWriteMemeDetails(File rep, int numeroRun, int numeroRep, IExplorationMethod explorator){
//		String toWrite ="";
//		toWrite += numeroRun + "-" + numeroRep;
//
//		// Config du fitting
//		for (IModelParameter<?> model : explorator.getModelParameterSet())
//			toWrite += ";" + model.valueString();
//
//		// detail sur les memes
//		List<Meme> combinaisonLookedAt;
//		int nbEntitesOwning;
//		for (Integer i: memeCombinaisonFittingAvailable.keySet()){
//			nbEntitesOwning = 0;
//			combinaisonLookedAt = memeCombinaisonFittingAvailable.get(i);
//			for (Entite entite: entitesActive) {
//				if(entite.getMyMemes().containsAll(combinaisonLookedAt)){
//					nbEntitesOwning++;
//				}
//			}
//
//			toWrite += ";Meme[";
//			for (Meme meme:memeCombinaisonFittingAvailable.get(i)) {
//				toWrite += meme.toFourCharString() +":";
//			}
//
//			toWrite += "]-" + nbEntitesOwning;
//		}
//
//		return toWrite;
//	}
//
//	public String getStringToWriteMemeCombinaison(int numeroRun, int numeroRep,  IExplorationMethod explorator){
//
//	}
//	// les entités du réseau
//	protected Set<Entite> entites;
//	// Entite possedant des actions
//	private ArrayList<Entite> entitesActive;

//	private Hashtable<String, String> memeTranslationReadable;

	//endregion

	//region PRIVATE

	//region Action

	/** va lancer l'action d'une entitée
	 *
	 */
	private String runEntite() {

		List<String> rez = new ArrayList<>();
		// Meme memeAction;

		boolean actionMe ;
		List<Meme> memeActions = new ArrayList<>();

		synchronized (workerFactory.waitingForReset) {
			toDisplayForRatio.clear();

			// CHOIX D'UNE ENTITÉE AU HASARD
			Entite entiteActing = selectActingEntiteV2();
			if(Configurator.debugEntiteHandler)
				System.out.println("[EH.runEntite]- entite choisie " + entiteActing.getIndex());
			if (entiteActing == null) {
				if(Configurator.debugEntiteHandler) System.err.println("[EH.runEntite()]- Aucune entité sélectionnée");
				return ("Nope pas d'entite prete");  }

			// CHOIX DE L'ACTION POUR CETTE ENTITE
			if(!Configurator.rebranchementAction) {
				memeActions.add(actionSelectionRulesVersion(entiteActing));
				if(Configurator.debugEntiteHandler)
					System.out.println("[EH.runEntite]- action choisie " + memeActions.get(0).toFourCharString());
			}
			else
				memeActions.addAll(entiteActing.getMyMemes());

			// APPLICATION ET PROPAGATION DE L'ACTION
			actionMe = !Configurator.useEntitySuccesProba || Toolz.rollDice(entiteActing.getProbaAppliying());
			for (Meme memeAction : memeActions) {
				if(actionMe)
					rez.add(doAction(entiteActing, memeAction));
				else {
					rez.add("Nope, entite ne veux pas agir");
					if (Configurator.debugEntiteHandler)
						System.out.println("[EH.runEntite]- resultat de l'action" + rez);
				}
			}

			// AFFICHAGE ET DEBUGGUAGE
			if (Configurator.displayLogRatioTryAddOverTryRmv) {
				for (Meme memeAction : memeActions) {
					if (memeAction != null) {
						if (memeAction.action.getActionType() == ActionType.AJOUTLIEN)
							cptActionAddTried++;
						else if (memeAction.action.getActionType() == ActionType.RETRAITLIEN)
							cptActionRmvTried++;

						toDisplayForRatio.add(memeAction.action.getActionType().name());
						toDisplayForRatio.add("AddTried/rmvTried;" + (double) cptActionAddTried / cptActionRmvTried);
					}
				}
			}

			// Si on veut afficher les X dernieres actions entreprises & action depuis le début
			if (Configurator.displayMemePosessionDuringSimulation) {
				int i = -1;
				for (Meme memeAction : memeActions) {
					i++;
					if (Configurator.displayLogRatioLogFailOverFail || Configurator.displayLogRatioLogFailOverSuccess )
					{
						List<String> temp = memeProperties.updateActionCount(memeAction, entiteActing.getIndex(), rez.get(i), cptModulo);
						if(temp != null)
							toDisplayForRatio.addAll(temp);
					}
					else
						memeProperties.updateActionCount(memeAction, entiteActing.getIndex(), rez.get(i), cptModulo);

					if(cptModulo % (Configurator.refreshInfoRate * 25)== 0) {

						sumFailAction =  memeProperties.lastFailAction(nbFail);
						//vueController.displayInfo("FAILSTUFF", Arrays.asList("" + sumFailAction));

						if(Configurator.writeFailDensityLink)
							vueController.displayInfo("failXDensity", getFailXDensity( nbFail.getValue(),
									networkConstruct.updatePreciseNetworkProperties
											(Configurator.getIndicateur(NetworkAttribType.DENSITY)).getDensity(),sumFailAction));
					}
				}

			}

			if(!toDisplayForRatio.isEmpty())
				vueController.displayInfo("Echecs", toDisplayForRatio);

			// Dans le cas ou on veut les filtres en semi step, remis a zero des couleurs.
			if (Configurator.semiStepProgression)
			{
				giveEntiteBaseColor();
				System.out.println("Remise a zero des couleurs");
				if (filterOnSemiAuto(null, null))
					pauseStepInSemiAutoAction();
			}
		}


		vueController.displayInfo("Density", Arrays.asList(""+networkConstruct.updatePreciseNetworkProperties
				(Configurator.getIndicateur(NetworkAttribType.DENSITY)).getDensity() ));

		workerFactory.getCalculator().incrementNbAction();
		return rez.stream().reduce(String::concat).toString();
	}

	/** Choix aléatoire d'une entité qui va faire une action.
	 * Prise parmis la liste des entités possédant des actions.
	 *
	 * @return
	 */
	private Entite selectActingEntiteV2(){
		return entitesActive.size() == 0? null :
				entitesActive.get(Toolz.getRandomNumber(entitesActive.size()));
	}

	/** Selection de l'entité qui va agir
	 *
	 * @return
	 */
	private Entite SelectActingEntite() {
		if (entitesActive.size() == 0) {
			return null;
		}

		int randomNumber = Toolz.getRandomNumber(entitesActive.size());
		for (Entite entite : entitesActive) {
			if(randomNumber == 0){
				return entite;
			}
			randomNumber--;
		}

		return null;
	}

	/** Selection d'une action pour l'entité en action rules version
	 *
	 * @param movingOne
	 * @return le meme sélectionné
	 */
	private Meme actionSelectionRulesVersion(Entite movingOne) {
		if(!Configurator.oneAddForOneRmv)
			return movingOne.chooseAction();
		else
			return actionSelectionControlledVersion(movingOne);
	}

	/** Application de l'action de l'entité
	 *
	 * @param movingOne
	 * @param memeAction
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String doAction(Entite movingOne, Meme memeAction) {
		String actionDone = "";String memeApply = "";Set<Entite> cibles ;Set<Integer> ciblesIndex = new HashSet<>();
		IFilter currentFilter = null; Iterator<Entite> ite; Entite one; Meme memeReturned;

		// Execution d'un meme de l'entite.
		if (memeAction != null) {
			cibles = new HashSet<>(entites);
			cibles.remove(movingOne);

			// FILTRE Pour chaque attribut sur lesquels on applique des filtres
			for (IAttribut<Integer> attribut : memeAction.getAttributs()) {
				// region semi auto
				// Si semi automatique.
				if (Configurator.semiStepProgression && filterOnSemiAuto(null, null)) {
					System.out.println("On va appliquer les filtres suivants pour l'action " + memeAction.name);
					System.out.println(memeAction.getFilter(attribut.toString()).values());
				} //endregion

				// Pour chaque filtre appliqué a un attribut
				for (int order = 0; order < memeAction.getFilter(attribut.toString()).size(); order++) {
					currentFilter = memeAction.getFilter(attribut.toString()).get(order);
					currentFilter.applyFilter(movingOne, cibles, attribut);
					//region semi-auto
					// Dans le cas ou on veut un mode semi automatique
					if (Configurator.semiStepProgression && filterOnSemiAuto(memeAction, currentFilter)) {
						ciblesIndex.clear();
						for (Entite entite : cibles)
							ciblesIndex.add(entite.getIndex());
						System.out.println("Application du filtre suivant: " + currentFilter.getFourCharName());
						this.giveEntiteTargetedColor(movingOne.getIndex(), ciblesIndex);
						pauseStepInSemiAutoAction();
					} // endregion
				}
			}

			// Le dernier filtre appliqué est tjrs un random() unitaire : sauf purify
			if (cibles.size() == 1 || memeAction.getAction().getFourCharName() == "PURI") {
				actionDone += memeAction.getAction().applyAction(movingOne, cibles);

				// region PROPAGATION du meme
				if(cibles.size() > 1 && Configurator.onlyOneToPropagate) {
					ite = cibles.iterator();
					for (int i = 0; i < Toolz.getRandomNumber(cibles.size()); i++) {
						ite.next();
					}

					one = ite.next();
					cibles.clear();
					cibles.add(one);
				}

				if (Configurator.usePropagation)
					for (Entite entite : cibles)
					{
						// region Transmission de l'un de behavior possédé par l'acting.
						if (Configurator.usePropagationSecondGeneration)
						{
							Meme selectedMeme = movingOne.chooseAction();
							if(selectedMeme == null){
								if(Configurator.debugEntiteHandler) System.out.println("Meme joué par " + movingOne.toString() + " disparu");
							}
							memeReturned = entite.receiveMeme(selectedMeme);
							// Si retour du meme a a jouter
							if(memeReturned == selectedMeme)
								eventMemeChanged(entite, selectedMeme,Configurator.MemeActivityPossibility.AjoutMeme.toString());

							// alors il y a eu remplacement
							if(memeReturned != null && memeReturned != selectedMeme){
								// un retrait
								eventMemeChanged(entite, memeReturned, Configurator.MemeActivityPossibility.RetraitMeme.toString());
								// et un ajout
								eventMemeChanged(entite, selectedMeme,Configurator.MemeActivityPossibility.AjoutMeme.toString());
							}
						}
						// endregion

						// ou transmission du behavior appliqué
						else {
							memeReturned = entite.receiveMeme(memeAction);
							// Si retour du meme a a jouter
							if(memeReturned == memeAction)
								eventMemeChanged(entite, memeAction, Configurator.MemeActivityPossibility.AjoutMeme.toString());

							// alors il y a eu remplacement
							if(memeReturned != null && memeReturned != memeAction){
								// un retrait
								eventMemeChanged(entite, memeReturned, Configurator.MemeActivityPossibility.RetraitMeme.toString());
								// et un ajout
								eventMemeChanged(entite, memeAction,Configurator.MemeActivityPossibility.AjoutMeme.toString());
							}
						}
					}

				// endregion

				// evenement d'application d'une action
				eventActionDone(movingOne, memeAction, actionDone);
			}

			// Dans le cas ou il y a plus d'une cible // ou pas action purify // ou aucune cible.
			else {
				if (cibles.size() > 1) System.err.println("Plusieurs cibles pour une action, pas normal");
				actionDone = "Nope, Entite " + movingOne.getIndex()  + " Aucune(ou trop de) cible pour l'action" + memeAction.toFourCharString();
				eventActionDone(movingOne, null, "NOTARGET " + actionDone);
			}
		}

		// Si le meme action est NULL
		else
		{
			actionDone = "Nope, Entite " + movingOne.getIndex() + " Liste d'action vide ou aucune action sélectionnée";
			eventActionDone(movingOne, null, "NOACTION");
		}

		if (Configurator.displayLogMemeApplication)
			vueController.displayInfo("memeApplication", Arrays.asList("MemeApplied- " + memeApply,"ActionDone- " +  actionDone));

		return actionDone;
	}

//	/** Mise a jour de la liste des X derniers meme applied ainsi que la liste
//	 * des memes applied depuis le début du run.
//	 * Appelé a chaque action réalisée.
//	 * @param memeApply
//	 */
//	private void updateActionCount(Meme memeApply, int entiteIndex, String message){
//
//		if (memeApply != null)
//		{
//			Meme elementRemoveOfCircular = null;
//			Toolz.addCountToElementInHashArray(nbActivationByMemes, memeApply, 1);
//
//			// partie last twenty
//			if(lastHundredActionDone.size() == lastHundredActionDone.maxSize())
//			{
//				elementRemoveOfCircular = lastHundredActionDone.poll();
//				Toolz.removeCountToElementInHashArray(countOfLastMemeActivation, elementRemoveOfCircular, 1);
//			}
//
//			lastHundredActionDone.add(memeApply);
//			Toolz.addCountToElementInHashArray(countOfLastMemeActivation, memeApply, 1);
//		}
//
//		// Dans le cas ou il n'y a pas de meme apply, c'est a dire que l'action d'application du meme a échouée.
//		else if (Configurator.displayLogRatioLogFailOverFail || Configurator.displayLogRatioLogFailOverSuccess )
//		{
//			int nbWin = 0;
//			if(Configurator.displayLogRatioLogFailOverSuccess)
//					for (Integer winTimes : nbActivationByMemes.values())
//						nbWin += winTimes;
//
//			if(Configurator.displayLogRatioLogFailOverFail)
//				if (message.contains("RMLK"))
//					cptActionRmvFail++;
//				else if (message.contains("ADLK"))
//					cptActionAddFail++;
//
//			vueController.displayInfo("Action-Echec", Arrays.asList(
//					"Iteration- "+ cptModulo,
//					"Ratio Rmv/Add -" + (Configurator.displayLogRatioLogFailOverFail? ((double) cptActionRmvFail / cptActionAddFail) : " NC"),
//					"Ratio Fail/success -" + (Configurator.displayLogRatioLogFailOverSuccess ? ((double) (cptActionRmvFail + cptActionAddFail) / nbWin) : "NC"),
//					"Aucune action réalisée par l'entité- " + entiteIndex,
//					"Message- " + message));
//		}
//	}

	//endregion

	//region Meme

	/** Renvoi true si aucune des entités initiales ne possède un slot vide ou de fluidité.
	 *
	 * @return False si memes initiaux non intégralement transmis.
	 */
	private boolean areAllMemeTransmitted(){
		for (Entite entite: entites) {
			if(!entite.isFullActif())
				return false;
		}

		return true;
	}

	/** Vérifie si les memes d'ajout // de retrait sont tous transmis.
	 *
	 * @return 1er bit pour les ajout, 2er les retrait
	 */
	private int areAllMemeTransmittedDetails(){
		int res = 4;
		boolean add = true, rmv = true, addtmp, rmvtmp;
		if(allAddTransmitted){
			add = false;
			res += 1;
		}
		if(allrmvTransmitted){
			rmv = false;
			res += 2;
		}

		for (Entite entite: entites) {
			if(add || rmv)
			{
				addtmp = rmvtmp = false;
				for (Meme myMeme : entite.getMyMemes()) {
					if (add && myMeme.getAction().getActionType() == ActionType.AJOUTLIEN && !myMeme.isFluide()) {
						addtmp = true;
					}
					if (rmv && myMeme.getAction().getActionType() == ActionType.RETRAITLIEN && !myMeme.isFluide()) {
						rmvtmp = true;
					}
				}

				add &= addtmp;
				rmv &= rmvtmp;
			}else // si add & rmv FALSE
				return 4;
		}

		if(add) res += 1;
		if(rmv) res += 2;
		return res;
	}


	/** Donne des memes aux entités. Ici chaque meme est donnée une fois pour une
	 * entité dans le réseau.
	 * x = nb de meme la map. Nombre d'agent avec comportement = x
	 */
	private void giveMemeToEntiteOnlyBasis() {
		ArrayList<Entite> entiteContente = new ArrayList<>();
		ArrayList<Entite> others = new ArrayList<>(entites);

		Iterator<Entite> entitees = entites.iterator();
		Entite actual;

		for (Meme meme : memeFactory.getMemes(Configurator.MemeList.ONMAP,Configurator.ActionType.ANYTHING)) {
			actual = entitees.next();
			actual.addMeme(meme, true);
			eventMemeChanged(actual, meme, Configurator.MemeActivityPossibility.AjoutMeme.toString());
			entiteContente.add(actual);
		}

		if(Configurator.initializeDefaultBehavior){
			others.removeAll(entiteContente);
			giveFluideMeme(others);
		}
	}

	/** Donne les combinaisons de meme existantes sur la map à tous les agents, un par un.
	 *
	 */
	private void giveMemeToEntiteGeneric() {
		Hashtable<Integer, ArrayList<Meme>> composition;
		composition = getMemeCombinaisonAvailable(Optional.empty());

		for (Entite entite : entites)
			for (Meme meme : composition.get( entite.getIndex()%composition.keySet().size() ))
				eventMemeChanged(entite, entite.addMeme(meme, true), Configurator.MemeActivityPossibility.AjoutMeme.toString());

	}

	/** Donne tout les memes basics réparti équitablement sur tout les nodes
	 *
	 */
	private void giveMemeBasicAllNodes() {
		int nbMeme = memeFactory.getMemes(Configurator.MemeList.ONMAP,Configurator.ActionType.ANYTHING).size();
		int partSize = entites.size() / nbMeme;
		int lastIndex = 0;
		int part = 0;
		Iterator<Entite> entitees = entites.iterator();
		Entite actual;
		for (Meme meme :memeFactory.getMemes(Configurator.MemeList.ONMAP,Configurator.ActionType.ANYTHING)) {
			part++;
			actual = entitees.next();
			for (int i = lastIndex; i < partSize * part; i++)
				eventMemeChanged(actual,actual.addMeme(meme, true),
						Configurator.MemeActivityPossibility.AjoutMeme.toString());
			lastIndex = partSize * part;
		}
	}

	/**
	 * Donne une instance de chaque combinaison a un agent.
	 *
	 */
	private void giveMemeToEntiteOnlyCombinaison() {
		Hashtable<ActionType, ArrayList<Meme>> memesByCategory = new Hashtable<ActionType, ArrayList<Meme>>();
		ArrayList<Meme> memeOfOneCategory;
		ArrayList<ActionType> key = new ArrayList<ActionType>();

		for (ActionType action : Configurator.ActionType.values()) {
			if (action == ActionType.AJOUTLIEN || action == ActionType.RETRAITLIEN) {
				memeOfOneCategory = memeFactory.getMemeAvailable(action, false);
				if (memeOfOneCategory.size() > 0) {
					memesByCategory.put(action, memeOfOneCategory);
					key.add(action);
				}
			}
		}

		ActionType[] keyz = new ActionType[memesByCategory.keySet().size()];
		for (int i = 0; i < key.size(); i++) {
			keyz[i] = key.get(i);
		}

		Hashtable<Integer, ArrayList<Meme>> composition = new Hashtable<Integer, ArrayList<Meme>>();
		ArrayList<Meme> memez = new ArrayList<Meme>();
		indexOfMemesCombinaisonRecursion = -1;
		recursive(memesByCategory, memez, keyz, composition, -1);
		Iterator<Entite> entitees = entites.iterator();
		Entite actual;

		for (Integer integer : composition.keySet()) {
			actual = entitees.next();
			for (Meme meme : composition.get(integer)) {
				eventMemeChanged(actual, actual.addMeme(meme, true),
						Configurator.MemeActivityPossibility.AjoutMeme.toString());

			}
		}
	}

	/**
	 *
	 */
	private void giveMemeToEntiteSpecific() {
		int i = 0;
		Entite entiteReceptrice;
		ArrayList<Double> swParamSet3 = new ArrayList<>(Arrays.asList(0.907489970963927,0.363546615459677,0.458976194767827,0.247873953220028,0.984710568248182));
		ArrayList<Double> currentParamSet = swParamSet3;
		Iterator<Entite> entitees = entites.iterator();
		for (Meme meme : memeFactory.getMemes(Configurator.MemeList.EXISTING,Configurator.ActionType.ANYTHING)) {
			entiteReceptrice = entitees.next();
			if(meme.getName().compareTo("Add∞") == 0){
				meme.setProbaOfPropagation(currentParamSet.get(i));
				entiteReceptrice.addMeme(meme, true);
				eventMemeChanged(entiteReceptrice, meme, Configurator.MemeActivityPossibility.AjoutMeme.toString());
			}
			if(meme.getName().compareTo("AddØ-Hop") == 0){
				meme.setProbaOfPropagation(currentParamSet.get(i));
				entiteReceptrice.addMeme(meme, true);
				eventMemeChanged(entiteReceptrice, meme, Configurator.MemeActivityPossibility.AjoutMeme.toString());
			}
			if(meme.getName().compareTo("Rmv-") == 0){
				meme.setProbaOfPropagation(currentParamSet.get(i));
				entiteReceptrice.addMeme(meme, true);
				eventMemeChanged(entiteReceptrice, meme, Configurator.MemeActivityPossibility.AjoutMeme.toString());
			}
			if(meme.getName().compareTo("Rmv+") == 0){
				meme.setProbaOfPropagation(currentParamSet.get(i));
				entiteReceptrice.addMeme(meme, true);
				eventMemeChanged(entiteReceptrice, meme, Configurator.MemeActivityPossibility.AjoutMeme.toString());
			}
			if(meme.getName().compareTo("RmvØ-2hop") == 0){
				meme.setProbaOfPropagation(currentParamSet.get(i));
				entiteReceptrice.addMeme(meme, true);
				eventMemeChanged(entiteReceptrice, meme, Configurator.MemeActivityPossibility.AjoutMeme.toString());
			}
			i++;
		}
	}

	// TODO REFACTORING Prendre les memes a appliquer a tous en parametre plutot qu'avoir des variables statics
	/** Dote les entités qui n'ont pas encore d'action des actions Add et remove de base.
	 *
	 * @param entitesToBeApplied
	 */
	private void giveFluideMeme(ArrayList<Entite> entitesToBeApplied){
		for (Entite entite : entitesToBeApplied) {
				if(addRandom != null)
					eventMemeChanged(entite, entite.addMeme(addRandom, false), Configurator.MemeActivityPossibility.AjoutMeme.toString());
				if(removeRandom != null)
					eventMemeChanged(entite, entite.addMeme(removeRandom, false), Configurator.MemeActivityPossibility.AjoutMeme.toString());
		}
	}

	/**
	 * Fait le lien entre les agents et les noeuds du réseaux A appeler une fois
	 * avant de commencer les routines de simulation
	 */
	protected void bindNodeWithEntite(Network network) {
		ArrayList<Node> nodes = network.getNodes();
		Entite linked, toBindToNode;
		Node node;

		// Iterator sur les nodes du réseau pour leur associer des memes
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			node = iterator.next();
			toBindToNode = new Entite();
			toBindToNode.setNode(node);
			entites.add(toBindToNode);
			if(Configurator.autoMemeForBreeder) {
				toBindToNode.ajoutRandomFlemme = addRandom;
				toBindToNode.retraitRandomFlemme = removeRandom;
			}
		}

		for (Entite entite : entites) {
			node = entite.getNode();
			// Mise à jour des noeuds auxquels sont connectés les entités
			for (Integer indexOfNode : node.getConnectedNodes()) {
				linked = getEntityCorresponding(network.getNode(indexOfNode));
				entite.addConnectedEntite(linked);
			}
		}
	}

	/** Utilisé pour l'application des filtres step by step.
	 *
	 */
	private void giveEntiteTargetedColor(Integer actingEntite, Set<Integer> targeted) {
		networkConstruct.changeColorClass(actingEntite,targeted);
	}

	/** Utilisé pour l'application des filtres step by step.
	 *
	 */
	private void giveEntiteBaseColor() {
		networkConstruct.resetColorClass();
	}

	/** utilisé lors du reset des réseaux.
	 *
	 */
	public void synchronizeNodeConnectionWithEntiteConnection() {
		Network network = networkConstruct.getNetwork();
		Entite concerne;

		for (Node node : network.getNodes()) {
			concerne = getEntityCorresponding(node.getIndex());
			concerne.resetEntiteStuff();
			for (Integer index : node.getConnectedNodes()) {
				concerne.addConnectedEntite(getEntityCorresponding(index));
			}
		}
	}

	// TODO REFACTORING Mieux positionner l'appel à cette fonction? Plutot que dans l'instanciation d'EH? Paramétriser
	// Ca pour en faire un truc joli depuis un main qui l'appel ( Jar etc )
	/**
	 * Génère les memes disponibles sur la map, qui seront associés ou non aux
	 * entités. Ajouté directement dans la liste des memes dispos du memeHandler
	 * TODO [WayPoint] - Génération des memes disponibles sur la map
	 *
	 */
	private void generateMemeAvailableForMap() {

		ArrayList<AttributType> attributs = new ArrayList<>();
		Hashtable<AttributType, Hashtable<Integer, AgregatorType>> KVAttributAgregator = new Hashtable<>();
		Hashtable<Integer, AgregatorType> agregators = new Hashtable<>();

		ActionType add = ActionType.AJOUTLIEN;
		ActionType remove = ActionType.RETRAITLIEN;

		@SuppressWarnings("unused")
		ActionType puri = ActionType.PURIFY;
		AttributType degree = AttributType.DEGREE;
		AgregatorType linked = AgregatorType.LINKED;
		AgregatorType notLinked = AgregatorType.NOTLINKED;
		AgregatorType mineInf = AgregatorType.MINEINF;
		AgregatorType mineSup = AgregatorType.MINESUP;
		AgregatorType random = AgregatorType.RANDOM;
		AgregatorType hopAWay = AgregatorType.HOPAWAY;
		AgregatorType triangle = AgregatorType.TRIANGLE;
		AgregatorType theirSup = AgregatorType.THEIRSUP;
		AgregatorType theMost = AgregatorType.THEMOST;
		KVAttributAgregator.put(degree, agregators);
		attributs.add(degree);

		agregators.clear();
		agregators.put(0, notLinked);
		agregators.put(1, random);
		memeFactory.registerMemeAction("AddØ",0.1, true, true, add, attributs, KVAttributAgregator, false);
		agregators.put(2, random);
		addRandom = memeFactory.registerMemeAction("AddØ-Neutral",0, false, false, add, attributs, KVAttributAgregator, true);

		agregators.put(0, notLinked);
		agregators.put(1, mineInf);
		agregators.put(2, random);
		memeFactory.registerMemeAction("Add+", 1, false ,false, add, attributs,KVAttributAgregator, false);

		agregators.clear();
		agregators.put(0, notLinked);
		agregators.put(1, mineSup);
		agregators.put(2, random);
		memeFactory.registerMemeAction("Add-",1, false ,false, add, attributs,KVAttributAgregator, false);

		agregators.clear();
		// agregators.put(0, notLinked);
		agregators.put(0, theMost);
		agregators.put(1, random);
		memeFactory.registerMemeAction("Add∞", 1, false, false, add, attributs, KVAttributAgregator,false);

		agregators.clear();
		agregators.put(0, hopAWay);
		agregators.put(1, notLinked);
		agregators.put(2, theMost);
		agregators.put(3, mineInf);
		agregators.put(4, random);
		memeFactory.registerMemeAction("AddØ-Hop", 1, false, true, add,attributs, KVAttributAgregator ,false);

		agregators.clear();
		agregators.put(0, linked);
		agregators.put(1, random);
		memeFactory.registerMemeAction("RmvØ",1, true, true, remove,  attributs, KVAttributAgregator, false);
		agregators.put(2, random);
		removeRandom = memeFactory.registerMemeAction("RmvØ-neutral",0, false, false, remove,  attributs, KVAttributAgregator, true);

		agregators.clear();
		agregators.put(0, linked);
		agregators.put(1, theirSup);
		agregators.put(2, random);
		memeFactory.registerMemeAction("Rmv2",.5, false, false, remove,  attributs, KVAttributAgregator, false);

		agregators.clear();
		agregators.put(0, linked);
		agregators.put(1, mineSup);
		agregators.put(2, random);
		memeFactory.registerMemeAction("Rmv-", 1, false, false, remove, attributs, KVAttributAgregator ,false);

		agregators.clear();
		agregators.put(0, linked);
		agregators.put(1, mineInf);
		agregators.put(2, random);
		memeFactory.registerMemeAction("Rmv+", 1, false, false, remove, attributs, KVAttributAgregator ,false);

		agregators.clear();
		agregators.put(0, linked);
		agregators.put(1, triangle);
		agregators.put(2, random);
		memeFactory.registerMemeAction("RmvØ-2hop", .3, false, false, remove, attributs,KVAttributAgregator ,false);

		agregators.clear();
		memeFactory.registerMemeAction("Puri",.1,false, false, puri, attributs, KVAttributAgregator, false);

		for (Meme memeDispo : memeFactory.getMemes(Configurator.MemeList.EXISTING,Configurator.ActionType.ANYTHING)) {
			memeTranslationReadable.put(memeDispo.toFourCharString(),memeDispo.getName());
		}
	}

	/**
	 * Renvoi une hashtable contenant les combinaisons possibles de meme avec
	 * les memes sur la map. Un meme du meme type par agent par combinaison.
	 * (Type: Ajout, retrait .. ) dépend de l'action appliquée
	 *
	 * @return une hash Int ( qui n'a pas de signification ), combinaison de
	 *         meme.
	 */
	private Hashtable<Integer, ArrayList<Meme>> getMemeCombinaisonAvailable(
			Optional<Hashtable<ActionType, ArrayList<Meme>>> memeByC) {
		Hashtable<ActionType, ArrayList<Meme>> memesByCategory = new Hashtable<>();
		ArrayList<Meme> memeOfOneCategory;
		ArrayList<ActionType> key = new ArrayList<>();

		if(memeByC.isPresent()) {
			memesByCategory = memeByC.get();
			for (ActionType actionT:
				 memesByCategory.keySet()) {
				key.add(actionT);
			}
		}
		else {
			for (ActionType action : Configurator.ActionType.values()) {
				if (action == ActionType.AJOUTLIEN || action == ActionType.RETRAITLIEN) {
					memeOfOneCategory = memeFactory.getMemeAvailable(action, false);
					if (memeOfOneCategory.size() > 0) {
						memesByCategory.put(action, memeOfOneCategory);
						key.add(action);
					}
				}
			}
		}

		ActionType[] keyz = new ActionType[memesByCategory.keySet().size()];
		for (int i = 0; i < key.size(); i++)
			keyz[i] = key.get(i);

		Hashtable<Integer, ArrayList<Meme>> composition = new Hashtable<>();
		ArrayList<Meme> memez = new ArrayList<>();
		indexOfMemesCombinaisonRecursion = -1;
		recursive(memesByCategory, memez, keyz, composition, -1);

		return composition;
	}

	/**
	 *            Renvoi
	 * @param indexAction
	 * @param memesByCategory
	 * @param actions
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void recursive(Hashtable<ActionType, ArrayList<Meme>> memesByCategory,
						   ArrayList<Meme> memes, ActionType[] actions,
						   Hashtable<Integer, ArrayList<Meme>> selection, int indexAction) {

		if ((++indexAction) < actions.length) {
			for (Meme meme : memesByCategory.get(actions[indexAction])) {
				ArrayList<Meme> copy = (ArrayList<Meme>) memes.clone();
				copy.add(meme);
				recursive(memesByCategory, copy, actions, selection, indexAction);
			}
		} else {
			indexOfMemesCombinaisonRecursion++;
			selection.put(Integer.parseInt("" + indexOfMemesCombinaisonRecursion),
					(ArrayList<Meme>) memes.clone());
		}
	}

	//endregion

	//region divers

    /** indicateurs liant echec d'application de meme et variation de densité.
     *
     * @return
     */
    private List<String> getFailXDensity(int nbActionTaken,double newDensity, int failSum){
        List<String> resultat = new ArrayList<>(Arrays.asList("After X failActions",""+nbActionTaken,"densityVaration[New-Old]", "" + (newDensity-lastDensity), "FailSum[ifNeg=rmvFail]", "" + failSum));
        lastDensity = newDensity;
        return resultat;

    }

	/**
	 * Utilisé pour mettre a jour l'affichage du nombre d'action par seconde
	 * réalisée.
	 *
	 */
	private void checkAPM() {
		long elapsed = System.nanoTime() - atmLastTime;
		if (elapsed >= Math.pow(10, 9)) {
			// mise a jour de l'affichage du nombre d'action par seconde.
			vueController.setDisplayNbAction(nbActionBySecond + " action/Sec");
			// remise a zero du compteur
			nbActionBySecond = 0;
			atmLastTime = System.nanoTime();
		} else {
			nbActionBySecond++;

		}
	}

	/**
	 * Définit le comportement d'attente entre les étapes de filtre etc des
	 * actions semi automatique
	 */
	private void pauseStepInSemiAutoAction() {
		Toolz.waitInMillisd(Configurator.semiAutoWaitingTime);
	}

	/**
	 *
	 * @param meme
	 * @param actualFilter
	 * @return
	 */
	private boolean filterOnSemiAuto(Meme meme, IFilter actualFilter) {
		if (meme != null && actualFilter != null
				&& actualFilter.getEnumType() == AgregatorType.HOPAWAY)
			return true;
		return false;
	}

	/**
	 * Si un lien existe entre one et two, renvoi true.
	 * @return
	 */
	private boolean isEntiteLinked(Entite first, Entite second, boolean directed) {
		Node one = first.getNode();
		Node two = second.getNode();
		boolean returnValue = false;

		if (one.getConnectedNodes().contains(two.getIndex()))
			return true;
		if (!directed)
			if (two.getConnectedNodes().contains(one.getIndex()))
				return true;

		if (one.getIndex() == two.getIndex())
			return true;

		return returnValue;
	}

	//endregion

	//region getter//Setter

	/**
	 * Renvoi, pour affichage, en condensé index, obj et meme. ttes entités
	 *
	 * @return index + obj + <meme.toShortString()>
	 */
	public String getEntitesInfoAsString() {
		String resultat = "";
		for (Entite entitee : entites) {
			resultat += "\n né" + entitee.getIndex();
			for (Meme meme : entitee.getMyMemes()) {
				resultat += "\n \t meme " + meme.toShortString();
			}
		}
		return resultat;
	}

	public ArrayList<Entite> getEntitesActive() {
		return entitesActive;
	}

	//endregion

	//endregion

	//region deprecated

	private void checkConsistency(){
		ArrayList<Meme> myMemes;
		for (Entite entite: entites) {
			myMemes = entite.getMyMemes();
			if(myMemes.size() > 2)
				System.out.println("TROP DE COMPORTEMENT");
			if(myMemes.size() == 2 && myMemes.get(0).equals(myMemes.get(1)))
				System.out.println("Scandale deux fois le meme");
		}
	}

	@Override
	public void handlerNbNodeChanged(NbNodeChangedEvent e) {
		entites.clear();
		bindNodeWithEntite(networkConstruct.getNetwork());
	}

	public class memeComparatorAscending implements Comparator<Meme> {
		@Override
		public int compare(Meme arg0, Meme arg1) {
			return arg0.toFourCharString().compareTo(arg1.toFourCharString());
		}

	}

	public class memeComparatorDescending implements Comparator<Meme> {
		public int compare(Meme arg0, Meme arg1) {
			return -1
					* arg0.toFourCharString()
					.compareTo(arg1.toFourCharString());
		}

	}

	/** Va retirer au noeud un edge aléatoirement si plus d'un noeud
	 *
	 */
	public void purgeLink() {

		Meme purify = memeFactory.getMemeFromFourString("PURIDG");
		purify.getAction().applyAction(null, entites);

//		Entite target;
//		ArrayList<Entite> connectedNodeSeveralConnection = new ArrayList<Entite>();
//
//		for (Entite entite : entites) {
//			connectedNodeSeveralConnection.clear();
//			if (entite.getDegree() > 1) {
//				for (Entite entite2 : entite.getConnectedEntite()) {
//					if (entite2.getDegree() > 1) {
//						connectedNodeSeveralConnection.add(entite2);
//					}
//				}
//
//				if (connectedNodeSeveralConnection.size() > 1) {
//					target = connectedNodeSeveralConnection.get
//							(Toolz.getRandomNumber(connectedNodeSeveralConnection.size()));
//					removeLink(entite, target);
//				}
//			}
//		}
	}

	/** Obtient les memes effectivements présent sur la map.
	 *
	 * @return la liste des memes possédés par les agents
	 */
	public ArrayList<Meme> getMemeOnMap() {
		ArrayList<Meme> memes = new ArrayList<Meme>();
		for (Entite entite : entites) {
			for (Meme meme : entite.getMyMemes()) {
				if (!memes.contains(meme))
					memes.add(meme);
			}
		}

		return memes;
	}

	/** Selection d'une action, en excluant la derniere choisi si il s'agit d'un
	 * ajout ou d'un retrait, et mis a jour de la dernier action faite.
	 *
	 * @param movingOne
	 * @return
	 */
	private Meme actionSelectionControlledVersion(Entite movingOne) {
		Meme selected = null;
		selected = movingOne.chooseActionExclusionVersion(lastAction);
		if (selected != null)
			if (selected.action.getActionType() == ActionType.RETRAITLIEN
					|| selected.action.getActionType() == ActionType.AJOUTLIEN)
				lastAction = selected.action.getActionType();

		return selected;
	}

	//endregion
}
package giteri.meme.entite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.Iterator;

import giteri.tool.math.Toolz;
import giteri.meme.mecanisme.AgregatorFactory.IAgregator;
import giteri.meme.mecanisme.AttributFactory.IAttribut;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.network.Network;
import giteri.network.network.Node;
import giteri.network.networkStuff.NetworkConstructor;
import giteri.network.networkStuff.WorkerFactory;
import giteri.tool.other.StopWatchFactory;
import giteri.run.ThreadHandler;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.ActionType;
import giteri.run.configurator.Configurator.AgregatorType;
import giteri.run.configurator.Configurator.AttributType;
import giteri.run.configurator.Configurator.FittingBehavior;
import giteri.run.configurator.Configurator.MemeDistributionType;
import giteri.run.controller.Controller.VueController;
import giteri.meme.event.ActionApplyEvent;
import giteri.meme.event.ActionApplyListener;
import giteri.meme.event.BehavTransmEvent;
import giteri.meme.event.BehaviorTransmissionListener;

/**
 * Classe qui gère les entités du réseau.
 *
 */
public class EntiteHandler extends ThreadHandler {

	// Region properties

	private VueController ihmController;
	private NetworkConstructor networkConstruct;

	// les entités du réseau
	protected ArrayList<Entite> entites;
	
	// Entite possedant des actions
	private ArrayList<Entite> entitesActive;
	private Hashtable<String, String> memeTranslationReadable;
	
	// Listener pour les évènements issu de l'obtention de giteri.meme ou application
	// de ce dernier.
	private ArrayList<ActionApplyListener> entityListeners;
	private ArrayList<BehaviorTransmissionListener> memeListeners;
	
	StopWatchFactory watcher ;
	private Meme addRandom = null, removeRandom = null;

	// Variable d'utilisation 
	private static int indexOfMemesCombinaisonRecursion;
	private long atmLastTime;
	private int cptModulo;
	private int cptActionAddTried = 1, cptActionRmvTried = 1;
	private ActionType lastAction = ActionType.RETRAITLIEN;
	private int nbActionBySecond;
	
	// EndRegion

	// Region constructeur Singleton
	protected static EntiteHandler INSTANCE = null;

	/** Constructeur sans param. Crée les memes de la map 
	 * 
	 */
	protected EntiteHandler() {

		if (Configurator.DisplayLogdebugInstantiation)
			System.out.println("EntiteHandler constructor Initialisation");

		networkConstruct = NetworkConstructor.getInstance();
		entites = new ArrayList<Entite>();
		entitesActive = new ArrayList<Entite>();
		entityListeners = new ArrayList<ActionApplyListener>();
		memeListeners = new ArrayList<BehaviorTransmissionListener>();
		memeTranslationReadable = new Hashtable<String, String>();

		generateMemeAvailableForMap();

		if (Configurator.displayLogTimeEating){
			watcher = StopWatchFactory.getInstance();
			watcher.addWatch("Main", "SelectionEntite");
			watcher.addWatch("Main", "ChoixAction");
			watcher.addWatch("ChoixAction", "one");
			watcher.addWatch("ChoixAction", "two");
			watcher.addWatch("ChoixAction", "three");
			watcher.addWatch("Main", "FaireAction");
			watcher.addWatch("FaireAction", "SelectionCible");
			watcher.addWatch("FaireAction", "ApplyAction");
		}
		
		if (Configurator.DisplayLogdebugInstantiation)
			System.out.println("EntiteHandler constructor Closing");
	
		bindNodeWithEntite(NetworkConstructor.getInstance().getNetwork());
		giveMemeToEntite(Configurator.methodOfGeneration);
	}

	/** instance d'EH qui commune a tous.
	 * 
	 * @return
	 */
	public static EntiteHandler getInstance() {
		if (INSTANCE == null)
			INSTANCE = new EntiteHandler();
		return INSTANCE;
	}

	// EndRegion

	// Region PUBLIC

	/** Les joies des constructeurs nécessitant l'un et l'autre
	 * 
	 * @param control
	 */
	public void setIHMController(VueController control) {
		ihmController = control;
	}

	/** Obtenir un thread a partir de la classe
	 * 
	 */
	public Thread getThread() {
		Thread returnThread = new Thread(this);
		returnThread.setName("EH");
		return returnThread;
	}

//	/** Mise en place du worker factory
//	 * 
//	 * @param wf
//	 */
//	public void setWorkerFactory(WorkerFactory wf){
//		workerFacto = wf;
//	}
	
	/** Run pour la création de lien, passage de giteri.meme, etc.
	 * 
	 */
	public void doRun() {
		if (!Configurator.turboMode){
			try 
			{
				Thread.sleep(Configurator.getThreadSleepMultiplicateur());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		OneStep();
		if(Configurator.displayLogTimeEating)
			watcher.publishResult();
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
			if (Configurator.displayLogAvgDegreeByMeme) {
				cptModulo++;
				if (cptModulo % (Configurator.refreshInfoRate * 25) == 0) {
					cptModulo = 0;
					System.out.println(checkPropertiesByMemePossession());
				}
			}
		}
	}

	/**
	 * Méthode de génération de giteri.network. A placer dans le NC? Va générer les
	 * liens, mais les noeuds doivent déjà etre présent. 0 empty, 1 30%, 2 full,
	 * 3 scalefree, 4 smallworld, 5 complet
	 * 
	 * @param activator
	 */
	public void generateNetwork(int activator) {
		networkConstruct.generateNetwork(activator);
	}

	// Region ENTITE

	/**
	 * Action qui va forcer chaque entité a essayer chacune des actions de son
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

		for (Integer integer : nodeIndex) {
			acting = entites.get(integer);
			for (Meme action : acting.getMyMemes()) {
				resultat = doAction(acting, action);
				if (!resultat.contains("Nope") && !resultat.contains("NOACTION")) {
					actionDone = true;
					return actionDone;
				}
			}
		}

		return actionDone;
	}

	/** Va retirer au noeud un edge aléatoirement si plus d'un noeud
	 * 
	 */
	public void purgeLink() {

		Entite target;
		ArrayList<Entite> connectedNodeSeveralConnection = new ArrayList<Entite>();

		for (Entite entite : entites) {
			connectedNodeSeveralConnection.clear();
			if (entite.getDegree() > 1) {
				for (Integer indexEventuality : entite.getConnectedNodesIndex()) {
					if (entites.get(indexEventuality).getDegree() > 1) {
						connectedNodeSeveralConnection.add(entites
								.get(indexEventuality));
					}
				}

				if (connectedNodeSeveralConnection.size() > 1) {
					target = connectedNodeSeveralConnection.get
							(Toolz.getRandomNumber(connectedNodeSeveralConnection.size()));
					removeLink(entite, target);
				}
			}
		}
	}

	// TODO REFACTORING A voir pour le positionnement de l'appel a cette fonction.
	/** Rappelle pour chaque entité la fonction qui définit les probabilités de
	 * choix des règles
	 */
	public void resetProba() {
		for (Entite entite : entites) {
			entite.defineRulesProbaLimited();
		}
	}

	/** Ajout d'un lien entre deux entitées, dans la couche giteri.network.
	 * 
	 * @param from
	 *            depuis cette entitées
	 * @param indexTo
	 *            jusqu'a celle la
	 */
	public boolean addLink(Entite from, Entite to) {
		if (!isEntiteLinked(from, to, false)) {
			// ajout dans la couche giteri.network; Ajout dans la couche giteri.meme
			// automatique car les entitées ont leur edges en passant par la
			// propriété dans les nodes.
			NetworkConstructor.getInstance().NMAddLink(
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
			return NetworkConstructor.getInstance().NMRemoveLink(
					from.getIndex(), to.getIndex(), false);
		}

		return false;
	}

	/** Renvoi une sous liste des agents auxquels est connecté l'asker
	 * 
	 * @param asker
	 * @return une sous liste d'entité
	 */
	public ArrayList<Entite> getLinkedEntite(Entite asker) {
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
	 * @param node
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
	 * lien et sans giteri.meme.
	 * 
	 */
	public void resetStat(boolean complete) {
		for (Entite entite : entites) {
			entite.resetStat();
		}

		entitesActive.clear();
		// TODO POTENTIAL ERROR Il ne semble pas qu'il soit nécessaire de tout reset puis le TRUE reset
		// vient de l'appel dans le giteri.fitting qui réapply lui giteri.meme les IModelMachin.
		if (complete){
			giveMemeToEntite(Configurator.methodOfGeneration);
		}
	}
	
	// EndRegion

	// Region EVENT

	/** Pour lancer les évènements de type giteri.meme transmis
	 * crée un evenement pour la mise a jour de l'interface Il s'agit d'un
	 * evenement de type acquisition ou perte d'un giteri.meme.
	 */
	public void eventMemeChanged(Entite entiteConcernee, Meme ajoutOrR, String message) {

		// On crée un événement rappelant l'état courant concernant les memes;
		synchronized (entitesActive) {
			entitesActive.add(entiteConcernee);
		}
		
		BehavTransmEvent myEvent = new BehavTransmEvent(this, entiteConcernee, ajoutOrR, message);

		// On prévient les entites qui listen 
		for (BehaviorTransmissionListener memeListener : memeListeners) {
			memeListener.handlerBehavTransm(myEvent);
		}
	}

	/** Pour lancer les évènements de type action réalisée.
	 * 
	 * @param entiteConcernee
	 * @param actionRealisee
	 * @param message
	 */
	public void eventActionDone(Entite entiteConcernee, Meme memeRealisee, String message) {

		// On crée un événement rappelant l'état courant concernant les memes;
		ActionApplyEvent myEvent = new ActionApplyEvent(this, entiteConcernee, memeRealisee, message);

		// ON PREVIENT LES ENTITES QUI LISTEN
		for (ActionApplyListener entityListener : entityListeners) {
			entityListener.handlerActionApply(myEvent);
		}
	}

	/** Ajout d'un listener a la liste des listeners a prévenir en cas d'event de
	 * type entity
	 * 
	 * @param myListener
	 */
	public void addEntityListener(ActionApplyListener myListener) {
		if (!entityListeners.contains(myListener)) {
			entityListeners.add(myListener);
		}
	}

	/** Retrait d'un listener depuis la liste des listeners
	 * 
	 * @param myListener
	 */
	public void removeEntityListener(ActionApplyListener myListener) {
		if (entityListeners.contains(myListener)) {
			entityListeners.remove(myListener);
		}
	}

	/** Ajout d'un listener a la liste des listeners a prévenir en cas d'event de
	 * type giteri.meme
	 * 
	 * @param myListener
	 */
	public void addMemeListener(BehaviorTransmissionListener myListener) {
		if (!memeListeners.contains(myListener)) {
			memeListeners.add(myListener);
		}
	}

	/** Retrait d'un listener depuis la liste des listeners
	 * 
	 * @param myListener
	 */
	public void removeMemeListener(BehaviorTransmissionListener myListener) {
		if (memeListeners.contains(myListener)) {
			memeListeners.remove(myListener);
		}
	}

	// EndRegion

	// Region MEME

	/** Distribut les memes aux entités suivant certain mode.
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
		case FourWithInverted:
			giveMemeWithInvertd();
			break;
		case OnlyOneAgent:
			giveMemeToEntiteExceptPurple();
			break;
		case FollowingFitting:
			giveMemeToEntiteFollowingFitting();
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

	/** Applique directement depuis une hash d'index // comportements les
	 * behavior au nodes spécifiées par la key.
	 * 
	 * @param affectation
	 */
	public void giveMemeToEntite(Hashtable<Integer, ArrayList<Meme>> affectation) {
		for (Integer index : affectation.keySet()) {
			if (entites.size() > index)
				for (Meme memeToAdd : affectation.get(index)) {
					eventMemeChanged(entites.get(index), entites.get(index).addMeme(memeToAdd, true),
							Configurator.MemeActivityPossibility.AjoutMeme.toString());
				}
		}
	}

	/** Applique directement depuis une liste de giteri.meme au X premier agents pour les
	 * recevoir. Si option de défault behavior, donne des memes fluides aux autres agent.
	 * 
	 * @param affectation
	 */
	public void giveMemeToEntiteXFirst(ArrayList<Meme> memes) {
		int i = 0;
		ArrayList<Entite> entiteContente = new ArrayList<Entite>();
		for (Meme meme : memes) {
			eventMemeChanged(entites.get(i),entites.get(i).addMeme(meme, true), Configurator.MemeActivityPossibility.AjoutMeme.toString());
			entiteContente.add(entites.get(i));
			i++;
		}
		
		if(Configurator.initializeDefaultBehavior) giveFluideMeme(entiteContente);
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
	public Hashtable<Integer, ArrayList<Meme>> getMemeAvailable(FittingBehavior setAsked) {
		
		Hashtable<Integer, ArrayList<Meme>> memes = new Hashtable<Integer, ArrayList<Meme>>();
		if (setAsked == FittingBehavior.onlyComplex || setAsked == FittingBehavior.simpleAndComplex) {
			memes = getMemeCombinaisonAvailable();
		}
		if (setAsked == FittingBehavior.onlySimple || setAsked == FittingBehavior.simpleAndComplex) {
			int lastIndex = memes.size();
			for (Meme meme : MemeFactory.getInstance().getMemeAvailable(false))
				Toolz.addElementInHashArray(memes, ++lastIndex, meme);
		}

		return memes;
	}

	// TODO REFACTORING A delete probablement
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
	
	/** Renvoi la liste des memes dispo sur la map en liste de string. 
	 * Utilisé pour les couleurs sur le graphe
	 * 
	 * @param setAsked Voir @getMemeAvailable
	 * @return
	 */
	public ArrayList<String> getMemeAvailableAsString(FittingBehavior setAsked) {
		ArrayList<String> memesAsString = new ArrayList<String>();
		Hashtable<Integer, ArrayList<Meme>> memes = getMemeAvailable(setAsked);
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
		for (String string : memeTranslationReadable.keySet()) 
			if(memeCombinaison.contains(string))
				compo += "." + memeTranslationReadable.get(string);
		return compo;
	}

	/** Regarde le propriété des éléments du réseau, en fonction de la
	 * combinaison de giteri.meme qu'ils possedent.
	 * 
	 */
	public String checkPropertiesByMemePossession() {
		Hashtable<String, ArrayList<Entite>> entiteByMemePossession = new Hashtable<String, ArrayList<Entite>>();
		@SuppressWarnings("unchecked")
		ArrayList<Entite> toExamine = (ArrayList<Entite>) entites.clone();
		ArrayList<Integer> SelfDegrees = new ArrayList<Integer>();
		ArrayList<Integer> othersDegrees = new ArrayList<Integer>();
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

				Toolz.addElementInHashArray(entiteByMemePossession, memes,
						entite);
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
						System.err
								.println("Ne DEVRAIT PAS ETRE POSSIBLE EH CHECKPROPERTIESBY...");
				}
			}

			resultat += "("
					+ entiteByMemePossession.get(memeCombinaison).size()
					+ ") - "
					+ "Degree moyen pour la combinaison de giteri.meme "
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

	// EndRegion

	// EndRegion

	// Region PRIVATE

	// Region ACTION

	/** va lancer l'action d'une entitée
	 * 
	 */
	private String runEntite() {

		String rez;
		synchronized (WorkerFactory.getInstance().waitingForReset) {
			
			if (Configurator.displayLogTimeEating)
			{
				watcher.startWatch("Main");
				watcher.startWatch("SelectionEntite");
			}
			Meme memeAction;

			// Choix d'une entitée au hasard
			Entite movingOne = SelectActingEntite();

			if (movingOne == null) {
				if(!Configurator.jarMode) System.err.println("Bullshit de liste entiteActive et de .giteri.run() qui s'arrete pas immédiatement");
				return ("Nope pas d'entite prete");
			}
			if (Configurator.displayLogTimeEating)
				watcher.stopWatch("SelectionEntite");

			// CHOIX DE L'ACTION POUR CETTE ENTITE
			if (Configurator.displayLogTimeEating)
				watcher.startWatch("ChoixAction");

			memeAction = actionSelectionRulesVersion(movingOne);
			if (Configurator.displayLogTimeEating)
				watcher.stopWatch("ChoixAction");

			// APPLICATION DE L'ACTION
			if (Configurator.displayLogTimeEating)
				watcher.startWatch("FaireAction");
			rez = doAction(movingOne, memeAction);
			
			if (Configurator.displayLogTimeEating)
				watcher.stopWatch("FaireAction");

			if (Configurator.displayLogTimeEating)
				watcher.stopWatch("Main");

			WorkerFactory.getInstance().getCalculator().incrementNbActionRelative();

			if (Configurator.displayLogTimeEating)
				watcher.publishResult();

			if (Configurator.displayLogRatioTryAddOverTryRmv) {
				if (memeAction != null)
					if (memeAction.action.getActionType() == ActionType.AJOUTLIEN)
						cptActionAddTried++;
					else if (memeAction.action.getActionType() == ActionType.RETRAITLIEN)
						cptActionRmvTried++;
				System.out.println("Tried Add/rmv " + (double) cptActionAddTried / cptActionRmvTried);
			}

			if (Configurator.semiStepProgression) 
			{
				giveEntiteBaseColor();
				System.out.println("Remise a zero des couleurs");
				if (filterOnSemiAuto(null, null))
					pauseStepInSemiAutoAction();
			}
		}
		return rez;
	}

	/** Selection de l'entité qui va agir
	 * 
	 * @return
	 */
	private Entite SelectActingEntite() {
		if (entitesActive.size() == 0) {
			return null; //entites.get(0);
		}
		return entitesActive.get(Toolz.getRandomNumber(entitesActive.size()));
	}

	/** Selection d'une action pour l'entité en action rules version
	 * 
	 * @param movingOne
	 * @return le giteri.meme sélectionné
	 */
	private Meme actionSelectionRulesVersion(Entite movingOne) {
		return movingOne.chooseAction();
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

	/** Application de l'action de l'entité
	 * 
	 * @param movingOne
	 * @param memeAction
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String doAction(Entite movingOne, Meme memeAction) {
		String actionDone = "";
		String memeApply = "";
		ArrayList<Entite> cibles = new ArrayList<Entite>();
		ArrayList<Integer> ciblesIndex = new ArrayList<Integer>();
		IAgregator currentFilter = null;

		// Execution d'un giteri.meme de l'entite.
		if (memeAction != null) {
			if (Configurator.displayLogTimeEating)
				watcher.startWatch("SelectionCible");
			cibles = (ArrayList<Entite>) entites.clone();
			cibles.remove(movingOne);

			// Pour chaque attribut sur lesquels on applique des filtres
			for (IAttribut<Integer> attribut : memeAction.getAttributs()) {
				if (Configurator.semiStepProgression && filterOnSemiAuto(null, null)) {
					System.out.println("On va appliquer les filtres suivants pour l'action " + memeAction.name);
					System.out.println(memeAction.getAgregators(attribut.toString()).values()); }

				// Pour chaque filtre appliqué a un attribut
				for (int order = 0; order < memeAction.getAgregators(attribut.toString()).size(); order++) {
					currentFilter = memeAction.getAgregators(attribut.toString()).get(order);
					cibles = currentFilter.applyAggregator(movingOne, cibles, attribut);

					// Dans le cas ou on veut un mode semi automatique
					if (Configurator.semiStepProgression && filterOnSemiAuto(memeAction, currentFilter)) {
						ciblesIndex.clear();
						for (Entite entite : cibles)
							ciblesIndex.add(entite.getIndex());
						System.out.println("Application du filtre suivant: " + currentFilter.getFourCharName());
						this.giveEntiteTargetedColor(movingOne.getIndex(), ciblesIndex);
						pauseStepInSemiAutoAction(); 
					}
				}
			}

			if (Configurator.displayLogTimeEating)
				watcher.stopWatch("SelectionCible");
			if (Configurator.displayLogTimeEating)
				watcher.startWatch("ApplyAction");
			
			if (cibles.size() == 1) {
				actionDone += memeAction.getAction().applyAction(movingOne, cibles);

				// PROPAGATION du giteri.meme
				if (Configurator.usePropagation)
					for (Entite entite : cibles) 
					{
						// Transmission de l'un de behavior possédé par l'acting.
						if (Configurator.usePropagationSecondGeneration) {
							Meme selectedMeme = movingOne.chooseAction();
							memeApply = selectedMeme.toFourCharString();
							if (entite.receiveMeme(selectedMeme))
								eventMemeChanged(entite, selectedMeme, Configurator.MemeActivityPossibility.AjoutMeme.toString());}
						else 
							if (entite.receiveMeme(memeAction))
								eventMemeChanged(entite, memeAction,	Configurator.MemeActivityPossibility.AjoutMeme.toString());
					}

				// evenement d'application d'une action
				// 2- A revoir niveau timing
				eventActionDone(movingOne, memeAction, actionDone);

			} else {
				if (cibles.size() > 1) System.err.println("Plusieurs cibles pour une action, pas normal");

				actionDone = "Nope, Entite " + movingOne.getIndex() 
						   + " Aucune(ou trop de) cible pour l'action" + memeAction.toFourCharString();
				eventActionDone(movingOne, null, "NOTARGET " + actionDone);
			}
			if (Configurator.displayLogTimeEating)
				watcher.stopWatch("ApplyAction");
		} else {
			actionDone = "Nope, Entite " + movingOne.getIndex() + " Liste d'action vide ou aucune action sélectionnée";
			eventActionDone(movingOne, null, "NOACTION");}

		if (Configurator.displayLogMemeApplication)
			System.out.println(memeApply + " " + actionDone);

		return actionDone;
	}

	// EndRegion

	// Region MEME

	/** Donne des memes aux entités. Ici chaque giteri.meme est donnée une fois pour une
	 * entité dans le réseau.
	 * x = nb de giteri.meme la map. Nombre d'agent avec comportement = x
	 */
	private void giveMemeToEntiteOnlyBasis() {
		int i = 0;
		ArrayList<Entite> entiteContente = new ArrayList<Entite>();
		for (Meme meme : MemeFactory.getInstance().getMemeAvailable(true)) {
			entites.get(++i).addMeme(meme, true);
			eventMemeChanged(entites.get(i), meme, Configurator.MemeActivityPossibility.AjoutMeme.toString());
			entiteContente.add(entites.get(i));
		}
		
		if(Configurator.initializeDefaultBehavior) giveFluideMeme(entiteContente);
	}

	/** Donne les combinaisons de giteri.meme existantes sur la map à tous les agents, un par un.
	 * 
	 */
	private void giveMemeToEntiteGeneric() {
		Hashtable<Integer, ArrayList<Meme>> composition ;//= new Hashtable<Integer, ArrayList<Meme>>();
		composition = getMemeCombinaisonAvailable();
		
		for (Entite entite : entites) 
			for (Meme meme : composition.get( entite.getIndex()%composition.keySet().size() )) 
				eventMemeChanged(entite, entite.addMeme(meme, true), Configurator.MemeActivityPossibility.AjoutMeme.toString());
		
	}

	/** Bullshit.
	 * 
	 */
	private void giveMemeToEntiteExceptPurple() {

	}

	/** Donne tout les memes basics réparti équitablement sur tout les nodes
	 * 
	 */
	private void giveMemeBasicAllNodes() {
		int nbMeme = MemeFactory.getInstance().getMemeAvailable(false).size();
		int partSize = entites.size() / nbMeme;
		int lastIndex = 0;
		int part = 0;

		for (Meme meme : MemeFactory.getInstance().getMemeAvailable(false)) {
			part++;
			for (int i = lastIndex; i < partSize * part; i++) 
				eventMemeChanged(entites.get(i),entites.get(i).addMeme(meme, true),
								Configurator.MemeActivityPossibility.AjoutMeme.toString());
			lastIndex = partSize * part;
		}
	}

	/** BULLSHIT
	 * 
	 */
	private void giveMemeWithInvertd() {
		
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
				memeOfOneCategory = MemeFactory.getInstance().getMemeAvailable(action, false);
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

		for (Integer integer : composition.keySet())
			for (Meme meme : composition.get(integer))
				eventMemeChanged(entites.get(integer), entites.get(integer).addMeme(meme, true),
								Configurator.MemeActivityPossibility.AjoutMeme.toString());

	}

	/**
	 * 
	 */
	private void giveMemeToEntiteFollowingFitting() {

	}

	/**
	 * 
	 */
	private void giveMemeToEntiteSpecific() {
		int i = 0;
		Hashtable<Integer, ArrayList<Meme>> behaviors = new Hashtable<Integer, ArrayList<Meme>>();
		Hashtable<String, Integer> nameKVindex = new Hashtable<String, Integer>();
		Meme toAdd;
		Entite entiteReceptrice;
		ArrayList<Double> scParamSet = new ArrayList<Double>(Arrays.asList(0.788335449538696,0.373092466173732,0.292052580578804,0.438882845642738,0.109153677952613));
		ArrayList<Double> swParamSet3 = new ArrayList<Double>(Arrays.asList(0.907489970963927,0.363546615459677,0.458976194767827,0.247873953220028,0.984710568248182));
		ArrayList<Double> currentParamSet = swParamSet3 ; 
		
		for (Meme meme : MemeFactory.getInstance().getMemeAvailable(true)) {
			entiteReceptrice = entites.get(i);
			if(meme.getName().compareTo("Add∞") == 0){
				meme.probaOfPropagation = currentParamSet.get(i);
				entiteReceptrice.addMeme(meme, true);
				eventMemeChanged(entiteReceptrice, meme, Configurator.MemeActivityPossibility.AjoutMeme.toString());
			}
			if(meme.getName().compareTo("AddØ-Hop") == 0){
				meme.probaOfPropagation = currentParamSet.get(i);
				entiteReceptrice.addMeme(meme, true);
				eventMemeChanged(entiteReceptrice, meme, Configurator.MemeActivityPossibility.AjoutMeme.toString());
			}
			if(meme.getName().compareTo("Rmv-") == 0){
				meme.probaOfPropagation = currentParamSet.get(i);
				entiteReceptrice.addMeme(meme, true);
				eventMemeChanged(entiteReceptrice, meme, Configurator.MemeActivityPossibility.AjoutMeme.toString());
			}
			if(meme.getName().compareTo("Rmv+") == 0){
				meme.probaOfPropagation = currentParamSet.get(i);
				entiteReceptrice.addMeme(meme, true);
				eventMemeChanged(entiteReceptrice, meme, Configurator.MemeActivityPossibility.AjoutMeme.toString());
			}
			if(meme.getName().compareTo("RmvØ-2hop") == 0){
				meme.probaOfPropagation = currentParamSet.get(i);
				entiteReceptrice.addMeme(meme, true);
				eventMemeChanged(entiteReceptrice, meme, Configurator.MemeActivityPossibility.AjoutMeme.toString());
			}
			
			i++;
		}
//		
//
//		for (Meme giteri.meme : MemeFactory.getInstance().getMemeAvailable(false)) {
//			Toolz.addElementInHashArray(behaviors, i, giteri.meme);
//			nameKVindex.put(giteri.meme.toFourCharString(), i);
//			i++;
//		}
//
//		for (Entite entite : entites) {
//
//			if (entite.getIndex() >= 1 && entite.getIndex() < 2) {
//				toAdd = behaviors.get(nameKVindex.get("RMLKDGRDMMNSPLK"))
//						.get(0);
//				// eventMemeChanged(entite, entite.addMeme(toAdd, true),
//				// Configurator.MemeActivityPossibility.AjoutMeme.toString());
//				toAdd = behaviors.get(nameKVindex.get("ADLKDGRDMNTLK")).get(0);
//				eventMemeChanged(entite, entite.addMeme(toAdd, true),
//						Configurator.MemeActivityPossibility.AjoutMeme
//								.toString());
//			}
//
//			if (entite.getIndex() >= 2 && entite.getIndex() < 3) {
//				toAdd = behaviors.get(nameKVindex.get("RMLKDGRDMMNIFLK"))
//						.get(0);
//				eventMemeChanged(entite, entite.addMeme(toAdd, true),
//						Configurator.MemeActivityPossibility.AjoutMeme
//								.toString());
//			}
//
//			// Hop away en ajout
//			if (entite.getIndex() >= 3 && entite.getIndex() < 4) {
//				toAdd = behaviors.get(nameKVindex.get("ADLKDGRDMNTLKHA"))
//						.get(0);
//				// eventMemeChanged(entite, entite.addMeme(toAdd, true),
//				// Configurator.MemeActivityPossibility.AjoutMeme.toString());
//			}
//			// ajout not linked mine inf
//			if (entite.getIndex() >= 4 && entite.getIndex() < 5) {
//				toAdd = behaviors.get(nameKVindex.get("ADLKDGRDMMNIFNTLK"))
//						.get(0);
//				// eventMemeChanged(entite, entite.addMeme(toAdd, true),
//				// Configurator.MemeActivityPossibility.AjoutMeme.toString());
//				toAdd = behaviors.get(nameKVindex.get("RMLKDGRDMLK")).get(0);
//				// eventMemeChanged(entite, entite.addMeme(toAdd, true),
//				// Configurator.MemeActivityPossibility.AjoutMeme.toString());
//			}
//			// add the most - si deja add pas d'effet
//			if (entite.getIndex() >= 5 && entite.getIndex() < 6) {
//				toAdd = behaviors.get(nameKVindex.get("ADLKDGRDMMT")).get(0);
//				// eventMemeChanged(entite, entite.addMeme(toAdd, true),
//				// Configurator.MemeActivityPossibility.AjoutMeme.toString());
//			}
//		}
	}

	// TODO REFACTORING Prendre les memes a appliquer a tous en parametre plutot qu'avoir des variables statics
	/** Dote les entités qui n'ont pas encore d'action des actions Add et remove de base. 
	 *  
	 * @param entiteAlreadyDone
	 */
	private void giveFluideMeme(ArrayList<Entite> entiteAlreadyDone){
		for (Entite entite : entites) {
			if(!entiteAlreadyDone.contains(entite)){
				if(addRandom != null)eventMemeChanged(entite, entite.addMeme(addRandom, false), Configurator.MemeActivityPossibility.AjoutMeme.toString());
				if(removeRandom != null)eventMemeChanged(entite, entite.addMeme(removeRandom, false), Configurator.MemeActivityPossibility.AjoutMeme.toString());
			}
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
			node = (Node) iterator.next();
			toBindToNode = new Entite();
			toBindToNode.setNode(node);
			entites.add(toBindToNode);
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

	/**
	 * 
	 * @param targeted
	 */
	private void giveEntiteTargetedColor(Integer actingEntite,
			ArrayList<Integer> targeted) {
		NetworkConstructor.getInstance().changeColorClass(actingEntite,targeted);
	}

	/**
	 * 
	 */
	private void giveEntiteBaseColor() {
		NetworkConstructor.getInstance().resetColorClass();
	}

	public void synchronizeNodeConnectionWithEntiteConnection(Network network) {
		Entite concerne;

		for (Node node : network.getNodes()) {
			concerne = getEntityCorresponding(node.getIndex());
			concerne.resetEntiteStuff();
			for (Integer index : node.getConnectedNodes()) {
				concerne.addConnectedEntite(getEntityCorresponding(index));
			}
		}
	}

	// TODO REFACTORING Mieux positionner l'appel a cette fonction? Plutot que dans l'instanciation d'EH? Paramétriser
	// Ca pour en faire un truc joli depuis un main qui l'appel ( Jar etc )
	/**
	 * Génère les memes disponibles sur la map, qui seront associés ou non aux
	 * entités. Ajouté directement dans la liste des memes dispos du memeHandler
	 * 
	 */
	private void generateMemeAvailableForMap() {

		MemeFactory memeFactory = MemeFactory.getInstance();
		ArrayList<AttributType> attributs = new ArrayList<AttributType>();
		Hashtable<AttributType, Hashtable<Integer, AgregatorType>> KVAttributAgregator = new Hashtable<AttributType, Hashtable<Integer, AgregatorType>>();
		Hashtable<Integer, AgregatorType> agregators = new Hashtable<Integer, AgregatorType>();

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
		AgregatorType theMost = AgregatorType.THEMOST;
		KVAttributAgregator.put(degree, agregators);

		attributs.add(degree);
		agregators.put(0, notLinked);
		agregators.put(1, mineInf);
		agregators.put(2, random);
//		memeFactory.registerMemeAction("Add+", .4, add, attributs,KVAttributAgregator, true);

		agregators.clear();
		agregators.put(0, notLinked);
		agregators.put(1, mineSup);
		agregators.put(2, random);
//		memeFactory.registerMemeAction("Add-",.4,add, attributs,KVAttributAgregator, true);

		agregators.clear();
		agregators.put(0, theMost);
		agregators.put(1, notLinked);
		agregators.put(2, random);
		memeFactory.registerMemeAction("Add∞", .4, add, attributs, KVAttributAgregator, true);

		agregators.clear();
		agregators.put(0, hopAWay);
		agregators.put(1, notLinked);
		agregators.put(2, random);
		memeFactory.registerMemeAction("AddØ-Hop", .4, add, attributs,KVAttributAgregator, true);

		agregators.clear();
		agregators.put(0, notLinked);
		agregators.put(1, random);
//		addRandom = memeFactory.registerMemeAction("AddØ",0.,add, attributs, KVAttributAgregator, true);
		
		agregators.clear();
		agregators.put(0, linked);
		agregators.put(1, random);
//		removeRandom = memeFactory.registerMemeAction("RmvØ",.4,remove, attributs,  KVAttributAgregator, false);

		agregators.clear();
		agregators.put(0, linked);
		agregators.put(1, mineSup);
		agregators.put(2, random);
		memeFactory.registerMemeAction("Rmv-", .4, remove, attributs, KVAttributAgregator, true);

		agregators.clear();
		agregators.put(0, linked);
		agregators.put(1, mineInf);
		agregators.put(2, random);
		memeFactory.registerMemeAction("Rmv+", .4, remove, attributs, KVAttributAgregator, true);
		
		agregators.clear();
		agregators.put(0, hopAWay);
		agregators.put(1, random);
		memeFactory.registerMemeAction("RmvØ-2hop", .4, remove, attributs, KVAttributAgregator, true);

		agregators.clear();
		// memeFactory.getMemeAction("Puri",0,puri, attributs,
		// KVAttributAgregator, true);

		for (Meme memeDispo : MemeFactory.getInstance().getMemeAvailable(false)) {
			memeTranslationReadable.put(memeDispo.toFourCharString(),memeDispo.getName());
		}
	
		MemeFactory lol = MemeFactory.getInstance();
		lol.getClass();
	
	}

	/**
	 * Renvoi une hashtable contenant les combinaisons possibles de giteri.meme avec
	 * les memes sur la map. Un giteri.meme du giteri.meme type par agent par combinaison.
	 * (Type: Ajout, retrait .. ) dépend de l'action appliquée
	 * 
	 * @return une hash Int ( qui n'a pas de signification ), combinaison de
	 *         giteri.meme.
	 */
	private Hashtable<Integer, ArrayList<Meme>> getMemeCombinaisonAvailable() {
		Hashtable<ActionType, ArrayList<Meme>> memesByCategory = new Hashtable<ActionType, ArrayList<Meme>>();
		ArrayList<Meme> memeOfOneCategory;
		ArrayList<ActionType> key = new ArrayList<ActionType>();

		for (ActionType action : Configurator.ActionType.values()) {
			if (action == ActionType.AJOUTLIEN || action == ActionType.RETRAITLIEN) {
				memeOfOneCategory = MemeFactory.getInstance().getMemeAvailable(action, false);
				if (memeOfOneCategory.size() > 0) {
					memesByCategory.put(action, memeOfOneCategory);
					key.add(action);
				}
			}
		}

		ActionType[] keyz = new ActionType[memesByCategory.keySet().size()];
		for (int i = 0; i < key.size(); i++)
			keyz[i] = key.get(i);

		Hashtable<Integer, ArrayList<Meme>> composition = new Hashtable<Integer, ArrayList<Meme>>();
		ArrayList<Meme> memez = new ArrayList<Meme>();
		indexOfMemesCombinaisonRecursion = -1;
		recursive(memesByCategory, memez, keyz, composition, -1);

		return composition;
	}

	/**
	 * 
	 * @param compo
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
				recursive(memesByCategory, copy, actions, selection,
						indexAction);
			}
		} else {
			indexOfMemesCombinaisonRecursion++;
			selection.put(Integer.parseInt("" + indexOfMemesCombinaisonRecursion),
					(ArrayList<Meme>) memes.clone());
		}
	}

	// EndRegion

	/**
	 * Utilisé pour mettre a jour l'affichage du nombre d'action par seconde
	 * réalisée.
	 * 
	 */
	private void checkAPM() {
		long elapsed = System.nanoTime() - atmLastTime;
		if (elapsed >= Math.pow(10, 9)) {
			// mise a jour de l'affichage du nombre d'action par seconde.
			ihmController.setDisplayNbAction(nbActionBySecond + " action/Sec");
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
	private boolean filterOnSemiAuto(Meme meme, IAgregator actualFilter) {
		if (meme != null && actualFilter != null
				&& actualFilter.getEnumType() == AgregatorType.HOPAWAY)
			return true;
		return false;
	}

	/**
	 * Si un lien existe entre one et two, renvoi true.
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean isEntiteLinked(int first, int second, boolean directed) {
		Node one = entites.get(first).getNode();
		Node two = entites.get(second).getNode();
		boolean returnValue = false;

		if (one.getConnectedNodes().contains(second))
			return true;
		if (!directed)
			if (two.getConnectedNodes().contains(first))
				return true;

		if (first == second)
			return true;

		return returnValue;
	}

	/**
	 * Si un lien existe entre one et two, renvoi true.
	 * 
	 * @param one
	 * @param two
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

	// Region getter//Setter

	/**
	 * Renvoi, pour affichage, en condensé index, obj et giteri.meme. ttes entités
	 * 
	 * @return index + obj + <giteri.meme.toShortString()>
	 */
	public String getEntitesInfoAsString() {
		String resultat = "";
		for (Entite entitee : entites) {
			resultat += "\n né" + entitee.getIndex();
			for (Meme meme : entitee.getMyMemes()) {
				resultat += "giteri/meme " + meme.toShortString();
			}
		}
		return resultat;
	}

	// EndRegion

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

	// EndRegion

}
package giteri.meme.entite;

import giteri.meme.event.ActionApplyEvent;
import giteri.meme.event.BehavTransmEvent;
import giteri.meme.event.IActionApplyListener;
import giteri.meme.event.IBehaviorTransmissionListener;
import giteri.meme.mecanisme.FilterFactory;
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
import giteri.run.controller.Controller.VueController;
import giteri.run.interfaces.Interfaces;
import giteri.run.interfaces.Interfaces.IUnitOfTransfer;
import giteri.tool.math.Toolz;
import giteri.tool.objects.ObjectRef;
import giteri.tool.other.StopWatchFactory;

import java.util.*;
import static giteri.run.configurator.Configurator.*;

/**
 * Classe qui gère les entités du réseau.
 *
 */
public class EntiteHandler extends ThreadHandler implements INbNodeChangedListener,IBehaviorTransmissionListener{

	//region properties & constructeur
	private VueController vueController;
	private NetworkConstructor networkConstruct;
	private MemeFactory memeFactory;
	private WorkerFactory workerFactory;
	private FilterFactory filterFactory;

	// les entités du réseau
	protected Set<Entite> entites;
	// Entite possedant des actions
	private ArrayList<Entite> entitesActive;

	private List<IUnitOfTransfer> memeFittingApplied;
	private IUnitOfTransfer<CoupleMeme> doubleRandom = null;
	private Map<IUnitOfTransfer, Double> kvMemeCodeNbEntities;

	public MemeProperties memeProperties;

	private boolean allTransmitted = false;
	private boolean allAddTransmitted = false;
	private boolean allrmvTransmitted = false;

	// Listener pour les évènements issu de l'obtention de meme ou application
	// de ce dernier.
	private ArrayList<IActionApplyListener> entityListeners;
	private ArrayList<IBehaviorTransmissionListener> memeListeners;





	// Variable d'utilisation
	private static int indexOfMemesCombinaisonRecursion;
	private long atmLastTime;
	private int cptModulo;
	private int cptMemePossession;
	private int cptActionAddTried = 1, cptActionRmvTried = 1;
	private TypeOfUOT lastAction = TypeOfUOT.RETRAITLIEN;
	private int nbActionBySecond;
	private List<String> toDisplayForRatio; // String pour affichage en utilisant la vueManager
	private double lastDensity;
	private int sumFailAction;
	private ObjectRef<Integer> nbFail = new ObjectRef<>(0);

	// Variable utilisé dans la fonction doAction - fn appelée a chaque step
	Set<Entite> cibles;
	Set<Integer> ciblesIndex;
	IFilter currentFilter;
	String attribString;
	String actionDone;

	/** Constructeur sans param.
	 *
	 */
	public EntiteHandler(NetworkConstructor networkC, MemeFactory memeF, WorkerFactory workF) {

		networkConstruct = networkC;
		memeFactory = memeF;
		workerFactory = workF;
		filterFactory = new FilterFactory();

		entites = new HashSet<>();
		entitesActive = new ArrayList<>();
		entityListeners = new ArrayList<>();
		memeListeners = new ArrayList<>();
		toDisplayForRatio = new ArrayList<>();
		memeProperties = new MemeProperties();

		if(Configurator.displayMemePossessionEvolution)
			kvMemeCodeNbEntities = new HashMap<>();
		this.addMemeListener(this);
	}

	public void initialisation(){
		generateMemeAvailableForMap();
		bindNodeWithEntite(networkConstruct.getNetwork());
	}

	/**
	 *
	 */
	public void updateMemeAvailableForProperties(){

		memeProperties.iUnitOfTransfers = this.memeFittingApplied;

		//combinaison de meme présent sur le run, classé par type d'action
		Hashtable<TypeOfUOT, ArrayList<Interfaces.IUnitOfTransfer>> iOTByCategory = new Hashtable<>();
		for (Interfaces.IUnitOfTransfer meme: this.memeFittingApplied)
			Toolz.addElementInHashArray(iOTByCategory,meme.getActionType(),meme);

		// Quand version classique, on crée les couples disponibles sur la map

		memeProperties.memeCombinaisonFittingAvailable = new HashMap<>();
		int i = 0;
		for (IUnitOfTransfer iUnitOfTransfer : this.memeFittingApplied) {
			memeProperties.memeCombinaisonFittingAvailable.put(i++, new ArrayList<IUnitOfTransfer>(Arrays.asList(iUnitOfTransfer)));

		}
	}

	//endregion

	//region Thread

	/** Les joies des constructeurs nécessitant l'un et l'autre
	 *
	 * @param control
	 */
	public void setVueController(VueController control) {
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
	 kvMemeCodeNbEntities = new HashMap<>();
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
		runEntite();
		if(!Configurator.jarMode){
			checkAPM();
		}
		cptModulo++;

		int multiRefresher = 200;
		// Indicateur meme repartition, etc.
		if(Configurator.displayMemePosessionDuringSimulation && (cptModulo % (Configurator.refreshInfoRate * multiRefresher) == 0) ) {
			// affichage sur l'IHM de la distribution des memes

				Map<IUnitOfTransfer, Integer> means = memeProperties.degreeMeanOfIOT;
				Map<IUnitOfTransfer, Integer> nbParti = new Hashtable<>();
				means.clear();
				for (Entite entite : entitesActive) {
					Toolz.addCountToElementInHashArray(means,entite.getMyUnitOfT().get(0),entite.getDegree() );
					Toolz.addCountToElementInHashArray(nbParti,entite.getMyUnitOfT().get(0),1 );
				}

				int nbDudy;
				for (IUnitOfTransfer iUnitOfTransfer : means.keySet()) {
					nbDudy = nbParti.get(iUnitOfTransfer);
					if(nbDudy != 0)
						means.put(iUnitOfTransfer, means.get(iUnitOfTransfer) / nbDudy);
					else
						means.put(iUnitOfTransfer, 0);
				}

				vueController.displayMemeUsage(cptModulo,
						memeProperties.getNbActivationByMemes(),
						means,
						memeProperties.lastHundredActionDone);
		}

		if(Configurator.displayMemePossessionEvolution && cptModulo % (Configurator.refreshInfoRate * multiRefresher) == 0){
			kvMemeCodeNbEntities.clear();
			for (IUnitOfTransfer meme : memeProperties.countOfEntitiesHavingMeme.keySet()) {
				kvMemeCodeNbEntities.put(meme, (double)memeProperties.countOfEntitiesHavingMeme.get(meme) / entites.size());
			}

			vueController.addValueToApplianceSerie(++cptMemePossession, kvMemeCodeNbEntities);
		}
		if (Configurator.displayLogAvgDegreeByMeme && cptModulo % (Configurator.refreshInfoRate*100)==0)
			vueController.displayInfo(ViewMessageType.AVGDGRBYMEME, Arrays.asList(checkPropertiesByMemePossession()));

		// Verification de la propagation totale des memes initiaux
		if(!fullSilent && Configurator.checkWhenFullPropagate && !allTransmitted &&  cptModulo % Configurator.checkFullProRefreshRate == 0) {
			if(areAllMemeTransmitted()) {
                allTransmitted = true;
                vueController.displayInfo(ViewMessageType.PROPAGATION, Arrays.asList("ALL TRANSMISTED IN ;" + cptModulo));
			}

			int resDetail = areAllMemeTransmittedDetails();
			if ((resDetail & 1) == 1){
				System.out.println("all add transmitted");
				vueController.displayInfo(ViewMessageType.PROPAGATION, Arrays.asList("ALL ADD TRANSMISTED IN ;" + cptModulo));
				allAddTransmitted =true;
			}
			if((resDetail & 2) == 2) {
				System.out.println("all rmv transmitted");
				vueController.displayInfo(ViewMessageType.PROPAGATION, Arrays.asList("ALL RMV TRANSMISTED IN ;" + cptModulo));
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

	public boolean[][] getMatrixNetwork(){
		return networkConstruct.getNetworkMatrix();
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
			networkConstruct.NMAddLink(from.getNode().getIndex(), to.getNode().getIndex(), false);
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
	public void eventMemeChanged(Entite entiteConcernee, IUnitOfTransfer ajoutOrR, String message) {

		/* Les évenements de retrait n'arrivent que lors d'un remplacement, il n'est donc pas utile de retirer de la lsite
		des entités actives l'entités qui subit le retrait.
		*/

		// Dans le cas ou ttes les entites ne sont pas encore actives.
		if(entitesActive.size() < entites.size()) {

			if (entiteConcernee.getMyUnitOfT().size() < 1){
				System.err.println("[EH-EventMemeChanged] NE DEVRAIT PAS ARRIVER");
			}

			synchronized (entitesActive) {
				if (!entitesActive.contains(entiteConcernee))
					entitesActive.add(entiteConcernee);
			}
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

	/** Applique directement depuis une liste de meme au X premier agents pour les
	 * recevoir. Si option de défault behavior, donne des UOT fluides aux autres agent.
	 * Utilisé par l'apply du IModelParameter
	 * @param UOT
	 */
	public void giveMemeToEntiteFitting(List<IUnitOfTransfer> UOT) {
		ArrayList<Entite> entiteContente = new ArrayList<>();
		Iterator<Entite> entitees = entites.iterator();
		Entite actual;
		ArrayList<Entite> others = new ArrayList<>(entites);
		memeFittingApplied = UOT;
		for (IUnitOfTransfer meme : UOT) {
			actual = entitees.next();
			eventMemeChanged(actual, actual.addUOT(meme, true), Configurator.MemeActivityPossibility.AjoutMeme.toString());
			entiteContente.add(actual);
		}
		// On file des UOT d'ajout et retrait random aux entités qui n'ont pas eu de comportement de base a l'initialisation
		// TODO a deplacer avnt l'appel de cette fonction pour que tout les giveMeme puisse faire de la fluidité?
		if(Configurator.initializeDefaultBehavior) {
			others.removeAll(entiteContente);
			giveFluideMeme(others);
		}
	}

	/** Obtention de la liste des memes disponibles sur la map, soit les simples,
	 * soit les combinaisons de deux memes existantes, en fonction du param de configuration.
	 * @param setAsked
	 *            Défini si on veut les comportements simples, leur combinaison
	 *            ou les deux.
	 * @return
	 */
	public Hashtable<Integer, ArrayList<IUnitOfTransfer>>
		getMemeAvailable(FittingBehavior setAsked, Optional<Hashtable<TypeOfUOT, ArrayList<IUnitOfTransfer>>> memeByC) {

		Hashtable<Integer, ArrayList<IUnitOfTransfer>> memes = new Hashtable<>();
		if (setAsked == FittingBehavior.onlyComplex || setAsked == FittingBehavior.simpleAndComplex) {
			memes = getMemeCombinaisonAvailable(memeByC);
		}
		if (setAsked == FittingBehavior.onlySimple || setAsked == FittingBehavior.simpleAndComplex) {
			int lastIndex = memes.size();
			for (IUnitOfTransfer meme : memeFactory.getMemes(Configurator.MemeList.ONMAP, TypeOfUOT.ANYTHING))
				Toolz.addElementInHashArray(memes, ++lastIndex, meme);
		}

		return memes;
	}

	/** Renvoi la liste des memes dispo sur la map en liste de string.
	 * Utilisé pour les couleurs sur le graphe
	 * TODO [WAYPOINT] - creation des classes pour graphstream
	 * @param setAsked Voir @getMemeAvailable
	 * @return
	 */
	public ArrayList<String> getMemeAvailableAsString(FittingBehavior setAsked) {
		ArrayList<String> memesAsString = new ArrayList<>();
		Hashtable<Integer, ArrayList<IUnitOfTransfer>> memes = getMemeAvailable(setAsked, Optional.empty());
		ArrayList<String> classes = new ArrayList<String>();
		String classe;

		for (ArrayList<IUnitOfTransfer> combinaison : memes.values()) {
			classes.clear();
			for (IUnitOfTransfer meme : combinaison)
				classes.add(meme.toFourCharString());

			classes.sort(null);
			classe = "";
			for (String string : classes)
				classe += string;

			memesAsString.add(classe);
		}

		return memesAsString;
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
		// compte des memes possédés par les entités connecté a la courante
		Map<String,Integer> othersMemes = new HashMap<>();
		// liste des indexs par meme
		ArrayList<String> entityByMeme = new ArrayList<>();
		String memes = "";
		Entite nodeConnected;
		String resultat = "";
		String part1, part2, part3;

		try {
			// On classe les entités par combinaison de memes qu'elles possedent
			for (Entite entite : toExamine) {
				memes = "";
				for (IUnitOfTransfer meme : entite.getMyUnitOfT())
					memes += "." + meme.toNameString();
				if (memes == "")
					memes = "AUCUN";

				Toolz.addElementInHashArray(entiteByMemePossession, memes, entite);
			}
		} catch (ConcurrentModificationException e) {
			System.err.println(e.getStackTrace());
		}

		int nbLien;
		resultat += "---------------------------------------------------------------";
		for (String memeCombinaison : entiteByMemePossession.keySet()) {
			nbLien = 0;
			SelfDegrees.clear();
			othersDegrees.clear();
			entityByMeme.clear();
			othersMemes.clear();

			part1 = "";part2 = "Connection "; part3 = "";
			// pour chaque entité possédant la combinaison
			for (Entite entite : entiteByMemePossession.get(memeCombinaison)) {
				entityByMeme.add(""+entite.getIndex());
				SelfDegrees.add(entite.getDegree());

				// pour chaque entité connecté a l'entité courante
				for (Integer nodeIndex : networkConstruct.getConnectedNodes(entite.getIndex())) {
					nbLien++;
					nodeConnected = this.getEntityCorresponding(nodeIndex);
					if (nodeConnected != null) {
						othersDegrees.add(nodeConnected.getDegree());
						part3 = nodeConnected.getMyUnitOfT()!=null? nodeConnected.getMyUnitOfT().get(0).toNameString():"vide";
						Toolz.addCountToElementInHashArray(othersMemes,part3, 1);
					}
					else
						System.err.println("Ne DEVRAIT PAS ETRE POSSIBLE EH CHECKPROPERTIESBY...");
				}
			}

			// liste des memes possédés par les entités liées a l'entité courante
			for (String s : othersMemes.keySet()) {
				part2 += "["+ s + "]: " + othersMemes.get(s) + " (" + Toolz.getNumberCutToPrecision(othersMemes.get(s)*100./nbLien,2) +"%)" +" && ";
			}

			// Index des entités courantes
			entityByMeme.sort(null);
			part1 = "[";
			// index des entites avec le meme courant
			for (String s : entityByMeme) {
				part1 += s+":";
			}
			part1 += "]";

			resultat += "\n("
					+ entiteByMemePossession.get(memeCombinaison).size()
					+ ")-["
					+ (memeCombinaison)
					+ "] degree: "
					+ Toolz.getNumberCutToPrecision(Toolz.getAvg(SelfDegrees),2)
					+ " degree nodes connected to : "
					+ Toolz.getNumberCutToPrecision(
					Toolz.getAvg(othersDegrees), 2);

			resultat += "\n";
			resultat += part2;
			resultat += "\n";
			resultat += part1;

		}

		return resultat;
	}

	public boolean memeAllTransmitted() {
		return allTransmitted;
	}

	//endregion

	//region PRIVATE

	//region Action

	/** va lancer l'action d'une entitée. Depend du boolean CoupleVersion.
	 *
	 */
	private String runEntite() {

		List<String> rez = new ArrayList<>();
		Meme oneAction = null;
		CoupleMeme coupleAction;

		synchronized (workerFactory.waitingForReset) {
			workerFactory.getCalculator().incrementNbAction();
			toDisplayForRatio.clear();

			// CHOIX D'UNE ENTITÉE AU HASARD
			Entite entiteActing = selectActingEntiteV2();
			if(Configurator.debugEntiteHandler)
				System.out.println("[EH.runEntite]- entite choisie " + entiteActing.getIndex());
			if (entiteActing == null) {
				if(Configurator.debugEntiteHandler)
					System.err.println("[EH.runEntite()]- Aucune entité sélectionnée");
				return ("Nope pas d'entite prete");
			}

			// CHOIX DE L'ACTION POUR CETTE ENTITE

			// Se démerder pour tjrs avoir un meme ici
			oneAction = entiteActing.chooseAction();
			if (Configurator.debugEntiteHandler)
				System.out.println("[EH.runEntite]- action choisie " + oneAction);
				//System.out.println("[EH.runEntite]- action choisie " + memeActions.stream().map(e->e.toFourCharString()).reduce("",(added, e)-> added +" : "+e));

			// APPLICATION ET PROPAGATION DE L'ACTION
			rez.add(doAction(entiteActing, oneAction));

			// AFFICHAGE ET DEBUGGUAGE
			if (Configurator.displayLogRatioTryAddOverTryRmv) {

				//for (IUnitOfTransfer memeAction : memeActions) {
				if (oneAction != null) {
					if (oneAction.getActionType() == TypeOfUOT.AJOUTLIEN)
						cptActionAddTried++;
					else if (oneAction.getActionType() == TypeOfUOT.RETRAITLIEN)
						cptActionRmvTried++;

					toDisplayForRatio.add(oneAction.getActionType().name());
					toDisplayForRatio.add("AddTried/rmvTried;" + (double) cptActionAddTried / cptActionRmvTried);
				}
			}

			// Si on veut afficher les X dernieres actions entreprises & action depuis le début
			if (Configurator.displayMemePosessionDuringSimulation) {

				int i = -1;
				//for (Meme memeAction : memeActions) {
				i++;
				if (Configurator.displayLogRatioLogFailOverFail || Configurator.displayLogRatioLogFailOverSuccess) {
					List<String> temp = memeProperties.updateActionCount(oneAction, entiteActing.getIndex(), rez.get(i), cptModulo);
					if (temp != null)
						toDisplayForRatio.addAll(temp);
				} else
					memeProperties.updateActionCount(oneAction, entiteActing.getIndex(), rez.get(i), cptModulo);

				if (cptModulo % (Configurator.refreshInfoRate * 25) == 0) {
					sumFailAction = memeProperties.lastFailAction(nbFail);
					if (Configurator.writeFailDensityLink)
						vueController.displayInfo(ViewMessageType.FAILXDENSITY, getFailXDensity(nbFail.getValue(),
								networkConstruct.updatePreciseNetworkProperties
										(Configurator.getIndicateur(NetworkAttribType.DENSITY)).getDensity(), sumFailAction));
				}
			}

			if(!fullSilent && writeFailMemeApply &&toDisplayForRatio.isEmpty())
				vueController.displayInfo(ViewMessageType.ECHECS, toDisplayForRatio);

			// Dans le cas ou on veut les filtres en semi step, remis a zero des couleurs.
			if (Configurator.semiStepProgression) {
				giveEntiteBaseColor();
				if (filterOnSemiAuto(null, null))
					pauseStepInSemiAutoAction();
			}
		}

		return rez.stream().reduce(String::concat).get();
	}

	/** Application de l'action de l'entité ET propagation
	 * Si version couple, ne prend pas en compte memeAction mais fait le couple tenu par movingOne.
	 *
	 * @param movingOne
	 * @param memeAction
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String doAction(Entite movingOne, Meme memeAction) {
		String actionDone, actionsDone = "";String memeApply = "";Set<Entite> cibles; Set<Integer> ciblesIndex = new HashSet<>();
		IFilter currentFilter = null; Iterator<Entite> ite; Entite one; Meme memeReturned;
		List<Meme> memesToApply = new ArrayList<>();

		// humhum utilité d'une liste?
		memesToApply.add(memeAction);

		// Tjrs un seul élément?
		for (Meme meme : memesToApply) {
			actionDone = "";
			if (meme != null) {
				cibles = new HashSet<>(entites);
				cibles.remove(movingOne);
				// actionDone = "";

				// FILTRE Pour chaque attribut sur lesquels on applique des filtres
				for (IAttribut<Integer> attribut : memeAction.getAttributs()) {
					attribString = attribut.toString();
					// region semi auto
					if (Configurator.semiStepProgression && filterOnSemiAuto(null, null)) {
						System.out.println("On va appliquer les filtres suivants pour l'action " + memeAction.getName());
						System.out.println(memeAction.getFilter(attribString).values());
					} //endregion

					// Pour chaque filtre appliqué à un attribut
					for (int order = 0; order < memeAction.getFilter(attribString).size(); order++) {
						currentFilter = memeAction.getFilter(attribString).get(order);
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

				// Le dernier filtre appliqué devrait tjrs être un random() unitaire ( par la construction des memes )
				if (cibles.size() == 1) {
					actionDone += memeAction.getAction().applyAction(movingOne, cibles);

					// hum. TODO [CV] réfléchir ici

					// Si on reste sur l'ancienne facon de faire par une unique transmission direct
					if( !coupleSingleTransmission){
						cibles = entites;
					}

					// TODO[CV] - ici a eu lieu un chg
					// Si couple version, que l'action jouée soit un ajout ou un retrait, c'est tout le couple qui est transmis
					propagation(cibles, movingOne, movingOne.getMyUnitOfT().get(0));
					// endregion

					// evenement d'application d'une action
					eventActionDone(movingOne, memeAction, actionDone);
				}
				// Dans le cas ou il y a plus d'une cible ou aucune
				else {
					if (cibles.size() > 1)
						System.err.println("Plusieurs cibles pour une action, pas normal");
					else
						if(Configurator.debugEntiteHandler)
							System.out.println("Aucune cible pour une action");
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

			actionsDone += actionDone;
		}

		return actionsDone;
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

	/**
	 *
	 * @param cibles les cibles qui ont subi l'action, souvent unitaire
	 * @param movingOne L'entité agissante
	 * @param memeAction L'action appliquée
	 * @param proba la proba est prise ici car la proba d'un couple est portée par la classe CoupleMeme
	 */
	private void propagation(Set<Entite> cibles, Entite movingOne, IUnitOfTransfer memeAction){

		IUnitOfTransfer removedAction;
		double probaAction = 0 ;

		for (Entite entite : cibles) {
			probaAction = memeAction.getProbaPropagation();
			probaAction = probaPropaDegree(movingOne, entite, probaAction);

			// Proba de propagation - si on veut ajouter le meme
			if(Toolz.rollDice(probaAction)) {

				// Si le retour est nul c'est que l'entité a refuser le transfert
				removedAction = entite.takeMyMeme(memeAction);

				// Si retour du meme a ajouter, il n'y a pas eu de remplacement
				if(removedAction == memeAction)
					eventMemeChanged(entite, memeAction, Configurator.MemeActivityPossibility.AjoutMeme.toString());

				// Si un remplacement à eu lieu
				if(removedAction != null && removedAction != memeAction) {
					// un retrait
					eventMemeChanged(entite, removedAction, Configurator.MemeActivityPossibility.RetraitMeme.toString());
					// et un ajout
					eventMemeChanged(entite, memeAction,Configurator.MemeActivityPossibility.AjoutMeme.toString());
				}
			}
		}


	}


	/** Dans le cas de la version coule, prends en compte la distance de degrée pour
	 * pondérer la propagation d'un meme.
	 *
	 * @param distance
	 * @param paramCouple
	 * @return
	 */
	private double probaPropaDegree(Entite acting, Entite cible, double proba){
		if(proba == 0)
			return 0;
		double multiplicateur = 0;
		int diffDeg =  Math.abs(acting.getDegree() - cible.getDegree());
		if(diffDeg == 0)
			multiplicateur = 1;
		else if(diffDeg == 1){
			multiplicateur = 0.75;
		}

		/*
		else if(diffDeg == 1){
			multiplicateur = 0.5;
		}
		else if(diffDeg == 1){
			multiplicateur = 0.5;
		}
		else if(diffDeg == 1){
			multiplicateur = 0.25;
		}
		else {
			multiplicateur = 0.01;
		}*/

		if(Configurator.useMemePropagationProba)
			proba *= multiplicateur ; // Math.sqrt(1 + Math.abs(acting.getDegree() - cible.getDegree()));
		else {
			proba = multiplicateur; // 1. / Math.sqrt(1 + Math.abs(acting.getDegree() - cible.getDegree()));
}

		return proba;
	}

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
				for (IUnitOfTransfer myMeme : entite.getMyUnitOfT()) {
					if (add && myMeme.getActionType() == TypeOfUOT.AJOUTLIEN && !myMeme.isFluide()) {
						addtmp = true;
					}
					if (rmv && myMeme.getActionType() == TypeOfUOT.RETRAITLIEN && !myMeme.isFluide()) {
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

	// TODO REFACTORING Prendre les memes a appliquer a tous en parametre plutot qu'avoir des variables statics
	/** Dote les entités qui n'ont pas encore d'action des actions Add et remove de base.
	 *
	 * @param entitesToBeApplied
	 */
	private void giveFluideMeme(ArrayList<Entite> entitesToBeApplied){
		for (Entite entite : entitesToBeApplied) {
			entite.addUOT(getDoubleRandom());
			eventMemeChanged(entite, getDoubleRandom(), MemeActivityPossibility.AjoutMeme.toString());
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
	@SuppressWarnings("UnusedAssignment")
	private void generateMemeAvailableForMap() {

		ArrayList<AttributType> attributs = new ArrayList<>();
		Hashtable<AttributType, Hashtable<Integer, AgregatorType>> KVAttributAgregator = new Hashtable<>();
		Hashtable<Integer, AgregatorType> agregators = new Hashtable<>();
		List<Meme> ajouts = new ArrayList<>();
		List<Meme> retraits = new ArrayList<>();

		int index;

		TypeOfUOT add = TypeOfUOT.AJOUTLIEN;
		TypeOfUOT remove = TypeOfUOT.RETRAITLIEN;

		@SuppressWarnings("unused")
		AttributType degree = AttributType.DEGREE;
		AgregatorType linked = AgregatorType.LINKED;
		AgregatorType blank = AgregatorType.BLANK;
		AgregatorType notLinked = AgregatorType.NOTLINKED;
		AgregatorType mineInf = AgregatorType.MINEINF;
		AgregatorType mineSup = AgregatorType.MINESUP;
		AgregatorType mineEq = AgregatorType.MINEEQUAL;
		AgregatorType random = AgregatorType.RANDOM;
		AgregatorType hopAWay = AgregatorType.HOPAWAY;
		AgregatorType hopAWay3 = AgregatorType.HOPAWAY3;
		AgregatorType triangle = AgregatorType.TRIANGLE;
		AgregatorType theirSup = AgregatorType.THEIRSUP;
		AgregatorType theirSupSix = AgregatorType.THEIRSUPSIX;
		AgregatorType theirEqual = AgregatorType.THEIREQUAL;
		AgregatorType theMost = AgregatorType.THEMOST;
		AgregatorType theLeast = AgregatorType.THELEAST;
		AgregatorType selfSup = AgregatorType.SELFSUP;

		KVAttributAgregator.put(degree, agregators);
		attributs.add(degree);

		agregators.clear();index= 0;
		agregators.put(index++, notLinked);
		agregators.put(index++, random);
		ajouts.add(memeFactory.registerMemeAction("AddØ",0.15, false, true, add, attributs, KVAttributAgregator, false));

		agregators.clear();index= 0;
		agregators.put(index++, notLinked);
		agregators.put(index++, mineInf);
		agregators.put(index++, random);
		ajouts.add(memeFactory.registerMemeAction("Add+", 1, true,true, add, attributs,KVAttributAgregator, false));

		agregators.clear();index = 0;
		agregators.put(index++, notLinked);
		agregators.put(index++, mineSup);
		agregators.put(index++, random);
		ajouts.add(memeFactory.registerMemeAction("Add-",1, true,true, add, attributs,KVAttributAgregator, false));

		agregators.clear(); index = 0;
		// agregators.put(index++, notLinked);
		agregators.put(index++, theMost);
		agregators.put(index++, random);
		ajouts.add(memeFactory.registerMemeAction("Add∞", 1, false, true, add, attributs, KVAttributAgregator,false));

		agregators.clear(); index = 0;
		agregators.put(index++, notLinked);
		agregators.put(index++, theMost);
		agregators.put(index++, random);
		ajouts.add(memeFactory.registerMemeAction("Add∞!", 1, false, true, add, attributs, KVAttributAgregator,false));

		agregators.clear(); index = 0;
		agregators.put(index++, theLeast);
		agregators.put(index++, random);
		ajouts.add(memeFactory.registerMemeAction("Add°", 1, false, true, add, attributs, KVAttributAgregator,false));

		agregators.clear(); index = 0;
		agregators.put(index++, notLinked);
		agregators.put(index++, theLeast);
		agregators.put(index++, random);
		ajouts.add(memeFactory.registerMemeAction("Add°!", 1, true, true, add, attributs, KVAttributAgregator,false));

		agregators.clear(); index = 0;
		agregators.put(index++, hopAWay);
		agregators.put(index++, notLinked);
		agregators.put(index++, random);
		ajouts.add(memeFactory.registerMemeAction("AddØ-Hop", .4, false, true, add,attributs, KVAttributAgregator ,false));

		agregators.clear(); index = 0;
		agregators.put(index++, hopAWay3);
		agregators.put(index++, notLinked);
		agregators.put(index++, random);
		ajouts.add(memeFactory.registerMemeAction("AddØ-3Hop", 1, false, true, add,attributs, KVAttributAgregator ,false));

		agregators.clear();index = 0;
		agregators.put(index++, notLinked);
		agregators.put(index++, mineEq);
		agregators.put(index++, random);
		ajouts.add(memeFactory.registerMemeAction("AddEq",1, true, true, add,  attributs, KVAttributAgregator, false));

		agregators.clear();index = 0;
		agregators.put(index++, blank);
		ajouts.add(memeFactory.registerMemeAction("AddVoid",1, true, true, add,  attributs, KVAttributAgregator, false));

		agregators.clear();index = 0;
		agregators.put(index++, linked);
		agregators.put(index++, random);
		retraits.add(memeFactory.registerMemeAction("RmvØ",.7, false, true, remove,  attributs, KVAttributAgregator, false));

		agregators.clear();index = 0;
		agregators.put(index++, linked);
		agregators.put(index++, mineSup);
		agregators.put(index++, random);
		retraits.add(memeFactory.registerMemeAction("Rmv-", 1, false, true, remove, attributs, KVAttributAgregator ,false));

		agregators.clear();index = 0;
		agregators.put(index++, linked);
		agregators.put(index++, mineInf);
		agregators.put(index++, random);
		retraits.add(memeFactory.registerMemeAction("Rmv+", 1, false, true, remove, attributs, KVAttributAgregator ,false));

		agregators.clear(); index = 0;
		agregators.put(index++, theMost);
		agregators.put(index++, random);
		retraits.add(memeFactory.registerMemeAction("Rmv∞", 1, false, true, remove, attributs, KVAttributAgregator,false));

		agregators.clear(); index = 0;
		agregators.put(index++, linked);
		agregators.put(index++, theMost);
		agregators.put(index++, random);
		retraits.add(memeFactory.registerMemeAction("Rmv∞!", 1, false, true, remove, attributs, KVAttributAgregator,false));

		agregators.clear(); index = 0;
		agregators.put(index++, theLeast);
		agregators.put(index++, random);
		retraits.add(memeFactory.registerMemeAction("Rmv°", 1, false, true, remove, attributs, KVAttributAgregator,false));

		agregators.clear(); index = 0;
		agregators.put(index++, linked);
		agregators.put(index++, theLeast);
		agregators.put(index++, random);
		retraits.add(memeFactory.registerMemeAction("Rmv°!", 1, false, true, remove, attributs, KVAttributAgregator,false));

		agregators.clear(); index = 0;
		agregators.put(index++, linked);
		agregators.put(index++, triangle);
		agregators.put(index++, random);
		retraits.add(memeFactory.registerMemeAction("RmvØ-2hop", .3, false, true, remove, attributs,KVAttributAgregator ,false));

		agregators.clear(); index = 0;
		agregators.put(index++, selfSup);
		agregators.put(index++, linked);
		agregators.put(index++, theirSup);
		agregators.put(index++, random);
		retraits.add(memeFactory.registerMemeAction("RmvChain",1, false, true, remove,  attributs, KVAttributAgregator, false));

		agregators.clear(); index = 0;
		agregators.put(index++, linked);
		agregators.put(index++, theirEqual);
		agregators.put(index++, random);
		retraits.add(memeFactory.registerMemeAction("RmvEq",1, true, true, remove,  attributs, KVAttributAgregator, false));

		agregators.clear(); index = 0;
		agregators.put(index++, blank);
		retraits.add(memeFactory.registerMemeAction("RmvVoid",1, true, true, remove,  attributs, KVAttributAgregator, false));

		agregators.clear();
		index = 0;

		// Creation des couples d'actions
		this.doubleRandom = memeFactory.extractAndDoNotRegister("AddØ", "RmvØ", 0);
		memeFactory.extractAndAddCoupleMeme( "AddEq", "Rmv-", .1, false);
		memeFactory.extractAndAddCoupleMeme( "Add∞", "RmvEq", 1., false);
		memeFactory.extractAndAddCoupleMeme( "Add°!", "RmvChain", .2, false);
	}

	/**
	 * Renvoi une hashtable contenant les combinaisons possibles de meme avec
	 * les memes sur la map. Un meme du meme type par agent par combinaison.
	 * (Type: Ajout, retrait .. ) dépend de l'action appliquée
	 *
	 * @return une hash Int ( qui n'a pas de signification ), combinaison de
	 *         meme.
	 */
	private Hashtable<Integer, ArrayList<IUnitOfTransfer>> getMemeCombinaisonAvailable
				(Optional<Hashtable<TypeOfUOT, ArrayList<IUnitOfTransfer>>> memeByC) {

		Hashtable<TypeOfUOT, ArrayList<IUnitOfTransfer>> memesByCategory = new Hashtable<>();
		ArrayList<IUnitOfTransfer> memeOfOneCategory;
		ArrayList<TypeOfUOT> key = new ArrayList<>();

		if(memeByC.isPresent()) {
			memesByCategory = memeByC.get();
			for (TypeOfUOT actionT: memesByCategory.keySet()) {
				key.add(actionT);
			}
		}
		else {
			for (TypeOfUOT action : TypeOfUOT.values()) {
				if (action == TypeOfUOT.AJOUTLIEN || action == TypeOfUOT.RETRAITLIEN) {
					memeOfOneCategory = memeFactory.getMemeAvailable(action, false);
					if (memeOfOneCategory.size() > 0) {
						memesByCategory.put(action, memeOfOneCategory);
						key.add(action);
					}
				}
			}
		}

		TypeOfUOT[] keyz = new TypeOfUOT[memesByCategory.keySet().size()];
		for (int i = 0; i < key.size(); i++)
			keyz[i] = key.get(i);

		Hashtable<Integer, ArrayList<IUnitOfTransfer>> composition = new Hashtable<>();
		ArrayList<IUnitOfTransfer> memez = new ArrayList<>();
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
	private void recursive(Hashtable<TypeOfUOT, ArrayList<IUnitOfTransfer>> memesByCategory,
						   ArrayList<IUnitOfTransfer> memes, TypeOfUOT[] actions,
						   Hashtable<Integer, ArrayList<IUnitOfTransfer>> selection, int indexAction) {

		if ((++indexAction) < actions.length) {
			for (IUnitOfTransfer meme : memesByCategory.get(actions[indexAction])) {
				ArrayList<IUnitOfTransfer> copy = (ArrayList<IUnitOfTransfer>) memes.clone();
				copy.add(meme);
				recursive(memesByCategory, copy, actions, selection, indexAction);
			}
		} else {
			indexOfMemesCombinaisonRecursion++;
			selection.put(Integer.parseInt("" + indexOfMemesCombinaisonRecursion),
					(ArrayList<IUnitOfTransfer>) memes.clone());
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

	@Override
	public void handlerNbNodeChanged(NbNodeChangedEvent e) {
		getEntites().clear();
		bindNodeWithEntite(networkConstruct.getNetwork());
	}

	/** Event lorsqu'un meme est transmis
	 *
	 * @param e Meme transmis
	 */
	@Override
	public void handlerBehavTransm(BehavTransmEvent e) {
		if(e.message.compareToIgnoreCase(Configurator.MemeActivityPossibility.RetraitMeme.toString()) == 0){
			memeProperties.updateMemePossession(e.meme,false);
		}else if(e.message.compareToIgnoreCase(MemeActivityPossibility.AjoutMeme.toString()) == 0){
			memeProperties.updateMemePossession(e.meme,true);
		}
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
		for (Entite entitee : getEntites()) {
			resultat += "\n né" + entitee.getIndex();
			for (IUnitOfTransfer meme : entitee.getMyUnitOfT()) {
				resultat += "\n \t meme " + meme.toNameString();
			}
		}
		return resultat;
	}

	public ArrayList<Entite> getEntitesActive() {
		return entitesActive;
	}

	/** renvoi une map de k:meme v:nom lisible
	 *
	 * @return
	 */
	public Map<Meme, String> getKVMemeTranslate() {
		Map<Meme, String> res = new HashMap<>();
		synchronized (memeFittingApplied) {
			//TODO
			for (IUnitOfTransfer<CoupleMeme> uot : memeFittingApplied) {
				for (Meme meme : uot) {
					if(!res.containsKey(meme))
						res.put(meme, meme.toString());
				}
			}
		}

		return res;
	}
		/** renvoi une map de k:meme v:nom lisible
	 *
	 * @return
	 */
	public Map<IUnitOfTransfer, String> getKVUOTTranslate(){
		Map<IUnitOfTransfer,String> res = new HashMap<>();
		synchronized (memeFittingApplied) {
			for (IUnitOfTransfer uot : memeFittingApplied) {
				res.put(uot, uot.toNameString());
			}
		}

		return res;
	}

	public IUnitOfTransfer<CoupleMeme> getDoubleRandom() {
		return doubleRandom;
	}

	//endregion

	//endregion

	//region deprecated

	public List<Entite> getEntites() {
		return new ArrayList<Entite>(entites);
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

	}

	/** Obtient les memes effectivements présent sur la map.
	 *
	 * @return la liste des memes possédés par les agents
	 */
	public ArrayList<IUnitOfTransfer> getMemeOnMap() {
		ArrayList<IUnitOfTransfer> memes = new ArrayList<>();
		for (Entite entite : getEntites()) {
			for (IUnitOfTransfer meme : entite.getMyUnitOfT()) {
				if (!memes.contains(meme))
					memes.add(meme);
			}
		}

		return memes;
	}


	//endregion
}
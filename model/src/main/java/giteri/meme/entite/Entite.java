package giteri.meme.entite;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import giteri.meme.mecanisme.MemeFactory;
import giteri.tool.math.Toolz;
import giteri.meme.mecanisme.ActionFactory;
import giteri.meme.mecanisme.ActionFactory.IAction;
import giteri.network.network.Node;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.ActionType;
import giteri.tool.objects.ObjectRef;

/** Entitée conteneur des memes.
 *
 */
public class Entite implements Comparable<Entite>{

	//region Properties

	int index;
	Node associatedNode;
	double probaLearning;
	double probaAppliying;

	// Liste de memes
	private ArrayList<Meme> myMemes;
	// Index du couple associé
	private CoupleMeme coupleMeme;
	private boolean breederOnCouple;

	// répartie sur 0 -> 1, réévalué a chaque ajout ou (retrait de meme)
	Hashtable<Meme, Double> intervalOfSelection;
	//	Hashtable<Entite, Integer> connectedTimeNodes;
	Set<Entite> connectedNodes;
	// défini sur quels memes l'entité fourni le réseau
	Set<IAction> breederOn;

	//endregion
	/** Constructeur d'entite.
	 *
	 */
	public Entite(){
		associatedNode = null;
		myMemes = new ArrayList<>();
		intervalOfSelection = new Hashtable<>();
		connectedNodes = new HashSet<>();
		breederOn = new HashSet<>();
		probaAppliying = 1;
		breederOnCouple = false;
	}

	/** Défini les regles de sélection de facon équiprobable.
	 *
	 */
	@SuppressWarnings("unchecked")
	public void defineRulesProbaLimited(){
		double roll;
		intervalOfSelection.clear();
		for (Meme meme : getMyMemes()) {
			roll = 1.0 / getMyMemes().size();
			intervalOfSelection.put(meme, roll);
		}
	}

	/** Apres application d'une action, l'entité apprend de cette action
	 * qu'il a vu.
	 *
	 * @Return Null si aucun ajout, l'ancien meme si remplacement, le nouveau meme si ajout sans remplacement
	 */
	public Meme receiveMeme(Meme subMeme){

		// (!A || B) && (!A || C) <=> !A && !A || !A && C || !A && B || B && C
		boolean ok;
		ok = !getMyMemes().contains(subMeme);
		ok = ok && (!Configurator.useMemePropagationProba || Toolz.rollDice(subMeme.getProbaOfPropagation()));
		ok = ok && (!Configurator.useEntitePropagationProba || Toolz.rollDice(this.probaLearning));

		if(ok) {
			return addOrReplaceFast(subMeme);
		}

		// null si aucun ajout a l'entité
		return null;
	}

	/** ajoute un meme ou remplace un meme déja existant,
	 * avec une notion de slot pour meme d'ajout et meme de retrait.
	 *
	 * @param memeToAdd
	 * @return memetoAdd si ajout, l'ancien meme si remplacement, null si rien
	 */
	public Meme addOrReplaceFast(Meme memeToAdd){
		boolean okToBeAdded = true;
		boolean needToBeReplaced = false;
		boolean addedOrReplaced = false;
		Meme memeReplaced = null;

		// On parcourt les meme existant et regarde si un meme of the same category exists
		for (Meme possededMeme : getMyMemes())
			if(memeToAdd.getAction().getActionType() == possededMeme.getAction().getActionType())
			{
				if(memeToAdd.compareTo(possededMeme) == 0) {
					okToBeAdded = false;
					break;
				}
				// si oui, il faudra que la configuration l'autorise pour remplacer l'ancien meme.
				memeReplaced = possededMeme;
				needToBeReplaced = true;
				break;
			}

		// Si il faut remplacer un meme pour pouvoir ajouter le meme courant,
		// et que la configuration l'accepte, on supprime l'ancien meme.
		if(needToBeReplaced && Configurator.memeCanBeReplaceByCategory)
		{
			// Un meme devrait etre remplacé, et la configuration l'autorise, masi on vérifie
			// que l'entité n'est pas un breeder de ce comportement
			if(Configurator.fixedSlotForBreeder && !breederOn.isEmpty()){
				for (IAction iAction : breederOn) {
					// Dans le cas ou il s'agit d'une action de breeder, donc à ne pas remplacer
					if(memeReplaced.getAction().toString() == iAction.toString()){
						return null;
					}
				}
			}
			synchronized(myMemes){
				myMemes.remove(memeReplaced);
			}

			okToBeAdded = true;
		}

		// Dans le cas ou on ajoute effectivement le nouveau meme,
		// soit apres remplacement soit parceque ca a été directement possible.
		if(okToBeAdded)
		{
			addedOrReplaced = true;
			addMeme(memeToAdd);
			myMemes.sort(null);
		}

		return needToBeReplaced ? memeReplaced : memeToAdd;
	}

	/** Ajout ou retrait d'un des memes du couple.
	 * Si on arrive la, c'est que l'entité ne possédait pas le meme couple C1 que l'acting.
	 * Even if le meme d'ajout C1A ou retrait C1R était commun au deux couples ( ancien C2 et nouveau C1 )
	 * on va remplacer le meme en question C2A ou C2R par celui du couple C1 de l'acting
	 *
	 * @param toAdd
	 */
//	public void addOrReplaceCoupleVersion(Meme toAdd){
//		boolean okToBeAdded = true;
//		boolean needToBeReplaced = false;
//		boolean addedOrReplaced = false;
//		Meme memeReplaced = null;
//
//		// On parcourt les meme existant et regarde si un meme of the same category exists
//		for (Meme possededMeme : getMyMemes())
//			if(memeToAdd.getAction().getActionType() == possededMeme.getAction().getActionType())
//			{
//				if(memeToAdd.compareTo(possededMeme) == 0) {
//					okToBeAdded = false;
//					break;
//				}
//				// si oui, il faudra que la configuration l'autorise pour remplacer l'ancien meme.
//				memeReplaced = possededMeme;
//				needToBeReplaced = true;
//				break;
//			}
//
//		// Si il faut remplacer un meme pour pouvoir ajouter le meme courant,
//		// et que la configuration l'accepte, on supprime l'ancien meme.
//		if(needToBeReplaced && Configurator.memeCanBeReplaceByCategory)
//		{
//			// Un meme devrait etre remplacé, et la configuration l'autorise, masi on vérifie
//			// que l'entité n'est pas un breeder de ce comportement
//			if(Configurator.fixedSlotForBreeder && !breederOn.isEmpty()){
//				for (IAction iAction : breederOn) {
//					// Dans le cas ou il s'agit d'une action de breeder, donc à ne pas remplacer
//					if(memeReplaced.getAction().toString() == iAction.toString()){
//						return null;
//					}
//				}
//			}
//			synchronized(myMemes){
//				myMemes.remove(memeReplaced);
//			}
//
//			okToBeAdded = true;
//		}
//
//		// Dans le cas ou on ajoute effectivement le nouveau meme,
//		// soit apres remplacement soit parceque ca a été directement possible.
//		if(okToBeAdded)
//		{
//			addedOrReplaced = true;
//			addMeme(memeToAdd);
//			myMemes.sort(null);
//		}
//
//		return needToBeReplaced ? memeReplaced : memeToAdd;
//
//	}

	/** Choisi et renvoi l'action qu'il va réalisée.
	 * Dans tout les cas, augmente d'un le temps depuis lequel une connection
	 * existe
	 * @return le meme action sélectionné
	 */
	public Meme chooseAction(){

		// On cherche l'élément qui est dans l'interval
		// ProbaEntite [.5=evapo, 0.7=action1, 1.0=action2]
		// si toSelect renvoi .4, evapo doit etre choisit.
		Meme resultat = null;
		double toSelect = Toolz.getProbaOneOut();
		double borneInf = 0;

		for (Meme looked : intervalOfSelection.keySet()) {
			if(toSelect >= borneInf && toSelect < borneInf + intervalOfSelection.get(looked) ) {
				resultat = looked;
				break;
			}else {
				borneInf += intervalOfSelection.get(looked);
			}
		}

		if(Configurator.debugEntite)
			if(resultat == null ){
				System.out.println("Choose action null on cherchait un toSelect a: " + toSelect + "et on avait de dispo " + intervalOfSelection.keySet().toString() + " avec un interval "+intervalOfSelection.values());
			}
		return resultat;
	}

	/** Choix d'une action en excluant l'option de celle
	 *
	 *
	 */
	public Meme chooseActionExclusionVersion(ActionType notToTake){
		ArrayList<Meme> restant = new ArrayList<Meme>();
		for(Meme meme : intervalOfSelection.keySet()) {
			if(!meme.action.getActionType().equals(notToTake))
				restant.add(meme);
		}

		return Toolz.getRandomElement(restant);
	}

	/** Reset l'état du noeud, garde son node associé mais oubli
	 * les liens avec les autres noeuds, ses mèmes
	 *
	 */
	public void resetStat(){
		getMyMemes().clear();
		intervalOfSelection.clear();
		associatedNode.resetStat();
		connectedNodes.clear();
	}

	public void resetEntiteStuff(){
		connectedNodes.clear();
	}

	/** Remet le temps des links a zero.
	 *
	 * @param entites
	 * @return si tout les liens sont rafraichis, false sinon
	 */
	public ArrayList<Boolean> refreshLinks(ArrayList<Entite> entites){
		ArrayList<Boolean> succes = new ArrayList<Boolean>();
		synchronized (connectedNodes) {
			for (Entite entite : entites) {
				if(!isConnectedTo(entite)){
					succes.add(false);
				}
				else{
					connectedNodes.add(entite);
					succes.add(true);
				}
			}
		}

		for (Entite entity : connectedNodes) {
			connectedNodes.add(entity);
		}

		return succes;
	}

	//region Getter/Setter
	/** Vérifie que les memes possédés par l'entité sont des memes propagés,
	 * et non slot vide // meme fluidité de bootstrap.
	 *
	 * @return
	 */
	public boolean isFullActif() {
		// LA FLEMME
		synchronized (myMemes) {
			if (this.myMemes.size() == 2) {
				for (Meme meme : myMemes) {
					if (meme.isFluide())
						return false;
				}
				return true;
			}

			return false;
		}
	}


	/**
	 *
	 * @param entite
	 * @return
	 */
	private boolean isConnectedTo(Entite entite){
		synchronized (connectedNodes) {
			return connectedNodes.contains(entite);
		}
	}

	/**
	 * Package-private modfier. Doit etre appeler uniquement
	 * entiteHandler pour garantir la cohérence network // couche entité
	 * @param toAdd
	 */
	void addConnectedEntite(Entite toAdd){
		synchronized (connectedNodes) {
			connectedNodes.add(toAdd);
		}
	}

	/**
	 *
	 * @param toRemove
	 */
	void removeConnectedEntite(Entite toRemove){
		synchronized (connectedNodes) {
		connectedNodes.remove(toRemove);
		}
	}

	/** Obtenir la classe graph stream a laquelle appartient le noeud.
	 * Utile pour la coloration.
	 * Composé des actions des memes.
	 *
	 * @return
	 */
	public String getGraphStreamClass(){
		String classe = "";
		ArrayList<String> classes = new ArrayList<>();

		if(Configurator.coupleVersion){
			if(coupleMeme != null)
				for (Meme meme : coupleMeme) {
					classes.add(meme.toFourCharString());
				}

		}else {
			for (Meme meme : getMyMemes()) {
				classes.add(meme.toFourCharString());
			}
		}
		classes.sort(null);
		for (String string : classes) {
			classe +=  string;
		}


		return classe;
	}

	public Node getNode(){
		return associatedNode;
	}

	public void setNode(Node myPlaceee){
		associatedNode = myPlaceee;
		index = myPlaceee.getIndex();
		if(Configurator.useEntitySuccesProba)
			probaAppliying = (index + 1.) / Configurator.getNbNode();
	}

	public ArrayList<Integer> getConnectedNodesIndex(){
		return associatedNode.getConnectedNodes();
	}

	public double getProbaAppliying() {
		return probaAppliying;
	}

	/** Obtient la liste des entités connectés depuis la hash de
	 * temps de connection entre les noeuds.
	 *
	 * @return
	 */
	public Set<Entite> getConnectedEntite(){
		return connectedNodes;
	}

	public int getDegree(){
		return associatedNode.getConnectedNodes().size();
	}

	public void setIndex(int indexxx){
		this.index = indexxx;
	}

	public int getIndex(){
		return index;
	}

	public ArrayList<Meme> getMyMemes(){
		synchronized(myMemes){
			return myMemes;
		}
	}

	public Meme addMeme(Meme e, boolean fixed){
		if(fixed) {
			breederOn.add(addMeme(e).getAction());
		}
		else
			addMeme(e);
		return e;
	}

	public Meme addMeme(Meme e){
		synchronized(myMemes){
			myMemes.add(e);
		}

		defineRulesProbaLimited();
		return e;
	}

	public String toString(){
		String resultat ="";
		resultat += "Index: "+getIndex();
		resultat += "\nDegree: "+getDegree();
		resultat += "\nMeme: ";
		if(Configurator.coupleVersion)
			resultat += "\n \t "+ coupleMeme.getName();
		else
			for (Meme memeAction : getMyMemes()) {
				resultat += "\n \t "+ memeAction;
			}
		return resultat;
	}

	/** Comparaison sur l'index.
	 *
	 */
	public int compareTo(Entite o) {
		return Integer.compare(this.index, o.index);
	}

	public int getCoupleMemeIndex() {
		return coupleMeme.getIndex();
	}

	public CoupleMeme getCoupleMeme() {
		return coupleMeme;
	}
	public void setCoupleMeme(CoupleMeme coupleMeme) {
		this.coupleMeme = coupleMeme;
	}

	public boolean isBreederOnCouple() {
		return breederOnCouple;
	}

	public void setBreederOnCouple(boolean breederOnCouple) {
		this.breederOnCouple = breederOnCouple;
	}

	//endregion

}

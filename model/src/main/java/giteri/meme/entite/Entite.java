package giteri.meme.entite;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Hashtable;

import giteri.tool.math.Toolz;
import giteri.meme.mecanisme.ActionFactory;
import giteri.meme.mecanisme.ActionFactory.IAction;
import giteri.network.network.Node;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.ActionType;

/** Entitée conteneur des memes.
 * 
 */
public class Entite implements Comparable<Entite>{

	// Region Properties
	
	int index;
	Node associatedNode;
	double probaLearning;
	
	// Liste de memes 
	private ArrayList<Meme> myMemes;
	// répartie sur 0 -> 1, réévalué a chaque ajout ou (retrait de giteri.meme)
	Hashtable<Meme, Double> intervalOfSelection;
	Hashtable<Entite, Integer> connectedTimeNodes;
	// défini sur quels memes l'entité fourni le réseau  
	ArrayList<IAction> breederOn;
	
	// EndRegion
	
	 /** Constructeur d'entite. 
	 * 
	 */
	public Entite(){
		associatedNode = null;
		myMemes = new ArrayList<Meme>();	
		intervalOfSelection = new Hashtable<Meme, Double>();
		connectedTimeNodes = new Hashtable<Entite, Integer>();
		breederOn = new ArrayList<ActionFactory.IAction>();
		probaLearning = Configurator.getProbaLearning(Configurator.probaEntiteLearning * - 1);
	}

	/** Défini les regles de sélection de facon équiprobable.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void defineRulesProbaLimited(){
		double roll;
		ArrayList<Meme> myMemeCloned = (ArrayList<Meme>) getMyMemes().clone();
		intervalOfSelection.clear();
		
		for (Meme meme : myMemeCloned) {
			roll = 1.0 / myMemeCloned.size();
			intervalOfSelection.put(meme, roll);
		}
	}
	
	/** Apres application d'une action, l'entité apprend de cette action
	 * qu'il a vu. 
	 * 
	 * @param subMeme
	 * @Return le giteri.meme qui a été remplacé s'il existe,
	 */
	public Boolean receiveMeme(Meme subMeme){
		boolean ok; 
		// Concernant l'usage de la proba propre au giteri.meme
		try {
			if(subMeme.probaOfPropagation == 0){
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		// (!A || B) && (!A || C) <=> !A && !A || !A && C || !A && B || B && C
		ok = !Configurator.useMemePropagationProba || Toolz.rollDice(subMeme.probaOfPropagation);
		ok = ok && (!Configurator.useEntitePropagationProba || Toolz.rollDice(this.probaLearning));
		if(ok) 
				return addOrReplaceSlotVersion(subMeme);
		return false;
	}
	
	/** Après application d'une action de la part d'une entité A, recoit l'un des 
	 * memes possédé par A.
	 * 
	 * @param actingOnMe
	 * @return
	 */
	public Boolean receiveRandomMemeFromActing(Entite actingOnMe){
		boolean ok;
		Meme selectedMeme = actingOnMe.chooseAction();
		// Concernant l'usage de la proba propre au giteri.meme
		ok = !Configurator.useMemePropagationProba || Toolz.rollDice(selectedMeme.probaOfPropagation);
		ok = ok && (!Configurator.useEntitePropagationProba || Toolz.rollDice(this.probaLearning));
		if(ok) 
				return addOrReplaceSlotVersion(selectedMeme);
		return false;
	}
	
	/** ajoute un giteri.meme ou remplace un giteri.meme déja existant,
	 * avec une notion de slot pour giteri.meme d'ajout et giteri.meme de retrait.
	 * 
	 * @param meme
	 * @return true si le giteri.meme a bien été remplacé.
	 */
	public boolean addOrReplaceSlotVersion(Meme meme){
		boolean okToBeAdded = true;
		boolean addedOrReplaced = false;
		Meme memeToReplace = null  ;
		
		
		// On parcourt les giteri.meme existant et regarde si un giteri.meme de la giteri.meme catégorie existe
		for (Meme possededMeme : getMyMemes()) 
			if(meme.getAction().toString() == possededMeme.getAction().toString())
			{
				// si oui, il faudra que la configuration l'autorise pour remplacer
				// l'ancien giteri.meme.
				memeToReplace = possededMeme;
				okToBeAdded = false;
				break;
			}
		
		// Si il faut remplacer un giteri.meme pour pouvoir ajouter le giteri.meme courant,
		// et que la configuration l'accepte, on supprime l'ancien giteri.meme.
		if(memeToReplace != null && Configurator.memeCanBeReplaceByCategory)
		{
			// Un giteri.meme devrait etre remplacé, et la configuration l'autorise, masi on vérifie
			// que l'entité n'est pas un breeder de ce comportement
			if(Configurator.fixedSlotForBreeder && !breederOn.isEmpty()){
				for (IAction iAction : breederOn) {
					// Dans le cas ou il s'agit d'une action de breeder, donc a ne pas remplacer
					if(memeToReplace.getAction().toString() == iAction.toString()){
						return false;
					}
				}
			}
			synchronized(myMemes){
				myMemes.remove(memeToReplace);
			}
			
			EntiteHandler.getInstance().eventMemeChanged(this, memeToReplace, Configurator.MemeActivityPossibility.RetraitMeme.toString());
			okToBeAdded = true;
		}
		
		// Dans le cas ou on ajoute effectivement le nouveau giteri.meme,
		// soit apres remplacement soit parceque ca a été directement possible.
		if(okToBeAdded)
		{
			addMeme(meme);
			if(Configurator.desgressiveLearningProba)
				probaLearning /= 2;
			if(Configurator.learningOnlyOnce)
				probaLearning = 0;
			myMemes.sort(null);
//			defineRulesProbaLimited();
			addedOrReplaced = true;
		}
		
		return addedOrReplaced;
	}
	
	/** Choisi et renvoi l'action qu'il va réalisée.
	 * Dans tout les cas, augmente d'un le temps depuis lequel une connection
	 * existe
	 * @return le giteri.meme action sélectionné
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
		
		if(toSelect == 1)
			System.out.println("[Entite:chooseAction()] MERDE");
		
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
		connectedTimeNodes.clear();
	}

	public void resetEntiteStuff(){
		connectedTimeNodes.clear();
	}

	/** Remet le temps des links a zero. 
	 * 
	 * @param entites
	 * @return si tout les liens sont rafraichis, false sinon
	 */
	public ArrayList<Boolean> refreshLinks(ArrayList<Entite> entites){
		ArrayList<Boolean> succes = new ArrayList<Boolean>();
		synchronized (connectedTimeNodes) {
			for (Entite entite : entites) {
				if(!isConnectedTo(entite)){
					succes.add(false);
				}
				else{
					connectedTimeNodes.put(entite, 0);
					succes.add(true);
				}
			}
		}
		
		for (Entite entity : connectedTimeNodes.keySet()) {
			connectedTimeNodes.put(entity, 0);
		}
		
		return succes;
	}
	
	// Region Getter/Setter
	
	/**
	 * 
	 * @param entite
	 * @return
	 */
	private boolean isConnectedTo(Entite entite){
		synchronized (connectedTimeNodes) {
			return connectedTimeNodes.keySet().contains(entite);
		}
	}
	
	/**
	 * Package-private modfier. Doit etre appeler uniquement
	 * entiteHandler pour garantir la cohérence giteri.network // couche entité
	 * @param toAdd
	 */
	void addConnectedEntite(Entite toAdd){
		synchronized (connectedTimeNodes) {
			connectedTimeNodes.put(toAdd, 0);
		}
	}
	
	/**
	 * 
	 * @param toRemove
	 */
	void removeConnectedEntite(Entite toRemove){
		//synchronized (connectedTimeNodes) {
			connectedTimeNodes.remove(toRemove);
		//}
	}
	
	/** Obtenir la classe graph stream a laquelle appartient le noeud.
	 * Utile pour la coloration.
	 * 
	 * @return
	 */
	public String getGraphStreamClass(){
		String classe = "";
		ArrayList<String> classes = new ArrayList<String>();
		for (Meme meme : getMyMemes()) {
			classes.add(meme.toFourCharString());
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
	}
	
	public ArrayList<Integer> getConnectedNodesIndex(){
		return associatedNode.getConnectedNodes();
	}
	
	/** Obtient la liste des entités connectés depuis la hash de 
	 * temps de connection entre les noeuds. 
	 * 
	 * @return
	 */
	public ArrayList<Entite> getConnectedEntite(){
		ArrayList<Entite> entites = new ArrayList<Entite>(connectedTimeNodes.keySet());
		return entites;
	}
	
	
	public Point getPosition(){
//		return myPlace.get
		return null;
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
		synchronized(myMemes){
			if(fixed)
				breederOn.add(addMeme(e).getAction());
			else 
				myMemes.add(e);
		}
		defineRulesProbaLimited();
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
		for (Meme memeAction : getMyMemes()) {
			resultat += "\n \t "+memeAction;
		}
		 
		return resultat;
	}
	
	public int getNbMemes() {
		synchronized(myMemes){	
			return myMemes.size();
		}
	}

	public ArrayList<ActionType> getActionsAvailables(){
		ArrayList<ActionType> actions = new ArrayList<>();
		synchronized(myMemes){
			for (Meme meme : myMemes) {
				if(!actions.contains(meme.action.getActionType()))
					actions.add(meme.action.getActionType());
			}
		}
		
		return actions;
	}
	
	public void setBreederOn(ArrayList<IAction> breederOn) {
		this.breederOn = breederOn;
	}

	/** Comparaison sur l'index.
	 * 
	 */
	public int compareTo(Entite o) {
		return Integer.compare(this.index, o.index);
	}

	// EndRegion

}

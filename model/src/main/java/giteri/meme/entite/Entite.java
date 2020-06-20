package giteri.meme.entite;

import java.util.*;

import giteri.tool.math.Toolz;
import giteri.network.network.Node;
import giteri.run.configurator.Configurator;
import giteri.run.interfaces.Interfaces.IUnitOfTransfer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/** Entitée conteneur des memes.
 *
 */
public class Entite implements Comparable<Entite>{

	//region Properties

	int index;
	Node associatedNode;
	double probaLearning;
	double probaAppliying;

	// Liste de memes - should be a set
	private List<IUnitOfTransfer> myMemes;

	// répartie sur 0 -> 1, réévalué a chaque ajout ou (retrait de meme)
	Hashtable<Meme, Double> intervalOfSelection;
	//	Hashtable<Entite, Integer> connectedTimeNodes;
	Set<Entite> connectedNodes;
	// défini sur quels memes l'entité fourni le réseau
	IUnitOfTransfer breederOn;
	boolean isBreeder;

	//endregion

	/** Constructeur d'entite.
	 *
	 */
	public Entite(){
		associatedNode = null;
		myMemes = new ArrayList<IUnitOfTransfer>();
		intervalOfSelection = new Hashtable<>();
		connectedNodes = new HashSet<>();
		probaAppliying = 1;
		isBreeder = false;
	}

	/** Défini les regles de sélection de facon équiprobable.
	 *
	 */
	@SuppressWarnings("unchecked")
	public void defineRulesProbaLimited(){
		double roll;
		intervalOfSelection.clear();
		List<Meme> memes = new ArrayList<>();
		Iterator<Meme> it;

		// pour chaque UnitOfTransfer de l'entité. En gros 2 meme ou 1 coupleMeme
		for (IUnitOfTransfer Iu : getMyUnitOfT()) {
			// Iterator sur meme. En gros 2 meme dans le cas d'un couple ou 1 meme dans le cas d'un meme
			it = Iu.iterator();
			while(it.hasNext())
				memes.add(it.next());
		}

		// En gros tjrs divisé par 2
		for (Meme meme : memes) {
			roll = 1.0 / memes.size();
			intervalOfSelection.put(meme, roll);
		}

	}


	/** Fait ses vérification internes avant d'accepter de prendre un meme
	 *
	 * @param toReceive
	 * @return
	 */
	public IUnitOfTransfer takeMyMeme(IUnitOfTransfer toReceive){
		boolean ok = false;

		if(!getMyUnitOfT().contains(toReceive) &&
		(breederOn == null || breederOn.getActionType() != toReceive.getActionType()))
			ok = true;

		if(ok)
			return addOrReplaceFast(toReceive);
		return null;
	}


	/** ajoute un meme ou remplace un meme déja existant,
	 * avec une notion de slot pour meme d'ajout et meme de retrait.
	 *
	 * @param memeToAdd
	 * @return memetoAdd si ajout, l'ancien meme si remplacement, null si rien
	 */
	private IUnitOfTransfer addOrReplaceFast(IUnitOfTransfer memeToAdd){
		boolean okToBeAdded = true;
		boolean needToBeReplaced = false;
		boolean addedOrReplaced = false;
		IUnitOfTransfer memeReplaced = null;

		// On parcourt les meme existant et regarde si un meme of the same category exists
		for (IUnitOfTransfer possededMeme : getMyUnitOfT())
			if(memeToAdd.getActionType() == possededMeme.getActionType())
			{
				// n'est pas censé arriver
				if(memeToAdd.compareTo(possededMeme) == 0) {
					okToBeAdded = false;
					System.err.println("[entite.addOrReplaceFast] - Le meme est déjà possede. la verification aurait du avoir deja exclu ce cas");
					throw new NotImplementedException();
				}
				// si oui, il faudra que la configuration l'autorise pour remplacer l'ancien meme.
				memeReplaced = possededMeme;
				needToBeReplaced = true;
				break;
			}

		// TODO regulariser les synchronize etc. Y compris pour le sort
		// Si on est dans le cas d'un remplacement
		if(needToBeReplaced)
			synchronized(myMemes){
				myMemes.remove(memeReplaced);
			}

		addUOT(memeToAdd);
		try {
			myMemes.sort(null);
		}catch ( ClassCastException cce){
			System.out.println(cce);
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
	 * // TODO [CV] - Verifier que la sélection sur couple ne pose pas de probleme
	 */
	public Meme chooseAction(){

		// On cherche l'élément qui est dans l'interval
		// ProbaEntite [.5=evapo, 0.7=action1, 1.0=action2]  si toSelect renvoi .4, evapo doit etre choisit.
		Meme resultat = null;
		double toSelect = Toolz.getProbaOneOut();
		double borneInf = 0;

		for (Meme looked : intervalOfSelection.keySet())
			if(toSelect >= borneInf && toSelect < borneInf + intervalOfSelection.get(looked) ) {
				resultat = looked;
				break;
			}else
				borneInf += intervalOfSelection.get(looked);

		if(Configurator.debugEntite)
			if(resultat == null )
				System.out.println("Choose action null on cherchait un toSelect a: " + toSelect + "et on avait de dispo " + intervalOfSelection.keySet().toString() + " avec un interval "+intervalOfSelection.values());

		return resultat;
	}

	/** Reset l'état du noeud, garde son node associé mais oubli
	 * les liens avec les autres noeuds, ses mèmes
	 *
	 */
	public void resetStat(){
		getMyUnitOfT().clear();
		intervalOfSelection.clear();
		associatedNode.resetStat();
		connectedNodes.clear();
	}

	public void resetEntiteStuff(){
		connectedNodes.clear();
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
				for (IUnitOfTransfer meme : myMemes) {
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

		for (IUnitOfTransfer meme : getMyUnitOfT()) {
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

	public List<IUnitOfTransfer> getMyUnitOfT(){
		synchronized(myMemes){
			return myMemes;
		}
	}

	public IUnitOfTransfer addUOT(IUnitOfTransfer e, boolean fixed){
		if(fixed) {
			breederOn = addUOT(e);
			isBreeder = true;
		}
		else
			addUOT(e);
		return e;
	}

	public IUnitOfTransfer addUOT(IUnitOfTransfer e){
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

		for (IUnitOfTransfer memeAction : getMyUnitOfT()) {
			resultat += "\n \t "+ memeAction;
		}
		return resultat;
	}


	public boolean isBreeder() {
		return isBreeder;
	}

	public void setBreeder(boolean breeder) {
		isBreeder = breeder;
	}


	/** Comparaison sur l'index.
	 *
	 */
	public int compareTo(Entite o) {
		return Integer.compare(this.index, o.index);
	}

	//endregion

}

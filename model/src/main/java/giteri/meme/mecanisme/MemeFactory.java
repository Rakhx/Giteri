package giteri.meme.mecanisme;

import java.util.*;

import giteri.meme.entite.CoupleMeme;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.ActionType;
import giteri.run.configurator.Configurator.AgregatorType;
import giteri.run.configurator.Configurator.AttributType;
import giteri.meme.entite.Meme;

import giteri.run.configurator.Configurator.MemeList;

/** Permet de gérer les meme du graph
 *
 */
public class MemeFactory {

	//region Properties
	private ActionFactory actionFactory;
	private FilterFactory filterFactory;
	private AttributFactory attributFactory;

	// Liste de meme qui sont instancié de abse sur la map. Pas nécessairement utilisé.
	private ArrayList<Meme> memeOnMap;
	// Liste des memes existant dans la simulation
	private ArrayList<Meme> memeExisting;
	// Liste des memes existant et qui seront utlisé pour le fitting
	private ArrayList<Meme> memeFitting;
	// association meme & index, pour coloration ET fitting
	private Hashtable<Meme, Integer> kvMemeIndex;
	// KV  fourChar :: name
	private Hashtable<String, String> memeTranslationReadable;

	// Concernant les couples d'actions et leur probabilité de propagation
	private Map<Integer, CoupleMeme> coupleAction;
	private Map<Integer, Double> coupleProbaPerIndex;

	private Integer lastMemeIndexUsed = -1;
	private boolean hasBeenSorted = false;
	//endregion

	//region  Constructor & Co
	public MemeFactory(ActionFactory actionFac, FilterFactory agregatorFac, AttributFactory attributFac ){
		memeExisting = new ArrayList<>();
		memeFitting = new ArrayList<>();
		memeOnMap = new ArrayList<>();
		kvMemeIndex = new Hashtable<>();
		actionFactory = actionFac;
		filterFactory = agregatorFac;
		attributFactory = attributFac;
		memeTranslationReadable = new Hashtable<>();
		coupleAction = new HashMap<>();
		coupleProbaPerIndex = new HashMap<>();
	}

	//endregion

	//region meme stuff

	/** Permet de créer un meme et de l'ajouter à la liste des memes dispos sur la map.
	 *
	 * @param name Son nom
	 * @param probaOfPropa Proba de propagation sur application si utilisée dans le model
	 * @param addForMap Ajout a la liste des memes disponibles sur la map
	 * @param addForFitting Ajout a la liste pour le fitting
	 * @param actionAsked Le type d'action associée
	 * @param attributs attributs concerné
	 * @param KVAttributAgregator liste de filtre
	 * @param fluidite meme considéré comme fluidité ~~
	 * @return Le meme nouvellement créé
	 */
	public Meme registerMemeAction(String name, double probaOfPropa, boolean addForMap, boolean addForFitting, ActionType actionAsked,
								   ArrayList<AttributType> attributs,
								   Hashtable<AttributType, Hashtable<Integer ,AgregatorType>> KVAttributAgregator, boolean fluidite){

		// L'action qui compose le meme
		ActionFactory.IAction action = actionFactory.getAction(actionAsked);

		ArrayList<AttributFactory.IAttribut> attribs = new ArrayList<>();
		Hashtable<String, Hashtable<Integer , FilterFactory.IFilter>> KVAttribAgreg = new Hashtable<>();

		AttributFactory.IAttribut<?> attribut;
		Hashtable<Integer , FilterFactory.IFilter> listAgregator;
		Hashtable<Integer ,AgregatorType> listAgreType;

		// Pour chaque attribut sur lequel porte le meme
		for (AttributType attributType : attributs) {
			attribut = attributFactory.getAttribut(attributType);
			attribs.add(attribut);
			listAgregator = new Hashtable<>();
			KVAttribAgreg.put(attribut.toString(), listAgregator);

			listAgreType = KVAttributAgregator.get(attributType);
			for (Integer agregatorTypeOrder : listAgreType.keySet()) {
				listAgregator.put(agregatorTypeOrder , filterFactory.getFilter(listAgreType.get(agregatorTypeOrder)));
			}
		}

		// bon bref le meme
		Meme toReturn = new Meme(name, probaOfPropa, fluidite ,action, attribs, KVAttribAgreg);
		for (Meme aMemeExisting : memeExisting) {
			if(aMemeExisting.getName().compareTo(name) == 0 ){
				System.err.println("[MemeFactory.registerMemeAction()] : Erreur, meme déjà présent avec le meme nom");
				return null;
			}
		}

		memeExisting.add(toReturn);
		if(addForFitting)
			memeFitting.add(toReturn);
		if(addForMap)
			memeOnMap.add(toReturn);

		kvMemeIndex.put(toReturn, ++lastMemeIndexUsed);
		memeTranslationReadable.put(toReturn.toFourCharString(),toReturn.getName());
		return toReturn;
	}

	/** pas opti mais plus rapide. ( espérons ).
	 *
	 * @param whichAction
	 * @return
	 */
	public ArrayList<Meme> getMemes(Configurator.MemeList typeMeme, ActionType whichAction){

		//region maps
		if(typeMeme == Configurator.MemeList.ONMAP) {
			if (whichAction == ActionType.ANYTHING)
				return memeOnMap;
			else {
				ArrayList<Meme> toReturn = new ArrayList<>();
				for (Meme meme : memeOnMap) {
					if (meme.getAction().getActionType() == whichAction)
						toReturn.add(meme);
				}

				return toReturn;
			}
		}
		//endregion
		//region fitting
		else if (typeMeme == Configurator.MemeList.FITTING){
			if(whichAction == ActionType.ANYTHING)
				return memeFitting;
			else {
				ArrayList<Meme> toReturn = new ArrayList<>();
				for (Meme meme : memeFitting) {
					if (meme.getAction().getActionType() == whichAction)
						toReturn.add(meme);
				}

				return toReturn;
			}
		}
		//endregion
		//region existing
		else {
			if(whichAction == ActionType.ANYTHING)
				return memeExisting;
			else {
				ArrayList<Meme> toReturn = new ArrayList<>();
				for (Meme meme : memeExisting) {
					if (meme.getAction().getActionType() == whichAction)
						toReturn.add(meme);
				}

				return toReturn;
			}
		}
		//endregion

	}

	/**
	 *
	 * @param typeMeme
	 * @param whichAction
	 * @return
	 */
	public String getMemesAsString(Configurator.MemeList typeMeme, ActionType whichAction){
		String resultat = "";
		for (Meme meme : getMemes(typeMeme, whichAction)) {
			resultat += ":" + meme.toFourCharString() + "%" + meme.getProbaOfPropagation();
		}

		return resultat;
	}

	/** Retourne les memes disponibles qui font un certain type d'action
	 * en paramètre.
	 *
	 * @param action Type de l'action que les memes doivent appliqués
	 * @return
	 */
	public ArrayList<Meme> getMemeAvailable(ActionType action, boolean forFitting){
		ArrayList<Meme> goodOne = new ArrayList<>();
		if(forFitting){
			for (Meme meme : memeFitting)
				if(meme.getAction().getActionType() == action)
					goodOne.add(meme);
		} else
			for (Meme meme : memeExisting)
				if(meme.getAction().getActionType() == action)
					goodOne.add(meme);

		return goodOne;
	}

	/** Retourne la liste des memes dispos en string.
	 *
	 */
	public String getMemeAvailableAsString(Configurator.MemeList usage){
		String resultat = "";
		for (Meme meme : getMemes(usage, ActionType.ANYTHING)) {
			resultat += ":" + meme.toFourCharString() + "-ProbaPropagation:" + meme.getProbaOfPropagation();
		}

		return resultat;
	}

	/** obtenir un meme depuis son fourcharname, sinon null.
	 *
	 * @param foursizeName
	 * @return
	 */
	public Meme getMemeFromFourString(String foursizeName){
		for (Meme meme: getMemes(MemeList.EXISTING, ActionType.ANYTHING)) {
			if(meme.toFourCharString().compareTo(foursizeName)==0)
				return meme;
		}

		return null;
	}

	/** Recherche dans la list memeTranslationReadable une entrée ayant pour value le name (add+),
	 * prend sa clef (ADLKMNSPNTLK) et renvoie le meme associé
	 *
	 * @param name
	 * @return
	 */
	public Meme getMemeFromName(String name){
		for (Map.Entry<String, String> ssEntry : memeTranslationReadable.entrySet()) {
			if(ssEntry.getValue().compareToIgnoreCase(name) == 0)
				return getMemeFromFourString(ssEntry.getKey());
		}

		return null;
	}

	/** Transforme les adlkminesup en add+;
	 * TODO [WayPoint]- traduction .add+ <= ADLKMTNTSPMN etc
	 * @param memeCombinaison
	 * @return un truc plus clair a lire.
	 */
	public String translateMemeCombinaisonReadable(String memeCombinaison) {
		String compo = "";
		String[] combinaison = memeCombinaison.contains(".")? memeCombinaison.split("\\."):new String[]{memeCombinaison};
		for (String oneName: memeTranslationReadable.keySet()) {
			for (String combi:combinaison) {
				if(combi.compareToIgnoreCase(oneName) == 0)
					compo += "." + memeTranslationReadable.get(combi);
			}
		}

		return compo;
	}
	//endregion

	//region couple meme Stuff

	/**
	 *
	 * @param add
	 * @param rmv
	 * @param proba
	 * @return
	 */
	private CoupleMeme registerCoupleMeme(int index, Meme add, Meme rmv, double proba){
		CoupleMeme cm = new CoupleMeme(index, add, rmv, proba);
		if(index >= 0)
			this.coupleAction.put(index, cm);
		return cm;
	}

	/** depuis le nom des memes d'ajout//retrait, associé à un index et une proba de T
	 *
	 * @param index
	 * @param addName
	 * @param rmvName
	 * @param proba
	 * @return
	 */
	public CoupleMeme extractAndAddCoupleMeme(int index, String addName, String rmvName, double proba){
		Meme add, rmv;
		add = this.getMemeFromName(addName);
		rmv = this.getMemeFromName(rmvName);
		return this.registerCoupleMeme(index, add, rmv, proba);
	}

	/** Génère une liste de coupleMeme depuis les deux tableaux d'activation d'add et rmv. Produit carthésien
	 *
	 * @param addActi
	 * @param rmvActi
	 * @return
	 */
	public List<CoupleMeme> generateCoupleFromActivation(boolean[] addActi, boolean[] rmvActi){
		// int i = 0, j = 0;
		List<CoupleMeme> selected = new ArrayList<>();
		List<Meme> addz, rmvz;
		Meme add, rmv;
		CoupleMeme cree;
		int index = -1;
		addz = getMemes(MemeList.FITTING, ActionType.AJOUTLIEN);
		rmvz = getMemes(MemeList.FITTING, ActionType.RETRAITLIEN);
		for (int i = 0; i < addActi.length; i++) {
			if(addActi[i]) {
				add = addz.get(i);
				for (int j = 0; j < rmvActi.length; j++) {
					// A new combinaison is born
					if(rmvActi[j]){
						rmv = rmvz.get(j);
						cree = extractAndAddCoupleMeme(++index, add.getName(), rmv.getName(), .1);
						selected.add(cree);
						this.coupleAction.put(index,cree);
					}
				}
			}
		}

		return selected;
	}

	public void associateProbaWithCouple(List<Double> proba){
		assert (coupleAction.size() == proba.size());
		for (int i = 0; i < coupleAction.size(); i++) {
			coupleAction.get(i).setProbaPropagation(proba.get(i));
			coupleProbaPerIndex.put(i, proba.get(i));

		}

	}

	public CoupleMeme getCoupleMemeFromIndex(int index){
		return coupleAction.get(index);
	}

	public ArrayList<CoupleMeme> getCoupleMemes(){
		return new ArrayList<>(coupleAction.values());
	}

	public Collection<CoupleMeme> getCouple(){
		return new ArrayList<>(coupleAction.values());
	}



	//endregion

	//region index stuff

	/** pour l'affichage. récupération apres association d'une couleur a un meme
	 *
	 * @param thismeme
	 * @return
	 */
	public Integer getIndexFromMeme(Meme thismeme){
		return kvMemeIndex.get(thismeme);
	}

	/** Pour l'affichage
	 *
	 * @param memeAsString
	 * @return
	 */
	public Integer getIndexFromMemeFourChar(String memeAsString){
		for (Meme meme : kvMemeIndex.keySet()) {
			if(meme.toFourCharString().compareTo(memeAsString) == 0)
				return kvMemeIndex.get(meme);
		}
		return null;
	}

	/**
	 *
	 * @param numero
	 * @return
	 */
	public Meme getMemeFromIndex(int numero){
		for (Meme meme : kvMemeIndex.keySet()) {
			if(kvMemeIndex.get(meme) == numero)
				return meme;
		}
		return null;
	}

	public Meme getIemeMemeFromSpecList(MemeList list, int ieme){
		if(!hasBeenSorted){
			memeExisting.sort(null);
			memeFitting.sort(null);
			memeOnMap.sort(null);
			hasBeenSorted = true;
		}

		int position = 0;
		for (Meme meme: getMemes(list, ActionType.ANYTHING )) {
			if(ieme == position++){
				return meme;
			}
		}

		return null;
	}

	//endregion
}


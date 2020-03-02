package giteri.meme.mecanisme;

import java.util.*;
import java.util.stream.Collectors;

import giteri.meme.entite.CoupleMeme;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.TypeOfUOT;
import giteri.run.configurator.Configurator.AgregatorType;
import giteri.run.configurator.Configurator.AttributType;
import giteri.meme.entite.Meme;

import giteri.run.configurator.Configurator.MemeList;
import giteri.run.interfaces.Interfaces.IUnitOfTransfer;
import sun.nio.ch.IOUtil;

/** Permet de gérer les meme du graph
 *
 */
public class MemeFactory {

	//region Properties
	private ActionFactory actionFactory;
	private FilterFactory filterFactory;
	private AttributFactory attributFactory;

	// Liste des memes de base existant dans la simulation
	private ArrayList<Meme> memeExisting;
	// Liste de IUnitOfTransfer qui sont instancié de base sur la map au démarrage
	private ArrayList<IUnitOfTransfer> uOTOnMap;
	// Liste de IUnitOfTransfer existant et qui seront utilisé pour le fitting
	private ArrayList<IUnitOfTransfer> uOTFitting;

	// association meme & index, pour coloration ET fitting
	private Hashtable<IUnitOfTransfer, Integer> kvMemeIndex;
	// KV  fourChar :: name
	private Hashtable<String, String> memeTranslationReadable;

	// Concernant les couples d'actions et leur probabilité de propagation
	// private Map<Integer, CoupleMeme> coupleAction;
	//private Map<Integer, Double> coupleProbaPerIndex;

	private Integer lastMemeIndexUsed = -1;
	private boolean hasBeenSorted = false;

	private static int indexOfIOT = -1;
	//endregion

	//region  Constructor & Co
	public MemeFactory(ActionFactory actionFac, FilterFactory agregatorFac, AttributFactory attributFac ){
		memeExisting = new ArrayList<>();
		uOTFitting = new ArrayList<>();
		uOTOnMap = new ArrayList<>();
		kvMemeIndex = new Hashtable<>();
		actionFactory = actionFac;
		filterFactory = agregatorFac;
		attributFactory = attributFac;
		memeTranslationReadable = new Hashtable<>();
//		coupleAction = new HashMap<>();
//		coupleProbaPerIndex = new HashMap<>();
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
	public Meme registerMemeAction(String name, double probaOfPropa, boolean addForMap, boolean addForFitting, TypeOfUOT actionAsked,
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
			uOTFitting.add(toReturn);
		if(addForMap)
			uOTOnMap.add(toReturn);

		kvMemeIndex.put(toReturn, ++lastMemeIndexUsed);
		memeTranslationReadable.put(toReturn.toFourCharString(),toReturn.getName());
		return toReturn;
	}

	/** Permet d'obtenir les unité d'action disponibles. Les couples action on été enregistré en type anything.
	 *
	 * @param memeUsage : onMap, Existing, Fitting
	 * @param typeAction : AJOUTLIEN, RETRAITLIEN, ANYTHING
	 * @return
	 */
	public ArrayList<IUnitOfTransfer> getMemes(Configurator.MemeList memeUsage, TypeOfUOT typeAction){

		//region maps
		if(memeUsage == Configurator.MemeList.ONMAP) {
			if (typeAction == TypeOfUOT.ANYTHING)
				return uOTOnMap;
			else {
				ArrayList<IUnitOfTransfer> toReturn = new ArrayList<>();
				for (IUnitOfTransfer meme : uOTOnMap) {
					if (meme.getActionType() == typeAction)
						toReturn.add(meme);
				}

				return toReturn;
			}
		}
		//endregion
		//region fitting
		else if (memeUsage == Configurator.MemeList.FITTING){
			if(typeAction == TypeOfUOT.ANYTHING)
				return uOTFitting;
			else {
				ArrayList<IUnitOfTransfer> toReturn = new ArrayList<>();
				for (IUnitOfTransfer meme : uOTFitting) {
					if (meme.getActionType() == typeAction)
						toReturn.add(meme);
				}

				return toReturn;
			}
		}
		//endregion
		//region existing
		else {
			ArrayList<IUnitOfTransfer> toReturn = new ArrayList<>();
			if(typeAction == TypeOfUOT.ANYTHING) {

				for (Meme meme : memeExisting) {
					toReturn.add(meme);
				}
				return toReturn;
			}
			else {
				for (Meme meme : memeExisting) {
					if (meme.getAction().getActionType() == typeAction)
						toReturn.add(meme);
				}

				return toReturn;
			}
		}
		//endregion

	}

	/** Retourne les memes disponibles qui font un certain type d'action
	 * en paramètre.
	 *
	 * @param action Type de l'action que les memes doivent appliqués
	 * @return
	 */
	public ArrayList<IUnitOfTransfer> getMemeAvailable(TypeOfUOT action, boolean forFitting){
		ArrayList<IUnitOfTransfer> goodOne = new ArrayList<>();
		if(forFitting){
			for (IUnitOfTransfer meme : uOTFitting)
				if(meme.getActionType() == action)
					goodOne.add(meme);
		} else
			for (Meme meme : memeExisting)
				if(meme.getAction().getActionType() == action)
					goodOne.add(meme);

		return goodOne;
	}

	/** obtenir un meme depuis son fourcharname, sinon null.
	 *
	 * @param foursizeName
	 * @return
	 */
	public IUnitOfTransfer getMemeFromFourString(String foursizeName){
		for (IUnitOfTransfer meme: getMemes(MemeList.EXISTING, TypeOfUOT.ANYTHING)) {
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
	public IUnitOfTransfer getMemeFromName(String name){
		for (Map.Entry<String, String> ssEntry : memeTranslationReadable.entrySet()) {
			if(ssEntry.getValue().compareToIgnoreCase(name) == 0)
				return getMemeFromFourString(ssEntry.getKey());
		}

		return null;
	}

	/**
	 *
	 * @param typeMeme
	 * @param whichAction
	 * @return
	 */
	public String getMemesAsString(Configurator.MemeList typeMeme, TypeOfUOT whichAction){
		String resultat = "";
		for (IUnitOfTransfer meme : getMemes(typeMeme, whichAction)) {
			resultat += ":" + meme.toFourCharString() + "%" + meme.getProbaPropagation();
		}

		return resultat;
	}

	/** Retourne la liste des memes dispos en string.
	 *
	 */
	public String getMemeAvailableAsString(Configurator.MemeList usage){
		String resultat = "";
		for (IUnitOfTransfer meme : getMemes(usage, TypeOfUOT.ANYTHING)) {
			resultat += ":" + meme.toFourCharString() + "-ProbaPropagation:" + meme.getProbaPropagation();
		}

		return resultat;
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

	/** depuis le nom des memes d'ajout//retrait, associé à un index et une proba de T
	 *
	 * @param index
	 * @param addName
	 * @param rmvName
	 * @param proba
	 * @return
	 */
	public IUnitOfTransfer<CoupleMeme> extractAndAddCoupleMeme(String addName, String rmvName, double proba, boolean forFitting){
		Meme add, rmv;
		add = (Meme)this.getMemeFromName(addName);
		rmv = (Meme)this.getMemeFromName(rmvName);
		return this.registerCoupleMeme(add, rmv, proba, forFitting);
	}

	/** depuis le nom des memes d'ajout//retrait, associé à un index et une proba de T. Ne l'enregistre pas dans la liste
	 * des memes disponibles sur la map pour fitting
	 *
	 * @param index
	 * @param addName
	 * @param rmvName
	 * @param proba
	 * @return
	 */
	public IUnitOfTransfer<CoupleMeme> extractAndDoNotRegister(String addName, String rmvName, double proba){
		Meme add, rmv;
		add = (Meme)this.getMemeFromName(addName);
		rmv = (Meme)this.getMemeFromName(rmvName);
		return new CoupleMeme(-1, add, rmv, proba);
	}

	/** Génère une liste de coupleMeme depuis les deux tableaux d'activation d'add et rmv. Produit carthésien
	 *
	 * @param addActi
	 * @param rmvActi
	 * @return
	 */
	public List<IUnitOfTransfer<CoupleMeme>> generateCoupleFromActivation(boolean[] addActi, boolean[] rmvActi){
		// int i = 0, j = 0;
		List<IUnitOfTransfer<CoupleMeme>> selected = new ArrayList<>();
		List<Meme> addz, rmvz;
		Meme add, rmv;
		IUnitOfTransfer<CoupleMeme> cree;
		int index = -1;

		if(!Configurator.jarMode)
			clearExistingCouple();

		addz = getMemes(MemeList.FITTING, TypeOfUOT.AJOUTLIEN).stream().map(e -> (Meme)e).collect(Collectors.toList());
		rmvz = getMemes(MemeList.FITTING, TypeOfUOT.RETRAITLIEN).stream().map(e -> (Meme)e).collect(Collectors.toList());;
		for (int i = 0; i < addActi.length; i++) {
			if(addActi[i]) {
				add = addz.get(i);
				for (int j = 0; j < rmvActi.length; j++) {
					// A new combinaison is born
					if(rmvActi[j]){
						rmv = rmvz.get(j);
						cree = extractAndAddCoupleMeme(add.getName(), rmv.getName(), .1, true);
						selected.add(cree);
					}
				}
			}
		}

		return selected;
	}

	/** Prends les couples d'action disponibles utilisable pour le fitting et y associe dans l'ordre des prba
	 * de transmission.
	 *
	 * @param proba
	 */
	public void associateProbaWithCouple(List<Double> proba){
		List<IUnitOfTransfer> coupleAction = this.getMemes(MemeList.FITTING,TypeOfUOT.COUPLE);
		assert (coupleAction.size() == proba.size());
		for (int i = 0; i < coupleAction.size(); i++) {
			coupleAction.get(i).setProbaPropagation(proba.get(i));
		}
	}

	private void clearExistingCouple(){
		List<IUnitOfTransfer> toRemove = new ArrayList<>();
		for (IUnitOfTransfer iUnitOfTransfer : uOTFitting) {
			if(iUnitOfTransfer.getActionType() == TypeOfUOT.COUPLE)
				toRemove.add(iUnitOfTransfer);
		}

		uOTFitting.removeAll(toRemove);
	}

	/** Register d'un couple meme avec les memes d'ajout, de retrait, et la proba de probagation.
	 *
	 *
	 * @param add
	 * @param rmv
	 * @param proba
	 * @return
	 */
	private IUnitOfTransfer<CoupleMeme> registerCoupleMeme(Meme add, Meme rmv, double proba, boolean forFitting){
		indexOfIOT++;
		IUnitOfTransfer<CoupleMeme> icm = new CoupleMeme(indexOfIOT, add, rmv, proba);
		if(forFitting)
		this.uOTFitting.add(icm);
		else
			this.uOTOnMap.add(icm);
		kvMemeIndex.put(icm, indexOfIOT);
		return icm;
	}
	//endregion

	//region index stuff

	/** pour l'affichage. récupération apres association d'une couleur à un IUnitOfTransfer
	 *
	 * @param thismeme
	 * @return
	 */
	public Integer getIndexFromMeme(IUnitOfTransfer thismeme){
		return kvMemeIndex.get(thismeme);
	}

	/** Pour l'affichage
	 *
	 * @param memeAsString
	 * @return
	 */
	public Integer getIndexFromMemeFourChar(String memeAsString){
		for (IUnitOfTransfer meme : kvMemeIndex.keySet()) {
			if(meme.toFourCharString().compareTo(memeAsString) == 0)
				return kvMemeIndex.get(meme);
		}
		return null;
	}

//	/**
//	 *
//	 * @param numero
//	 * @return
//	 */
//	public Meme getMemeFromIndex(int numero){
//		for (IUnitOfTransfer meme : kvMemeIndex.keySet()) {
//			if(kvMemeIndex.get(meme) == numero)
//				return meme;
//		}
//		return null;
//	}

	public IUnitOfTransfer getIemeMemeFromSpecList(MemeList list, int ieme){
		if(!hasBeenSorted){
			memeExisting.sort(null);
			uOTFitting.sort(null);
			uOTOnMap.sort(null);
			hasBeenSorted = true;
		}

		int position = 0;
		for (IUnitOfTransfer meme: getMemes(list, TypeOfUOT.ANYTHING)) {
			if(ieme == position++){
				return meme;
			}
		}

		return null;
	}

	//endregion
}


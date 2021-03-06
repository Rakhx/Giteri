package giteri.meme.mecanisme;

import java.util.ArrayList;
import java.util.Hashtable;

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

	// Singleton
	// Liste de meme qui existe. Pas nécessairement utilisé.
	private ArrayList<Meme> memeOnMap;

	// Liste des memes disponibles sur la map
	private ArrayList<Meme> memeExisting;

	// Liste des memes dispo sur la map et qui seront utlisé pour le fitting
	private ArrayList<Meme> memeFitting;

	// association meme & index, pour coloration ET fitting
	private Hashtable<Meme, Integer> kvMemeIndex;

	private Hashtable<String, String> memeTranslationReadable;

	private Integer lastIndexUsed = -1;

	private ActionFactory actionFactory;
	private FilterFactory filterFactory;
	private AttributFactory attributFactory;

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
	}

	//endregion

	/** Permet de créer un meme et de l'ajouter à la liste des memes dispos sur la map.
	 *
	 * @param name
	 * @param actionAsked
	 * @param attributs
	 * @param KVAttributAgregator
	 * @return
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

		kvMemeIndex.put(toReturn, ++lastIndexUsed);

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
	public ArrayList<Meme> getMemeAvailable(ActionType action, boolean forFitting, boolean forNormal){
		ArrayList<Meme> goodOne = new ArrayList<Meme>();
		if(forFitting){
			for (Meme meme : memeFitting)
				if(meme.getAction().getActionType() == action || action == ActionType.ANYTHING)
					goodOne.add(meme);

		}
		else if(forNormal){
			for (Meme meme : memeOnMap)
				if(meme.getAction().getActionType() == action || action == ActionType.ANYTHING)
					goodOne.add(meme);
		}
		else
			for (Meme meme : memeExisting)
				if(meme.getAction().getActionType() == action|| action == ActionType.ANYTHING)
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
		for (Meme meme: getMemes(MemeList.EXISTING, ActionType.ANYTHING )) {
			if(meme.toFourCharString().compareTo(foursizeName)==0)
				return meme;
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


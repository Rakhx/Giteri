package giteri.meme.mecanisme;

import java.util.ArrayList;
import java.util.Hashtable;

import giteri.run.IHM;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.ActionType;
import giteri.run.configurator.Configurator.AgregatorType;
import giteri.run.configurator.Configurator.AttributType;
import giteri.meme.entite.Meme;

/** Permet de gérer les meme du graph
 *
 */
public class MemeFactory {

	//region Properties

	// Singleton
	private static MemeFactory INSTANCE = null;

	// Liste de meme qui existe. Pas nécessairement utilisé.
	private ArrayList<Meme> memeOnMap;

	// Liste des memes disponibles sur la map
	private ArrayList<Meme> memeExisting;

	// Liste des memes dispo sur la map et qui seront utlisé pour le fitting
	private ArrayList<Meme> memeFitting;

	private Hashtable<Meme, Integer> kvMemeIndexColor;

	private Integer lastIndexUsed = -1;

	private ActionFactory actionFactory;
	private AgregatorFactory agregatorFactory;
	private AttributFactory attributFactory;

	//endregion

	//region SINGLETON Constructor & Co
	public MemeFactory(ActionFactory actionFac, AgregatorFactory agregatorFac, AttributFactory attributFac ){
		memeExisting = new ArrayList<Meme>();
		memeFitting = new ArrayList<Meme>();
		memeOnMap = new ArrayList<>();
		kvMemeIndexColor = new Hashtable<Meme, Integer>();
		actionFactory = actionFac;
		agregatorFactory = agregatorFac;
		attributFactory = attributFac;
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
	public Meme registerMemeAction(String name, double proba, ActionType actionAsked, ArrayList<AttributType> attributs,
								   Hashtable<AttributType, Hashtable<Integer ,AgregatorType>> KVAttributAgregator,boolean addForMap ,boolean addForFitting){

		// L'action qui compose le meme
		ActionFactory.IAction action = actionFactory.getAction(actionAsked);
		@SuppressWarnings("rawtypes")
		ArrayList<AttributFactory.IAttribut> attribs = new ArrayList<AttributFactory.IAttribut>();
		Hashtable<String, Hashtable<Integer ,AgregatorFactory.IAgregator>> KVAttribAgreg = new Hashtable<String, Hashtable<Integer ,AgregatorFactory.IAgregator>>();

		AttributFactory.IAttribut<?> attribut;
		Hashtable<Integer ,AgregatorFactory.IAgregator> listAgregator;
		Hashtable<Integer ,AgregatorType> listAgreType;

		// Pour chaque attribut sur lequel porte le meme
		for (AttributType attributType : attributs) {
			attribut = attributFactory.getAttribut(attributType);
			attribs.add(attribut);
			listAgregator = new Hashtable<>();
			KVAttribAgreg.put(attribut.toString(), listAgregator);

			listAgreType = KVAttributAgregator.get(attributType);
			for (Integer agregatorTypeOrder : listAgreType.keySet()) {
				listAgregator.put(agregatorTypeOrder ,agregatorFactory.getAgregator(listAgreType.get(agregatorTypeOrder)));
			}
		}

		// bon bref le meme
		Meme toReturn = new Meme(name, proba, action, attribs, KVAttribAgreg);
		for (Meme aMemeExisting : memeExisting) {
			if(aMemeExisting.getName().compareTo(name) == 0 ){
				System.err.println("[MemeFactory.registerMemeAction()] : Erreur, meme déjà présent avec le meme nom");
				return null;
			}
		}

		if(addForFitting)
			memeFitting.add(toReturn);
		if(addForMap)
			memeOnMap.add(toReturn);
		memeExisting.add(toReturn);
		kvMemeIndexColor.put(toReturn, ++lastIndexUsed);

		return toReturn;
	}

	/** Va définir les memes disponibles sur la simu courante.
	 *
	 */
	public void setMemeAvailableForSimulation(){
	ArrayList<Meme> hihi = (ArrayList<Meme>) blabla(true);
	}


	public ArrayList<?> blabla(boolean po){
		if(po)
			return new ArrayList<String>();
		else
			return new ArrayList<Meme>();
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



	/** Permet de retourner l'ensemble des memes disponibles pour la map
	 *
	 * @return l'ensemble des memes qui ont été généré par la factory.
	 */
//	public ArrayList<Meme> getMemeAvailable(boolean forFitting) {
//		if(forFitting)
//			return memeFitting;
//		return memeExisting;
//	}

	/** Retourne les memes disponibles qui font un certain type d'action
	 * en paramètre.
	 *
	 * @param action Type de l'action que les memes doivent appliqués
	 * @return
	 */
	public ArrayList<Meme> getMemeAvailable(ActionType action, boolean forFitting){
		ArrayList<Meme> goodOne = new ArrayList<Meme>();
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
	public String getMemeAvailableAsString(boolean forFitting){
		String resultat = "";
		if(forFitting)
			for (Meme meme : memeFitting) {
				resultat += ":" + meme.toFourCharString() + "%" + meme.getProbaOfPropagation();
			}
		else
			for (Meme meme : memeExisting) {
				resultat += ":" + meme.toFourCharString() + "%" + meme.getProbaOfPropagation();
			}

		return resultat;
	}

	/** pour l'affichage. récupération apres association d'une couleur a un meme
	 *
	 * @param thismeme
	 * @return
	 */
	public Integer getColorIndex(Meme thismeme){
		return kvMemeIndexColor.get(thismeme);
	}

	/** Pour l'affichage
	 *
	 * @param memeAsString
	 * @return
	 */
	public Integer getColorIndexStringConversion(String memeAsString){
		for (Meme meme : kvMemeIndexColor.keySet()) {
			if(meme.toFourCharString().compareTo(memeAsString) == 0)
				return kvMemeIndexColor.get(meme);
		}
		return null;
	}

	public Meme getMemeFromInteger(int numero){
		for (Meme meme : kvMemeIndexColor.keySet()) {
			if(kvMemeIndexColor.get(meme) == numero)
				return meme;
		}
		return null;
	}

}

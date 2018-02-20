package giteri.meme.mecanisme;

import java.util.ArrayList;
import java.util.Hashtable;

import giteri.run.configurator.Configurator.ActionType;
import giteri.run.configurator.Configurator.AgregatorType;
import giteri.run.configurator.Configurator.AttributType;
import giteri.meme.entite.Meme;

/** Permet de gérer les meme du graph
 *
 */
public class MemeFactory {

	// region Properties

	// Singleton
	private static MemeFactory INSTANCE = null;

	// Liste des memes disponibles sur la map
	private ArrayList<Meme> memeAvailable;

	// Liste des memes dispo sur la map et qui seront utlisé pour le fitting
	private ArrayList<Meme> memeAvailableFitting;

	private Hashtable<Meme, Integer> kvMemeIndexColor;

	private Integer lastIndexUsed = -1;

	private ActionFactory actionFactory;
	private AgregatorFactory agregatorFactory;
	private AttributFactory attributFactory;

	// endregion

	// Region SINGLETON Constructor & Co
	public MemeFactory(ActionFactory actionFac, AgregatorFactory agregatorFac, AttributFactory attributFac ){
		memeAvailable = new ArrayList<Meme>();
		memeAvailableFitting = new ArrayList<Meme>();
		kvMemeIndexColor = new Hashtable<Meme, Integer>();
		Init();
		actionFactory = actionFac;
		agregatorFactory = agregatorFac;
		attributFactory = attributFac;

	}

	/**
	 * Initialisation des types de meme
	 */
	public void Init(){
	}

	// EndRegion

	/** Permet de créer un meme et de l'ajouter a la liste des memes dispos sur la map.
	 *
	 * @param name
	 * @param actionAsked
	 * @param attributs
	 * @param KVAttributAgregator
	 * @return
	 */
	public Meme registerMemeAction(String name, double proba, ActionType actionAsked, ArrayList<AttributType> attributs, Hashtable<AttributType, Hashtable<Integer ,AgregatorType>> KVAttributAgregator, boolean addToList){

		ActionFactory.IAction action = actionFactory.getAction(actionAsked);
		@SuppressWarnings("rawtypes")
		ArrayList<AttributFactory.IAttribut> attribs = new ArrayList<AttributFactory.IAttribut>();
		Hashtable<String, Hashtable<Integer ,AgregatorFactory.IAgregator>> KVAttribAgreg = new Hashtable<String, Hashtable<Integer ,AgregatorFactory.IAgregator>>();

		AttributFactory.IAttribut<?> attribut;
		Hashtable<Integer ,AgregatorFactory.IAgregator> listAgregator;
		Hashtable<Integer ,AgregatorType> listAgreType;

		for (AttributType attributType : attributs) {
			attribut = attributFactory.getAttribut(attributType);
			attribs.add(attribut);
			listAgregator = new Hashtable<Integer, AgregatorFactory.IAgregator>();
			KVAttribAgreg.put(attribut.toString(), listAgregator);

			listAgreType = KVAttributAgregator.get(attributType);
			for (Integer agregatorTypeOrder : listAgreType.keySet()) {
				listAgregator.put(agregatorTypeOrder ,agregatorFactory.getAgregator(listAgreType.get(agregatorTypeOrder)));
			}
		}

		Meme toReturn = new Meme(name,proba, action, attribs, KVAttribAgreg);
		for (Meme existingMeme : memeAvailable) {
			if(existingMeme.getName().compareTo(name) == 0 ){
				System.err.println("[MemeFactory.registerMemeAction()] : Erreur, meme déjà présent avec le meme nom");
				return null;
			}
		}

		if(addToList)
			memeAvailableFitting.add(toReturn);
		memeAvailable.add(toReturn);
		kvMemeIndexColor.put(toReturn, ++lastIndexUsed);

		return toReturn;
	}

	/** Permet de retourner l'ensemble des memes disponibles pour la map
	 *
	 * @return l'ensemble des memes qui ont été généré par la factory.
	 */
	public ArrayList<Meme> getMemeAvailable(boolean forFitting) {
		if(forFitting)
			return memeAvailableFitting;
		return memeAvailable;
	}

	/** Retourne les memes disponibles qui font un certain type d'action
	 * en paramètre.
	 *
	 * @param action Type de l'action que les memes doivent appliqués
	 * @return
	 */
	public ArrayList<Meme> getMemeAvailable(ActionType action, boolean forFitting){
		ArrayList<Meme> goodOne = new ArrayList<Meme>();
		if(forFitting){
			for (Meme meme : memeAvailableFitting)
				if(meme.getAction().getActionType() == action)
					goodOne.add(meme);
		} else
			for (Meme meme : memeAvailable)
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
			for (Meme meme : memeAvailableFitting) {
				resultat += ":" + meme.toFourCharString() + "%" + meme.probaOfPropagation;
			}
		else
			for (Meme meme : memeAvailable) {
				resultat += ":" + meme.toFourCharString() + "%" + meme.probaOfPropagation;
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

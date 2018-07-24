package giteri.meme.mecanisme;
import java.util.ArrayList;
import java.util.Set;

import giteri.tool.math.Toolz;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.ActionType;
import giteri.meme.entite.Entite;
import giteri.meme.entite.EntiteHandler;
import giteri.meme.entite.Meme;
/** Distributeur d'action
 *
 */
public class ActionFactory{

	//region  constructeur
	private EntiteHandler entiteHandler;

	public ActionFactory(){ }

	public void setEntiteHandler(EntiteHandler eh){
		entiteHandler = eh;
	}
	//endregion

	/** Distributeur d'action.
	 *
	 * @param actionName
	 * @return une instance de la classe action
	 */
	public IAction getAction(Configurator.ActionType actionName){
		switch (actionName) {
			case AJOUTLIEN:
				return new ActionAddLink(entiteHandler);
			case COPIERANDOMMEME:
				return new ActionCopyRandomMeme(entiteHandler);
			case RETRAITLIEN:
				return new ActionRemoveLink(entiteHandler);
			case EVAPORATION:
				return new ActionEvaporation(entiteHandler);
			case REFRESH:
				return new ActionRefreshLinks(entiteHandler);
			case PURIFY:
				return new ActionPurifyLinks(entiteHandler);
			default:
				return null;
		}
	}

	/** INTERFACE d'action
	 *
	 */
	public interface IAction
	{
		/** Application de l'action, renvoi l'action réalisé en string
		 *
		 * @param asker l'entité qui agit
		 * @param cibles la cible l'action de l'entite
		 * @return String = action réalisée, vide sinon
		 */
		String applyAction(Entite asker, Set<Entite> cibles);

		ActionType getActionType();

		String getFourCharName();

		/** Intance d'entite handler pour faire les actions.
		 *
		 */

	}

	/** Action de base commun a ttes les actions.
	 *
	 * @author Felix
	 *
	 */
	public abstract class ActionBase {
		public EntiteHandler applier;
		protected ActionBase(EntiteHandler eh){
			applier = eh;
		}
	}

	/** CLASSE d'action qui copie un meme // Objectif pour l'agent
	 * Le choix du meme est parfaitement aléatoire.
	 */
	private class ActionCopyRandomMeme extends ActionBase implements IAction{

		public ActionCopyRandomMeme(EntiteHandler eh){
			super(eh);
		}

		/** L'apply action qui permet de copier un meme.
		 *  Les conditions//aggrégation associées et propriétés définissent l'objectif.
		 *  La fonction va choisir un meme de la liste des entitées et le copier.
		 */
		public String applyAction(Entite asker, Set<Entite> cibles) {
			ArrayList<Meme> memesOfTarget ;
			ArrayList<Meme> memesOfTargetNotPosseded = new ArrayList<Meme>();

			Meme memeToCopy = null;
			Entite modele;
			String resultat = "ApplyAction de actionCopyRandomMeme";

			// Si des cibles existent pour essayer de leur copier un meme
			if(cibles.size() > 0){

				// On choisit une cible au hasard dans la liste
				modele = Toolz.getRandomElement(cibles);
//						cibles.get(Toolz.getRandomNumber(cibles.size()));
				memesOfTarget = modele.getMyMemes();
				resultat += " Modèle trouvé pour copier sur lui "+ modele.getIndex();

				// Si il posséde des memes
				if(memesOfTarget.size() > 0)
				{
					ArrayList<String> memeAsString = new ArrayList<String>();
					for (Meme meme : asker.getMyMemes())
						memeAsString.add(meme.toString());

					// Vérification que ce meme n'est pas déja acquis
					for (Meme meme : memesOfTarget)
						if(!memeAsString.contains(meme.toString()))
							memesOfTargetNotPosseded.add(meme);

					// des memes que l'asker ne possede pas déja
					if(memesOfTargetNotPosseded.size() > 0)
					{
						memeToCopy = memesOfTargetNotPosseded.get(Toolz.getRandomNumber(memesOfTargetNotPosseded.size()));
						asker.addMeme(memeToCopy);
						resultat += " COPIE du meme " + memeToCopy;
					}
					else
						resultat += " RIEN le modèle ne posséde aucun meme non déjà acquis";

				}
				else
					resultat += " RIEN Un modèle mais qui n'a pas de meme dans sa liste";

			}
			else
				resultat += "RIEN Aucun modèle sur qui copier un meme fourni en paramètre";

			System.out.println(resultat);
			if(memeToCopy != null)
				return "Meme copied :"+memeToCopy.toString();
			return "Aucune copie de meme";
		}

		/** To string de l'action.
		 *
		 */
		public String toString(){
			return "CopieRandomMeme";
		}

		/** Type de l'action en question.
		 *
		 */
		public ActionType getActionType() {
			return ActionType.COPIERANDOMMEME;
		}

		@Override
		public String getFourCharName() {
			return "CRDM";
		}
	}

	/** CLASSE d'action ajout d'un lien.
	 *
	 */
	private class ActionAddLink extends ActionBase implements IAction{
		public ActionAddLink(EntiteHandler eh){
			super(eh);
		}
		/** Methode applyAction, depuis un asker vers une liste de cible.
		 *
		 */
		public String applyAction(Entite asker, Set<Entite> cibles) {
			String actionDone = "";

			// Va choisir une cible parmi celles disponibles.
			Entite cible = Toolz.getRandomElement(cibles);

			// Application de l'action
			if (applier.addLink(asker, cible)) {
				actionDone += this.toString() + " " + asker.getIndex() + " => " + cible.getIndex();
			}


			if(actionDone.isEmpty()) actionDone = "Nope, lien déjà existant";
			return actionDone;
		}

		/** Type de l'action en question.
		 *
		 */
		public ActionType getActionType() {
			return ActionType.AJOUTLIEN;
		}

		/** Le to string de cette action.
		 *
		 */
		public String toString(){
			return "AjoutLien";
		}

		@Override
		public String getFourCharName() {
			return "ADLK";
		}
	}

	/** CLASSE d'action qui retire un lien entre entites.
	 *
	 *
	 */
	private class ActionRemoveLink extends ActionBase implements IAction{
		public ActionRemoveLink(EntiteHandler eh){
			super(eh);
		}
		/** Methode applyAction, depuis un asker vers une liste de cible.
		 *
		 */
		public String applyAction(Entite asker, Set<Entite> cibles) {
			String actionDone = "";

			// Va choisir une cible parmi celle disponible.
			Entite cible = Toolz.getRandomElement(cibles);

			// Application de l'action
			if (applier.removeLink(asker, cible)) {
				actionDone += this.toString() + " " + asker.getIndex() + " => " + cible.getIndex();
			}
			if(actionDone.isEmpty()) actionDone = "Nope, lien inexistant";

			return actionDone;
		}

		/** Type de l'action en question.
		 *
		 */
		public ActionType getActionType() {
			return ActionType.RETRAITLIEN;
		}

		/** Le to string de cette action.
		 *
		 */
		public String toString(){
			return "RetraitLien";
		}

		@Override
		public String getFourCharName() {
			return "RMLK";
		}
	}

	/** Action de type évaporation de lien, comparable a retrait de lien.
	 *
	 *
	 */
	private class ActionEvaporation extends ActionBase implements IAction {
		public ActionEvaporation(EntiteHandler eh){
			super(eh);
		}
		/** Methode d'action de l'application de l'évaporation.
		 * Fait la meme chose que la supression de lien...
		 *
		 */
		public String applyAction(Entite asker, Set<Entite> cibles) {
			String actionDone = "";

			for (Entite cible : cibles) {
				if (applier.removeLink(asker, cible)) {
					actionDone += this.toString() + " " + asker.getIndex() + " => " + cible.getIndex();
				}
			}

			return actionDone;
		}

		/** Type de l'action en question.
		 *
		 */
		public ActionType getActionType() {
			return ActionType.EVAPORATION;
		}

		/** Le to string de cette action.
		 *
		 */
		public String toString(){
			return "Evaporation";
		}

		@Override
		public String getFourCharName() {
			return "EVAP";
		}
	}

	/** Action permettant le rafraichissement des liens avec
	 * ceux déjà linké
	 *
	 */
	private class ActionRefreshLinks extends ActionBase implements IAction {
		public ActionRefreshLinks(EntiteHandler eh){
			super(eh);
		}
		/** Application de l'action refresh links.
		 *
		 */
		public String applyAction(Entite asker, Set<Entite> cibles) {

			return "NOPE not implement";
		}

		/** Type de l'action en question.
		 *
		 */
		public ActionType getActionType() {
			return ActionType.REFRESH;
		}

		/** Le to string de cette action.
		 *
		 */
		public String toString(){
			return "Refresh";
		}

		@Override
		public String getFourCharName() {
			return "RHLK";
		}
	}

	private class ActionPurifyLinks  extends ActionBase implements IAction {

		public ActionPurifyLinks(EntiteHandler eh){
			super(eh);
		}
		@Override
		public String applyAction(Entite asker, Set<Entite> cibles) {
			String actionDone = "";
			Entite target;
			ArrayList<Entite> connectedNodeSeveralConnection = new ArrayList<Entite>();

			for (Entite entite : cibles)
			{
				if(Toolz.rollDice(.01)){
					connectedNodeSeveralConnection.clear();
					if(entite.getDegree() > 1){
						for (Integer indexEventuality : entite.getConnectedNodesIndex()) {
							if(applier.getEntityCorresponding(indexEventuality).getDegree() > 1){
								connectedNodeSeveralConnection.add(applier.getEntityCorresponding(indexEventuality));
							}
						}

						if(connectedNodeSeveralConnection.size() > 1){
							target = connectedNodeSeveralConnection.get(Toolz.getRandomNumber(connectedNodeSeveralConnection.size()));
							applier.removeLink(entite, target);
							actionDone += this.toString() + " " + entite.getIndex() + " => " + target.getIndex();
						}
					}
				}
			}
			return actionDone;
		}

		@Override
		public ActionType getActionType() {
			return ActionType.PURIFY;
		}

		@Override
		public String getFourCharName() {
			return "PURI";
		}

		public String toString(){
			return "Purification";
		}

	}
}
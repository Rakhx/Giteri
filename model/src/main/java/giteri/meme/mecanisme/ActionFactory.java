package giteri.meme.mecanisme;
import java.util.Set;

import giteri.tool.math.Toolz;
import giteri.run.configurator.Configurator.TypeOfUOT;
import giteri.meme.entite.Entite;
import giteri.meme.entite.EntiteHandler;

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
	public IAction getAction(TypeOfUOT actionName){
		switch (actionName) {
			case AJOUTLIEN:
				return new ActionAddLink(entiteHandler);
			case RETRAITLIEN:
				return new ActionRemoveLink(entiteHandler);
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

		TypeOfUOT getActionType();

		String getFourCharName();

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
		public TypeOfUOT getActionType() {
			return TypeOfUOT.AJOUTLIEN;
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

			//if(Toolz.rollDice(.9)) {
				// Va choisir une cible parmi celle disponible.
				Entite cible = Toolz.getRandomElement(cibles);

				// Application de l'action
				if (applier.removeLink(asker, cible)) {
					actionDone += this.toString() + " " + asker.getIndex() + " => " + cible.getIndex();
				}

				if (actionDone.isEmpty()) actionDone = "Nope, lien inexistant";
			//}
			return actionDone;
		}

		/** Type de l'action en question.
		 *
		 */
		public TypeOfUOT getActionType() {
			return TypeOfUOT.RETRAITLIEN;
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


}
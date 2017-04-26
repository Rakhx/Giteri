package giteri.meme.mecanisme;
import java.util.ArrayList;

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

	// Region singleton Stuff
	private static ActionFactory instance = null;
	private ActionFactory(){
		
	}
	public static ActionFactory getInstance(){
		if(instance == null)
			instance = new ActionFactory();
		return instance;
	}
	// EndRegion

	/** Distributeur d'action.
	 * 
	 * @param actionName
	 * @return une instance de la classe action 
	 */
	public IAction getAction(Configurator.ActionType actionName){
		switch (actionName) {
		case AJOUTLIEN:
				return new ActionAddLink();
		case COPIERANDOMMEME:
				return new ActionCopyRandomMeme();
		case RETRAITLIEN:
				return new ActionRemoveLink();
		case EVAPORATION:
				return new ActionEvaporation(); 
		case REFRESH:
				return new ActionRefreshLinks();
		case PURIFY:
				return new ActionPurifyLinks();
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
		public abstract String applyAction(Entite asker,ArrayList<Entite> cibles);
		
		public ActionType getActionType();
		
		public String getFourCharName();
		
		/** Intance d'entite handler pour faire les actions.
		 * 
		 */
		public EntiteHandler applier = EntiteHandler.getInstance();
		
	}
	
	/** CLASSE d'action qui copie un giteri.meme // Objectif pour l'agent
	 * Le choix du giteri.meme est parfaitement aléatoire.
	 */
	private class ActionCopyRandomMeme implements IAction{
		
		/** L'apply action qui permet de copier un giteri.meme.
		 *  Les conditions//aggrégation associées et propriétés définissent l'objectif.
		 *  La fonction va choisir un giteri.meme de la liste des entitées et le copier.
		 */
		public String applyAction(Entite asker, ArrayList<Entite> cibles) {
			ArrayList<Meme> memesOfTarget ;
			ArrayList<Meme> memesOfTargetNotPosseded = new ArrayList<Meme>();
			
			Meme memeToCopy = null;
			Entite modele;
			String resultat = "ApplyAction de actionCopyRandomMeme";
			
			// Si des cibles existent pour essayer de leur copier un giteri.meme
			if(cibles.size() > 0){
				
				// On choisit une cible au hasard dans la liste
				modele = cibles.get(Toolz.getRandomNumber(cibles.size()));
				memesOfTarget = modele.getMyMemes();
				resultat += " Modèle trouvé pour copier sur lui "+ modele.getIndex();
				
				// Si il posséde des memes
				if(memesOfTarget.size() > 0)
				{	
					ArrayList<String> memeAsString = new ArrayList<String>();	
					for (Meme meme : asker.getMyMemes())
						memeAsString.add(meme.toString());
					
					// Vérification que ce giteri.meme n'est pas déja acquis
					for (Meme meme : memesOfTarget) 
						if(!memeAsString.contains(meme.toString()))
							memesOfTargetNotPosseded.add(meme);
					
					// des memes que l'asker ne possede pas déja
					if(memesOfTargetNotPosseded.size() > 0)
					{
						memeToCopy = memesOfTargetNotPosseded.get(Toolz.getRandomNumber(memesOfTargetNotPosseded.size()));					
						asker.addMeme(memeToCopy); 
						resultat += " COPIE du giteri.meme " + memeToCopy;
					}
					else 
						resultat += " RIEN le modèle ne posséde aucun giteri.meme non déjà acquis";
					
				}
				else
					resultat += " RIEN Un modèle mais qui n'a pas de giteri.meme dans sa liste";
				
			} 
			else
				resultat += "RIEN Aucun modèle sur qui copier un giteri.meme fourni en paramètre";
			
			System.out.println(resultat);
			if(memeToCopy != null)
				return "Meme copied :"+memeToCopy.toString();
			return "Aucune copie de giteri.meme";
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
	private class ActionAddLink implements IAction{
		
		/** Methode applyAction, depuis un asker vers une liste de cible. 
		 * 
		 */
		public String applyAction(Entite asker, ArrayList<Entite> cibles) {
			String actionDone = "";
			
			// Va choisir une cible parmi celles disponibles.
			cibles = Toolz.getOneElementList(cibles);
			
			// Application de l'action
			for (Entite cible : cibles) {
				if (applier.addLink(asker, cible)) {
					actionDone += this.toString() + " " + asker.getIndex() + " => " + cible.getIndex();
				}
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
	private class ActionRemoveLink implements IAction{

		/** Methode applyAction, depuis un asker vers une liste de cible. 
		 * 
		 */
		public String applyAction(Entite asker, ArrayList<Entite> cibles) {
			String actionDone = "";
							
			// Va choisir une cible parmi celle disponible.
			cibles = Toolz.getOneElementList(cibles);
			
			// Application de l'action
			for (Entite cible : cibles) {
				if (applier.removeLink(asker, cible)) {
					actionDone += this.toString() + " " + asker.getIndex() + " => " + cible.getIndex();
				}
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
	private class ActionEvaporation implements IAction {

		/** Methode d'action de l'application de l'évaporation.
		 * Fait la giteri.meme chose que la supression de lien...
		 * 
		 */
		public String applyAction(Entite asker, ArrayList<Entite> cibles) {
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
	private class ActionRefreshLinks implements IAction {

		/** Application de l'action refresh links.
		 * 
		 */
		public String applyAction(Entite asker, ArrayList<Entite> cibles) {
			String actionDone = asker.getIndex() + " a refresh les liens vers: ";
			ArrayList<Boolean> resultat = asker.refreshLinks(cibles); 
			int count = 0;
			for (Boolean bool : resultat) {
				if(bool)
				{
					actionDone += "la cible " + cibles.get(count) + ";";
					count++;
				}
			}
			
			//System.out.println("applied refresh " + count);
			return actionDone;
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

	private class ActionPurifyLinks implements IAction {
//
//		@Override
//		public String applyAction(Entite asker, ArrayList<Entite> cibles) {
//			String actionDone = "";
//			
//			if(Toolz.rollDice(.2)){
//				Entite target;
//				ArrayList<Entite> connectedNodeSeveralConnection = new ArrayList<Entite>();
//				
//				for (Entite entite : cibles) 
//				{
//					connectedNodeSeveralConnection.clear();
//					if(entite.getDegree() > 1){
//						for (Integer indexEventuality : entite.getConnectedNodesIndex()) {
//	//						if(cibles.get(indexEventuality).getDegree() > 1){
//							if(EntiteHandler.getInstance().getEntityCorresponding(indexEventuality).getDegree() > 1){
//	//							connectedNodeSeveralConnection.add(cibles.get(indexEventuality));
//								connectedNodeSeveralConnection.add(EntiteHandler.getInstance().getEntityCorresponding(indexEventuality));
//							}
//						}
//						
//						if(connectedNodeSeveralConnection.size() > 1){
//							target = connectedNodeSeveralConnection.get(Toolz.getRandomNumber(connectedNodeSeveralConnection.size()));
//							applier.removeLink(entite, target);
//							actionDone += this.toString() + " " + entite.getIndex() + " => " + target.getIndex();
//						}
//					}
//				}
//			}
//			return actionDone;
//		}

		@Override
		public String applyAction(Entite asker, ArrayList<Entite> cibles) {
			String actionDone = "";
			Entite target;
			ArrayList<Entite> connectedNodeSeveralConnection = new ArrayList<Entite>();
			
			for (Entite entite : cibles) 
			{
				if(Toolz.rollDice(.01)){
					connectedNodeSeveralConnection.clear();
					if(entite.getDegree() > 1){
						for (Integer indexEventuality : entite.getConnectedNodesIndex()) {
	//						if(cibles.get(indexEventuality).getDegree() > 1){
							if(EntiteHandler.getInstance().getEntityCorresponding(indexEventuality).getDegree() > 1){
	//							connectedNodeSeveralConnection.add(cibles.get(indexEventuality));
								connectedNodeSeveralConnection.add(EntiteHandler.getInstance().getEntityCorresponding(indexEventuality));
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

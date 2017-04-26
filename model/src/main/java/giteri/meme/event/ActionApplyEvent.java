package giteri.meme.event;

import giteri.meme.entite.Entite;
import giteri.meme.entite.Meme;

/** Evenement d'une entité, c'est a dire quand une action est
 * réalisés. 
 * 
 */
public class ActionApplyEvent extends GlobalEvent {

	private static final long serialVersionUID = 1L;
	public Meme memeApply;
	
	/** Constructeur pour un event qu'une entité peut faire, c'est a dire 
	 * l'application d'une action de giteri.meme.
	 * 
	 * @param source la source qui envoi l'event
	 * @param entity l'entité qui applique un giteri.meme
	 * @param meme le giteri.meme appliqué par l'entité
	 * @param message un message, optionnel
	 */
	public ActionApplyEvent(Object source, Entite entity, Meme meme, String message) {
		super(source, entity, message);
		this.memeApply = meme;
	}
	
}

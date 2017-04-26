package event;

import entite.Entite;
import entite.Meme;

/** Evenement de type meme, c'est a dire quand un meme est ajouté ou retiré
 * d'un agent. 
 *
 */
public class BehavTransmEvent extends GlobalEvent {

	private static final long serialVersionUID = 1L;
	public Meme meme;
	
	/**
	 * 
	 * @param source
	 * @param entity
	 * @param meme
	 * @param message
	 */
	public BehavTransmEvent(Object source, Entite entity, Meme meme, String message) {
		super(source, entity, message);
		this.meme = meme;
	}

}

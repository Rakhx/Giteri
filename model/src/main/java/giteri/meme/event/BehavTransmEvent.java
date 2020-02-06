package giteri.meme.event;

import giteri.meme.entite.Entite;
import giteri.meme.entite.Meme;
import giteri.run.interfaces.Interfaces;

/** Evenement de type giteri.meme, c'est a dire quand un giteri.meme est ajouté ou retiré
 * d'un agent. 
 *
 */
public class BehavTransmEvent extends MemeEvent {

	private static final long serialVersionUID = 1L;
	public Interfaces.IUnitOfTransfer meme;
	
	/**
	 * 
	 * @param source
	 * @param entity
	 * @param meme
	 * @param message
	 */
	public BehavTransmEvent(Object source, Entite entity, Interfaces.IUnitOfTransfer meme, String message) {
		super(source, entity, message);
		this.meme = meme;
	}

}

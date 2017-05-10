package giteri.meme.event;

import java.util.EventObject;

import giteri.meme.entite.Entite;

/** Commun element of every event send by object, giteri.meme or entity.
 *
 */
public abstract class MemeEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	public Entite entite;
	public String message;
	
	/** 
	 * 
	 * @param source
	 * @param entity
	 * @param message
	 */
	public MemeEvent(Object source, Entite entity, String message){
		super(source);
		this.message = message;
		entite = entity;
	}
}

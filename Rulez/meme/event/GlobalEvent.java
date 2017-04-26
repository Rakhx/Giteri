package event;

import java.util.EventObject;

import entite.Entite;

/** Commun element of every event send by object, meme or entity.
 *
 */
public abstract class  GlobalEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	public Entite entite;
	public String message;
	
	/** 
	 * 
	 * @param source
	 * @param entity
	 * @param message
	 */
	public GlobalEvent(Object source, Entite entity, String message){
		super(source);
		this.message = message;
		entite = entity;
	}
}

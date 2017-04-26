package event;

/** Listener interface, for listening to entity applying memes
 * 
 */
public interface ActionApplyListener {

	/** Method calls when an action is send, e.i. a meme applied
	 * 
	 * @param e Object send about this action
	 */
	void handlerActionApply(ActionApplyEvent e);
}

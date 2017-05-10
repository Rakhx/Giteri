package giteri.meme.event;

/** Listener interface, for listening to entity applying memes
 * 
 */
public interface IActionApplyListener {

	/** Method calls when an action is send, e.i. a giteri.meme applied
	 * 
	 * @param e Object send about this action
	 */
	void handlerActionApply(ActionApplyEvent e);
}

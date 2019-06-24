package giteri.meme.event;

/** Ecoute des memes en ce qui concerne leurs TRANSMISSIONS aux entités
 *
 */
public interface IBehaviorTransmissionListener {
	/** methode qui sera appelé lorsqu'un meme est transmis.
	 *
	 * @param e Meme transmis
	 */
	void handlerBehavTransm(BehavTransmEvent e);
}

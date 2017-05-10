package giteri.meme.event;

/** Interface pour gérer les évènements de type ajout ou retrait de nouveau meme disponibles
 * sur la map
 *
 */
public interface IMemeAvailableListener {
    void handlerMemeAvailable(MemeAvailableEvent e);
}

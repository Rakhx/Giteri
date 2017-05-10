package giteri.meme.event;

import giteri.meme.entite.Meme;

import java.util.List;

/** Evènement d'ajout ou de retrait d'un meme disponible sur la map
 *
 */
public class MemeAvailableEvent extends MemeListEvent {
    private static final long serialVersionUID = 1L;

    /** Constructeur pour un évenement de type list de meme available sur la map.
     *
     * @param source
     * @param memes
     * @param message
     */
    public MemeAvailableEvent(Object source, List<Meme> memes, String message){
        super(source, memes, message);
    }



}

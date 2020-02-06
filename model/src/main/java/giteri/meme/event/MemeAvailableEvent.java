package giteri.meme.event;

import giteri.meme.entite.Meme;
import giteri.run.interfaces.Interfaces;

import java.util.List;

/** Evènement d'ajout ou de retrait d'un meme disponible sur la map
 * Lors du fitting, lorsqu'on change la liste des memes sur la map
 */
public class MemeAvailableEvent extends MemeListEvent {
    private static final long serialVersionUID = 1L;

    /** Constructeur pour un évenement de type list de meme available sur la map.
     *
     * @param source
     * @param memes
     * @param message
     */
    public MemeAvailableEvent(Object source, List<Interfaces.IUnitOfTransfer> memes, String message){
        super(source, memes, message);
    }



}

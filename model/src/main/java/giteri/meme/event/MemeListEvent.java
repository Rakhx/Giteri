package giteri.meme.event;

import giteri.meme.entite.Meme;

import java.util.EventObject;
import java.util.List;

/** Classe d'évènement concernant des listes de meme.
 */
public class MemeListEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    public List<Meme> listOfMeme;
    public String message;

    /** Constructeur pour un event qui va concerner une liste de meme.
     *
     * @param source La source qui lance l'event
     * @param memes la liste de meme concerné
     * @param message message associé
     */
    public MemeListEvent(Object source, List<Meme> memes, String message){
        super(source);
        this.message = message;
        listOfMeme = memes;
    }

}

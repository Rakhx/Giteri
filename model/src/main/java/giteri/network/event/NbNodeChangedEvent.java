package giteri.network.event;

import java.util.EventObject;

/** Evenemment du type chanhement du nombre de node dans la simulation
 *
 */
public class NbNodeChangedEvent extends EventObject {
    public int nbNode ;
    public String message;

    /**
     *
     * @param source
     * @param nbNode
     * @param message
     */
    public NbNodeChangedEvent(Object source, int nbNode, String message) {
        super(source);
        this.message = message;
        this.nbNode = nbNode;
    }
}

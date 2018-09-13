package giteri.network.event;

/** Listener interface pour le chngement de nombre de noeud dans la simulation
 *
 */
public interface INbNodeChangedListener {
    void handlerNbNodeChanged(NbNodeChangedEvent e);
}

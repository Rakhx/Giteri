package giteri.network.networkStuff;

import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

import giteri.run.interfaces.Interfaces.DrawerNetworkInterface;
import giteri.network.network.Network;
import giteri.meme.event.BehavTransmEvent;

/** Implemente l'interface de Drawer mais ne fait rien
 * niveau affichage. Renvoi néanmoins les bonnes valeurs sur
 * les propriétés du réseau.
 * 
 *
 */
public class DrawerStub implements DrawerNetworkInterface {

	@Override
	public void handlerBehavTransm(BehavTransmEvent e) {
		
	}

	@Override
	public void drawThisNetwork(Network net, boolean outsideView) {
		
	}

	@Override
	public void addEdge(int from, int to) {
	}
	@Override
	public void addNode(int from) {
		
	}

	@Override
	public void removeEdge(int from, int to) {
		
	}

	@Override
	public void networkOverview() {
	}
	@Override
	public void resetDisplay() {
		
	}

	@Override
	public void screenshotDisplay(ArrayList<String> rep) {
		
	}

	@Override
	public void giveColorIndex(int nodeIndex, int color) {

	}

	@Override
	public void applyTargetColor(Network net, Integer actingEntite,
			Set<Integer> nodeToDesignAsTarget) {
	}

	@Override
	public void resetGoodColor(Network net) {
	}

	public Color getColorAsColor(int i){
		return new Color(0,0,0);
	}
}

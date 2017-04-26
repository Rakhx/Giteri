package networkStuff;

import java.util.ArrayList;

import interfaces.Interfaces.DrawerInterface;
import network.Network;
import event.BehavTransmEvent;

/** Implemente l'interface de Drawer mais ne fait rien
 * niveau affichage. Renvoi néanmoins les bonnes valeurs sur
 * les propriétés du réseau.
 * 
 *
 */
public class DrawerStub implements DrawerInterface{

	@Override
	public void handlerBehavTransm(BehavTransmEvent e) {
		
	}

	@Override
	public void drawThisNetwork(Network net) {
		
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
	public void applyTargetColor(Network net, Integer actingEntite,
			ArrayList<Integer> nodeToDesignAsTarget) {
	}

	@Override
	public void resetGoodColor(Network net) {
	}

}

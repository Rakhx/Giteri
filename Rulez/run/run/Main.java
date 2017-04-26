package run;

import interfaces.Interfaces.IView;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import networkStuff.DrawerGraphStream;
import networkStuff.NetworkConstructor;
import networkStuff.WorkerFactory;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import configurator.Configurator;
import controller.Controller;
import controller.Controller.ModelController;
import controller.Controller.VueController;
import entite.EntiteHandler;

public final class Main {

	public static void main(String[] args)  {		

//		WorkerFactory wf = WorkerFactory.getInstance();
		
		// Constructeur du réseau, des éléments qui le composent, fait le lien avec la partie 
		// graphique
		final NetworkConstructor nc = NetworkConstructor.getInstance();
		
		// Gestion des entités du réseau, ainsi que les nodes auxquels ils sont associées
		final EntiteHandler eh = EntiteHandler.getInstance();
		
		// Fenetre d'affichage 
		IHM fenetre;
		
		// Controller
		Controller c = new Controller();
		VueController vControl = c.new VueController();
		ModelController mControl = c.new ModelController(vControl);
		
		// La fenetre en elle meme
		// Controller de Model donné a l'IHM
		fenetre = new IHM(mControl);
		vControl.setView((IView)fenetre);
		
		
		// Le graph associé lors de l'affichage avec graphstream
		if(Configurator.withGraphicalDisplay){
			 Graph graph = new SingleGraph("Embedded");
			 DrawerGraphStream.getInstance().setGraph(graph);
			 @SuppressWarnings("unused")
			Viewer lol = graph.display();
		}
		
		fenetre.setVisible(true);
		eh.setIHMController(vControl);
		
		// fait le lien entre les entités d'action et de transmission de meme
		// avec l'IHM, pour permettre la mise a jour des affichages etc
		eh.addEntityListener(fenetre);
		eh.addMemeListener(fenetre);
		 
		// De meme, le dessinateur graphique s'abonne aux évènements de type transmission de meme
		// Dans le but de faire changer la couleur du noeud en fonction des memes possédés par ce dernier
		eh.addMemeListener(WorkerFactory.getInstance().getDrawer());
		eh.addEntityListener(WorkerFactory.getInstance().getCalculator());      
		
		nc.start();
		if(! Configurator.isSystemPaused()){
			nc.start();
			eh.start();
		}
		else {
			nc.start();
			nc.suspend();
			eh.start();
			eh.suspend();
		}
	}
} 

/** Fermeture de fenetre.
 *  
 */
class FrameListener extends WindowAdapter
{
   public void windowClosing(WindowEvent e)
  {
    System.exit(0);
  }
}

/** Vitesse des tics de la simulation.
 * 
 *
 */
class JSlideListener implements ChangeListener
{
	@Override
	public void stateChanged(ChangeEvent e){
		JSlider source = (JSlider)e.getSource();
	    if (!source.getValueIsAdjusting()) {
	    	source.setToolTipText("Value "+(int)(source.getValue()));
	        int fps = (int)source.getValue();
	        if(fps > 0)
	        {
	        	if(Configurator.turboMode = true)
	        		Configurator.turboMode = false;
	        }
	        else 
	        {
	        	Configurator.turboMode = true;
	        }
	        
	        Configurator.setThreadSpeed(fps);
	        Configurator.refreshInfoRate = Configurator.turboMode  ? 3000 : 100;
	    }		
	}
}

/*
AMELIORATIONS possibles

-------------------------------
Description : Utilisation de reflexion pour faire le lien entre l'enum d'action
et l'obtention de la classe associée, évite de passer par le switch case,
permet juste de définir une classe implémentant l'interface d'action, et ajouter
le nom de cette classe dans l'enum.
Avantage : évite d'avoir a penser a rajouter dans le switch case
Priorité : 1/4
-------------------------------


-------------------------------
Description : Cohérence sur les références disponibles sur le dessinateur de graphe. Si
volonté d'utiliser des interfaces, faire implémenter a drawerGraphStream l'interface
pour le dessin et une autre interface pour avoir les informations sur le graphs comme
la densité Etc. Permet de faciliter le passage a une autre interface. Sinon, garder
la référence directe en passant par un singleton etc.
Avantage : Faciliter le passage a une autre interface de graphe, cohérence de code
Priorité : 2/4
-------------------------------



*/
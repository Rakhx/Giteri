package giteri.run;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import giteri.meme.entite.EntiteHandler;
import giteri.meme.mecanisme.ActionFactory;
import giteri.meme.mecanisme.AgregatorFactory;
import giteri.meme.mecanisme.AttributFactory;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.networkStuff.CommunicationModel;
import giteri.network.networkStuff.DrawerGraphStream;
import giteri.network.networkStuff.NetworkConstructor;
import giteri.network.networkStuff.NetworkFileLoader;
import giteri.network.networkStuff.WorkerFactory;
import giteri.run.configurator.Configurator;
import giteri.run.controller.Controller;
import giteri.run.interfaces.Interfaces.IReadNetwork;
import giteri.run.interfaces.Interfaces.IView;
import giteri.tool.other.WriteNRead;

public final class Main {

	/** Lorsque l'appli est lancée depuis l'IDE.
	 *
	 * @param args
	 */
	public static void main(String[] args)  {
		Initializer.initialize(Configurator.EnumLauncher.ihm, null, null);
	}
} 

//region osef

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
	        Configurator.setThreadSpeed(fps);

	    }		
	}
}

//endregion
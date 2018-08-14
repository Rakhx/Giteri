package giteri.run;

import giteri.run.configurator.Configurator;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
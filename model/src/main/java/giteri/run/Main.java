package giteri.run;

import giteri.run.configurator.Configurator;
import giteri.run.configurator.Initializer;
import giteri.tool.other.StopWatchFactory;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class Main {

	/** Lorsque l'appli est lancée depuis l'IDE.
	 *
	 * @param args
	 */
	public static void main(String[] args)  {
		Initializer.initialize(Configurator.EnumLauncher.ihm, null, null ,null);
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



//endregion
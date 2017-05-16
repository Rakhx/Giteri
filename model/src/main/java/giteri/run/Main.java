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

	public static void main(String[] args)  {

		Initializer.initialize(Configurator.EnumLauncher.ihm, null, null);
//		WriteNRead writeNRead = new WriteNRead();
//		AttributFactory attributFactory = new AttributFactory();
//		AgregatorFactory agregatorFactory = new AgregatorFactory();
//		ActionFactory actionFactory = new ActionFactory() ;
//		MemeFactory memeFactory = new MemeFactory(actionFactory, agregatorFactory, attributFactory);
//		WorkerFactory workerFactory = new WorkerFactory();
//		NetworkConstructor networkConstructor = new NetworkConstructor();
//		EntiteHandler entiteHandler = new EntiteHandler(networkConstructor, memeFactory, workerFactory);
//		NetworkFileLoader networkFileLoader = new NetworkFileLoader( memeFactory, writeNRead);
//		DrawerGraphStream drawerGraphStream = new DrawerGraphStream(entiteHandler, memeFactory,
//				networkConstructor, writeNRead, networkFileLoader, workerFactory);
//
//		// Communication model
//
//		CommunicationModel communicationModel = new CommunicationModel(entiteHandler, networkConstructor, networkFileLoader, workerFactory,drawerGraphStream);
//		drawerGraphStream.setCommunicationModel(communicationModel);
//		networkFileLoader.setCommunicationModel(communicationModel);
//		actionFactory.setEntiteHandler(entiteHandler);
//		agregatorFactory.setEntiteHandler(entiteHandler);
//		workerFactory.setNecessary(drawerGraphStream, drawerGraphStream);
//		networkConstructor.setDrawer(drawerGraphStream);
//
//
//		// Controller
//		Controller c = new Controller();
//		Controller.VueController vControl = c.new VueController();
//		Controller.ModelController mControl = c.new ModelController(vControl, communicationModel);
//
//		entiteHandler.initialisation();
//
//		// La fenetre en elle meme Controller de Model donné a l'IHM
//		IHM fenetre = new IHM(mControl,
//				networkConstructor,
//				memeFactory,
//				workerFactory,
//				entiteHandler,
//				actionFactory ,
//				drawerGraphStream,
//				writeNRead);
//
//		vControl.setView((IView)fenetre);
//
//		// Le graph associé lors de l'affichage avec graphstream
//		if(Configurator.withGraphicalDisplay){
//			Graph graph = new SingleGraph("Embedded");
//			drawerGraphStream.setGraph(graph);
//			Viewer lol = graph.display();
//		}else{
////			drawerGraphStream.setGraph();
//		}
//
//
//		IReadNetwork nl = mControl.getReader();
//		try {
//			writeNRead.readAndCreateNetwork("" + Configurator.defaultPathForReadingNetwork, nl, " ", "#");
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//
//
//		fenetre.setVisible(true);
//		entiteHandler.setIHMController(vControl);
//
//		// fait le lien entre les entités d'action et de transmission de meme
//		// avec l'IHM, pour permettre la mise a jour des affichages etc
//		entiteHandler.addEntityListener(fenetre);
//		entiteHandler.addMemeListener(fenetre);
//
//		// De meme, le dessinateur graphique s'abonne aux évènements de type transmission de meme
//		// Dans le but de faire changer la couleur du noeud en fonction des memes possédés par ce dernier
//		entiteHandler.addMemeListener(workerFactory.getDrawer());
//		entiteHandler.addEntityListener(workerFactory.getCalculator());
//
//		networkConstructor.start();
//		if(! Configurator.isSystemPaused()){
//			networkConstructor.start();
//			entiteHandler.start();
//		}
//		else {
//			networkConstructor.start();
//			networkConstructor.suspend();
//			entiteHandler.start();
//			entiteHandler.suspend();
//		}
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
	        Configurator.setThreadSpeed(fps);

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
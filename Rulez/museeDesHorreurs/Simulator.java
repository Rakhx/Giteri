package run;

import interfaces.Interfaces.DrawerInterface;
import interfaces.Interfaces.StatAndPlotInterface;

import java.util.ArrayList;
import java.util.Optional;

import math.Toolz;
import networkStuff.NetworkConstructor;
import networkStuff.WorkerFactory;

import org.jfree.data.xy.XYSeries;

import configurator.Configurator;
import configurator.Configurator.NetworkAttribType;
import controller.Controller.VueController;
import entite.EntiteHandler;

/** Classe qui va gérer le lancement des simulations pour tester
 * les modèles a plusieurs reprise sur des variations de paramètre
 * 
 *
 */
public class Simulator {
	
	// Region PROPERTIES
	
	NetworkConstructor nc;
	EntiteHandler eh;
//	IHM fenetre;
	VueController viewControl;
	DrawerInterface drawer;
	StatAndPlotInterface computer;
	
	XYSeries seriesDensityOverProba;
	
	// EndRegion
	
	// Region SINGLETON 
	private static Simulator INSTANCE = null;
	
	/** Constructeur sans paramètre.
	 * 
	 */
	private Simulator() {
		nc = NetworkConstructor.getInstance();
		eh = EntiteHandler.getInstance();
//		fenetre = null;
		drawer = WorkerFactory.getInstance().getDrawer();
		computer = WorkerFactory.getInstance().getCalculator();
	}

	/** Fourni l'unique instance de la classe.
	 * 
	 */
	public static Simulator getInstance()
	{
		if( INSTANCE == null)
			INSTANCE = new Simulator();
		
		return INSTANCE;
	}
	
	// EndRegion

	// Region PUBLIC METHODS
	
	/** Fait varier la proba d'évaporation, et regarde le résultat
	 * 
	 */
	public void simulationOnProbaEvapVariating(){
		
		ArrayList<Double> borneValues = new ArrayList<Double>();
		double step = .05;		
		ArrayList<Double> avgValues = new ArrayList<Double>();
		double avgValue;
		double borneDelta = 0.01;
		ArrayList<Double> densitiesValue = new ArrayList<Double>();
		double densitiesStdDvt;
		
		// Turbo mode
		Configurator.baseSleepTimeMulti = 1;
		
		// boucle for qui fait varier les proba d'évaporation
		for (double i = .0; i < .3; i += step ) {
			avgValues.clear();
			avgValue = 0;
			
			// Boucle interne pour x lancé à la meme valeur d'évap	
			for (int j = 0; j < Configurator.nbSimulationByStep; j++) {
				
				System.out.println("On suspend les threads & On reset tous les éléments du graph");
					
				// Mettre en pause les threads		
				eh.suspend();
				nc.suspend();	
				// Set la nouvelle valeur de proba d'évap
				Configurator.setProbaRetraitLien(i);
				EntiteHandler.getInstance().resetProba();
							
				try {			Thread.sleep(1000);			} catch (InterruptedException e) {e.printStackTrace();}
							
				// Reset tout les éléments	
				eh.resetStat(true);
				nc.resetStat();
				drawer.resetDisplay();
				viewControl.resetIHM();
				
				// Relancer les threads	
				synchronized (nc) {				nc.resume();			}
				synchronized (eh) {				eh.resume();			}				
					
				borneDelta = .01;
				
				do 
				{
					densitiesValue.clear();
					borneDelta *= Configurator.borneAugmentRate;
					for (int tour = 0; tour < 3; tour++) {
						nc.updatePreciseNetworkProperties(Configurator.getIndicateur(NetworkAttribType.DENSITY));
						
						densitiesValue.add(nc.getNetworkProperties().getDensity());
						try {				Thread.sleep(1000);			} catch (InterruptedException e) {				e.printStackTrace();			}
					}
					
					Optional<Double> avg = Optional.ofNullable(null);
					densitiesStdDvt = Toolz.getDeviation(densitiesValue, avg);
					System.out.println("Borne: "+borneDelta + " deviation: "+ densitiesStdDvt);
//					System.out.println(borneDelta > densitiesStdDvt? "LOLAMSILOL":"ouais bah non");
				} while (borneDelta < densitiesStdDvt ); 
				
				borneValues.add(borneDelta);
				
				System.out.println("Et ca repart avec une valeur d'évap de: " +i);			
				try {				Thread.sleep(5000);			} catch (InterruptedException e) {				e.printStackTrace();			}
				
				nc.updatePreciseNetworkProperties(Configurator.getIndicateur(NetworkAttribType.DENSITY));
				avgValues.add(nc.getNetworkProperties().getDensity());
			} 
		
			for (Double double1 : avgValues) 
				avgValue += double1;
			if(avgValues.size() > 0)
				avgValue /= avgValues.size();
			
			// Avant de passer au thread suivant, on plot la valeur de la densité
			viewControl.addDensityOverProbaValue(Configurator.getProbaRetraitLien(), avgValue);
			
		}		
		
		
		// OK Initialisation des entités blabalblaa zéro
		// OK [Fonction Reset dans entité handler ( meme possédés etc, dans NC (structure du réseau
		// OK dans l'affichage graphique Drawer, Eventuellement stopper les threads?]
		
		// lancement de la simulation
		// avec des vérifications sur la variation des memes possédés ainsi que sur 
		// la variation de la densité.[ dans NC? Avec les handler d'event qui remettent un compteur a zero quand
		// un meme est a nouveau ajouter] [ Dans le oneStep du NC? ]
		// Faire une méthode qui consiste a borner la densité et a vérifier
		// la variation de part et d'autre de la valeur moyenne mesuré sur les 10 dernieres densité, 
		// augmenter les borne inf et sup si elles sont dépassées
		
		// une fois la stabilité atteinte, sauvegarder la valeur de la densité et l'écart type ou valeur
		// équivalente, l'ajouter au graphe que l'on va afficher, ( éventuellement grossir le trait proportionnellement 
		// a l'écart type dans un 1er temps. )
		
		// Boucle suivante.
		
	}

	/** Lui associe le controller de view avant de pouvoir reset certains éléments
	 * etc.
	 * @param viewC
	 */
	public void setViewController(VueController viewC){
		this.viewControl = viewC;
	}
	
	// EndRegion
}

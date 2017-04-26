package giteri.run.jarVersion;

import java.util.ArrayList;
import java.util.Hashtable;

import giteri.run.configurator.Configurator;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.networkStuff.NetworkAnalyzer;
import giteri.fitting.parameters.FittingClass;
import giteri.fitting.parameters.IModelParameter;
import giteri.fitting.parameters.IModelParameter.GenericBooleanParameter;
import giteri.fitting.parameters.IModelParameter.GenericDoubleParameter;
import giteri.fitting.parameters.IModelParameter.MemeAvailability;
import giteri.fitting.parameters.IModelParameter.MemeDiffusionProba;
import giteri.fitting.algo.IExplorationMethod.ExplorationOneShot;
import giteri.meme.entite.Meme;

public class StatAndPlotJarVersion extends NetworkAnalyzer {
	
	public ArrayList<Double> probaVoulu = new ArrayList<Double>();
	boolean debug = Configurator.overallDebug;
	private static StatAndPlotJarVersion INSTANCE = null;
	
	/** Constructeur sans paramètre.
	 * 
	 */
	protected StatAndPlotJarVersion() {
		super();
	}

	/** Fourni l'unique instance de la classe.
	 * 
	 */
	public static StatAndPlotJarVersion getInstance()
	{
		if( INSTANCE == null)
			INSTANCE = new StatAndPlotJarVersion();
		
		return INSTANCE;
	}

	protected void initializeConfigForStability(FittingClass fitting){
		int i = 0;
		double value;
		Hashtable<Meme, GenericBooleanParameter> memeDispo = new Hashtable<Meme,GenericBooleanParameter>();
		Hashtable<Integer, IModelParameter<?>>  providers = new Hashtable<Integer, IModelParameter<?>>();
		
		
		Hashtable<Meme, GenericDoubleParameter> kvMemeValue = new Hashtable<Meme, IModelParameter.GenericDoubleParameter>();
		
		for (Meme meme : MemeFactory.getInstance().getMemeAvailable(true)) 
			memeDispo.put(meme, new GenericBooleanParameter());

		// classe truqué pour tjrs renvoyer tous les behaviors. Trop chiant a refaire en l'enlevant et pas forcement
		// pertinent.
		MemeAvailability memeProvider = new MemeAvailability(memeDispo);
		providers.put(1,memeProvider);
		MemeDiffusionProba memeDiffu = new MemeDiffusionProba(memeProvider.availableMeme);//, new GenericDoubleParameter(.1,.1,.2,.05));
		providers.put(0,memeDiffu);
		
		for (Meme meme : MemeFactory.getInstance().getMemeAvailable(true)){
			value = probaVoulu.get(i);
			i++;
			kvMemeValue.put(meme, new GenericDoubleParameter(value,value, value,1.));
			memeDiffu.specifyMemeBound(meme, new GenericDoubleParameter(value,value, value,.1));
			
		}
		
		memeDiffu.setValue(kvMemeValue);
		
		fitting.explorator = new ExplorationOneShot(providers);
	}
	
	
	
}

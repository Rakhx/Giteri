package jarVersion;

import java.util.ArrayList;
import java.util.Hashtable;

import mecanisme.MemeFactory;
import networkStuff.NetworkAnalyzer;
import parameters.FittingClass;
import parameters.IModelParameter;
import parameters.IModelParameter.GenericBooleanParameter;
import parameters.IModelParameter.GenericDoubleParameter;
import parameters.IModelParameter.MemeAvailability;
import parameters.IModelParameter.MemeDiffusionProba;
import algo.IExplorationMethod.ExplorationOneShot;
import configurator.Configurator;
import entite.Meme;

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

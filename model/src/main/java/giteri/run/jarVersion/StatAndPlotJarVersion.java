package giteri.run.jarVersion;

import java.util.ArrayList;
import java.util.Hashtable;

import giteri.meme.entite.EntiteHandler;
import giteri.network.networkStuff.NetworkConstructor;
import giteri.network.networkStuff.NetworkFileLoader;
import giteri.network.networkStuff.WorkerFactory;
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
import giteri.tool.other.WriteNRead;

public class StatAndPlotJarVersion extends NetworkAnalyzer {

	public ArrayList<Double> probaVoulu = new ArrayList<Double>();
	boolean debug = Configurator.overallDebug;

	/** Constructeur sans paramètre.
	 *
	 */
	public StatAndPlotJarVersion(EntiteHandler entiteHandler, MemeFactory memeFactory, NetworkConstructor networkConstructor,
								 WriteNRead wnr, NetworkFileLoader nfl, WorkerFactory wf) {
		super(entiteHandler, memeFactory, networkConstructor, wnr, nfl, wf);
	}

	protected void initializeConfigForStability(FittingClass fitting){
		int i = 0;
		double value;
		Hashtable<Meme, GenericBooleanParameter> memeDispo = new Hashtable<Meme,GenericBooleanParameter>();
		Hashtable<Integer, IModelParameter<?>>  providers = new Hashtable<Integer, IModelParameter<?>>();


		Hashtable<Meme, GenericDoubleParameter> kvMemeValue = new Hashtable<Meme, IModelParameter.GenericDoubleParameter>();

		for (Meme meme : memeFactory.getMemeAvailable(true))
			memeDispo.put(meme, new GenericBooleanParameter());

		MemeAvailability memeProvider = new MemeAvailability(memeDispo);
		providers.put(1,memeProvider);
		memeProvider.setEntiteHandler(entiteHandler);

		for (Meme meme : memeFactory.getMemeAvailable(true)){
			value = probaVoulu.get(i++);
			kvMemeValue.put(meme, new GenericDoubleParameter(value,value, value,1.));
		}

		MemeDiffusionProba memeDiffu = new MemeDiffusionProba(kvMemeValue);//, new GenericDoubleParameter(.1,.1,.2,.05));
		providers.put(0,memeDiffu);
		memeDiffu.setEntiteHandler(entiteHandler);

		fitting.explorator = new ExplorationOneShot(providers);
	}

}

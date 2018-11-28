package giteri.run.jarVersion;

import giteri.meme.entite.EntiteHandler;
import giteri.meme.mecanisme.MemeFactory;
import giteri.network.networkStuff.NetworkAnalyzer;
import giteri.network.networkStuff.NetworkConstructor;
import giteri.network.networkStuff.NetworkFileLoader;
import giteri.network.networkStuff.WorkerFactory;
import giteri.tool.other.WriteNRead;

public class StatAndPlotJarVersion extends NetworkAnalyzer {

	/**
	 *
	 */
	public StatAndPlotJarVersion(EntiteHandler entiteHandler, MemeFactory memeFactory, NetworkConstructor networkConstructor,
								 WriteNRead wnr, NetworkFileLoader nfl, WorkerFactory wf) {
		super(entiteHandler, memeFactory, networkConstructor, wnr, nfl, wf);
	}

//	protected void initializeConfigForStability(FittingClass fitting){
////		int i = 0;
////		double value;
////		Hashtable<Meme, GenericBooleanParameter> memeDispo = new Hashtable<>();
////		Hashtable<Integer, IModelParameter<?>>  providers = new Hashtable<>();
////		Hashtable<Meme, GenericDoubleParameter> kvMemeValue = new Hashtable<>();
////
////		for (Meme meme :memeFactory.getMemes(Configurator.typeOfMemeUseForFitting,Configurator.ActionType.ANYTHING))
////			memeDispo.put(meme, new GenericBooleanParameter());
////
////		MemeAvailability memeProvider = new MemeAvailability(memeDispo);
////		providers.put(1,memeProvider);
////		memeProvider.setEntiteHandler(entiteHandler);
////
////		for (Meme meme : memeFactory.getMemes(Configurator.typeOfMemeUseForFitting,Configurator.ActionType.ANYTHING)){
////			value = probaVoulu.get(i++);
////			kvMemeValue.put(meme, new GenericDoubleParameter(value,value, value,1.));
////		}
////
////		MemeDiffusionProba memeDiffu = new MemeDiffusionProba(kvMemeValue);
////		providers.put(0,memeDiffu);
////		memeDiffu.setEntiteHandler(entiteHandler);
//
////		fitting.explorator = IExplorationMethod.ExplorationMethod.getSpecificExplorator(Configurator.EnumExplorationMethod.oneShot, providers);
////		if(Configurator.debugOpenMole)
////			System.out.println("Config lancee avec " + probaVoulu);
//	}

}

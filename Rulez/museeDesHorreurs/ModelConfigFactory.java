package parameters;

import java.util.ArrayList;
import java.util.Hashtable;

import configurator.Configurator;
import configurator.Configurator.EnumExplorationMethod;
import entite.Meme;

/** défini de manière générique les paramètres du modèle.
 *
 */
public class ModelConfigFactory {
	
	// Les behaviors disponibles sur la map. 
	private Hashtable<Integer, ArrayList<Meme>> behavior;

	// Region Singleton

	private static ModelConfigFactory instance = null;
	
	private ModelConfigFactory(){
		behavior = new Hashtable<Integer, ArrayList<Meme>>();
		context = new ArrayList<ParameterContext>();
		providers = new ArrayList<IConfigProvider>();
	}

	/** Obtenir le singleton de la factory. 
	 * 
	 * @return
	 */
	public static ModelConfigFactory getInstance(){
		if(instance == null){
			instance = new ModelConfigFactory();
			
		}
		return instance;
	}
	 
	// EndRegion

	/** retourne un explorateur de paramètre en fonction de ce qui est demandé
	 * ou de celui défini dans la classe de configuration. 
	 * 
	 * @return Un explorateur initialisé.
	 */
	public IExplorationMethod getExplorator(EnumExplorationMethod explorator){
		if(explorator == null)
			explorator = Configurator.explorator;
		switch (explorator) {
		case exhaustive:
			explo = new ExplorationExhaustive(getProviders());
			return explo;
		default:
			return null;
		}
	}

	/** Obtient une hash de behavior // distribution sur la map.
	 * key: La combinaison en string des sets de behavior
	 * value: La distribution de ces comportements sur les agents. 
	 * @return key value
	 */
	public Hashtable<String, Double> getBehaviorDistributionAsString(){
		Hashtable<String, Double> behaviorKVproportion = new Hashtable<String, Double>();
		IConfigProvider<ParameterDistribution> distribProvider = null;
		String memeCombinaison;
		for (IConfigProvider iProvider : providers) {
			if(iProvider.getParameter().getName() == Configurator.FittingParamType.DISTRIB.name())
				distribProvider = iProvider;
		}
		
		if(distribProvider != null){
			ParameterDistribution distrib = distribProvider.getParameter().getValue();
				for (Integer index : distrib.indexKVProportion.keySet()) {
					memeCombinaison = "";
					for (Meme meme : distrib.indexKVCombinaison.get(index)) {
						memeCombinaison += meme.toFourCharString();
					}
					
					behaviorKVproportion.put(memeCombinaison, distrib.indexKVProportion.get(index));
			}
		}
		
		return behaviorKVproportion;
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<String> getConfigParamAsList(){
		ArrayList<String> names = new ArrayList<String>();
		for (IConfigProvider iProvider : providers) {
			names.add(iProvider.getParameter().getName());
		}
		return names;
	}
	
	/** essaye d'ajouter le set de comportement dans les behaviors disponibles
	 * sur la map, dans le cas ou ce premier n'est pas déja inclus dans ce dernier. 
	 * 
	 * @param setComportement
	 */
	public void addBehaviorAvailable(ArrayList<Meme> setComportement){
		boolean alreadyHave = false;
		ArrayList<String> translation = new ArrayList<String>();
		int count = 0; 
		for (Meme meme : setComportement) {
			translation.add(meme.toFourCharString());
		}
		
		// on parcourt tous les set de behavior que l'on possède sur la map
		for (ArrayList<Meme> memes : behavior.values()) {
			// on regarde tout les memes contenus dans UN set
			for (Meme meme : memes) {
				// Dans le cas ou un meme n'est pas contenu, c'est que ce n'est pas le meme set.
				if(translation.contains(meme.toFourCharString()))
					count++;
			}
			
			// Dans le cas ou le count est égal au nombre de meme, c'est a dire que l'un des sets de values
			// contenu dans behavior s'est entièrement retrouvé dans le setComportement donné en paramètre
			if(count == setComportement.size()){
				alreadyHave = true;
				break;
			}else
				count = 0;
		}
		
		if(!alreadyHave){
			behavior.put(behavior.size(), setComportement);
		}
		else
			System.err.println("Le set de comportement est déjà contenu dans le behavior");
	}
	
	
}

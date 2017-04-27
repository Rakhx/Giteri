package giteri.fitting.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import giteri.tool.math.Toolz;
import giteri.fitting.parameters.IModelParameter;
import giteri.run.configurator.Configurator.EnumExplorationMethod;

public interface IExplorationMethod {
	boolean gotoNext();
	void apply();
	public String toString();
	public ArrayList<IModelParameter<?>> getModelParameterSet();

	/** CLASSE ABSTRAITE commune a tte les méthodes d'explorations.
	 * 
	 */
	public abstract class ExplorationMethod implements IExplorationMethod{
	
		public Hashtable<Integer, IModelParameter<?>> provider;
		
		protected ExplorationMethod(Hashtable<Integer, IModelParameter<?>> provids){
			provider = provids;
		}
		
		/** Renvoi l'ensemble des string de la configuration du model. 
		 * 
		 */
		public String toString(){
			String res = "";
			for (IModelParameter<?> iConfigProvider : provider.values()) {
				res += "\n"+iConfigProvider.valueString();
			}
		
		return res;
		}
		
		/**
		 * 
		 */
		public ArrayList<IModelParameter<?>> getModelParameterSet(){
			return new ArrayList<IModelParameter<?>>(provider.values()) ;
		}
		
		/** APplication au modèle dans l'ordre indiqué par la hashtable.
		 * 
		 */
		public void apply() {
			for (int i = provider.keySet().size() - 1; i >= 0 ; i--) {
				provider.get(i).apply();
			}
		}
		
		public static  IExplorationMethod getSpecificExplorator(EnumExplorationMethod explo, Hashtable<Integer, IModelParameter<?>> provids){
			switch (explo) {
			case random:
				return new ExplorationRandom(provids);
			case exhaustive:
				return new ExplorationExhaustive(provids);
			default:
				return new ExplorationOneShot(provids);
			}
		}
	}
	
	/** CLASSE instanciation de la méthode d'exploration, le fait de facon exhaustive.
	 * Nouvelle version utilisant les nouveaux type de paramètre.
	 *
	 */
	public class ExplorationExhaustive extends ExplorationMethod {

		/** Constructeurs qui prend un map de paramètre, avec en clef l'ordre d'application.
		 * Premier a etre appliqué = 0
		 * 
		 * @param parameters
		 */
		public ExplorationExhaustive(Hashtable<Integer, IModelParameter<?>> parameters){
			super(parameters);
		}
		
		/** Essaye de passer a la valeur de paramètre suivant sur le premier paramètre de la liste,
		 * si pas possible revient a zéro et essaye le parametre suivant de la giteri.meme facon.
		 * Assure de passer sur toutes les combinaisons. 
		 * 
		 */
		public boolean gotoNext() {
			boolean hasGo = false;
			for (int i = 0; i < provider.keySet().size(); i++) {
				if(provider.get(i).gotoNext()){
					hasGo = true;
					break;
				}else {
					provider.get(i).gotoMinValue();
				}
			}
			
			return hasGo;
		}

		
	}

	/** Classe intanciation de la méthode d'exploration, le fait de facon random
	 * 
	 *
	 */
	public class ExplorationRandom extends ExplorationMethod {
		
		// Numéro de profil, map<IModelParameter,String de configuration>
		List<HashMap<IModelParameter<?>, String>> matrice;
		
		/** Constructeur qui prend une map de parametre, avec comme clef l'ordre d'application. 
		 * Premier a etre appliqué @key = 0
		 * 
		 * @param provids
		 */
		public ExplorationRandom(Hashtable<Integer, IModelParameter<?>> provids) {
			super(provids);

			IModelParameter<?> champion = null;
			
			// valeur possible par provider
		    List<List<String>> possibleValues = new ArrayList<List<String>>();
		    for (IModelParameter<?> providers : provids.values()) {
		    	// Cheat code
		    	if(providers.nameString().compareToIgnoreCase("Proba Diffusion") == 0){
		    		champion = providers;
		    		possibleValues.add(providers.getPossibleValue());
		    	}
			}
		    
		    matrice = new ArrayList<HashMap<IModelParameter<?>,String>>();
		    for (List<String> list : possibleValues) {
				for (String string : list) {
					HashMap<IModelParameter<?>, String> element = new HashMap<IModelParameter<?>, String>();
					element.put(champion, string);
			    	matrice.add( element);
				}
			}

		}

		/** En choisis un aléatoirement dans la structure et le retire(?) de la liste.
		 * 
		 */
		public boolean gotoNext() {
			int selectedScenar;
			boolean hasGo = true;
			HashMap<IModelParameter<?>, String> tupleModelXConfig;
			if(!matrice.isEmpty()){
				selectedScenar = Toolz.getRandomNumber(matrice.size());
				tupleModelXConfig = matrice.get(selectedScenar);
				for (IModelParameter<?> model : tupleModelXConfig.keySet()) {
					model.setPossibleValue(tupleModelXConfig.get(model));
				}
			
				matrice.remove(selectedScenar);
			}
			else{
				hasGo = false;
			}
			
			return hasGo;
		}
	}
	
	/** Exploration pour un seul type de réseau.
	 * 
	 * @author Felix
	 *
	 */
	public class ExplorationOneShot extends ExplorationMethod {

		public ExplorationOneShot(Hashtable<Integer, IModelParameter<?>> provids) {
			super(provids);
			
		}
		
		
		@Override
		public boolean gotoNext() {
			return false;
		}
		
	}
	
}

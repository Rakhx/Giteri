package giteri.fitting.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import giteri.tool.math.Toolz;
import giteri.fitting.parameters.IModelParameter;
import giteri.run.configurator.Configurator.EnumExplorationMethod;

/** Interface des classes d'exploration de l'espace de paramètre pour
 * la fonction de fitting.
 *
 */
public interface IExplorationMethod {
	/** Passer le IModelParameter au step suivant
	 *
	 * @return true si il existe un step suivant
	 */
	boolean gotoNext();
	/** Appliquer au modèle la valeur courante du IModel parameter
	 *
	 */
	void apply();
	/** Affichage lisible de la valeur courante des ensembles des IModelParameters
	 * contenu dans l'explorateur.
	 *
	 * @return un string détaillant les informations de la configuration courante.
	 */
	String toString();

	/** Renvoi une liste des IModelParameter contenu dans le provider.
	 *
	 * @return la liste des parametre de l'explorateur
	 */
	List<IModelParameter<?>> getModelParameterSet();

	/** CLASSE ABSTRAITE commune a tte les méthodes d'explorations.
	 * 
	 */
	 abstract class ExplorationMethod implements IExplorationMethod{

		/** La liste des paramètres sur laquelle tourne les explorations. L'integer indique
		 * l'ordre d'application des param. Le premier IModelParameter à cycler est celui en position
		 * 0.
		 */
		Hashtable<Integer, IModelParameter<?>> provider;

		ExplorationMethod(Hashtable<Integer, IModelParameter<?>> provids){
			provider = provids;
		}
		
		/** Renvoi l'ensemble des string de la configuration actuelle du model.
		 *
		 */
		public String toString(){
			StringBuilder res = new StringBuilder();
			for (IModelParameter<?> iConfigProvider : provider.values()) {
				res.append("\n").append(iConfigProvider.valueString());
			}
		
		return res.toString();
		}
		
		/**
		 * 
		 */
		public ArrayList<IModelParameter<?>> getModelParameterSet(){
			return new ArrayList<>(provider.values()) ;
		}
		
		/** APplication au modèle dans l'ordre indiqué par la hashtable.
		 * 
		 */
		public void apply() {
			for (int i = provider.keySet().size() - 1; i >= 0 ; i--) {
				provider.get(i).apply();
			}
		}

		/** Obtenir un explorateur spécifique.
		 *
		 * @param explo L'explo voulu
		 * @param provids les providers demandé
		 * @return L'explorateur voulu
		 */
		public static  IExplorationMethod getSpecificExplorator(EnumExplorationMethod explo, Hashtable<Integer, IModelParameter<?>> provids){
			switch (explo) {
			case random:
				return new ExplorationRandom(provids);
			case exhaustive:
				return new ExplorationExhaustive(provids);
			case oneShot:
				return new ExplorationOneShot(provids);
			default:
				return null;
			}
		}
	}
	
	/** CLASSE instanciation de la méthode d'exploration, le fait de facon exhaustive.
	 * Nouvelle version utilisant les nouveaux type de paramètre.
	 *
	 */
	 class ExplorationExhaustive extends ExplorationMethod {

		/** Constructeurs qui prend un map de paramètre, avec en clef l'ordre d'application.
		 * Cycle en premier sur la key 0, applique en premier la key max.
		 * 
		 * @param parameters liste des params de config.
		 */
		ExplorationExhaustive(Hashtable<Integer, IModelParameter<?>> parameters){
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

	/** Classe instanciation de la méthode d'exploration, le fait de facon random
	 * 
	 *
	 */
	 class ExplorationRandom extends ExplorationMethod {
		
		// Numéro de profil, map<IModelParameter,String de configuration>
		List<HashMap<IModelParameter<?>, String>> matrice;
		
		/** Constructeur qui prend une map de parametre, avec comme clef l'ordre d'application. 
		 * Premier a etre appliqué @key = 0
		 * 
		 * @param provids La liste des providers sur lesquels varier
		 */
		ExplorationRandom(Hashtable<Integer, IModelParameter<?>> provids) {
			super(provids);

			IModelParameter<?> champion = null;
			
			// valeur possible par provider
		    List<List<String>> possibleValues = new ArrayList<>();
		    for (IModelParameter<?> providers : provids.values()) {
		    	// Cheat code
		    	if(providers.nameString().compareToIgnoreCase("Proba Diffusion") == 0){
		    		champion = providers;
		    		possibleValues.add(providers.getPossibleValue());
		    	}
			}
		    
		    matrice = new ArrayList<>();
		    for (List<String> list : possibleValues) {
				for (String string : list) {
					HashMap<IModelParameter<?>, String> element = new HashMap<>();
					element.put(champion, string);
			    	matrice.add(element);
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
	
	/** Exploration pour un seul type de configuration.
	 * 
	 */
	 class ExplorationOneShot extends ExplorationMethod {

		public ExplorationOneShot(Hashtable<Integer, IModelParameter<?>> provids) {
			super(provids);
			
		}
		
		
		@Override
		public boolean gotoNext() {
			return false;
		}
		
	}
	
}

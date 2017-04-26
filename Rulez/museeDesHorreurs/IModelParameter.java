package parameters;

import java.util.ArrayList;
import java.util.Hashtable;

import configurator.Configurator.FittingParamType;
import entite.Meme;

/** INTERFACE. les paramètres du modèle.
 *
 */
public interface IModelParameter<T> {

	String getName();
	String getValueAsString();
	IModelParameter<T> copy(IModelParameter<T> toCopy);
	T getValue();

	/** CLASSE. ensemble des valeurs de param. 
	*
	*/
	public class ParameterSetOfValue implements Cloneable{
	
		public ArrayList<IModelParameter<?>> valuz = new ArrayList<IModelParameter<?>>();
		
		/** Clone du set de paramètre. 
		 * 
		 */
		public ParameterSetOfValue clone(){
			ParameterSetOfValue newMe;
			newMe = new ParameterSetOfValue();
			for (IModelParameter genericValue : valuz) {
				newMe.valuz.add( genericValue.copy(genericValue) );
			}
			
			return newMe;
		}
		
		/** ToString() sur l'ensemble des param de la liste.
		 * 
		 */
		public String toString(){
			String res = "";
			for (IModelParameter paramValue : valuz) {
//				res += " - " + paramValue.getName() +":"+ paramValue.getValueAsString();
				res += valuz.toString();
			}
			
			return res;
		}

		public ArrayList<String> toStringAsArray(){
			ArrayList<String> params = new ArrayList<String>();
			for (IModelParameter paramValue : valuz) {
				params.add(paramValue.getValueAsString());
			}
			
			return params;
		}
		
		/** ToString prennant moins de place.
		 * 
		 * @return
		 */
		public String toShortString(){
			String res = "";
			
			for (IModelParameter paramValue : valuz) {
				res += paramValue.getName().substring(0, 2) +":"+ paramValue.getValueAsString() +"-";
			}
			
			return res;
		}
	}
	
	/** CLASSE. d'une valeur générique d'un parametre 
	 * 
	 */
	public abstract class ValueParameterGeneric implements IModelParameter<Double> {

		public ParameterContext myContext; // pareil
		public double value;

//		public abstract void applyValue();
//		protected abstract ValueParameterGeneric copy(ValueParameterGeneric gnagna);
		
		/** Contructeur prennant un contexte en paramètre.
		 * 
		 * @param context
		 */
		protected ValueParameterGeneric(ParameterContext context) {
			myContext = context;
		}

		/** Met une valeur et apply cette value automatiquement.
		 * 
		 * @param valeur
		 */
		public void setValue(double valeur) {
			this.value = valeur;
//			this.applyValue();
		}
		
		public ParameterContext getMyContexte(){
			return myContext;
		}
		
		public void setMyContexte(ParameterContext contexte){
			myContext = contexte;
		}
	
		/** Classe pour obtenir la valeur des params en string.
		 * 
		 */
		public String getValueAsString() {
			return "" + value;
		}

		/** Nom du paramètre concerné par la classe.
		 * 
		 */
		public String getName() {
			return myContext.name.toString();
		}

		/**
		 * 
		 */
		public String toString(){
			return getName() + " : " + getValueAsString();
		}
	}

	/** CLASSE. Instanciation de la classe de la valeur du param de learning.
	 * 
	 */
	public class ParameterLearningValue extends ValueParameterGeneric {
		
		public ParameterLearningValue(ParameterContext context) {
			super(context);
		}

		@Override
		public IModelParameter<Double> copy(IModelParameter<Double> toCopy) {
			ParameterLearningValue newOne = new ParameterLearningValue( ((ValueParameterGeneric)toCopy).getMyContexte());
			newOne.value = ((ValueParameterGeneric)toCopy).value;
			return  newOne;
		}

		@Override
		public Double getValue() {
			return value;
		}
		
	}
	
	/** CLASSE. qui permet faire le lien entre set de comportement et distribution de ces comportements.
	 * 
	 */
	public class ParameterDistribution implements IModelParameter<ParameterDistribution>  {
		public Hashtable<Integer, ArrayList<Meme>> indexKVCombinaison;
		public Hashtable<Integer, Double> indexKVProportion;

		/** Constructeur du distrib de behavior 
		 * 
		 */
		public ParameterDistribution(Hashtable<Integer, ArrayList<Meme>> setOfBehavior){
			indexKVCombinaison = new Hashtable<Integer, ArrayList<Meme>>();
			indexKVProportion = new Hashtable<Integer, Double>();
			for (Integer index : setOfBehavior.keySet()) {
				indexKVCombinaison.put(index, setOfBehavior.get(index));
				indexKVProportion.put(index, 0.);
			}
		}

		/** Constructeur du distrib de behavior 
		 * 
		 */
		public ParameterDistribution(Hashtable<Integer, ArrayList<Meme>> setOfBehavior, double defaultValue){
			indexKVCombinaison = new Hashtable<Integer, ArrayList<Meme>>();
			indexKVProportion = new Hashtable<Integer, Double>();
			for (Integer index : setOfBehavior.keySet()) {
				indexKVCombinaison.put(index, setOfBehavior.get(index));
				indexKVProportion.put(index, defaultValue);
			}
		}

		/** Le clone n'est pas utilisé?
		 * 
		 */
		public IModelParameter<ParameterDistribution> copy(IModelParameter<ParameterDistribution>  toCopy) {
			ParameterDistribution newOne = new ParameterDistribution(indexKVCombinaison);
			newOne.indexKVProportion.putAll(((ParameterDistribution)toCopy).indexKVProportion);
			return newOne;
		}
		
		@Override
		public ParameterDistribution getValue() {
			return this;
		}
		
		/** Set la value pour un index de behavior à une valeur
		 * 
		 */
		public void setValue(int index, double proportion){
			indexKVProportion.put(index, proportion);
		}
		
		/** Ajoute un parameter, c'est a dire un behavior, avec un 
		 * index pour le représenter. Proba de sélection set a 0 par
		 * défault.
		 * 
		 * @param index
		 * @param set
		 */
		public void addParameter(int index, ArrayList<Meme> set){
			indexKVCombinaison.put(index, set);
			indexKVProportion.put(index, 0d);
		}
		
		/** Ajoute un paramètre, c'est a dire un behavior, avec un index
		 * pour la représenter et un double inférieur a 1 pour sa proportion
		 * 
		 * @param index
		 * @param set
		 * @param proportion
		 */
		public void addParameter(int index, ArrayList<Meme> set, double proportion){
			indexKVCombinaison.put(index, set);
			if(proportion <= 1)
				indexKVProportion.put(index, proportion);
			else {
				indexKVProportion.put(index, 0d);
				System.err.println("[DistribParam] ajout d'un behavior avec proport. > 1");
			}
		}

		/** retourne le nom du model parameter: Distribution
		 * 
		 */
		public String getName(){
			return FittingParamType.DISTRIB.name();
		}
		
		/** Obtient la distribution sous forme comportement // distrib
		 * 
		 */
		public String getValueAsString(){
			String res = "";
			for (Integer index : indexKVCombinaison.keySet()) {
				for (Meme meme : indexKVCombinaison.get(index)) {
					res += " &"+ meme.toFourCharString();
				}
				
				res += " = " + indexKVProportion.get(index) + "|"; 
			}
			return res;
		}

		/**
		 * 
		 */
		public String toString(){
			return getName() + " : " + getValueAsString();
		}
		
	}
	
	/** Classe d'une valeur de type boolean, définition générique. 
	 * 
	 */
	public abstract class BooleanParameterGeneric implements IModelParameter<Boolean> {
		public boolean bool;
	
		public BooleanParameterGeneric(){
			bool = false;
		}
		public BooleanParameterGeneric(boolean baseValue){
			bool = baseValue;
		}
		
		
		/** Commune pour tout les classes filles
		 * 
		 */
		public String getValueAsString() {
			return bool? "True" : "False";
		}

		public Boolean getValue() {
			return bool;
		}
		
		public String toString(){
			return getValueAsString();
		}
	}
	
	/** Défini si 
	 *
	 */
	public class BooleanRelearnBehavior extends BooleanParameterGeneric {

		public BooleanRelearnBehavior(){
			bool = false;
		}
		
		public BooleanRelearnBehavior(boolean value){
			bool = value;
		}
		
		@Override
		public String getName() {
			return "WithRelearn";
		}

		/** Copy de l'objet courant.
		 * 
		 */
		public BooleanParameterGeneric copy(IModelParameter<Boolean> toCopy) {
				return new BooleanRelearnBehavior(toCopy.getValue());
		}

		
	}
}

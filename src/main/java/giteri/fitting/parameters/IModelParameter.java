package giteri.fitting.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import giteri.tool.math.Toolz;
import giteri.run.configurator.Configurator;
import giteri.meme.entite.EntiteHandler;
import giteri.meme.entite.Meme;

/** INTERFACE des paramètres pour le modèle, qui sera cycle automatiquement. 
 * 
 * @param <T> Le type de donnée sur le lequel cycler.
 */
public interface IModelParameter<T> {
	/** Obtenir la valeur contenu par le paramètre.
	 * 
	 * @return Une donnée de type T
	 */
	public T getValue();
	/** Applique la nouvelle valeur au systeme. doit avoir acces au 
	 * entités ec etc
	 * 
	 */
	public void apply();
	/** Va a la donnée suivante. Si int, i++
	 * 
	 * @return la nouvelle valeur que va avoir la donnée.
	 */
	public boolean gotoNext();
	/** Va à une valeur random de la plage disponible.
	 * 
	 */
	public void gotoRandom();
	/** Retourne a la valeur minimum de la donnée. Pour explorer tt les
	 * valeurs lors que plusieurs paramètres sont couplés.
	 * 
	 */
	public void gotoMinValue();
	/** Obtenir une copie de la donnée.
	 * 
	 * @return
	 */
	public T getCopyMyValue();
	/** La donnée sous forme string pour affichage. La rendre explicite en 
	 * précisant de quelle donnée il s'agit.
	 * 
	 * @return
	 */
	public String valueString();
	/**
	 * 
	 * @return
	 */
	public String nameString();
	
	public List<String> getPossibleValue();
	
	public void setPossibleValue(String ref);
	
	
	/** CLASSE ABSTRAITE qui implémente une partie de l'interface, définissant
	 * la valeur T contenu, sa valeur min et sa valeur max. 
	 * Implémente aussi getValue() & gotoMinValue().
	 * 
	 *
	 * @param <T>
	 */
	public abstract class AbstractModelParameter<T> implements IModelParameter<T>{
		T value;
		T minValue;
		T maxValue;
		
		public T getValue() {
			return value;
		}
		
		public void gotoMinValue(){
			value = minValue;
		}
	}
	
	/** CLASSE ABSTRAITE qui définit T comme un boolean. Valeur de base false. 
	 * 
	 *
	 */
	public abstract class AbstractBooleanParameter extends AbstractModelParameter<Boolean>{
		
		protected AbstractBooleanParameter(){
			minValue = true;
			maxValue = false;
			value = true;
		}
		
		public boolean gotoNext(){
			if(value == minValue){
				value = maxValue;
				return true;
			}
			return false;
		}
		
		public void gotoRandom(){
			if(Toolz.getProbaOneOut() < .5)
				value = true;
			else
				value = false;
		}
		
		public Boolean getCopyMyValue(){
			return new Boolean(value);
		}
		
		public String valueString(){
			return value? "TRUE":"FALSE";
		}
		
		public String nameString(){
			return "boolean";
		}

		public void setValue(boolean valeur){
			value = valeur? true: false;
//			if(valeur)
//				value = val;
//			else
//				value = minValue;
		}
		
		public List<String> getPossibleValue(){
			return new ArrayList<String>(Arrays.asList("true", "false")); 
		}
		
		public void setPossibleValue(String valu){
			if(valu.compareToIgnoreCase("TRUE") == 0)
				value = true;
			else if(valu.compareToIgnoreCase("FALSE") == 0)
				value = false;
		}
		
	}
	
	/** CLASSE ABSTRAITE qui définit T comme un double. Défini un champs step pour connaitre la 
	 * vitesse de progression de minvalue (0.0) a maxvalue
	 * 
	 *
	 */
	public abstract class AbstractDoubleParameter extends AbstractModelParameter<Double>{
		Double step;
		
		protected AbstractDoubleParameter(){
			minValue = .0;
			value = minValue;
		}
		
		public boolean gotoNext(){
			double calcul =Toolz.getNumberCutToPrecision(value + step, 4); 
			if(calcul <= maxValue){
				value = calcul;
				return true;
			}
			return false;
		}
		
		public void gotoRandom(){
			double diff = maxValue - minValue;
			int nbStepPossible = (int)(diff / step);
			// Le max est exclu dans la fonction getRandom
			int stepChoose = Toolz.getRandomNumber(nbStepPossible + 1);
			value = minValue + stepChoose * step;
		}
		
		public String nameString(){
			return "Double";
		}
		
		public List<String> getPossibleValue(){
			List<String> res = new ArrayList<String>();
			double diff = maxValue - minValue;
			int nbStepPossible = (int)(diff / step);
			double value;
			for (int i = 0; i <= nbStepPossible; i++) {
				value = minValue + i * step;
				res.add("" + value);
			}
			
			
			return res;
		}
		
		public void setPossibleValue(String value){
			double valueToSet = Double.parseDouble(value);
			this.value = valueToSet;
		}
	}

	/** CLASSE ABSTRAITE qui définit T comme une Map<Meme, P> ou P est un type quelconque, extend de
	 * AbractModelParameter. Permet d'avoir a nouveau acces au méthode gotoNext etc etc.
	 * 
	 *
	 * @param <P>
	 */
	public abstract class AbstractMapParameter<P extends AbstractModelParameter<?>> extends AbstractModelParameter<Hashtable<Meme, P>>{
		public void gotoMinValue(){
			for (Meme meme : value.keySet()) {
				value.get(meme).gotoMinValue();
			}
		} 
		
		/**
		 * 
		 */
		public List<String> getPossibleValue(){
			boolean debug = false;
			boolean allFinish = false;
			int indexMeme = 0;
			int index;
			ArrayList<String> res = new ArrayList<String>();
			
			// Meme dont il est question
			ArrayList<Meme> memes = new ArrayList<Meme>(value.keySet());
			memes.sort(null);
			
			if(debug)System.out.println("Memes " + memes);
			
			// Avoir les IModelParameter associé a chaque giteri.meme
			ArrayList<P> elements = new ArrayList<P>();
			for (Meme meme : memes) 
				elements.add(value.get(meme));

			if(debug)System.out.println("Elements " + elements);
			
			// Valeur possible pour chaque element
			Hashtable<P, List<String>> possibleValues = new Hashtable<P, List<String>>();
			for (P p : elements) 
				possibleValues.put(p, p.getPossibleValue());
			
			if(debug)System.out.println("Valeur possible pour chaque elements " + possibleValues);
			
			// Index des valeurs courantes sur la liste des possibles values
			List<Integer> indexValueCourante = new ArrayList<Integer>(value.size());
			for (int j = 0; j < memes.size(); j++) 
				indexValueCourante.add(0);

			do
			{
				String config = "";
				indexMeme = 0;
				// on trouve les valeurs correspondantes aux index indexValueCourante
				for(int integer = 0; integer < memes.size() ;integer++){	
					config += memes.get(indexMeme) + ";" + 
					possibleValues.get(elements.get(indexMeme)).
							get(indexValueCourante.get(integer)) + ":";
					indexMeme++;
				}
				
				res.add(config);
				
				// on regarde si on peut augmenter l'index du premier élément. 
				index = 0;
				
				for (Integer integer : indexValueCourante) {
					// Dans le cas ou un élément suivant existe 
					try{
							possibleValues.get(elements.get(index)).get(integer+1);
							indexValueCourante.set(index, integer + 1);
							break;
					}
					// Dans le cas contraire on le met a zero et laisse passer a l'élément suivant
					catch(IndexOutOfBoundsException e){
						indexValueCourante.set(index, 0);
					}
						
					
					// Passage au giteri.meme suivant
					index++;
				}

				if(debug)System.out.println(indexValueCourante);
			// dans le cas ou on est revenu au début
			if(indexValueCourante.stream().mapToInt(i -> i.intValue()).sum() == 0){
				allFinish = true;
				if(debug)System.out.println("all sum = 0");
			}
				
			}while(!allFinish);
			
			return res;
		}
		
		/**
		 * 
		 */
		public void setPossibleValue(String val){
			String[] nMeme = val.split(":");
			String[] oneMeme;
			
			for (String aMeme : nMeme) {
				oneMeme = aMeme.split(";");
				for (Meme meme : value.keySet()) {
					if(meme.toFourCharString().compareTo(oneMeme[0]) == 0){
						value.get(meme).setPossibleValue(oneMeme[1]);
						break;
					}
				}
			}
		}
		
	}
	
	/** CLASSE de boolean générique, utilisé pour etre utilisé dans la map<Meme, P>. Donc pas d'apply en tant que tel.
	 * 
	 * @author Felix
	 *
	 */
	public class GenericBooleanParameter extends AbstractBooleanParameter{

		public GenericBooleanParameter(){
		}
		
		/** 
		 * 
		 * @param valeur
		 */
		public GenericBooleanParameter(Boolean valeur){
			value = valeur;
		}
		
		@Override
		public void apply() {
			// Rien, normal.
		}
		
	}
	
	/** CLASSE de double générique, utilisé dans la map<Meme, P>. pas d'apply définit.
	 * 
	 * @author Felix
	 *
	 */
	public class GenericDoubleParameter extends AbstractDoubleParameter {
		public GenericDoubleParameter(){
			
		}

		/** 
		 * 
		 * @param valeur
		 */
		public GenericDoubleParameter(Double valeur){
			value = valeur;
		}
		
		/** 
		 * 
		 * @param valeur
		 */
		public GenericDoubleParameter(Double valeur, Double min, Double max, Double step){
			value = new Double(valeur);
			minValue = min;
			maxValue = max;
			this.step = step;
		}
		
		@Override
		public void apply() {
			// Rien, normal.
			
		}

		@Override
		public Double getCopyMyValue() {
			return new Double(getValue());
		}

		@Override
		public String valueString() {
			// Rien, ~~ normal
			return null;
		}
	
		public GenericDoubleParameter copyMe(){
			return new GenericDoubleParameter(value, minValue, maxValue, step);
		}
	}
	
	/** CLASSE qui permet de définir si un giteri.meme est présent sur la map ou non au début de la
	 * simulation. Ne prends pas de set<Meme> mais jsute un giteri.meme et l'applique successivement
	 * a la liste de node disponible. 
	 * 
	 *
	 */
	public class MemeAvailability extends AbstractMapParameter<GenericBooleanParameter> {
		public ArrayList<Meme> availableMeme; 
//		GenericDoubleParameter defaultValue;

		/** Constructeur prenant un ensemble de giteri.meme; booleanParameter.
		 * 
		 * @param p
		 */
		public MemeAvailability(Hashtable<Meme, GenericBooleanParameter> p ){
			value = p;
			availableMeme = new ArrayList<Meme>();
			for (Meme meme : p.keySet()) {
				if(p.get(meme).getValue())
					availableMeme.add(meme);
			}
			
			availableMeme.sort(null);
		}
	
		/** Constructeur prenant un ensemble de giteri.meme; booleanParameter.
		 * 
		 * @param p
		 */
		public MemeAvailability(Hashtable<Meme, GenericBooleanParameter> p, GenericDoubleParameter defautDoubleParam){
			value = p;
			availableMeme = new ArrayList<Meme>();
			for (Meme meme : p.keySet()) {
				if(p.get(meme).getValue())
					availableMeme.add(meme);
			}
			
			availableMeme.sort(null);
//			defaultValue = defautDoubleParam;
		}
		
		/** valeur minimum pour tous les éléments. Ici, false.
		 * 
		 */
		public void gotoMinValue(){
			for (GenericBooleanParameter bool : value.values()) {
				bool.gotoMinValue();
			}
		}
		
		/** Enchaine sur la valeur suivante, applique si possible gotoNext() sur le parametre
		 * GenericBooleanParameter.
		 * 
		 */
		public boolean gotoNext() {
			
			ArrayList<Meme> memes = new ArrayList<Meme>();
			memes.addAll(value.keySet());
			memes.sort(null);
			for (Meme meme : memes) {
				if(value.get(meme).gotoNext())
					return true;
				else
					value.get(meme).gotoMinValue();
			}
			
			return false;
		}

		/** Prend une valeur random. 
		 * génère un int compris entre 0 et 2^nbBoolean, le converti en masque de boolean
		 */
		public void gotoRandom(){
			System.err.println("Pas implémenté");
		}
		
		/** Donne au entité prise successivement les memes 
		 * 
		 */
		public void apply() {
			availableMeme.clear();
			for (Meme meme : value.keySet()) {
				if(value.get(meme).getValue())
					availableMeme.add(meme);
			}
			availableMeme.sort(null);
			if(!Configurator.doNotApplyMemeAvailability) EntiteHandler.getInstance().giveMemeToEntiteXFirst(availableMeme);
		}
		
		/**
		 * 
		 */
		public Hashtable<Meme, GenericBooleanParameter> getCopyMyValue(){
			Hashtable<Meme, GenericBooleanParameter> copy = new Hashtable<Meme, GenericBooleanParameter>(); 
			for (Meme meme : value.keySet()) {
				copy.put(meme, new GenericBooleanParameter( value.get(meme).getValue()));
			}
			
			return copy;
		}
		
		/** Retourne en lisible le contenu.
		 * 
		 */
		public String valueString(){
			String res = "MemeActivated: ";
			for (Meme meme : value.keySet()) {
				if(value.get(meme).getValue())
					res += "&" + EntiteHandler.getInstance().translateMemeCombinaisonReadable(meme.toFourCharString()) + " ";  
			}
			
			return res;
		}
	
		public String nameString(){
			return "Meme Availability";
		}
	}

	/** CLASSE pour la probabilité de diffusion des memes.
	 * 
	 */
	public class MemeDiffusionProba extends AbstractMapParameter<GenericDoubleParameter>{
		
		ArrayList<Meme> availableMeme;
		Hashtable<Meme, GenericDoubleParameter> precisedMeme;
		boolean memeSetWillChange = true;
		GenericDoubleParameter defautDoubleParam = null;
		
		/** Constructeur sans paramètre.
		 * 
		 */
		public MemeDiffusionProba(){
			value = new Hashtable<Meme, GenericDoubleParameter>();
			precisedMeme = new Hashtable<Meme, GenericDoubleParameter>();
		}
		
		/** si avec memeAvailability, doit etre la giteri.meme arraylist pour mise a jour
		 * lorsque des memes se désactivent. 
		 * 
		 * @param memes
		 */
		public MemeDiffusionProba(ArrayList<Meme> memes){
			this();
			availableMeme = memes;
			for (Meme meme : availableMeme)
			{
				value.put(meme, new GenericDoubleParameter(.0, .0, 1., .1));
			}
		}
		
		public MemeDiffusionProba(ArrayList<Meme> memes, GenericDoubleParameter defautParam){
			this();
			availableMeme = memes;
			for (Meme meme : availableMeme)
			{
				value.put(meme, defautParam.copyMe());
			}
			
			defautDoubleParam = defautParam;
		}
		
		/** Va a la valeur suivante, suivant l'ordre de available giteri.meme qui est
		 * sort() en amont par memeAvailability
		 * 
		 */
		public boolean gotoNext() {
			// les memes d'available sont sort()
			for (Meme meme : availableMeme) {
				if(value.get(meme).gotoNext())
					return true;
				else
					value.get(meme).gotoMinValue();
			}	
			
			return false;
		}

		/** Apply aux memes sa probabilité de propagation.
		 * 
		 */
		public void apply() {
			if(memeSetWillChange)
			{
				value.clear();
				for (Meme meme : availableMeme){
					if(precisedMeme.containsKey(meme)){
						value.put(meme, precisedMeme.get(meme));
					}
					else{
						if(defautDoubleParam != null)
							value.put(meme, defautDoubleParam.copyMe());
						else{
							value.put(meme, new GenericDoubleParameter(.0, .0, 1., .1));
							System.err.println("[IModelParameter:MemeDiffusionProba:Apply()] Utilisation d'un genericDoubleParameter aux valeurs de base douteuses");
						}
					}
				}
				memeSetWillChange = false;
			}
			for (Meme meme : availableMeme) {
				meme.probaOfPropagation = value.get(meme).value;
			}
		}

		/** Va choisir un giteri.meme aléatoirement et lui donner une valeur de propagation
		 * aléatoire. 
		 * 
		 */
		public void gotoRandom(){
			Meme meme = availableMeme.get(Toolz.getRandomNumber(availableMeme.size()));
			value.get(meme).gotoRandom();
		}
		
		/** Surcharge de la méthode, permet de redéfinir les giteri.meme actifs de la
		 * simulation. Utile lorsque cycle en conjonction avec des paramètres modifiant
		 * les memes actifs sur le giteri.network.
		 * 
		 */
		public void gotoMinValue(){
			super.gotoMinValue();
			memeSetWillChange = true;
		}
		
		/** Précise les valeurs des bornes de recherche pour un giteri.meme.
		 * 
		 * @param meme Meme cible
		 * @param newParam
		 * @return Retourne false si le giteri.meme n'existe pas dans la liste
		 */
		public void specifyMemeBound(Meme meme, GenericDoubleParameter newParam){
			precisedMeme.put(meme, newParam);
		}
		
		public void setValue(Hashtable<Meme, GenericDoubleParameter> value){
			this.value = value;
		}
		
		@Override
		public Hashtable<Meme, IModelParameter.GenericDoubleParameter> getCopyMyValue() {
			return null;
		}

		@Override
		public String valueString() {
			String rez = "ProbaPropagation: ";
			for (Meme meme : availableMeme) {
				rez += EntiteHandler.getInstance().translateMemeCombinaisonReadable(meme.toFourCharString()) + ":" + value.get(meme).value + " ";
			}
			
			return rez;
		}

		public String nameString(){
			return "Proba Diffusion";
		}
	}
}

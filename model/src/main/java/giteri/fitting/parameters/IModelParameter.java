package giteri.fitting.parameters;

import java.util.*;

import giteri.meme.event.IMemeAvailableListener;
import giteri.meme.event.MemeAvailableEvent;
import giteri.network.event.INbNodeChangedListener;
import giteri.network.event.NbNodeChangedEvent;
import giteri.run.configurator.Configurator;
import giteri.tool.math.Toolz;
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
	 T getValue();
	/** Applique la nouvelle valeur au systeme. doit avoir acces au 
	 * entités ec etc
	 *
	 */
	 void apply();
	/** Va a la donnée suivante. Si int, i++
	 *
	 * @return true si la valeur existe
	 */
	 boolean gotoNext();
	/** Va à une valeur random de la plage disponible.
	 *
	 */
	 void gotoRandom();
	/** Retourne a la valeur minimum de la donnée. Pour explorer tt les
	 * valeurs lors que plusieurs paramètres sont couplés.
	 *
	 */
	 void gotoMinValue();
	/** La donnée sous forme string pour affichage. La rendre explicite en 
	 * précisant de quelle donnée il s'agit.
	 *
	 * @return
	 */
	 String valueString();
	/**
	 *
	 * @return
	 */
	 String nameString();
	/** Obtient une liste de string définissant les valeurs possibles.
	 * Doit etre réutilisable dans setPossibleValue
	 *
	 * @return
	 */
	 List<String> getPossibleValue();
	/** Obtient la valeur actuelle qui pourra etre remise par le setPossibleValue
	 *
	 * @return
	 */
	 String getActualValue();
	/** Permet de partir d'un string et
	 *
	 * @param ref
	 */
	 void setPossibleValue(String ref);
	/**
	 *
	 * @param eh
	 */
	 void setEntiteHandler(EntiteHandler eh);

	/** CLASSE ABSTRAITE qui implémente une partie de l'interface, définissant
	 * la valeur T contenu, sa valeur min et sa valeur max.
	 * Implémente aussi getValue() & gotoMinValue().
	 *
	 *
	 * @param <T>
	 */
	abstract class AbstractModelParameter<T> implements IModelParameter<T>{
		T value;
		T minValue;
		T maxValue;
		EntiteHandler entiteHandler;
		boolean hasChanged = true;

		public void setEntiteHandler(EntiteHandler eh){
			entiteHandler = eh;
		}

		public T getValue() {
			return value;
		}

		public void gotoMinValue(){
			value = minValue;
		}
	}

	/** CLASSE ABSTRAITE qui définit T comme un boolean. Valeur de base true.
	 *
	 *
	 */
	abstract class AbstractBooleanParameter extends AbstractModelParameter<Boolean>{

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

		public String valueString(){
			return value ? "TRUE":"FALSE";
		}

		public String nameString(){
			return "Boolean";
		}

		public void setValue(boolean valeur){
			value = valeur;
		}

		public List<String> getPossibleValue(){
			return new ArrayList<>(Arrays.asList("true", "false"));
		}

		public void setPossibleValue(String valu){
			if(valu.compareToIgnoreCase("TRUE") == 0)
				value = true;
			else if(valu.compareToIgnoreCase("FALSE") == 0)
				value = false;
		}

		public String getActualValue(){
			return valueString();
		}
	}

	/** CLASSE ABSTRAITE qui définit T comme un double. Défini un champs step pour connaitre la
	 * vitesse de progression de minvalue a maxvalue
	 *
	 */
	abstract class AbstractDoubleParameter extends AbstractModelParameter<Double>{

		Double step;
		// détermine l'arrondi fait lors de l'ajout de step a la valeur courante.
		// Problème de précision autrement ( 0.400000001 au lieu de 0.4 )
		int precision = 4;

		protected AbstractDoubleParameter(){
			minValue = .0;
			value = minValue;
		}

		public boolean gotoNext(){
			double nextValue =Toolz.getNumberCutToPrecision(value + step, precision);
			if(nextValue <= maxValue){
				value = nextValue;
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

		public String valueString() {
			return ""+value;
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

		public String getActualValue(){
			return valueString();
		}
	}

	/** CLASSE ABSTRAITE qui définit T comme un double. Défini un champs step pour connaitre la
	 * vitesse de progression de minvalue a maxvalue
	 *
	 */
	abstract class AbstractIntParameter extends AbstractModelParameter<Integer>{

		int step;
		// détermine l'arrondi fait lors de l'ajout de step a la valeur courante.
		// Problème de précision autrement ( 0.400000001 au lieu de 0.4 )
		int precision = 4;

		protected AbstractIntParameter(){
			minValue = 0;
			value = minValue;
		}

		public boolean gotoNext(){
			int nextValue =value + step;
			if(nextValue <= maxValue){
				value = nextValue;
				return true;
			}
			return false;
		}

		public void gotoRandom(){
			int diff = maxValue - minValue;
			int nbStepPossible = (diff / step);
			// Le max est exclu dans la fonction getRandom
			int stepChoose = Toolz.getRandomNumber(nbStepPossible + 1);
			value = minValue + stepChoose * step;
		}

		public String valueString() {
			return ""+value;
		}

		public String nameString(){
			return "Integer";
		}

		public List<String> getPossibleValue(){
			List<String> res = new ArrayList<>();
			int diff = maxValue - minValue;
			int nbStepPossible = (diff / step);
			int value;
			for (int i = 0; i <= nbStepPossible; i++) {
				value = minValue + i * step;
				res.add("" + value);
			}


			return res;
		}

		public void setPossibleValue(String value){
			int valueToSet = Integer.parseInt(value);
			this.value = valueToSet;
		}

		public String getActualValue(){
			return valueString();
		}
	}

	/** CLASSE ABSTRAITE qui définit T comme une Map<Meme, P> ou P est un type quelconque, extend de
	 * AbractModelParameter. Permet d'avoir a nouveau acces au méthode gotoNext etc etc.
	 *
	 *
	 * @param <P>
	 */
	 abstract class AbstractMapParameter<P extends AbstractModelParameter<?>> extends AbstractModelParameter<Hashtable<Meme, P>>{
		public void gotoMinValue(){
			for (Meme meme : value.keySet()) {
				value.get(meme).gotoMinValue();
			}
		}

		/** renvoi une liste de string définissant la liste des valeurs possibles pour la map en question.
		 * Va dépendre du type de parametre P sur lequel sera appelé la meme fonction, et créer une projection de
		 * meme x P.getPossibleValue.
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

			// Avoir les IModelParameter associés à chaque meme
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

			// Boucle qui va flat map les listes de valeurs possibles
			do
			{
				String config = "";
				indexMeme = 0;
				// on trouve les valeurs correspondantes aux index indexValueCourante
				for(int integer = 0; integer < memes.size() ;integer++){
					config += memes.get(indexMeme) + "=" +
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


					// Passage au meme suivant
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

		/** prend un string et l'applique en tant que valeur effective.
		 * //TODO [Refact5.0]- non implémenté encore
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

		public String getActualValue(){
			String result = "";

			for (Meme meme : value.keySet()) {
				result += meme + "=" + value.get(meme).getActualValue() +":";
			}

			return result;
		}
	}

	/** CLASSE de boolean générique, utilisé dans la map<Meme, P>. Donc pas d'apply en tant que tel.
	 *
	 * @author Felix
	 */
	class GenericBooleanParameter extends AbstractBooleanParameter{

		public GenericBooleanParameter(){}

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
	 */
	class GenericDoubleParameter extends AbstractDoubleParameter {
		public GenericDoubleParameter(){}

		public GenericDoubleParameter(Double valeur){
			value = maxValue = minValue = valeur;
			step = 1.;
		}

		public GenericDoubleParameter(Double valeur, Double min, Double max, Double step){
			value = new Double(valeur);
			minValue = min;
			maxValue = max;
			this.step = step;
			if(step < 1)
				precision = (int)Math.log10(((double)1) / step) + 1;
		}

		@Override
		public void apply() {
			// Rien, normal.

		}

		public GenericDoubleParameter copyMe(){
			return new GenericDoubleParameter(value, minValue, maxValue, step);
		}
	}

	/** CLASSE qui permet de définir si un meme est présent sur la map ou non au début de la
	 * simulation. Ne prends pas de set<Meme> mais juste un meme et l'applique successivement
	 * à la liste de node disponible.
	 *
	 *
	 */
	class MemeAvailability extends AbstractMapParameter<GenericBooleanParameter> {
		private List<Meme> activatedMeme = new ArrayList<>();
		private List<IMemeAvailableListener> memeAvailableListeners = new ArrayList<>();

		/** Constructeur le plus simple. Prend un set de Meme. Y associe un BooleanParam
		 * initialisé a la valeur par défault. ( true )
		 *
		 * @param memes
		 */
		public MemeAvailability(List<Meme> memes){
			memes.sort(null);
			for (Meme meme : memes) {
				value.put(meme, new GenericBooleanParameter());
			}
		}

		/** Constructeur prenant un ensemble de meme; booleanParameter.
		 * Permet d'associer une valeur précise pour le boolean du meme.
		 * /!\ valeur sera perdu en cas d'exploration exhaustive ou random.
		 * @param p
		 */
		public MemeAvailability(Hashtable<Meme, GenericBooleanParameter> p ){
			value = p;
		}

		/** Enchaine sur la valeur suivante, applique si possible gotoNext() sur le parametre
		 * GenericBooleanParameter.
		 *
		 */
		public boolean gotoNext() {
			hasChanged = true;
			ArrayList<Meme> memes = new ArrayList<Meme>();
			memes.addAll(value.keySet());
			memes.sort(null); // TODO [Opti 1.0]- Peut etre inutile a sort, puisque le constructeur le fait déjà
			// dans sa version la plus simple
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
			hasChanged = true;
		}

		/** Donne aux entités les memes actifs.
		 * TODO [Refact4.0]- Faire en sorte de créer un nouveau MemeDiffusionProba avec les bons memes actifs
		 *
		 */
		public void apply() {
			if(hasChanged) {
				activatedMeme.clear();
				for (Meme meme : value.keySet()) {
					if (value.get(meme).getValue())
						activatedMeme.add(meme);
				}

				this.memesAvailablesChange(activatedMeme, "New list of active memes on map");
				hasChanged = false;
			}
		}

		/** Pour lancer les évènements de type meme dispo changz .
		 *
		 */
		private void memesAvailablesChange(List<Meme> listAvailableMemes, String message) {

			// On crée un événement rappelant l'état courant concernant les memes;
			MemeAvailableEvent myEvent = new MemeAvailableEvent(this, listAvailableMemes, message);

			// ON PREVIENT LES ENTITES QUI LISTEN
			for (IMemeAvailableListener memeAvailableListener : memeAvailableListeners) {
				memeAvailableListener.handlerMemeAvailable(myEvent);
			}
		}

		/** Ajout d'un listener a la liste des listeners a prévenir en cas d'event de
		 * type entity
		 *
		 * @param myListener
		 */
		public void addMemeListListener(IMemeAvailableListener myListener) {
			if (!memeAvailableListeners.contains(myListener)) {
				memeAvailableListeners.add(myListener);
			}
		}

		/** Retrait d'un listener depuis la liste des listeners
		 *
		 * @param myListener
		 */
		public void removeMemeListListener(IMemeAvailableListener myListener) {
			if (memeAvailableListeners.contains(myListener)) {
				memeAvailableListeners.remove(myListener);
			}
		}

		/** Retourne en lisible le contenu.
		 *
		 */
		public String valueString(){
			String res = "MemeActivated: ";
			for (Meme meme : value.keySet()) {
				if(value.get(meme).getValue())
					res += "&" + entiteHandler.translateMemeCombinaisonReadable(meme.toFourCharString()) + " ";
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
	class MemeDiffusionProba extends AbstractMapParameter<GenericDoubleParameter> implements IMemeAvailableListener {
		GenericDoubleParameter defautDoubleParam;

		/** Constructeur sans paramètre.
		 *
		 */
		public MemeDiffusionProba(){
			value = new Hashtable<>();
		}

		/** Prend un hashmap de meme associé a un doubleParameter
		 *
		 */
		public MemeDiffusionProba(Hashtable<Meme, GenericDoubleParameter> kvMemesParameter){
			this();
			ArrayList<Meme> memesSorted = new ArrayList<Meme>(kvMemesParameter.keySet());
			memesSorted.sort(null);
			for (Meme meme: memesSorted) {
				value.put(meme, kvMemesParameter.get(meme));
			}
		}

		/** Prend une liste de meme et un parameter generic de double
		 *
		 * @param memes
		 * @param defautParam
		 */
		public MemeDiffusionProba(ArrayList<Meme> memes, GenericDoubleParameter defautParam){
			this();
			memes.sort(null);
			for (Meme meme : memes)
				value.put(meme, defautParam.copyMe());

			defautDoubleParam = defautParam;
		}

		/** Va a la valeur suivante, suivant l'ordre de available meme qui est
		 * sort() en amont par memeAvailability
		 *
		 */
		public boolean gotoNext() {
			// les memes d'available sont sort()
			for (Meme meme : value.keySet()) {
				if(value.get(meme).gotoNext())
					return true;
				else
					value.get(meme).gotoMinValue();
			}

			return false;
		}

		/** Apply aux memes leurs probabilités de propagation.
		 *
		 */
		public void apply() {
			ArrayList<Meme> memeo = new ArrayList<>();
			for (Meme meme : value.keySet()) {
				meme.setProbaOfPropagation( value.get(meme).value);
				memeo.add(meme);
			}

			entiteHandler.giveMemeToEntiteFitting(memeo);
		}

		/** Va choisir un meme aléatoirement et lui donner une valeur de propagation
		 * aléatoire.
		 */
		public void gotoRandom(){
			Meme meme = new ArrayList<>(value.keySet()).get(Toolz.getRandomNumber(value.keySet().size()));
			value.get(meme).gotoRandom();
		}

		/** Surcharge de la méthode, permet de redéfinir les meme actifs de la
		 * simulation. Utile lorsque cycle en conjonction avec des paramètres modifiant
		 * les memes actifs sur le network.
		 *
		 */
//		public void gotoMinValue(){
//			super.gotoMinValue();
//		}

		public void setValue(Hashtable<Meme, GenericDoubleParameter> value){
			this.value = value;
		}

		@Override
		public String valueString() {
			String rez = "ProbaPropagation: ";
			for (Meme meme : value.keySet()) {
				rez += entiteHandler.translateMemeCombinaisonReadable(meme.toFourCharString()) + ":" + value.get(meme).value + " ";
			}

			return rez;
		}

		public String nameString(){
			return "Proba Diffusion";
		}

		/** Implemente la réception de l'évènement changement de liste de meme actif sur la map.
		 * Ne devrait arriver pour l'instant que lors de l'utilisation de l'IModelParameter @MemeAvailability
		 *
		 * @param e
		 */
		@Override
		public void handlerMemeAvailable(MemeAvailableEvent e) {
			value.clear();
			for (Meme meme:e.listOfMeme) {
				value.put(meme, defautDoubleParam.copyMe());
			}
		}
	}

	/** Permet de cycler sur le nombre de noeud présent dans la simulation.
	 *
	 */
	class ModelParamNbNode extends AbstractIntParameter{
		private List<INbNodeChangedListener> listenersToNbNodeChanged = new ArrayList<>();

		public ModelParamNbNode(int miniValue, int maxiValue, int stepi){
			minValue = miniValue;
			maxValue = maxiValue;
			value = minValue;
			step = stepi;
		}

		@Override
		public void apply() {
			if(!value.equals(minValue)) {
				Configurator.setNbNode(value);
				nbNodesChanged(value, "Nouveau nombre de noeud changé par IModelParam.INbNode");
			}
		}

		/** Prévient les listeners d'un nouveau nombdre de noeud dans la simulation
		 *
		 * @param nbNodes
		 * @param message
		 */
		public void nbNodesChanged(int nbNodes, String message){
			NbNodeChangedEvent myEvent = new NbNodeChangedEvent(this, nbNodes, message );
			for (INbNodeChangedListener iNbNodeChangedListener : listenersToNbNodeChanged) {
				iNbNodeChangedListener.handlerNbNodeChanged(myEvent);
			}
		}

		/** Ajout d'un listener a la liste des listeners a prévenir en cas d'event de
		 * type entity
		 *
		 * @param myListener
		 */
		public void addMemeListListener(INbNodeChangedListener myListener) {
			if (!listenersToNbNodeChanged.contains(myListener)) {
				listenersToNbNodeChanged.add(myListener);
			}
		}

		/** Retrait d'un listener depuis la liste des listeners
		 *
		 * @param myListener
		 */
		public void removeMemeListListener(INbNodeChangedListener myListener) {
			if (listenersToNbNodeChanged.contains(myListener)) {
				listenersToNbNodeChanged.remove(myListener);
			}
		}

		public String nameString(){
			return "nb nodes";
		}

	}


}

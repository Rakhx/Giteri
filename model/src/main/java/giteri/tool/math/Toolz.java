package giteri.tool.math;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.math3.distribution.BetaDistribution;

/** classe avec des outils dedans.
 *
 */
public class Toolz {
	static final int precision = 1000;
	static Long lastSetSeed = new Random().nextLong();
	static Random rand = new Random(lastSetSeed); 
	static final boolean meLikeDebug = false;
	DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
	final static Charset ENCODING = StandardCharsets.UTF_8;
	
	public static void setSeed(long seed){
		rand.setSeed(seed); 
		synchronized(lastSetSeed){
			lastSetSeed = seed;
		}
	}
	
	public static long getLastSeed(){
		synchronized(lastSetSeed){
			return lastSetSeed;
		}
	}


	/** Obtient un integer aléatoire compris entre 0 et max, 
	 * 0 inclu max exclu
	 * 
	 * @param max
	 * @return int @ [0;max[
	 */
	public static int getRandomNumber(int max){
		return rand.nextInt(max);
	}
	
	/** Renvoi un élément d'une liste choisi aléatoirement, après l'avoir
	 *  retiré de la liste qui été donné en paramètre
	 * 
	 * @param list une liste d'éléments
	 * @return un éléments qui a été remove de cette liste aléatoirement.
	 */
	public static <T extends Object> T getRandomElementAndRemoveIt(ArrayList<T> list){
		return list.remove(Toolz.getRandomNumber(list.size()));
	}
	
	/** Renvoi un élément aléatoirement choisi d'une liste. Si la liste est vide, renvoi 
	 * null. 
	 * 
	 * @param list
	 * @return Un élément de la liste ou null. 
	 */
	public static <T extends Object> T getRandomElement(ArrayList<T> list){
		return list.size() > 0 ? list.get(Toolz.getRandomNumber(list.size())) : null; 
	}

	public static <T extends Object> T getRandomElement(Set<T> set){
		int randomNumber = Toolz.getRandomNumber(set.size());
//		T resultat = null;

		for (T t : set) {
			if(randomNumber == 0)
				return t;
			randomNumber--;
		}

		return null;
	}

	/** retourne la liste réduite à un élément. 
	 * 
	 * @param list
	 * @return la liste réduite à un élément choisi aléatoirement.
	 */
	public static <T extends Object> ArrayList<T> getOneElementList(ArrayList<T> list){
		T selectedElement = getRandomElement(list); 
//				list.size() > 0 ? list.get(Toolz.getRandomNumber(list.size())) : null;
		list.clear();
		list.add(selectedElement);
		return list;
	}
	
	/** [0;1] retourne un entier entre 0 et 1 avec une précision de
	 * la valeur du champs précision de la giteri.meme classe Toolz.
	 * 
	 * @return
	 */
	public static double getProba(){
		return (double)getRandomNumber(precision+1) / precision;
	}
	
	/** [0;1[ retourne un entier entre 0 et 1 avec une précision de
	 * la valeur du champs précision de la giteri.meme classe Toolz.
	 * 
	 * @return
	 */
	public static double getProbaOneOut(){
		return (double)getRandomNumber(precision) / precision;
	}
	
	/** [0;1[ retourne un entier entre 0 et 1 avec une précision de
	 * la valeur du champs précision de la giteri.meme classe Toolz, de facon
	 * non uniforme et paramétré par l'indicateur, qui est compris entre [0;1]
	 * 
	 * @param indicateur
	 * @return un double compris entre 0 et 1
	 */
	public static double getProbaNonUniform(double indicateur){
		return (double)getRandomNumber(precision) / precision;
	}

	/**
	 * 
	 * @param alphaParam
	 * @param betaParam
	 * @return
	 */
	public static double getProbaBetaDistribution(double alphaParam, double betaParam){
		double x;
        double b;
        BetaDistribution beta = new BetaDistribution(alphaParam, betaParam);
        x = Math.random();
        b = beta.inverseCumulativeProbability(x);
        return b;
	}
	
	/** Prend une proba en entrée et renvoi true avec cette probabilité.
	 * Proba compris entre 0 et 1. La precision est de {@value #precision}, toute les probas
	 * inférieur a 1 / {@value #precision} seront false.
	 * @param proba
	 * @return
	 */
	public static boolean rollDice(double proba){
		return proba * precision > getRandomNumber(precision);
	}
	
	/** Prend une proba en entrée et renvoi true avec cette probabilité.
	 * Proba compris entre 0 et 1. La precision est de {@value #precision}, toute les probas
	 * inférieur a 1 / {@value #precision} seront false.
	 * @param proba
	 * @return
	 */
	public static boolean rollDiceWithThreshold(double proba, double threshold){
		return proba * precision > getRandomNumber(precision);
	}
	
	/**
	 * 
	 * @param maxValue
	 * @return
	 */
	public static double getARandomNumber(double maxValue, int precision){
		return ((double)getRandomNumber(precision+1) / precision) * maxValue;
	}
	
	/** Shuffle une arrayList
	 * 
	 * @param toUnsort 
	 * @return l'arraylist shuffled avec des 5*size() de swap. 
	 */
	public static <T extends Object> ArrayList<T> unsortArray(ArrayList<T> toUnsort){
		for (int i = 0; i < toUnsort.size() * 5; i++) {
			toUnsort.add(toUnsort.remove(getRandomNumber(toUnsort.size())));			
		}		
		
		return toUnsort;
	}


	/** Shuffle les corespondances key et value d'une hashMap.
	 *
	 * @param toShuffle
	 * @param totalRandom Défini l'aspect déterministe de l'opération.
	 * @param <A> type des keys
	 * @param <B> type des values
	 * @return une hashmap pour laquelle les associations key value ont été mélangées.
	 */
	public static <A extends Object,B extends Object> Hashtable<A,B> shuffleHashmap(Map<A,B> toShuffle, boolean totalRandom){
		Hashtable<A,B> unSorted = new Hashtable<A,B>();
		Set<A> keys = toShuffle.keySet();
		Collection<B> values = toShuffle.values();
		ArrayList<B> valuesConversion = new ArrayList<>(values);
		Random myRand = new Random(0);

		if(totalRandom){
			valuesConversion = unsortArray(valuesConversion);
		}else {
			for (int i = 0; i < valuesConversion.size() * 5; i++) {
				valuesConversion.add(valuesConversion.remove(myRand.nextInt(valuesConversion.size())));
			}
		}

		int i = 0;
		for (A key: keys) {
			unSorted.put(key, valuesConversion.get(i));
			i++;
		}


		return unSorted;
	}
	
	/** Ajoute, dans une hashtable(key, arraylist<value>) une valeur, que la key existe
	 * déjà ou non. 
	 * 
	 * @param table
	 * @param key
	 * @param value
	 * @return true si la key existait déjé, false sinon.
	 */
	public static <T1 extends Object, T2 extends Object> boolean addElementInHashArray(Hashtable<T1,ArrayList<T2>> table,T1 key, T2 value){
		if(table.containsKey(key)){
			table.get(key).add(value);
			return true;
		}else
		{
			table.put(key, new ArrayList<T2>(Arrays.asList(value)));
			return false;
		}
	}
	
	/** Ajoute, dans une hashtable(key, arraylist<value>) une valeur, que la key existe
	 * déjà ou non. 
	 * 
	 * @param table
	 * @param key
	 * @param value
	 * @return true si la key existait déjé, false sinon.
	 */
	public static <T1 extends Object, T2 extends Object> boolean removeElementInHashArray(Hashtable<T1,ArrayList<T2>> table,T1 key, T2 value){
		if(table.containsKey(key))
			return table.get(key).remove(value);
		else
			return false;
	}

	/** Ajoute, dans une hashtable(key, arraylist<value>) une valeur, que la key existe
	 * déjà ou non. 
	 * 
	 * @param table
	 * @param key
	 * @param value
	 * @return true si la key existait déjé, false sinon.
	 */
	public static <T1 extends Object> Integer addCountToElementInHashArray(Map<T1,Integer> table,T1 key, Integer value){
		Integer newValue;
		if(table.containsKey(key)){
			 newValue = table.get(key) + value;
			 table.put(key, newValue);
			 return newValue;
		}else
		{
			newValue = new Integer(value);
			table.put(key, newValue);
			return newValue;
		}
	}
	
	/** Retire depuis une hash de <key, integer> une quantite value au compte.
	 * Retourne la valeur de ce qui reste si la clef a été trouvé, null sinon. 
	 * @param table
	 * @param key
	 * @param value
	 * @return true si la key existait déjé, false sinon.
	 */
	public static <T1 extends Object> Integer removeCountToElementInHashArray(Map<T1,Integer> table,T1 key, Integer value){
		Integer newValue;
		if(table.containsKey(key)){
			 newValue = table.get(key) - value;
			 if(newValue >= 0)
				 table.put(key, newValue);
			 return newValue;
		}
		else
			return null;
	}
	
	/** Renvoi une distribution aléatoire de probabilité, 
	 * n double dont la somme vaut 1
	 * @param nbElement Le nombre de nombre aléatoire a renvoyer
	 * @return une arraylist de taille nbElement.
	 */
	public static ArrayList<Double> getRandomDistrib(int nbElement){
		ArrayList<Double> distrib = new ArrayList<Double>();
		int total = 0;
		int oneInt;
		//double rapport;
		
	
		for (int i = 0; i < nbElement; i++) {
			oneInt = Toolz.getRandomNumber(precision);
			total += oneInt;
			distrib.add((double)oneInt);
		}
		
		for (int i = 0; i < nbElement; i++) {
			distrib.add(i, distrib.remove(i) / (double)total );
		}
		
		return distrib;
	}	

	/** Renvoi la valeur moyenne des valeurs entrée dans le tableau.
	 * 
	 * @param entry
	 * @return
	 */
	public static <T extends Number> Double getAvg(List<T> entry){
		Double resultat = 0.0;
		try {
		for (Number number : entry) {
			resultat += Double.parseDouble(""+number);
		}
		} catch(NumberFormatException e){
			System.err.println("Oui oui mais non les number veulent pas se convertir en Double");
			return null;
		}
		
		return resultat / entry.size();
	}

	/** Obtenir la moyenne depuis une hash<valeur de l'occurence, nombre d'occurence>
	 * 
	 * @param entry
	 * @return
	 */
	public static <T extends Number> Double getAvg(Hashtable<Integer,T> entry){
		double resultat = 0.0;
		double sumDesFrequences = 0;
		for (Integer index : entry.keySet()) {
			sumDesFrequences += (Double) entry.get(index);
			resultat += index * (Double) entry.get(index);
//			System.out.println(index + ":" + (double)(T)entry.get(index));
		}
		
		return resultat / sumDesFrequences;
	}
	
	/** Moyenne des éléments numbers de la queue, castés en double.
	 * 
	 * @param queue
	 * @return
	 */
	public static <T extends Number> Double getAvg(CircularFifoQueue<T> queue){
		double resultat = 0;
		for (T t : queue) {
			resultat += (Double)t;
		}
		resultat /= queue.size();
		return resultat;
	}

	/** calcul la déviation et la moyenne de la série de donnée en entrée, et donne ces valeurs en pseudo référence
	 * aux variables 0 -> avg et 1 -> sd.
	 * 
	 * @param entry IN
	 * @param avg OUT
	 * @param sd OUT
	 */
	public static <T extends Number> Double[] getDeviation(Hashtable<Integer,T> entry, Double avg, double sd ){
		return getMeanAndSd(new ArrayList<T>(entry.values()));
	}

	/**
	 *
	 * @param entry
	 * @param <T>
	 * @return Double[2] 0 -> avg : 1 -> sd
	 */
	public static <T extends Number> Double[] getMeanAndSd(List<T> entry){
		Double avg = 0., sd = 0.;
		double sumFreq = 0;
		for (T value : entry)
			sumFreq += (Double)value;
		
		avg = sumFreq / entry.size();
				
		for (T value : entry) 
			sd +=  Math.pow( (Double)value - avg, 2);
		
		sd /= entry.size();
		sd = Math.sqrt(sd);
		return new Double[]{avg, sd};
	}
	
	/** Obtenir l'écart type des valeurs autour de la moyenne, fourni en paramètre ou non.
	 * @param entry key = un nombre value = sa fréquence
	 * @param avg
	 * @return
	 */
	public static <T extends Number> Double getDeviation(Hashtable<Integer,T> entry, Optional<Double> avg ){
		
		return getDeviation(new ArrayList<T>(entry.values()), avg);

	}
	
	/** Obtenir l'écart type des valeurs autour de la moyenne, fourni en paramètre ou non.
	 * 
	 * @param entry
	 * @param avg
	 * @return
	 */
	public static <T extends Number> Double getDeviation(ArrayList<T> entry, Optional<Double> avg ){
		Double avgValue;
		Double deviation = .0;
		if (!avg.isPresent())
			avgValue = getAvg(entry);
		else
			avgValue = avg.get();
		
		
		for (Number number : entry) {			
			deviation += Math.pow(/*(Double)number*/Double.parseDouble(""+number) - avgValue, 2);
		}
		 
		return Math.sqrt(deviation / entry.size());
	}

	/** Calcule la deviation d'une liste de Number en circularqueue
	 * 
	 * @param queue
	 * @return
	 */
	public static <T extends Number> Double getDeviation(CircularFifoQueue<T> queue){
		double mean = getAvg(queue);
		double deviation = 0;
		for (T number : queue) {			
			deviation += Math.pow((Double)number - mean, 2);
		}
		 
		return Math.sqrt(deviation / queue.size());
	}
	
	/**
	 * 
	 * @param entry
	 * @return
	 */
	public static <T extends Number> String getInfoOnSerie(Hashtable<Integer,T> entry){
		String donnees = "";
		double firstQ, thirdQ;
		double nbValues = 0;
		double temp;
		double moyenne;
		
		// Moyenne
		moyenne = getAvg(entry);
				
		for (int degree : entry.keySet()) 
			nbValues += (Double)entry.get(degree);
		
		// on trie la valeur des clefs, pour pouvoir les ordonnées et les parcourir en ordre croissant
		Object[] list = entry.keySet().toArray();
		Integer[] listInt = new Integer[list.length];
		int i = 0;
		for (Object object : list) {
			listInt[i++] = (Integer)object;
		}
		 
		Arrays.sort(listInt);
		ArrayList<Integer> degrees = new ArrayList<Integer>(Arrays.asList(listInt));
		
		// 1er quartile
		int parcouru = 0;
		int index = -1;
		temp = nbValues * .25f;
		do
		{
			index++;
			parcouru += (Double)entry.get(degrees.get(index));
		} while ( parcouru < temp);
		
		firstQ = index; 
		
		// 3er quartile
		parcouru = 0;
		index = -1;
		temp = nbValues * .75f;
		do
		{
			index++;
			parcouru += (Double)entry.get(degrees.get(index));
		} while ( parcouru < temp);
		
		thirdQ = index; 

		System.out.println(moyenne+","+(thirdQ-firstQ));
		System.out.println("FIRST Q "+firstQ + "; THIRD Q "+ thirdQ );
		
		return donnees;
	}

	/** Renvoi le double tronqué à un maximum de @precision decimal
	 * 
	 * @param input
	 * @param precision
	 * @return
	 */
	public static double getNumberCutToPrecision(Double input, int precision){
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
		otherSymbols.setDecimalSeparator('.');
		// otherSymbols.setGroupingSeparator('.');
		DecimalFormat decimal = new DecimalFormat("", otherSymbols);
		decimal.setMaximumFractionDigits(precision); // arrondi à 2 chiffres apres la virgules
		decimal.setMinimumFractionDigits(0);
		try{
		return Double.parseDouble(decimal.format(input));
		}catch( Exception e){
			if(meLikeDebug) System.err.println("Erreur de conversion");
			return 0;
		}
	}
	
	/** Renvoi une arrayList<Key> trié par Sort() appliqué au value associée.
	 * 
	 * @param t
	 * @return
	 */
    @SuppressWarnings("rawtypes")
	public static <T1 extends Object, T2 extends Comparable> ArrayList<T1> sortKeyByValue(Hashtable<T1, T2> t){
    	
    	//Transfer as List and sort it
        @SuppressWarnings("unchecked")
		ArrayList<Map.Entry<T1, T2>> l = new ArrayList(t.entrySet());
        Collections.sort(l, new Comparator<Map.Entry<T1, T2>>(){

          @SuppressWarnings("unchecked")
		public int compare(Map.Entry<T1, T2> o1, Map.Entry<T1, T2> o2) {
             return o1.getValue().compareTo(o2.getValue());
         }});

        
        ArrayList<T1> hope = new  ArrayList<T1>();
        for (Map.Entry<T1, T2> entry : l) {
			hope.add(entry.getKey());
		}
        
	    return hope;
     }
    
    /** Renvoi un element des <Key> issu de la hashtable, choisi aléatoirement avec une 
     * proba proportionnel au chiffre associé dans l'arrayList.
     * 
     * @param objectByProba
     * @return
     */
    public static <T extends Object> T getElementByUniformProba(Hashtable<T,Double> objectByProba){
    	double somProba = objectByProba.values().stream().mapToDouble(Double::doubleValue).sum();
    	double tirage = Toolz.getARandomNumber(somProba , 1000);
    	double summing = 0;
    	double borneInf;
    	int nbCount=0;
    	T selectedKey = null;
    	
    	for (T key : objectByProba.keySet()) {
			nbCount ++;
    		borneInf = summing;
    		summing += objectByProba.get(key);
			if(tirage >= borneInf && tirage < summing){
				selectedKey = key;
				break;
			}
			if(nbCount == objectByProba.size())
				selectedKey = key;
		}
    	
    	
    	
    	return selectedKey;
    }
    
    /** Retourne la valeur maximum des clefs d'une hashtable. Les clefs
     * doivent etre comparable. 
     * 
     * @param source
     * @return
     */
    public static <T1 extends Comparable<T1>> T1 getMaxOfKeys(Hashtable<T1, ?> source){
    	
    	boolean firstValueDone = true;
    	T1 maxValue = null;
    	
    	for (T1 keyEntry : source.keySet()) {
			if(firstValueDone){
				maxValue = keyEntry;
				firstValueDone = false;
			}
			if(maxValue.compareTo(keyEntry) < 0)
				maxValue = keyEntry;
		}
    	
    	return maxValue;
    }
    
    /**
     * 
     * @param milli
     */
    public static void waitInMillisd(int milli){
    	try {				
			Thread.sleep(milli);
		} catch (InterruptedException e) {
		
			e.printStackTrace();
		}
    }
    
    public static void waitTilKeyPressed(){
    	
    }
   
    public static ArrayList<String> readTxtFiles(File file){
    	ArrayList<String> resultat = new ArrayList<String>();
    	Path path = Paths.get(file.getPath());
    	String line;
    	try (Scanner scanner =  new Scanner(path, ENCODING.name())){
    	      while (scanner.hasNextLine()){
    	    	  line = scanner.nextLine();
    	    	  // # for instance
//    	    	  if(!line.startsWith(comString))
    	    		  //process each line in some way
    	    		  resultat.add(line);
    	      }
    	      
    	      scanner.close();
    	    }
    		catch (Exception e){
    			System.out.println("[Toolz.readTxtFiles] - Exception "+ e.getMessage());
    	     }
    	
    	
    	return resultat;
    	
    }

  

}

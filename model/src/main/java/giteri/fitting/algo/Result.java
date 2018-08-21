package giteri.fitting.algo;

import giteri.fitting.parameters.IModelParameter;
import giteri.tool.math.Toolz;

import java.util.*;

/** Classe contenant les résultats d'une configuration particulière -
 * Mais contenant chacun des REPETITIONS sur cette config.
 *
 */
public class Result {
    // Provider associé a une string définissant sa valeur (GetPossibleValue() appelé sur une config du provider)
    // <Provider.nameString(), provider.getActualValue()>
    private Map<String,String> kvProviderValues;

    // liste des scores obtenus; score direct de distance entre deux réseaux
    private List<Double> score;

    // Liste de properties as string pour les config
    List<String> propertiesAsString;

    // TODO structure quelconque pour les bornes de fin de simulation
    // huhum pas utilisé pour le moment
   List<Double> nextStepScore;

    Result(){
        kvProviderValues = new Hashtable<String, String>();
        score = new ArrayList<>();
        propertiesAsString = new ArrayList<>();
        nextStepScore = new ArrayList<>();
    }

    /** Constructeur qui prend en param les providers contenant la configuration courante
     *
     * @param providers de la configuration courante.
     */
    Result(Collection<IModelParameter<?>> providers){
        this();
        for (IModelParameter<?> provider : providers)
            kvProviderValues.put(provider.nameString(), provider.getActualValue());
    }

    /** Ajout du score a la fin d'un run pour la config
     *
     * @param score double
     */
    public void addScore(double score){
        this.score.add(score);
    }

    /** ajout du toString sur le networkProperties. Seul l'ordre d'ajout
     * dans la liste fait la corrélation avec le score obtenu par cette propriété.
     * Pas top.
     *
     * @param property
     */
    public void addProperties(String property){
        propertiesAsString.add(property);
    }

    /** Obtient la string de la configuration associée aux scores.
     * Ensemble des providers et de leur getActualValue()
     *
     * @return
     */
    public String getCurrentConfig(){
        String result="";
        for (String s : kvProviderValues.keySet())
            result += "Provider: " + s + "Values :" + kvProviderValues.get(s);

        return result;
    }

    public double getAvgScore(){
        return Toolz.getAvg(score);
    }

    public double getLastScore(){
        if(score.size()> 0)
            return score.get(score.size() - 1);
        else
            return -1;
    }

    public List<Double> getScores(){
        return this.score;
    }

}

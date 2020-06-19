package giteri.fitting.algo;

import giteri.fitting.parameters.IModelParameter;
import giteri.run.configurator.Configurator;
import giteri.tool.math.Toolz;
import giteri.run.configurator.Configurator.NetworkAttribType;
import java.util.*;

/** Classe contenant les résultats d'une configuration particulière -
 * Mais contenant chacun des REPETITIONS sur cette config.
 * <=> Une classe Result par RUN.
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

    // <k:repetition v:<k:attrib,v:value>>
    Map<Integer, Map<Configurator.NetworkAttribType, Double>> detailledScore;
    Map<Integer,Map<Configurator.NetworkAttribType, Double>> detailledProp;

    /** Constructeur sans paramètre.
     *
     */
    public Result(){
        kvProviderValues = new Hashtable<>();
        score = new ArrayList<>();
        propertiesAsString = new ArrayList<>();
        detailledScore = new HashMap<>();
        detailledProp = new HashMap<>();
    }

    /** Constructeur qui prend en param les providers contenant la configuration courante
     *
     * Proba Diffusion -> ADLKDGRDMNTLKHA=0.1:ADLKDGRDMMT=0.0:ADLKDGRDMMNSPNTLK=0.0:RMLKDGRDMMNSPLK=0.0:
     * ADLKDGRDMMNIFNTLK=0.0:RMLKDGRDMGLLK=0.0:RMLKDGRDMMNIFLK=0.0:
     *
     * @param providers de la configuration courante.
     */
    public Result(Collection<IModelParameter<?>> providers){
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

    /** Ajout du score a la fin d'un run pour la config
     *
     * @param score double
     */
    public void addScore(int repetition, Configurator.NetworkAttribType attrib, double value, double score){
        Map<NetworkAttribType, Double> kvValeurs;
        Map<NetworkAttribType, Double> kvScores;
        if(detailledScore.containsKey(repetition)){
            kvValeurs = detailledProp.get(repetition);
            kvScores = detailledScore.get(repetition);
        }else {
            kvValeurs = new HashMap<>();
            kvScores = new HashMap<>();
        }
        detailledProp.put(repetition, kvValeurs);
        detailledScore.put(repetition, kvScores);
        kvScores.put(attrib,score);kvValeurs.put(attrib,value);
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
    public List<String> getCurrentConfig(){
        List<String> result= new ArrayList<>();
        for (String s : kvProviderValues.keySet())
            result.add("Provider: " + s + "Values :" + kvProviderValues.get(s));
        return result;
    }

    public double getAvgScore(){
        return Toolz.getAvg(score);
    }

    /** L'écart type des scores des répétitions au sein du RUN
     *
     * @return
     */
    public double getScoreStdDvt() {
        return Toolz.getDeviation(score, Optional.empty());
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

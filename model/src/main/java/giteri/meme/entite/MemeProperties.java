package giteri.meme.entite;
import giteri.fitting.algo.IExplorationMethod;
import giteri.fitting.parameters.IModelParameter;
import giteri.run.configurator.Configurator;
import giteri.run.interfaces.Interfaces;
import giteri.run.interfaces.Interfaces.IUnitOfTransfer;
import giteri.tool.math.Toolz;
import giteri.tool.objects.ObjectRef;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import java.util.*;

/**
 * Classe qui va résumer l'état en terme de meme d'une simulation.
 * Mise à jour par l'entité handler
 *
 */
public class MemeProperties {

    //region properties & constructeur

    // iUnitOfTransfers sur la map
    List<IUnitOfTransfer> iUnitOfTransfers;

    // Nombre d'appel depuis le début de la simu
    private Map<IUnitOfTransfer, Integer> nbActivationByMemes;
    private Map<IUnitOfTransfer, Integer> nbActivationByCouple;

    // Compte sur les X derniers appels des memes
    Map<IUnitOfTransfer, Integer> countOfLastMemeActivation;

    // k:meme v:nb entity with this meme
    Map<IUnitOfTransfer, Integer> countOfEntitiesHavingMeme;

    // K V
   public Map<IUnitOfTransfer, Integer> degreeMeanOfIOT;

    // Sur les 100 dernières actions, quel meme a été appelé
    final CircularFifoQueue<IUnitOfTransfer> lastHundredActionDone;

    Map<IUnitOfTransfer, Integer> KVDegreeByIOT ;

    List<Integer> lastFailActionTried;

    // Combinaison de meme disponible
    Map<Integer, ArrayList<IUnitOfTransfer>> memeCombinaisonFittingAvailable;

    //Structure pour information sur les flux d'entité entre les CA
    // k:CoupleMeme+CoupleMeme v:Integer d'index
    Map<String, Integer> kvCoupleTransitionIndex;
    // Count du nombre de transition entre couple, indexer par rapport a kvCoupleTransitionIndex
    Map<Integer, Integer> kvCoupleTransCount;

    int cptActionRmvFail = 0, cptActionAddFail = 0;

     MemeProperties(){
        nbActivationByMemes = new Hashtable<>();
        nbActivationByCouple = new Hashtable<>();
        countOfLastMemeActivation = new Hashtable<>();
        countOfEntitiesHavingMeme = new Hashtable<>();
        lastHundredActionDone = new CircularFifoQueue<>(Configurator.sizeOfCircularForLastActionDone);
        lastFailActionTried = new ArrayList<>(); KVDegreeByIOT = new HashMap<>();
        degreeMeanOfIOT = new Hashtable<>();
        kvCoupleTransitionIndex = new Hashtable<>();
        kvCoupleTransCount = new Hashtable<>();
    }

    public void clear(){
        nbActivationByMemes.clear();
        nbActivationByCouple.clear();
        countOfLastMemeActivation.clear();
        lastHundredActionDone.clear();
        countOfEntitiesHavingMeme.clear();
        lastFailActionTried.clear();
    }

    //endregion

    /** Appelée a chaque action effectuée
     * mise à jour des listes de possession de meme et des dernier meme jouée,
     * +
     *  renvoi une list de string composée de ce qu'il faut output si on veut suivre l'évolution des ratio de fail etc.
     *
     * @param memeApply
     * @param entiteIndex
     * @param message
     * @param cptModulo
     * @return Une liste décrivant les échecs en détails, null sinon.
     */
    List<String> updateActionCount(Meme memeApply, IUnitOfTransfer fromCouple, int entiteIndex, String message, int cptModulo){
        // SUCCES: En cas de réussite de l'action
        if (memeApply != null && !message.contains("Nope"))
        {
            Interfaces.IUnitOfTransfer elementRemoveOfCircular = null;
            Toolz.addCountToElementInHashArray(nbActivationByMemes, memeApply, 1);
            Toolz.addCountToElementInHashArray(nbActivationByCouple, fromCouple, 1);
            synchronized (lastHundredActionDone) {
                // partie last twenty
                if (lastHundredActionDone.size() == lastHundredActionDone.maxSize()) {
                    elementRemoveOfCircular = lastHundredActionDone.poll();
                    Toolz.removeCountToElementInHashArray(countOfLastMemeActivation, elementRemoveOfCircular, 1);
                }

                lastHundredActionDone.add(fromCouple);
            }

            Toolz.addCountToElementInHashArray(countOfLastMemeActivation, fromCouple, 1);
        }
        // ECHEC: Dans le cas ou il n'y a pas de meme apply, c'est a dire que l'action d'application du meme à échouée.
        // Ne contient que de l'affichage et erciture.
        else if (Configurator.displayLogRatioLogFailOverFail || Configurator.displayLogRatioLogFailOverSuccess )
        {
            int nbWin = 0;
            if(Configurator.displayLogRatioLogFailOverSuccess)
                for (Integer winTimes : nbActivationByMemes.values())
                    nbWin += winTimes;

            if(Configurator.displayLogRatioLogFailOverFail)
                if (message.contains("RMLK")) {
                    cptActionRmvFail++;
                    lastFailActionTried.add(-1);
                }
                else if (message.contains("ADLK")) {
                    cptActionAddFail++;
                    lastFailActionTried.add(+1);
                }
            return Arrays.asList(
                    "Iteration-; "+ cptModulo,
                    ";Ratio fail Rmv/Add -;" + (Configurator.displayLogRatioLogFailOverFail ? ((double) cptActionRmvFail / cptActionAddFail) : " NC"),
                    ";Ratio Fail/success -;" + (Configurator.displayLogRatioLogFailOverSuccess ? ((double) (cptActionRmvFail + cptActionAddFail) / nbWin) : "NC"),
                    ";Aucune action réalisée par l'entité- " + entiteIndex,
                    "Message- " + message);
        }

        return null;
    }

    /** Check des derniers echecs sur une fenetre glissante;
     *
     * @return la somme des derniers fail, rmv étant -1, add +1
     */
    int lastFailAction(ObjectRef<Integer> nbAction){
        nbAction.setValue(lastFailActionTried.size());
        int sumOfFail = lastFailActionTried.stream().reduce(0,Integer::sum);
        lastFailActionTried.clear();
        return sumOfFail;
    }

    /** Mise a jour de la table du nombre de possession par meme.
     *
     * @param meme Le meme qui a recu ou perdu une entité associée
     * @param added Si true, une entité a recu le mail, sinon elle l'a perdu
     */
    void updateMemePossession(IUnitOfTransfer meme, boolean added ){
        if(added)
            Toolz.addCountToElementInHashArray(countOfEntitiesHavingMeme,meme,1);
        else
            Toolz.removeCountToElementInHashArray(countOfEntitiesHavingMeme,meme,1);
    }

    // region Ecriture dans fichier

    /** Header pour le fichier
     *
     * @param param
     * @param forRepetition si pour répetition, valeur de la répétition, sinon, celui des répétitions du runs
     * @return
     */
    public String getStringHeaderMemeDetail(List<IModelParameter<?>> param, boolean forRepetition){
        String header = "Run-Rep";
        for (IModelParameter<?> model :param)
            header += ";" + model.nameString();

        // CALCUL DES INDICATEURS A ECRIRE

        // La liste des memes courant devrait etre a jour, apply fait avant dans la fitting classe
        for (Interfaces.IUnitOfTransfer meme: iUnitOfTransfers) {
            header += ";Meme[";
            header += meme.toFourCharString() +":";
            header += "]-nbEntiteOwning";
            if(forRepetition)
                header += ";SD";
            header += ";last X appli."; // Nombre / X + pourcentage
            if(forRepetition)
                header += ";SD";
        }

        return header;
    }

    /**
     *
     * @param param
     * @param memeCombinaisonFittingAvailable
     * @param forRepetition
     * @return
     */
    public String getStringHeaderCombinaison(List<IModelParameter<?>> param, Map<Integer, ArrayList<Meme>> memeCombinaisonFittingAvailable,boolean forRepetition){
        String header = "Run-Rep";
        for (IModelParameter<?> model :param)
            header += ";" + model.nameString();

        // combinaison de meme présent sur le run, classé par type d'action
        Hashtable<Configurator.TypeOfUOT, ArrayList<IUnitOfTransfer>> memesByCategory = new Hashtable<>();
        for (Interfaces.IUnitOfTransfer meme: iUnitOfTransfers)
            Toolz.addElementInHashArray(memesByCategory,meme.getActionType(),meme);
       // memeCombinaisonFittingAvailable = this.getMemeAvailable(Configurator.FittingBehavior.simpleAndComplex, Optional.of(memesByCategory));

        // La liste des memes courant devrait etre a jour, apply fait avant dans la fitting classe
        for (Integer i:memeCombinaisonFittingAvailable.keySet()) {
            header += ";Meme[";
            for (Meme meme:memeCombinaisonFittingAvailable.get(i)) {
                header += meme.toFourCharString() +":";
            }

            header += "]-nbEntiteOwning";
            if(forRepetition)
                header += ";SD";
            header += ";last X appli."; // Nombre / X + pourcentage

        }

        return header;
    }

    /** création du header pour le fichier de detail concernant les memes sur l
     * la simulation.
     *  Ligne 1: meme
     *  ligne 2: possession;nbActi;last nbActi
     *
     * @param translation
     * @param numeroRun
     * @param numeroRep
     * @param explorator
     * @return
     */
    public List<String> getHeaderToWriteMemeDetails(Map<IUnitOfTransfer, String> translation , int numeroRun, int numeroRep, IExplorationMethod explorator){
        List<String> lines = new ArrayList<>();
        String toWrite ="";
        String line2= "";
        // toWrite += numeroRun + "-" + numeroRep;
        String memeLisible;

        // Config du fitting
//        for (IModelParameter<?> model : explorator.getModelParameterSet())
//            toWrite += ";" + model.valueString();
        lines.add(toWrite);
        toWrite = "";
        for (IUnitOfTransfer meme : translation.keySet()) {
            toWrite += translation.get(meme);
            line2 += "#possession;";
            toWrite += ";";
            line2 += "nbAppliFromStart;";
            toWrite += ";";
            line2 += "nbAppliLast100;";
            toWrite += ";";
        }

        lines.add(toWrite);
        lines.add(line2);

        return lines;
    }

    /** Ecrire les détails en eu meme sur les UOT
     *
     * @param translation
     * @return
     */
    public String getStringToWriteMemeDetails(Map<IUnitOfTransfer, String> translation){
        String toWrite ="";
        for (IUnitOfTransfer<CoupleMeme> cMeme : translation.keySet()) {
            toWrite += countOfEntitiesHavingMeme.get(cMeme) + ";";
            toWrite += nbActivationByCouple.get(cMeme) + ";";
            toWrite += countOfLastMemeActivation.get(cMeme) + ";";
        }

        toWrite = String.valueOf(toWrite.subSequence(0, toWrite.length()-1));

        return toWrite;
    }

    /** a ecrire a la fin du fichier pour des formules de calcul excel
     *
     * @param translation
     * @return
     */
    public List<String> getCloserToWriteMemeDetails(Map<IUnitOfTransfer, String> translation){//} , int numeroRun, int numeroRep, IExplorationMethod explorator){

        List<String> retur= new ArrayList<>();
        String toWrite ="";
        String line2 ="";

        char a = 'a';
        String base = "";
        String colonne;
        for (IUnitOfTransfer meme : translation.keySet()) {
            for (int i = 0; i < 3; i++) {
                colonne = base + a;
                a++;
                if(a == 'z'){
                    a = 'a';
                    base ="a";
                }

                toWrite += "=AVERAGE("+colonne+"4:INDIRECT(\""+colonne+"\"&(ROW()-1)));";
                line2 += "=STDEV("+colonne+"4:INDIRECT(\""+colonne+"\"&(ROW()-2)));";
            }

        }

        toWrite = String.valueOf(toWrite.subSequence(0, toWrite.length()-1));
        line2 = String.valueOf(line2.subSequence(0, line2.length()-1));

        retur.add(toWrite);
        retur.add(line2);
        return retur;
    }

    // endregion

    Map<IUnitOfTransfer, Integer> getNbActivationByMemes() {
        return nbActivationByMemes;
    }

    Map<IUnitOfTransfer, Integer> getNbActivationByCouple() {
        return nbActivationByCouple;
    }

}

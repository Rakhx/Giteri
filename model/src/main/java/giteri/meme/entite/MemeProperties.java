package giteri.meme.entite;

import giteri.fitting.algo.IExplorationMethod;
import giteri.fitting.parameters.IModelParameter;
import giteri.meme.event.IMemeAvailableListener;
import giteri.meme.event.MemeAvailableEvent;
import giteri.run.configurator.Configurator;
import giteri.tool.math.Toolz;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.File;
import java.util.*;

/** Classe qui va résumer l'état en terme de meme d'une simulation.
 * Mise à jour par l'entité handler
 *
 */
public class MemeProperties{

    // Meme actif sur la map
    public List<Meme> memeOnMap;

    // Nombre d'appel depuis le début de la simu
    public Map<Meme, Integer> nbActivationByMemes;

    public Map<Meme, Integer> countOfLastMemeActivation;

    // Sur les 100 dernières actions, quel meme a été appelé
    public CircularFifoQueue<Meme> lastHundredActionDone;
    public int sizeOfCircularQueue = 100;

    // Combinaison de meme disponible
    public Map<Integer, ArrayList<Meme>> memeCombinaisonFittingAvailable;

    public int cptActionRmvFail = 0, cptActionAddFail = 0;

    public MemeProperties(){
        nbActivationByMemes = new Hashtable<>();
        countOfLastMemeActivation = new Hashtable<>();
        lastHundredActionDone = new CircularFifoQueue<>(sizeOfCircularQueue);

    }

    public void clear(){
        nbActivationByMemes.clear();
        countOfLastMemeActivation.clear();
        lastHundredActionDone.clear();
    }

    public List<String> updateActionCount(Meme memeApply, int entiteIndex, String message, int cptModulo){
        if (memeApply != null)
        {
            Meme elementRemoveOfCircular = null;
            Toolz.addCountToElementInHashArray(nbActivationByMemes, memeApply, 1);

            // partie last twenty
            if(lastHundredActionDone.size() == lastHundredActionDone.maxSize())
            {
                elementRemoveOfCircular = lastHundredActionDone.poll();
                Toolz.removeCountToElementInHashArray(countOfLastMemeActivation, elementRemoveOfCircular, 1);
            }

            lastHundredActionDone.add(memeApply);
            Toolz.addCountToElementInHashArray(countOfLastMemeActivation, memeApply, 1);
        }

        // Dans le cas ou il n'y a pas de meme apply, c'est a dire que l'action d'application du meme a échouée.
        else if (Configurator.displayLogRatioLogFailOverFail || Configurator.displayLogRatioLogFailOverSuccess )
        {
            int nbWin = 0;
            if(Configurator.displayLogRatioLogFailOverSuccess)
                for (Integer winTimes : nbActivationByMemes.values())
                    nbWin += winTimes;

            if(Configurator.displayLogRatioLogFailOverFail)
                if (message.contains("RMLK"))
                    cptActionRmvFail++;
                else if (message.contains("ADLK"))
                    cptActionAddFail++;

            return Arrays.asList(
                    "Iteration- "+ cptModulo,
                    "Ratio Rmv/Add -" + (Configurator.displayLogRatioLogFailOverFail? ((double) cptActionRmvFail / cptActionAddFail) : " NC"),
                    "Ratio Fail/success -" + (Configurator.displayLogRatioLogFailOverSuccess ? ((double) (cptActionRmvFail + cptActionAddFail) / nbWin) : "NC"),
                    "Aucune action réalisée par l'entité- " + entiteIndex,
                    "Message- " + message);
        }

        return null;
    }

    // region Ecriture dans fichier

    /**
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
        for (Meme meme:memeOnMap) {
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

    public String getStringHeaderCombinaison(List<IModelParameter<?>> param, Map<Integer, ArrayList<Meme>> memeCombinaisonFittingAvailable,boolean forRepetition){
        String header = "Run-Rep";
        for (IModelParameter<?> model :param)
            header += ";" + model.nameString();

        // combinaison de meme présent sur le run, classé par type d'action
        Hashtable<Configurator.ActionType, ArrayList<Meme>> memesByCategory = new Hashtable<>();
        for (Meme meme: memeOnMap)
            Toolz.addElementInHashArray(memesByCategory,meme.getAction().getActionType(),meme);
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

    public String getStringToWriteMemeDetails(List<Entite> entitesActive, int numeroRun, int numeroRep, IExplorationMethod explorator){
        String toWrite ="";
        toWrite += numeroRun + "-" + numeroRep;

        // Config du fitting
        for (IModelParameter<?> model : explorator.getModelParameterSet())
            toWrite += ";" + model.valueString();

        // detail sur les memes
        List<Meme> combinaisonLookedAt;
        int nbEntitesOwning;
        for (Integer i: memeCombinaisonFittingAvailable.keySet()){
            nbEntitesOwning = 0;
            combinaisonLookedAt = memeCombinaisonFittingAvailable.get(i);
            for (Entite entite: entitesActive) {
                if(entite.getMyMemes().containsAll(combinaisonLookedAt)){
                    nbEntitesOwning++;
                }
            }

            toWrite += ";Meme[";
            for (Meme meme:memeCombinaisonFittingAvailable.get(i)) {
                toWrite += meme.toFourCharString() +":";
            }

            toWrite += "]-" + nbEntitesOwning;
        }

        return toWrite;
    }

    public String getStringToWriteMemeCombinaison(int numeroRun, int numeroRep,  IExplorationMethod explorator){
        return null;
    }


    public Map<Meme, Integer> getNbActivationByMemes() {
        return nbActivationByMemes;
    }

    public Map<Meme, Integer> getCountOfLastMemeActivation() {
        return countOfLastMemeActivation;
    }

    public CircularFifoQueue<Meme> getLastHundredActionDone() {
        return lastHundredActionDone;
    }


    // endregion
}

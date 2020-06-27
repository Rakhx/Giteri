package giteri.run.configurator;

import giteri.run.interfaces.Interfaces.IOpenMoleParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Version couple meme // caste
 */
public class CasteOpenMoleParameter implements IOpenMoleParameter {

    public int activation,nbMemes,maxMeme;
    public List<Double> probaPropa;

    public CasteOpenMoleParameter(int activationCode, int nbMeme, int maxCombinaison, List<Double> probas){
        activation = activationCode; nbMemes = nbMeme; maxMeme = maxCombinaison; probaPropa = probas;
    }

   // public CasteOpenMoleParameter()

    /**
     * Le numero d'activator. Le Max défini le nombre de combinaison max, et le Nb le nombre
     * d'élément qui constitue la combinaison considérée.
     * Si choix de 3 meme sur 10 max possible, le nombre de combinaison est de 10!/7!
     *
     * @param activator    Xeme  combinaison" effectivement choisi. i.e. la 56eme
     * @param nbActivator  3
     * @param maxactivator 10
     * @return un tableau de boolean a true pour les slots activé.
     */
    public Map<Integer, Integer> getActionActivation(int activator, int nbActivator, int maxactivator) {
        boolean debug = Configurator.debugCouple && !Configurator.fullSilent;

       if(debug) System.out.println("[ParamOpen.getActionActi()] activ " + activator+ " nbActi "+nbActivator + " maxActi " + maxactivator);
        boolean[] resultat = new boolean[maxactivator];
        int currentPlace, place;

        // Key: les memes i,j,k d'enabler Value: Leur index dans le tableau de boolean
        Map<Integer, Integer> kvIndexerIndex = new HashMap<>(nbActivator);
        Map<Integer, Integer> kvLastStarter = new HashMap<>(nbActivator);

        // startpoint [true, true, true, false, false false false ... ]
        for (int i = 0; i < nbActivator; i++) {
            // Le meme 0 est active le slot 2 du tableau de boolean, etc etc.
            kvIndexerIndex.put(i, nbActivator - 1 - i);
            kvLastStarter.put(i, nbActivator - 1 - i);
            // Symétrisé afin de pouvoir faire avancer le 0eme meme qui est au bout du tableau
            // [meme2,meme1,meme0, rien, rien, rien, rien ]
            //  [true, true, true, false, false false false ... ]
        }

        do {
            // (1) on commence a essayer d'augmenter i, puis
            // (2) on se retrouve ici si il n'a pas été possible d'augmenter facilement meme 0eme
            for (Integer index : kvIndexerIndex.keySet()) {
                // (1) La place alloué au meme correspondant a l'index i si for imbriqué
                currentPlace = kvIndexerIndex.get(index);
                // si le dudy s'est pas retiré du game
                if (currentPlace != -1) {
                    // (1) Par exemple si 0eme meme, on essaye d'avance de 1 son slot
                    // (2) 1eme meme, on avance de 1 son slot
                    if (currentPlace + 1 < maxactivator - index) {
                        kvIndexerIndex.put(index, ++currentPlace);
                        break;
                    }
                    // si on peut pas avancer de 1 mais que le lastStarter + 1 est possible
                    // (1) Sinon il va falloir aussi incrementer les autres
                    // (2) s'il est a bout, rebelotte.
                    else if (kvLastStarter.get(index) + 1 < maxactivator - index) {
                        place = kvLastStarter.get(index) + 1;
                        kvIndexerIndex.put(index, place);
                        kvLastStarter.put(index, place);
                        for(int i=index-1; i>= 0;i--){
                            kvIndexerIndex.put(i, ++place);
                            kvLastStarter.put(i, place);
                        }

                        // (1) pas de break car le suivant va devoir augmenter son index aussi
                    }
                    // (1) Si aucun des deux, c'est qu'on est au bout du bout
                    else {
                        // se retire du game
                        kvIndexerIndex.put(index, -1);
                    }
                }
            }
        } while (--activator > 0);

        String rez = "elements: ";
      if(debug) {
          for (Integer index : kvIndexerIndex.keySet()) {
              rez += "-" + kvIndexerIndex.get(index);
          }

          System.out.println(rez);
      }

      for (int i = 0; i < resultat.length; i++) {
            resultat[i] = false;
        }
        for (Integer integer : kvIndexerIndex.values()) {
            resultat[integer] = true;
        }

        return kvIndexerIndex;
    }

}

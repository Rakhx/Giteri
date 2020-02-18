package giteri.run.configurator;

import giteri.run.interfaces.Interfaces;
import giteri.run.interfaces.Interfaces.IOpenMoleParameter;
import giteri.tool.math.Toolz;

import java.util.*;

/**
 * Version couple meme // caste
 */
public class CasteOpenMoleParameter implements IOpenMoleParameter {
    // Code d'activation, nombre de meme voulu activé, nombre de meme totaux
    public int addActi, addNb, rmvActi, rmvNb;
    // Obligation de passer par les setter afin de générer un tableau d'activation de la bonne taille
    private int addTotal, rmvTotal;

    public boolean[] addActivation;
    public boolean[] rmvActivation;
    public List<Double> probaPropa;

    /**
     * Si lancé depuis l'IHM, nécessite des valeurs pars défault
     *
     * @param addA Ajout Activation ( 110 )
     * @param addN Ajout numbre ( 3)
     * @param addT Ajout Total (10)
     */
    public CasteOpenMoleParameter(int addA, int addN, int addT, int rmvA, int rmvN, int rmvT) {
        addActi = addA;
        addNb = addN;
        setAddTotal(addT);
        rmvActi = rmvA;
        rmvNb = rmvN;
        setRmvTotal(rmvT);
        probaPropa = new ArrayList<>();

        // Dans le cas d'un appelle par Jar, probaProba est réécrit apres le constructeur

        for (int i = 0; i < addNb * rmvNb; i++) {
            probaPropa.add(1.);
        }

      //  probaPropa.clear();
      //  probaPropa.addAll(Arrays.asList(.1,.2,.3,.4));


        init();
    }

    /**
     * Transcription des 6 parametres en deux tableaux a deux dimensions de boolean
     */
    public void init() {

        // addActivation = getActionActivation(addActi,addNb, addTotal);
        addActivation = getActionActivation2(addActi, addNb, addTotal);
        rmvActivation = getActionActivation2(rmvActi, rmvNb, rmvTotal);
    }


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
    public boolean[] getActionActivation(int activator, int nbActivator, int maxactivator) {
        System.out.println("CALL: " + activator);
        boolean[] resultat = new boolean[maxactivator];
        boolean again = true;
        int nbCombinaison = Toolz.getLittleFactorial(maxactivator) / (Toolz.getLittleFactorial(maxactivator - nbActivator)
                * Toolz.getLittleFactorial(nbActivator));

        boolean ok = true;
        boolean hadMoved = false;


        // position de base des activators, i<j<k
        // int i= 1, j=2,k = 3;
        for (int i = 0; i < maxactivator; i++) {
            for (int j = i + 1; j < maxactivator; j++) {
                for (int k = j + 1; k < maxactivator; k++) {
                    if (activator > 0) {
                        activator--;
                    } else if (activator == 0) {
                        System.out.println("ijk- " + i + j + k);
                        for (int i1 = 0; i1 < maxactivator; i1++) {
                            if ((i == i1) || (j == i1) || (k == i1))
                                resultat[i1] = true;
                            else
                                resultat[i1] = false;
                        }

                        again = false;
                    }

                    if (!again)
                        break;

                }
                if (!again)
                    break;
            }

            if (!again)
                break;
        }

        String res = "[";
        for (int i = 0; i < resultat.length; i++) {
            res += ";";
            res += resultat[i] ? "1" : "0";
        }
        res += "]";

        System.out.println(res);
        return resultat;
    }


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
    public boolean[] getActionActivation2(int activator, int nbActivator, int maxactivator) {
        System.out.println("CALL: " + activator);
        boolean[] resultat = new boolean[maxactivator];
        boolean again = true;
        int nbCombinaison = Toolz.getLittleFactorial(maxactivator) / (Toolz.getLittleFactorial(maxactivator - nbActivator)
                * Toolz.getLittleFactorial(nbActivator));

        boolean ok = true;
        boolean hadMoved = false;

        int currentPlace, offset, lastStarter;


        // Key: les memes i,j,k d'enabler Value: Leur index dans le tableau de boolean
        Map<Integer, Integer> kvIndexerIndex = new HashMap<Integer, Integer>(nbActivator);
        Map<Integer, Integer> kvLastStarter = new HashMap<Integer, Integer>(nbActivator);


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
            hadMoved = true;
            offset = -1;
            // (1) on commence a essayer d'augmenter i, puis
            // (2) on se retrouve ici si il n'a pas été possible d'augmenter facilement meme 0eme
            for (Integer index : kvIndexerIndex.keySet()) {
                // (1) La place alloué au meme correspondant a l'index i si for imbriqué
                currentPlace = kvIndexerIndex.get(index);
                offset++;
                // si le dudy s'est pas retiré du game
                if (currentPlace != -1) {
                    // (1) Par exemple si 0eme meme, on essaye d'avance de 1 son slot
                    // (2) 1eme meme, on avance de 1 son slot
                    if (currentPlace + 1 < maxactivator - offset) {
                        kvIndexerIndex.put(index, ++currentPlace);
                        break;
                    }
                    // si on peut pas avancer de 1 mais que le lastStarter + 1 est possible
                    // (1) Sinon il va falloir aussi incrementer les autres
                    // (2) s'il est a bout, rebelotte.
                    else if (kvLastStarter.get(index) + 1 < maxactivator - offset) {
                        kvIndexerIndex.put(index, kvLastStarter.get(index) + 1);
                        kvLastStarter.put(index, kvLastStarter.get(index) + 1);
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


        System.out.println("ijk- " + kvIndexerIndex.get(0) + kvIndexerIndex.get(1) + kvIndexerIndex.get(2));

        for (int i = 0; i < resultat.length; i++) {
            resultat[i] = false;
        }
        for (Integer integer : kvIndexerIndex.values()) {
            resultat[integer] = true;
        }

        String res = "[";
        for (int i = 0; i < resultat.length; i++) {
            res += ";";
            res += resultat[i] ? "1" : "0";
        }
        res += "]";

        System.out.println(res);
        return resultat;
    }


    public int getAddTotal() {
        return addTotal;
    }

    public void setAddTotal(int addTotal) {
        this.addTotal = addTotal;
        addActivation = new boolean[addTotal];
    }

    public int getRmvTotal() {
        return rmvTotal;
    }

    public void setRmvTotal(int rmvTotal) {
        this.rmvTotal = rmvTotal;
        rmvActivation = new boolean[rmvTotal];
    }


}

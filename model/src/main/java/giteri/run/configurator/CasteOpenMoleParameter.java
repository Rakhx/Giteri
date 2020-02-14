package giteri.run.configurator;

import giteri.run.interfaces.Interfaces;
import giteri.run.interfaces.Interfaces.IOpenMoleParameter;
import giteri.tool.math.Toolz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Version couple meme // caste
 *
 */
public class CasteOpenMoleParameter implements IOpenMoleParameter {
    // Code d'activation, nombre de meme voulu activé, nombre de meme totaux
    public int addActi, addNb, rmvActi, rmvNb;
    // Obligation de passer par les setter afin de générer un tableau d'activation de la bonne taille
    private int addTotal, rmvTotal;

    public boolean[] addActivation;
    public boolean[] rmvActivation;
    public List<Double> probaPropa;

    /** Si lancé depuis l'IHM, nécessite des valeurs pars défault
     *
     * @param addA Ajout Activation ( 110 )
     * @param addN Ajout numbre ( 3)
     * @param addT Ajout Total (10)
      */
    public CasteOpenMoleParameter(int addA, int addN, int addT, int rmvA, int rmvN, int rmvT){
        addActi = addA;
        addNb = addN;
        setAddTotal(addT);
        rmvActi = rmvA;
        rmvNb = rmvN;
        setRmvTotal(rmvT);
        probaPropa = new ArrayList<>();

        // Dans le cas d'un appelle par Jar, probaProba est réécrit apres le constructeur
        for (int i = 0; i < addNb*rmvNb; i++) {
            probaPropa.add(1.);
        }

        init();
    }

    /** Transcription des 6 parametres en deux tableaux a deux dimensions de boolean
     *
     */
    public void init(){
        addActivation = getActionActivation(addActi,addNb, addTotal);
        rmvActivation = getActionActivation(rmvActi, rmvNb, rmvTotal);
    }



    /** Le numero d'activator. Le Max défini le nombre de combinaison max, et le Nb le nombre
     * d'élément qui constitue la combinaison considérée.
     * Si choix de 3 meme sur 10 max possible, le nombre de combinaison est de 10!/7!
     *
     *
     * @param activator Xeme  combinaison" effectivement choisi. i.e. la 56eme
     * @param nbActivator 3
     * @param maxactivator 10
     * @return
     */
    public boolean[] getActionActivation(int activator, int nbActivator, int maxactivator){
        System.out.println("CALL: " + activator);
        boolean[] resultat = new boolean[maxactivator];
        boolean again = true;
        int nbCombinaison = Toolz.getLittleFactorial(maxactivator) / (Toolz.getLittleFactorial(maxactivator-nbActivator)
                *Toolz.getLittleFactorial(nbActivator));
        // position de base des activators, i<j<k
        // int i= 1, j=2,k = 3;
        for (int i = 0; i < maxactivator; i++) {
            for (int j = i+1; j < maxactivator; j++) {
                for (int k = j+1; k < maxactivator ; k++) {
                    if(activator > 0) {
                        activator--;
                    }
                    else if(activator == 0){
                        System.out.println("ijk- " + i + j + k);
                        for (int i1 = 0; i1 < maxactivator; i1++) {
                            if(( i == i1) || (j == i1) || (k==i1))
                                resultat[i1] = true;
                            else
                                resultat[i1] = false;
                        }

                        again = false;
                    }

                    if(!again)
                        break;

                }
                if(!again)
                    break;
            }

            if(!again)
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

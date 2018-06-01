package giteri.run;


import java.io.Serializable;
import java.util.List;

/** Classe qui défini une configuration en entière
 * Ne possède aucune référence à des objets du systeme
 */
public class PluggedConfig implements Serializable {

    // Choix des actions disponibles et de leur probas
    // Fonction avec
    public void selectActions(int code, List<Double> probas){
        // les actions et leur probas
    }

    // Regrouper les setters d'attributs par type

    // choix sur les termes de propagations
    public void selectPropagation(){
        // A voir avec le fichier Configurator

    }

    // Choix sur l'algo de fitting, fin d'un run
    public void selectFitting(){
        // Type d'explorator

        // Nombre de run

        // condition d'arret

        // paramètre sur les conditions d'arrets

    }

    // Eventuellement les modes debugs des différentes classes
    public void debugModes(){}

    // Possiilité d'appliquer la config?


    // possibilité de visualiser la config dans l'IHM


    // Possibilité de sauvegarder la config, et de la remettre
    public void save(String name){ // or write the serialized class
         }

    public void load(String name){ // or with a file selecto
        }
}

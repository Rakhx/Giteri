package giteri.run.configurator;

import giteri.run.interfaces.Interfaces.IOpenMoleParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Version classique.
 * List<Activator> boolean, list<Proba> Double,
 *
 */
public class ClassicOpenMoleParameter implements IOpenMoleParameter {
    public List<Boolean> memeActication;
    public List<Double> memeProba;

    /** valeur par défault lorsque non préciser
     *
     */
    public ClassicOpenMoleParameter(){
        memeActication = new ArrayList<>();
        memeProba = new ArrayList<>();
        List<Integer> toConvert = new ArrayList<>(Arrays.asList(0,0,0,1,0,0,1,0,0,0,0,1,0));
        memeProba.addAll(Arrays.asList(
                0.9627720666747352,0.2559838797762375,0.7969203634094254,0.8580268727470562,0.3358543882264913,0.849136850072306,0.7667072298886954,0.18527085724031966,0.8605838516655864,0.743762583297914,0.5992868107380205,0.09960619627293377,0.6068812397689891
        ));
        // A vérifier
        memeActication.addAll(toConvert.stream().map(e -> e==1).collect(Collectors.toList()));
    }

}


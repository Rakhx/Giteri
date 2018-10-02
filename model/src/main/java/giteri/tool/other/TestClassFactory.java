package giteri.tool.other;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Classe qui va nous permettre de tester les performances de X classes
 *
 */
public class TestClassFactory {

    List<IDummy> bouclezNous;

    public TestClassFactory(List<IDummy> pickUs){
        bouclezNous = pickUs;
    }

    /** itere X fois en appelant la fonction infiniteMe de la classe
     *
     * @param nbIteration
     */
    public void letsGo(int nbIteration){
        StopWatchFactory watch =  StopWatchFactory.getInstance();
        for (IDummy nous : bouclezNous) {
           watch.addWatch("Main", nous.myOSix());
           watch.startWatch(nous.myOSix());
            for (int i = 0; i < nbIteration; i++) {
                nous.infiniteMe();
            }
            watch.stopWatch(nous.myOSix());
        }

        watch.publishResult();
    }




    public interface IDummy{
        void infiniteMe();
        String myOSix();

    }

    public class dummyNotSynchro implements IDummy{

        private Map<Integer, List<Integer>> nodesAndConnections;

        public String myOSix(){
            return "notSync";
        }

        public void infiniteMe(){

        }

    }
}

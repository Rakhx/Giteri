package giteri.tool.other;

import giteri.tool.math.Toolz;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/** Classe qui va nous permettre de tester les performances de X classes
 *
 */
public class TestClassFactory {

    public List<IDummy> bouclezNous;

    public TestClassFactory(){
        bouclezNous = new ArrayList<>();
    }
    public TestClassFactory(List<IDummy> pickUs){
        bouclezNous = pickUs;
    }

    /** itere X fois en appelant la fonction infiniteMe de la classe
     *
     * @param nbIteration
     */
    public void letsGo(int nbIteration){
        StopWatchFactory watch =  StopWatchFactory.getInstance();
        String name;
        String fnName;
        watch.startWatch("Main");
        for (IDummy nous : bouclezNous) {

            for (Function<Object, Object> function : nous.getFunctions()) {
                fnName = (function.equals(((AbstractDummy)nous).ajout) ? "add" :
                        (function.equals(((AbstractDummy)nous).lecture) ? "lecture" : "!addOrLecture"));

                watch.addWatch("",  fnName);
                watch.startWatch(fnName);

                name = nous.myOSix() + "-" + fnName;
                watch.addWatch(fnName,  name);
                watch.startWatch(name);

                for (int j = 0; j < 10; j++) {
                    for (int i = 0; i < nbIteration; i++) {
                        function.apply(0);
                    }
                }

                watch.stopWatch(fnName);
                watch.stopWatch(name);
            }
        }

        watch.stopWatch("Main");
        watch.publishResult();
    }

    public interface IDummy{
        String myOSix();
        <R,T> List<Function<R,T>> getFunctions();
        Map<Integer, List<Double>> getList();
        <T1, T2> void
        addElementInMap(Map<T1,List<T2>> table,T1 key, T2 value);
    }

    public abstract class AbstractDummy<R,T> implements IDummy{
        public List<Function> fntToTest = new ArrayList<>();
        Map<Integer, List<Double>> nodesAndConnections;

        public AbstractDummy(){
            fntToTest.add(ajout);
            fntToTest.add(lecture);
        }

        public String myOSix(){

            return this.getClass().toString().split("\\$")[this.getClass().toString().split("\\$").length-1];
        }

        public List<Function> getFunctions(){
            return fntToTest;
        }


        public Function<Integer, Double> ajout = (Integer input) -> {
            addElementInMap(getList(),input,(input*Math.PI));
            return 0.;
        };

        public Function<Integer, Double> lecture = (Integer input) -> {
            int nbElement = getList().keySet().size();
            Double randomElmt;
            for (int i = 0; i < nbElement; i = i + 5) {
                randomElmt = getList().get(Toolz.getRandomNumber(getList().size())).get(Toolz.getRandomNumber(10));
                randomElmt = 2.0;
                if(false)
                    break;
            }


            return 0.;
        };




    }


    public class dummyManuelSynchro extends AbstractDummy<Number,Number> {

        public dummyManuelSynchro() {
            nodesAndConnections = new Hashtable<>();
        }

        public <R,T> void addElementInMap(Map<R,List<T>> table,R key, T value){
            if(table.containsKey(key))
                table.get(key).add(value);
            else
                table.put(key, new ArrayList<T>(Arrays.asList(value)));
        }

        public Map<Integer, List<Double>> getList(){
            synchronized (nodesAndConnections){
                return nodesAndConnections;
            }
        }
    }

    public class dummyAutoSynchro extends AbstractDummy<Number, Number>{

        public dummyAutoSynchro() {
            nodesAndConnections = new ConcurrentHashMap<>();
        }

        public <R,T> void addElementInMap(Map<R,List<T>> table,R key, T value){
            if(table.containsKey(key))
                table.get(key).add(value);
            else
                table.put(key, new CopyOnWriteArrayList<T>(Arrays.asList(value)));
        }

        public Map<Integer, List<Double>> getList(){
                return nodesAndConnections;
        }
    }

}

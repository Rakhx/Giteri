package giteri.test;

import giteri.tool.math.Toolz;

import java.util.HashMap;
import java.util.Map;

public class RandomTest {
    public static void main(String[] args)  {

        HashMap<Integer, Double> map = new HashMap<>();
        for (int i = 0; i< 10; i++)
            map.put(i, Double.parseDouble(""+i));

        Map<Integer,Double> randomized = Toolz.shuffleHashmap(map, true);
        System.out.println(randomized);

        Map<Integer,Double> randomized2 = Toolz.shuffleHashmap(map, true);
        System.out.println(randomized2);

//       Mother imnot = new Son();
//       imnot.sayHello();

    }

}
 class Mother {
    public void sayHello(){
        System.out.println("Mother");
    }
}
 class Son extends Mother {
    public void sayHello(){
        System.out.println("Son");
    }

}
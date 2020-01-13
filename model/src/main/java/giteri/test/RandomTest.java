package giteri.test;

import giteri.run.configurator.Initializer;
import giteri.tool.math.Toolz;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RandomTest {
    public static void main(String[] args)  {

        Initializer balbla = new Initializer();
        for (int i = 0; i < 120; i++) {
            balbla.getActionActivation(i,3,10);
        }

        // FONCTION RANDOM
//        int ajout = 0;
//        int nbIteration = 0;
//        while(true){
//            ajout += Toolz.rollDice(.5)? 1 : 0;
//            nbIteration ++;
//
//            System.out.println((double)ajout/nbIteration);
//        }


//        for (int i = 0; i < 100; i++) {
//            System.out.println(VOILA(.5));
//        }



//        // Double mis dans un number
//        List<Number> listNombre;
//        listNombre = new ArrayList<>();
//        Double aDouble = 2.;
//        listNombre.add((Number)aDouble);
//        System.out.println(listNombre.get(0) instanceof Double ? ((Number) listNombre.get(0)) : "non");


//        // ref vs valeur
//        Double kiki = 4.;
//        if(true)
//            kiki = lolade();
//        System.out.println(kiki);


//        int rez1, rez2, rez3, succes = 0;
//        int nbIter = 10000000;
//        for (int i = 0; i < nbIter; i++) {
//            rez1 = Toolz.getRandomNumber(6) + 1;
//            rez2 = Toolz.getRandomNumber(6) + 1;
//            rez3 = Toolz.getRandomNumber(6) + 1;
//            if(rez1 == rez2 || rez3 == rez2 || rez1 == rez3)
//                succes++;
//        }
//
//        System.out.println((double)succes / nbIter);

        // OPTIONAL
//        Optional<Double> opt = Optional.of(new Double(3.));
//        changeRef(opt);
//        System.out.println(opt);

        // STREAM
        // - Transformation de list etc
//        List<String> list = new ArrayList<>(Arrays.asList("bla","bla","troiseimebla", "hihi", "hihi", "hihi", "hihi"));
//        String resultat;
//        resultat = list.stream().reduce("", (String::concat));
//        System.out.println(resultat);
//        list.stream().reduce((a,b)->  a.compareTo(b) > 0 ? a : b)
//                .ifPresent(System.out::println);
//
//        List<String> quantity =
//        list.stream().collect(
//                Collectors.groupingBy(String::toString,
//                Collectors.counting())
//        ).entrySet().stream().map(Object::toString).collect(Collectors.toList());
//
//        System.out.println(quantity);

        // BITWISE
//        bitwiseAdd(10,10);
//        add(10,18);

          // TEST DU SHUFFLE DE MAP DETERMINISTE ET NON
//        HashMap<Integer, Double> map = new HashMap<>();
//        for (int i = 0; i< 10; i++)
//            map.put(i, Double.parseDouble(""+i));
//
//        Map<Integer,Double> randomized = Toolz.shuffleHashmap(map, true);
//        System.out.println(randomized);
//
//        Map<Integer,Double> randomized2 = Toolz.shuffleHashmap(map, true);
//        System.out.println(randomized2);
    }

    static public int VOILA(double proba){
        return 1 + (int)((Math.log(1.- Toolz.getProba()))/(Math.log(1.-proba)));
    }


    static public Double lolade(){
        return  new Double(10.0);
    }

    static void changeRef(Optional<Double> changera){
//        changera.
        changera = Optional.of(new Double(5.1));
    }

    static int add(int x, int y)

    {

        int carry;

        while(y!=0)
        {
            carry = x & y;
            x = x ^ y;
            y = carry << 1;
        }

        return x;

    }
    public static void bitwiseMultiply(int n1, int n2) {
        int a = n1, b = n2, result=0;
        while (b != 0) // Iterate the loop till b==0
        {
            if ((b & 01) != 0) // Logical ANDing of the value of b with 01
            {
                result = result + a; // Update the result with the new value of a.
            }
            a <<= 1;              // Left shifting the value contained in 'a' by 1.
            b >>= 1;             // Right shifting the value contained in 'b' by 1.
        }
        System.out.println(result);
    }
    public static void bitwiseAdd(int n1, int n2) {
        int x = n1, y = n2;
        int xor, and, temp;
        and = x & y;
        xor = x ^ y;

        while (and != 0) {
            and <<= 1;
            temp = xor ^ and;
            and &= xor;
            xor = temp;
        }
        System.out.println(xor);
    }
}


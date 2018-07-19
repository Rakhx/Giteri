package giteri.test;

public class RandomTest {
    public static void main(String[] args)  {
       Mother imnot = new Son();
       imnot.sayHello();

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
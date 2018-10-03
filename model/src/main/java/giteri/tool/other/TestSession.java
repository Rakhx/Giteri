package giteri.tool.other;

import java.util.Arrays;

public class TestSession {
    public static void main(String[] args) {
      //  TestClassFactory.dummyNotSynchro dummyOne = new TestClassFactory.dummyNotSynchro();

        TestClassFactory tc = new TestClassFactory();
        TestClassFactory.IDummy dummyOne =
                tc.new dummyManuelSynchro();
        TestClassFactory.IDummy dummyTwo =
                tc.new dummyAutoSynchro();

        tc.bouclezNous.addAll(Arrays.asList(dummyOne, dummyTwo));

        tc.letsGo(10000);


    }
}

package giteri.test;

import giteri.fitting.algo.IExplorationMethod;
import giteri.run.configurator.Initializer;
import giteri.run.configurator.Configurator;

/** Classe de test pour vérifier les providers.
 *
 */
public class TestProvider {
    public static void main(String[] args)  {
        Initializer.initialize(Configurator.EnumLauncher.testProvider, null, null,null);
    }

    public static class Companion {
        IExplorationMethod explorator;

        public Companion(IExplorationMethod explorator){
            this.explorator = explorator;
        }

        public Runnable giveMyself(){
            return () -> {
                do{
                    explorator.apply();
                    System.out.println(explorator.toString());
                }while(explorator.gotoNext());

            };
        }

    }

}

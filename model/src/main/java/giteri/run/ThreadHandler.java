package giteri.run;

import giteri.run.configurator.Configurator;

/** Classe dont vont hériter les threads, et qui propose
 * des méthodes permettant de se faire gérer de l'extérieur.
 * 
 *
 */
public abstract class ThreadHandler implements Runnable {
	
	private boolean suspend = Configurator.isSystemPaused();
	private Boolean working = true;
	public Thread t;
	public abstract void doRun();
	public abstract Thread getThread();

	public void run() {
    	try {    
    		while(working){
    			
	    		synchronized(this) {
					while(suspend) {
	                   wait();
	                }
	    		}
	    		
	    		doRun();
    		}
		}
    	catch (InterruptedException e) {
            System.out.println("Thread interrupted.");
    	}
    }

	@SuppressWarnings("WeakerAccess")
	public void start(){
		if(t == null){
			t = getThread();
			t.start();
		}
	}
	
	public void suspend() {
		suspend = true;
	}

	public synchronized void resume() {
		suspend = false;
		notify();
	}
	
	public void stop(){
		synchronized(working){
			working = false;
		}
		
	}

}


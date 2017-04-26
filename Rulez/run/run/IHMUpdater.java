package run;

/** Le faire handle les events
 * 
 *
 */
public class IHMUpdater extends ThreadHandler{

	Thread t;
	@Override
	public void doRun() {
		// TODO ICI
		// Method d'update d'IHM
		// puis wait sur le NC
		
	}

	@Override
	public Thread getThread() {
		Thread returnThread = new Thread(this);
		returnThread.setName("IHMUpdater");
		t = returnThread;
		return returnThread;
	}

}

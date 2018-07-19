package giteri.tool.other;


public class StopWatchFactory {

	TreeNode<StopWatch> watches;
	
	//region singleton
	
	static private StopWatchFactory INSTANCE= null;
	private StopWatchFactory(){
		watches = new TreeNode<StopWatch>("Main", new StopWatch("Main"));
	}

	public static StopWatchFactory getInstance(){
		if(INSTANCE == null)
			INSTANCE = new StopWatchFactory();
		return INSTANCE;
	}
	
	//endregion
	
	/** Va afficher sur l'interface les temps d'execution des différentes
     * watch.
     * 
     */
    public void publishResult(){
    	watches.print();
    }

	/**
	 * 
	 * @param parentName
	 * @param name
	 * @return
	 */
	public boolean addWatch(String parentName, String name){
		TreeNode<StopWatch> parent = watches.findNode(parentName);
		if(parent == null){
			parent = watches;
		}
		
		TreeNode<StopWatch> child = parent.addChild(name, new StopWatch(name));
		child.parent = parent;
		return true;
	}

	/**
	 * 
	 * @param name
	 */
	public void startWatch(String name){
		TreeNode<StopWatch> watch = watches.findNode(name);
		if(watch == null){
			System.err.println("Pas de watch a ce nom");
		}else {
			watch.data.startTiming();
		}
	}
	
	/**
	 * 
	 * @param name
	 */
	public void stopWatch(String name){
		TreeNode<StopWatch> watch = watches.findNode(name);
		if(watch == null){
			System.err.println("Pas de watch a ce nom");
		}else {
			watch.data.stopTiming();
		}
	}
	
}

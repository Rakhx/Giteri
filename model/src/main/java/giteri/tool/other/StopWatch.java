package giteri.tool.other;


/** Classe permettant de vérifier les temps d'exec.
* approximatif puisque ne prend pas en compte les changements 
* de thread etc.
 */
public class StopWatch
{ 
	//region Properties
    
	@SuppressWarnings("unused")
	private String name;   
	private long cumuledTime;
    private long startTime;
    private boolean isRunning;
    
    //endregion
    
    /** Création d'un timer, mise a zéro.
     * 
     * @param name Nom de la montre
     */
    public StopWatch(String name){
    	this.name = name;
    	cumuledTime = 0;
    	isRunning = false;
    }
    
    /** lancement de la clock
     * 
     */
    public void startTiming(){
    	isRunning = true;
    	startTime = System.nanoTime();
    }
 
    /** Stop de la clock 
     * 
     */
    public void stopTiming()
    {
    	isRunning = false;
        cumuledTime += System.nanoTime() - startTime; 
    }

    /** Affichage de la valeur 
     * 
     */
    public String toString(){
    	return isRunning() ? ""+(System.nanoTime() - startTime): "" + cumuledTime;
    }


    public boolean isRunning() {
        return isRunning;
    }
}
package networkStuff;

import java.io.Serializable;
import java.util.ArrayList;

/*
http://www.tutorialspoint.com/java/java_serialization.htm
*/
public class ConfigurationObject implements Serializable {

	private static final long serialVersionUID = 1L;
	ArrayList<String> memeList;
	
	/** application de la config au systeme.
	 * 
	 */
	public void applyConfig(){
		
	}
	
	/** Sauvegarder les éléments de la config puis s'auto save
	 * après sérialisation. 
	 */
	public void serialize(){
		
	}
	
	
}

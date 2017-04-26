package giteri.tool.other;

import giteri.run.interfaces.Interfaces.IReadNetwork;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

import giteri.network.network.INetworkRepresentations;

/** Classe qui permet la lecture et l'écriture depuis / vers
 * les fichiers texte
 *
 */
public class WriteNRead {

	// Region Singleton
	private static WriteNRead instance = null;
	private WriteNRead(){
		
	}
	public static WriteNRead getInstance(){
		if(instance == null)
			instance = new WriteNRead();
		return instance;
	}
	
	// EndRegion
	
	final static File defaultPath = new File("DefaultPath");
	final static Charset ENCODING = StandardCharsets.UTF_8;
	Hashtable<String, Path> rscNameToPath = new Hashtable<String, Path>();

	/** Ecriture rapide dasn un fichier texte
	 * 
	 * @param reps
	 * @param fileName
	 * @param toWrite
	 */
	public void writeSmallFile2(File reps, String fileName, List<String> toWrite){
		// Si il n'y a pas encore d'entrée de path dans la hashtable
		
//		if(!rscNameToPath.containsKey(fileName)){
//			Path path = Paths.get(reps  + System.getProperty("file.separator")	+ fileName + ".txt");
//			buildWriter(fileName, Optional.of(path) );
//		}
//		
//		Path path = rscNameToPath.get(fileName);

		Path path = Paths.get(reps  + System.getProperty("file.separator")	+ fileName + ".txt");
		
		try {
			if(!Files.exists(path))	
				Files.write(path, toWrite, ENCODING);
			else
				Files.write(path, toWrite, StandardOpenOption.APPEND);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Obtenir un file dont le chemin contient tout les répertoires 
	 * de la liste de string, séparé par les séparateurs du systeme.
	 * 
	 * @param rep
	 * @return
	 */
	public File createAndGetDirFromString(ArrayList<String> rep){
		String allRep = "";
		File rez;
		for (String string : rep) 
			allRep += string + System.getProperty("file.separator");
		
		rez = new File(allRep);
		rez.mkdirs();
		return rez;
	}

	/** Lecture d'un fichier texte, et appelle de la fonction de l'interface IReadNetwork
	 * a chaque ligne lue. 
	 * 
	 * @param aFile File fichier texte contenant des données
	 * @param networkLoader Interface qui va définir l'actionà faire lors de la lecture d'une ligne
	 * @param separator Caractere de séparation entre les éléments d'une ligne, si neccesaire.
	 * @param comString spécifie la string qui est la marque des commentaires, et ignore la ligne
	 * commencant par cette string. 
	 * @return l'objet qui a lu le réseau et qui le contient.
	 * @throws IOException
	 */
	public IReadNetwork readAndCreateNetwork(File aFile, IReadNetwork networkLoader, String separator ,String comString) throws IOException{
		Path path = aFile.toPath();
		String line;
		networkLoader.init();
	    try (Scanner scanner =  new Scanner(path, ENCODING.name())){
	      while (scanner.hasNextLine()){
	    	  line = scanner.nextLine();
	    	  // # for instance
	    	  if(!line.startsWith(comString))
	    		  //process each line in some way
	    		  networkLoader.whatToDoWithOneLine(line , separator);
	      }
	      
	      scanner.close();
	    }
		catch (Exception e){
			System.out.println("TROPLOL");
	     }
	    
		return networkLoader;
	}
	
	/** Transcrit le réseau en liste d'edge.
	 * 
	 * @param reps
	 * @param fileName
	 * @param nr
	 * @throws IOException
	 */
	public void writeFileFromNetwork(File reps,String fileName, INetworkRepresentations nr) throws IOException {
		Path path = Paths.get(reps  + System.getProperty("file.separator")	+ fileName + ".txt");
		try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING))
		{
			ArrayList<String> toWrite = nr.getNetworkEdges();
		    for (String string : toWrite) 
		    {
				writer.write(string);
		        writer.newLine();
			}
		}
	}

}

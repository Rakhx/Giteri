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
import java.util.List;
import java.util.Scanner;

import giteri.network.network.IInternalNetReprestn;

/** Classe qui permet la lecture et l'écriture depuis / vers
 * les fichiers texte
 * Classe pas trop car possède des objets de type réseau. Devrait etre mieux séparé
 */
public class WriteNRead {
	final static Charset ENCODING = StandardCharsets.UTF_8;

	/** Ecriture rapide dasn un fichier texte
	 *
	 * @param reps
	 * @param fileName
	 * @param toWrite
	 */
	public void writeSmallFile(File reps, String fileName, List<String> toWrite){
		// Si il n'y a pas encore d'entrée de path dans la hashtable
		Path path = Paths.get(reps  + System.getProperty("file.separator")	+ fileName + ".csv");

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
	public File createAndGetDirFromString(List<String> rep){
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
	 * @param aFileName String du path + file name du fichier texte contenant des données
	 * @param networkLoader Interface qui va définir l'actionà faire lors de la lecture d'une ligne
	 * @param separator Caractere de séparation entre les éléments d'une ligne, si neccesaire.
	 * @param comString spécifie la string qui est la marque des commentaires, et ignore la ligne
	 * commencant par cette string.
	 * @return l'objet qui a lu le réseau et qui le contient.
	 * @throws IOException
	 */
	public IReadNetwork readAndCreateNetwork(String aFileName, IReadNetwork networkLoader, String separator ,String comString) throws IOException{
		Path path = Paths.get(aFileName);
		return pReadAndCreateNetwork(path, networkLoader, separator, comString);
	}

	/** lecture d'un fichier texte contenant une liste d'edge, pour le convertir en Réseaux.
	 *
	 * @param aFile
	 * @param networkLoader
	 * @param separator
	 * @param comString
	 * @return
	 * @throws IOException
	 */
	public IReadNetwork readAndCreateNetwork(File aFile, IReadNetwork networkLoader, String separator ,String comString) throws IOException{
		Path path = aFile.toPath();
		return pReadAndCreateNetwork(path, networkLoader, separator, comString);
	}

	/** voir doc. des fonctions public éponymes
	 *
 	 * @param path
	 * @param networkLoader
	 * @param separator
	 * @param comString
	 * @return
	 * @throws IOException
	 */
	private IReadNetwork pReadAndCreateNetwork(Path path, IReadNetwork networkLoader, String separator ,String comString) throws IOException{
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
			System.out.println("[WriteNRead.readAndCreateNetwork]- Erreur de lecture de fichier associé");
		}

		return networkLoader;
	}

	/** Transcrit le réseau en liste d'edge.
	 *
	 * @param reps Repertoire d'écriture
	 * @param fileName Nom du fichier dans lequel écrire le réseau
	 * @param nr Représentation du réseau qui va etre transcrit en liste d'edge
	 * @throws IOException
	 */
	public void writeFileFromNetwork(File reps,String fileName, IInternalNetReprestn nr) throws IOException {
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

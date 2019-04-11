package giteri.run;

import giteri.fitting.parameters.FittingClass;
import giteri.network.network.IInternalNetReprestn;
import giteri.network.network.IInternalNetReprestn.TinyNetworks;
import giteri.network.network.NetworkModificator;
import giteri.network.network.NetworkProperties;
import giteri.network.networkStuff.NetworkFileLoader;
import giteri.run.configurator.Configurator;
import giteri.run.interfaces.Interfaces.IReadNetwork;
import giteri.tool.other.WriteNRead;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.IOException;

/**
 * Classe pour lancer des commandes simples
 * Mode 1: comparaison de deux fichiers de network et renvoie du score entre les deux. Noms des fichiers dans le repertoire courant network1/2.txt
 * Mode 2: A certains paramètre fixe, génération de réseau
 */
public class Cmdline {

    public static void main(String[] args) {
        switch (args[0]) {
            //region Scoring
            case "1":
                String net1 = "network1.txt";
                String net2 = "network2.txt";
                IReadNetwork fileNetRdr = new NetworkFileLoader(null, new WriteNRead());
                NetworkProperties fileReadProperties;
                NetworkProperties currentNetProperties;

                try {
                    fileNetRdr = new WriteNRead().readAndCreateNetwork(net1, fileNetRdr, " ", "#");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                fileReadProperties = fileNetRdr.getNetworkProperties();

                try {
                    fileNetRdr = new WriteNRead().readAndCreateNetwork(net2, fileNetRdr, " ", "#");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                currentNetProperties = fileNetRdr.getNetworkProperties();
                double score = FittingClass.getNetworksDistanceDumb(Configurator.activationCodeForScore, fileReadProperties, currentNetProperties);
                System.out.println(score);

                break;
            //endregion

            //region Graph Generator

            case "2":
                // Lecture d'un fichier texte et conversion en objet graph
                Graph graphReaded = new SingleGraph("Lu");
                IReadNetwork networkReader = readThatFile(graphReaded, "networkToRead.txt");

                // affichage de ce graphe dans une fenetre
                networkReader.getGraphFromDataRead();

                // Choix des propriétés inchangées
                int fixedProperties = 0;

                // Parcours du graph pour en déduire la valeur des propriétés auxquelles on s'intéresse
                NetworkProperties properties = networkReader.getNetworkProperties();

                // Fonction qui construit un graph possédant les memes propriétés que le 1er
                Graph graphMade = new SingleGraph("Generated");
                NetworkModificator netgen = new NetworkModificator(graphMade, properties, fixedProperties);
                netgen.generate();


                break;

            //endregion

            default:
                System.err.println("Aucun mode choisit");
                break;
        }


    }

    /** Lit un fichier texte, génère un tinyNetwork, modification de l'objet graph pour matcher.
     *
     *
     * @param graph
     * @param filename
     */
    public static IReadNetwork readThatFile(Graph graph, String filename) {

        // Lecture du fichier txt
        IReadNetwork fileNetRdr = new NetworkFileLoader(null, new WriteNRead());
        NetworkProperties fileReadProperties;
        try {
            fileNetRdr = new WriteNRead().readAndCreateNetwork(filename, fileNetRdr, " ", "#");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // appelle necessaire pour la création d'un tiny network.
        fileReadProperties = fileNetRdr.getNetworkProperties();

        return fileNetRdr;
    }

    /**
     * Lis les propriétés d'un graph graphStream et en retire des propriétés qui
     * sont définis comme intéressante par le code d'activation
     *
     * @param graph
     * @return
     */
    public static NetworkProperties readThatGraph(Graph graph, int activationCode) {
        NetworkProperties properties = new NetworkProperties();
        properties.createStub();
        return properties;
    }

}

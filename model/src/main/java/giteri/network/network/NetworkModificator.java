package giteri.network.network;

import giteri.run.configurator.Configurator;
import giteri.tool.math.Toolz;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Classe qui va modifier un réseau et vérifier les propriétés
 */
public class NetworkModificator {

    // classe en construction
    Graph graphUnderWork;
    // l'activator de cette classe n'est pas le meme que celui qu'on veut utiliser pour la fénération
    NetworkProperties targetedProperties;
    // Activator voulu pour la génération
    int activator;

    // Structure des propriétés que l'on souhaite conserver
    boolean density, degreeDistrib, clustCoeff;

    /**
     *
     * @param graphUnderWork
     * @param targetedProperties
     * @param activator
     */
    public NetworkModificator(Graph graphUnderWork, NetworkProperties targetedProperties, int activator) {
        this.graphUnderWork = graphUnderWork;
        this.targetedProperties = targetedProperties;
        this.activator = activator;
    }

    /** Génération d'un graphe en fonction des propriétés qu'on veut y voir
     *
     */
    public void generate() {
        density = Configurator.isAttribActived(targetedProperties.getActivator(), Configurator.NetworkAttribType.DENSITY);
        degreeDistrib = Configurator.isAttribActived(targetedProperties.getActivator(), Configurator.NetworkAttribType.DDARRAY);
        clustCoeff = Configurator.isAttribActived(targetedProperties.getActivator(), Configurator.NetworkAttribType.AVGCLUST);
        int nbNode = targetedProperties.nbNodes;
        int nbEdge = targetedProperties.nbEdges;
        TargetStructure target = new TargetStructure();
        target.edgeAvailable = nbEdge;
        target.nbNode = nbNode;
        target.degreeAvailable = targetedProperties.getDd();

        for (int i = 0; i < nbNode; i++) {
            graphUnderWork.addNode("" + i);
        }

        easyAdd.addLinks(target, graphUnderWork);
        graphUnderWork.display();
        ddAdd.addLinks(target, graphUnderWork);
    }

    /**
     * Interface de la fonction qui va ajouter un lien dans le graphe
     */
    public interface LinkAdder {
        /** Ajout d'une série de lien.
         *
         */
        void addLinks(TargetStructure target, Graph graph);
    }

    public interface LinkPredictor {
        /** renvoie true si l'ajout est possible.
         *
         * @param target
         * @param graph
         * @param from
         * @param to
         * @return
         */
        boolean canAddLink(TargetStructure target, Graph graph, Node from, Node to);
    }

    LinkPredictor predictorDensity =  (target, graph, nodeOne, nodeTwo ) -> {
        if(nodeOne == nodeTwo)
           return false;
        else if(graph.getEdge(nodeOne+":"+nodeTwo) != null || graph.getEdge(nodeTwo+":"+nodeOne) != null )
            return false;
        else {
            return true;
        }
    };

    // retourne la possibilité d'ajouter un lien, sur la DD des cibles
    LinkPredictor predictorDistribDegre =  (target, graph, nodeOne, nodeTwo ) -> {
        int[] ddWanted = target.degreeAvailable;
        int degreeOne = nodeOne.getDegree(), degreeTwo = nodeTwo.getDegree();
        if (--ddWanted[degreeOne] >= 0 && --ddWanted[degreeTwo] >= 0) {
            return true;
        }else
            return false;
    };

    // ajoute les liens qu'il faut. Selection des noeuds et demande aux predictors si c'est OK
    LinkAdder easyAdd = (target, graph) -> {
        Node nodeOne, nodeTwo;
        do {
            nodeOne = graph.getNode(Toolz.getRandomNumber(target.nbNode));
            nodeTwo = graph.getNode(Toolz.getRandomNumber(target.nbNode));
            if (predictorDensity.canAddLink(target, graph, nodeOne, nodeTwo))
                try {
                    graph.addEdge(nodeOne + ":" + nodeTwo, nodeOne, nodeTwo, false);
                    target.edgeAvailable--;
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
        } while (target.edgeAvailable > 0);
    };


    /**
     * Modification du graph pour prendre en compte la distribution de degrée;
     */
    LinkAdder ddAdd = (target, graph) -> {
        // Recherche des liens a supprimer
        int[] ddwanted, ddActual;
        Node nodeOne = null, nodeTwo = null;
        Edge edgeToRemove;
        ddwanted = target.degreeAvailable;
        ddActual = new int[graph.getNodeCount()];
        // Lecture du graphe en paramètre.
        graph.getEachNode().forEach(e -> ddActual[e.getDegree()]++);
        boolean again = true, edgeHasBeeRemove, firstNode, twoNodes;
        int index;

        // RELINKAGE DES LIENS UN A UN
        do {




            index = target.nbNode; edgeHasBeeRemove = false;
            // RECHERCHE D'UN LIEN A ENLEVER
            do {
                index--;
                // On prend le degre le plus grand qui est en trop
                if (ddActual[index] > ddwanted[index]) {
                    // on cherche le premier noeud qu'on trouve possédant ce degré
                    for (Node node : graph.getEachNode()) {
                        if (node.getDegree() == index) {
                            // selection d'un edge au hasard
                            edgeToRemove = node.getEdge(Toolz.getRandomNumber(index));
                            nodeOne = edgeToRemove.getNode0();
                            nodeTwo = edgeToRemove.getNode1();
                            ddActual[nodeOne.getDegree()]--;
                            ddActual[nodeTwo.getDegree()]--;
                            ddActual[nodeOne.getDegree()-1]++;
                            ddActual[nodeTwo.getDegree()-1]++;
                            if(ddActual[nodeOne.getDegree()] < 0 || ddActual[nodeTwo.getDegree()]< 0){
                                System.err.println("c'est quoi cette merde");
                            }
                            graphUnderWork.removeEdge(edgeToRemove);
                            edgeHasBeeRemove = true;
                            break;
                        }
                    }
                }
            } while (index > 0 && edgeHasBeeRemove == false);

            // dans le cas ou aucun edge a été retiré mais qu'on a pas la forme final du réseau ( puisqu'on en est la )
            // Est ce possible OU ALORS C FINI
            if (!edgeHasBeeRemove) {
                System.err.println("INCROYABLE PAS PREVU");
                // again = false;
            }

            // Choix de deux noeuds compltement random
//            boolean goodPair = false;
//            do{
//                nodeOne = graph.getNode(Toolz.getRandomNumber(target.nbNode));
//                nodeTwo = graph.getNode(Toolz.getRandomNumber(target.nbNode));
//
//                // si le degré du noeud sélection est supérieur au degré qu'on
//                goodPair = ddActual[nodeOne.getDegree()] < ddwanted[nodeOne.getDegree()];
//                goodPair &= (ddActual[nodeTwo.getDegree()] + nodeOne.getDegree() == nodeTwo.getDegree()? 1:0) < ddwanted[nodeTwo.getDegree()];
//                goodPair &= graph.getEdge(nodeTwo+":"+nodeOne) == null;
//                goodPair &= graph.getEdge(nodeOne+":"+nodeTwo) == null;
//            }while (!goodPair);

            firstNode = true; twoNodes = false;
            // On ajoute un edge, en vérifiant le pedigree des noeuds. Pas d'heuristique particulière pour les choisi
            for (Node node : graph.getEachNode()) {
                if (ddActual[node.getDegree()] < ddwanted[node.getDegree()])
                    if (!firstNode) {
                        nodeTwo = node;
                        twoNodes = true;
                    } else {
                        nodeOne = node;
                        firstNode = false;
                    }

                // Dans le cas ou le lien existe déjà
                if(graph.getEdge(nodeTwo+":"+nodeOne) != null || graph.getEdge(nodeOne+":"+nodeTwo) != null){
                    twoNodes = false;
                }
                // Si on a bien les deux noeuds, qu'ils ne sont pas lié déjà, on sait quoi ajouter;
                if(twoNodes)
                    break;
            }

            // les deux noeuds en poche, on ajoute fierement
            try {
//                graph.addEdge(nodeOne + ":" + nodeTwo, nodeOne, nodeTwo, false);
//                ddActual[nodeOne.getDegree()]++;
//                ddActual[nodeTwo.getDegree()]++;
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

        } while (again);
    };




    /** Classe qui contient un "décompte" des propriétés a atteindre
     *
     */
    class TargetStructure {
        public TargetStructure() {

        }

        int nbNode;
        int edgeAvailable;
        int[] degreeAvailable;
        double ccAvailable;
    }
}


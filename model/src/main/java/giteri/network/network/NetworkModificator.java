package giteri.network.network;

import giteri.run.configurator.Configurator;
import giteri.tool.math.Toolz;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        // pour un réseau random a densité correct
       // easyAdd.addLinks(target, graphUnderWork);

        // pour un réseau à DDt
        trveddAdd.addLinks(target, graphUnderWork);


        graphUnderWork.display();
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
        Node nodeOne = null, nodeKeeped = null;
        Edge edgeToRemove;
        int offset;
        ddwanted = target.degreeAvailable;
        ddActual = new int[graph.getNodeCount()];
        // Lecture du graphe en paramètre.
        graph.getEachNode().forEach(e -> ddActual[e.getDegree()]++);
        boolean again = true, edgeHasBeeRemove, firstNode, okToAdd;
        int index;

        // RELINKAGE DES LIENS UN A UN
        do {
            index = target.nbNode; edgeHasBeeRemove = false; nodeKeeped = null;
            // RECHERCHE D'UN LIEN DONT UN BOUT EST A RELINKER ( dont un noeud est a relinker )
            do {
                index--;
                // On prend le degre le plus grand qui est en trop
                if (ddActual[index] > ddwanted[index]) {
                    // on cherche le premier noeud qu'on trouve possédant ce degré
                    for (Node node : graph.getEachNode()) {
                        if (node.getDegree() == index) {

                            // selection d'un edge au hasard
                            edgeToRemove = node.getEdge(index != 0? Toolz.getRandomNumber(index):0);
                            if(node == edgeToRemove.getNode0()){
                                nodeOne = node;
                                nodeKeeped = edgeToRemove.getNode1();
                            }else {
                                nodeKeeped = node;
                                nodeOne = edgeToRemove.getNode1();
                            }

                            ddActual[nodeOne.getDegree()]--;
                            ddActual[nodeOne.getDegree()-1]++;

                            graphUnderWork.removeEdge(edgeToRemove);
                            edgeHasBeeRemove = true;
                            break;
                        }
                    }
                }
            } while (index > 1 && edgeHasBeeRemove == false);

            // dans le cas ou aucun edge a été retiré mais qu'on a pas la forme final du réseau ( puisqu'on en est la )
            // Est ce possible OU ALORS C FINI
            if (!edgeHasBeeRemove) {
                System.err.println("INCROYABLE PAS PREVU");
                // again = false;
            }

            okToAdd = false;
            // On ajoute un edge, en gardant le noeud qui y est pour rien
            for (Node node : graph.getEachNode()) {
                if (ddActual[node.getDegree()+1] < ddwanted[node.getDegree()+1]) {
                    nodeOne = node;
                    okToAdd = true;
                }

                // si on est dans le cas d'avoir virer un lien
                if(nodeKeeped != null) {
                    if (nodeOne == nodeKeeped)
                        okToAdd = false;
                    // sinon
                }else
                        for (Node node2 : graph.getEachNode()) {
                            if(node2 != nodeOne) {
                                if (node2.getDegree() == nodeOne.getDegree())
                                    offset = 2;
                                else
                                    offset = 1;

                                if (ddActual[node2.getDegree() + offset] < ddwanted[node2.getDegree() + offset]) {
                                    nodeKeeped = node2;
                                    okToAdd = true;
                                    break;
                                }
                            }
                        }

                // Dans le cas ou le lien existe déj
                if(graph.getEdge(nodeKeeped+":"+nodeOne) != null || graph.getEdge(nodeOne+":"+nodeKeeped) != null){
                    okToAdd = false;
                }
                // Si on a bien les deux noeuds, qu'ils ne sont pas lié déjà, on sait quoi ajouter;
                if(okToAdd)
                    break;
            }

            // les deux noeuds en poche, on ajoute fierement
            if(okToAdd)
            try {

                graph.addEdge(nodeOne + ":" + nodeKeeped, nodeOne, nodeKeeped, false);
                ddActual[nodeOne.getDegree()]++;
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

        } while (again);
    };


    LinkAdder trueddAdd = (target, graph) -> {
        int[] ddwanted, ddActual;
        ddwanted = target.degreeAvailable;
        ddActual = new int[target.nbNode];
        ddActual[0] = target.nbNode;
        int nbAsked = 0;
        Node[] nodes = new Node[target.nbNode];
        Node nodeOne = null;
        Node nodeTwo = null;
        for (Node node : graph.getEachNode())
            nodes[nbAsked++] = node;
        // On remplit les liens par strate
        for (int index = 1; index < target.nbNode; index++) {
            nbAsked = ddwanted[index];

            // on le fait un nombre négatif de fois par rapport aux nombre qu'on veut voir disparaitre pour cette étage
            for (int nbAjout = 0; nbAjout < (target.nbNode - nbAsked - ddActual[index])/2; nbAjout++) {
                // en deux étapes pour avoir deux noeuds consécutifs qui se lient
                nodeOne = nodes[nbAjout];
                nodeTwo = nodes[(nbAjout + 1)];

                // si le lien n'existe pas ( evite qu'un noeud du haut de la pyramide se foute avec un du bas
                // deg3:..
                // deg2:...
                // deg1:.......
                // deg0:.................
                // nombre de noeud ayant le degré en question.
                if (graph.getEdge(nodeOne + ":" + nodeTwo) == null && graph.getEdge(nodeTwo + ":" + nodeOne) == null) {
                    // les deux noeuds vont disparaitre de l'étage du bas
                    ddActual[nodeOne.getDegree()]--;
                    ddActual[nodeTwo.getDegree()]--;
                    try {
                        graph.addEdge(nodeOne + ":" + nodeTwo, nodeOne, nodeTwo, false);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    // Si l'ajout a réussi ou non comme on a viré ceux d'avant
                    if (nodeOne.getDegree() == 198 || nodeTwo.getDegree() == 198)
                        System.err.println("zighail");
                    ddActual[nodeOne.getDegree()]++;
                    ddActual[nodeTwo.getDegree()]++;
                    nodeOne = null;
                }
            }
        }

    };

    LinkAdder trveddAdd = (target, graph) -> {
        // structures utilisées
        int[] ddwanted, ddActual;
        ddwanted = target.degreeAvailable;
        ddActual = new int[target.nbNode];
        double[] ddDiffRatio  = new double[target.nbNode];
        double[] ddTransitionRatio  = new double[target.nbNode];


        double pourcentOffset = 1.05;

        for (int i = 0; i < ddActual.length; i++) {
            ddActual[i] = 1;
        }

        // Variable temp. d'utilisation
        int diff;
        double pourcent;

        //region preparation de la liste de priorité

        /** Si trop de noeud, mettre (en frac?) 5%
         *  Si manque un noeud frac de 30% + 70% ?
         *
         */

        int fracToMany = 5;
        int fracNeedMore = 30;
        int fracFine = 10;
        int offsetNotEnought = 100 - fracToMany - fracNeedMore;

        // Liste de pourcentage de manquement pour atteindre le bon # noeud par degré
        for (int i = 0; i < target.nbNode; i++) {
            // Si la diff est négative, on a trop de lien. pourcentage "négatif". Si positif, il en manque. Si zero, on en a le bon nombre. Wanted
            // est à +1 pour pouvoir diviser directement
            diff = ddwanted[i] - ddActual[i];

            // Si le noeud a trop de lien
            if(diff < 0) {
                pourcent = -1./diff * fracToMany; // plus la diff est grand plus on arriver vers 0%
            }
            // Si le noeud manque de lien
            else if (diff > 0){
                pourcent = (double)diff / ddwanted[i] * fracNeedMore + offsetNotEnought; // Ratio a quel point il manque de noeud
            }
            // Si on a le bon nombre de noeud
            else{
                pourcent = fracFine ;
            }

            ddDiffRatio[i] = pourcent;

            // Tableau de transition
            // Pour la premiere case, donc les noeuds a degré un, ca sera de tte facon rempli en premier avec le premier ajout

            if(i == 0) // sera mis a jour a la prochaine itération ( inutile )?
                ddTransitionRatio[i] = fracFine ;
            else {
                // on utilise le ratio du degré courant et du suivant pour déterminer le bénéfice d'un ajout de lien
                // donc en pratique du courant et du précédent pour déterminer la valeur sur le précédent
                // a quelle point on manque du ratio courant - a quelle point le précédent est réalisé
                // aucune valeur a 0%
                pourcent = ((double)(ddDiffRatio[i-1] + ddDiffRatio[i]) / 2);
                ddTransitionRatio[i-1] = pourcent;
            }
            // sur la derniere itération on set aussi le pourcentage courant
            if(i == target.nbNode - 1){
                ddTransitionRatio[i] = 0; // jamais utilisé? le noeud de degré max n'ajoute jamais de lien
            }
        }

        //endregion

        boolean again = true;
        int nbNodeRemaining = target.nbNode;
        int nbNodeToAdd;
        Map<Integer, Double> kvDdTrRatio = new HashMap<>();
        Map<Integer, Double> kvTopXDdTrRatio ;
        Map<Integer, Double> kvRoulette = new LinkedHashMap<>();
        double addedRlt= 0;
        double selectedRlt;
        int indexSelected;
//        Stream<Map.Entry<Integer,Double>> sorted;
//        sorted = kvDdTrRatio.entrySet().stream().sorted(Map.Entry.comparingByValue());

        // creation de la map K:Index -> V:transitionRatio
        for (int i = 0; i < ddTransitionRatio.length; i++)
            kvDdTrRatio.put(i, ddTransitionRatio[i]);

        graph.display();

        while (again) {
            // nombre pair
            nbNodeToAdd = ((int) (nbNodeRemaining * (.05))) * 2;
            // On sort
            kvTopXDdTrRatio = kvDdTrRatio.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(nbNodeToAdd)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            // Pour chaque noeud du top X qui a besoin de lien
            for (Integer nodeIndex : kvTopXDdTrRatio.keySet()) {
                // On cherche un noeud auquel il n'est pas connecté et qui est aussi en besoin de lien

                // Pour chaque noeud autre que le courant
                for (Node oneNode : graph.getNodeSet()) {
//                    if (kvDdTrRatio.get(oneNode.getIndex()) > 1) // Si besoin d'un ajout
                    if (oneNode.getIndex() != nodeIndex) { // si pas le noeud courant
                        // Check si le lien n'existe pas
                        if (graph.getEdge(oneNode + ":" + nodeIndex) == null && graph.getEdge(nodeIndex + ":" + oneNode) == null) {
                            // Ajout dans une keylist "roulette", dans le cas ou
                            kvRoulette.put(oneNode.getIndex(), addedRlt += kvDdTrRatio.get(oneNode.getIndex()));
                        }
                    }
                }

                // Tirage d'un node de la keylist
                selectedRlt = Toolz.getRandomNumber((int)(addedRlt * 10000))/10000;
                indexSelected = 0;

                for (Integer integer : kvRoulette.keySet()) {
                    if (kvRoulette.get(integer) >= selectedRlt) { // bingo loto
                        indexSelected = integer;
                        break;
                    }
                }

                ddActual[graph.getNode(nodeIndex).getDegree()]--;
                ddActual[graph.getNode(indexSelected).getDegree()]--;
                // Ajout du lien
                graph.addEdge(nodeIndex + ":" + indexSelected, nodeIndex, indexSelected, false);
                ddActual[graph.getNode(nodeIndex).getDegree()]++;
                ddActual[graph.getNode(indexSelected).getDegree()]++;
            }

         try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {

            }
        }
    };

    public void majWeight(TargetStructure target, Graph graph, double[] ddDiffRatio,double[] ddTransitionRatio){
        /** Si trop de noeud, mettre (en frac?) 5%
         *  Si manque un noeud frac de 30% + 70% ?
         *
         */
        double pourcent;
        int diff;
        int fracToMany = 5;
        int fracNeedMore = 30;
        int fracFine = 10;
        int offsetNotEnought = 100 - fracToMany - fracNeedMore;
        int[] ddwanted, ddActual;
        ddwanted = target.degreeAvailable;
        ddActual = new int[target.nbNode];
        // Liste de pourcentage de manquement pour atteindre le bon # noeud par degré
        for (int i = 0; i < target.nbNode; i++) {
            // Si la diff est négative, on a trop de lien. pourcentage "négatif". Si positif, il en manque. Si zero, on en a le bon nombre. Wanted
            // est à +1 pour pouvoir diviser directement
            diff = ddwanted[i] - ddActual[i];

            // Si le noeud a trop de lien
            if(diff < 0) {
                pourcent = -1./diff * fracToMany; // plus la diff est grand plus on arriver vers 0%
            }
            // Si le noeud manque de lien
            else if (diff > 0){
                pourcent = (double)diff / ddwanted[i] * fracNeedMore + offsetNotEnought; // Ratio a quel point il manque de noeud
            }
            // Si on a le bon nombre de noeud
            else{
                pourcent = fracFine ;
            }

            ddDiffRatio[i] = pourcent;

            // Tableau de transition
            // Pour la premiere case, donc les noeuds a degré un, ca sera de tte facon rempli en premier avec le premier ajout

            if(i == 0) // sera mis a jour a la prochaine itération ( inutile )?
                ddTransitionRatio[i] = fracFine ;
            else {
                // on utilise le ratio du degré courant et du suivant pour déterminer le bénéfice d'un ajout de lien
                // donc en pratique du courant et du précédent pour déterminer la valeur sur le précédent
                // a quelle point on manque du ratio courant - a quelle point le précédent est réalisé
                // aucune valeur a 0%
                pourcent = ((double)(ddDiffRatio[i-1] + ddDiffRatio[i]) / 2);
                ddTransitionRatio[i-1] = pourcent;
            }
            // sur la derniere itération on set aussi le pourcentage courant
            if(i == target.nbNode - 1){
                ddTransitionRatio[i] = 0; // jamais utilisé? le noeud de degré max n'ajoute jamais de lien
            }
        }
    }



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


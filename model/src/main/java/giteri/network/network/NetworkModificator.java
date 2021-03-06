package giteri.network.network;

import giteri.run.configurator.Configurator;
import giteri.tool.math.Toolz;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.*;
import java.util.stream.Collectors;

/** NON UTILISE
 * Classe qui va modifier un réseau et vérifier les propriétés -
 *
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
     * @param graphUnderWork
     * @param targetedProperties
     * @param activator
     */
    public NetworkModificator(Graph graphUnderWork, NetworkProperties targetedProperties, int activator) {
        this.graphUnderWork = graphUnderWork;
        this.targetedProperties = targetedProperties;
        this.activator = activator;
    }

    /**
     * Génération d'un graphe en fonction des propriétés qu'on veut y voir
     */
    public void generate() {
        density = Configurator.isAttribActived(targetedProperties.getActivator(), Configurator.NetworkAttribType.DENSITY);
        degreeDistrib = Configurator.isAttribActived(targetedProperties.getActivator(), Configurator.NetworkAttribType.DDARRAY);
        clustCoeff = Configurator.isAttribActived(targetedProperties.getActivator(), Configurator.NetworkAttribType.AVGCLUST);
        int nbNode = (int) targetedProperties.nbNodes;
        int nbEdge = (int) targetedProperties.nbEdges;
        TargetStructure target = new TargetStructure();
        target.nbEdges = nbEdge;
        target.nbNode = nbNode;
        target.DistribDegree = targetedProperties.getDd();

        for (int i = 0; i < nbNode; i++) {
            graphUnderWork.addNode("" + i);
        }

        graphUnderWork.display();

        // pour un réseau random a densité correct
        easyAdd.addLinks(target, graphUnderWork);

        // pour un réseau à DDt
        simpleDdAdd.addLinks(target, graphUnderWork);


    }

    /**
     * Interface de la fonction qui va ajouter un lien dans le graphe
     */
    public interface LinkAdder {
        /**
         * Ajout d'une série de lien.
         */
        void addLinks(TargetStructure target, Graph graph);
    }

    public interface LinkPredictor {
        /**
         * renvoie true si l'ajout est possible.
         *
         * @param graph
         * @param from
         * @param to
         * @return
         */
        boolean canAddLink(Graph graph, Node from, Node to);
    }

    LinkPredictor predictorDensity = (graph, nodeOne, nodeTwo) -> {
        if (nodeOne == nodeTwo)
            return false;
        else if (graph.getEdge(nodeOne + ":" + nodeTwo) != null || graph.getEdge(nodeTwo + ":" + nodeOne) != null)
            return false;
        else {
            return true;
        }
    };

    // ajoute les liens qu'il faut. Selection des noeuds et demande aux predictors si c'est OK
    LinkAdder easyAdd = (target, graph) -> {
        Node nodeOne, nodeTwo;
        do {
            nodeOne = graph.getNode(Toolz.getRandomNumber(target.nbNode));
            nodeTwo = graph.getNode(Toolz.getRandomNumber(target.nbNode));
            if (predictorDensity.canAddLink(graph, nodeOne, nodeTwo))
                try {
                    graph.addEdge(nodeOne + ":" + nodeTwo, nodeOne, nodeTwo, false);
                    target.nbEdges--;
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
        } while (target.nbEdges > 0);
    };

    LinkAdder simpleDdAdd = (target, graph) -> {
        // structures utilisées pour liste de priorité
        int[] ddwanted, ddActual; // distribution de degrée voulue et actuelle
        int[] ddDiff = new int[target.nbNode]; // Diff entre wanted & get
//        double[] ddTransitionRatioAdd = new double[target.nbNode]; // Priorité des degrés a atteindre
//        double[] ddTransitionRatioRmv = new double[target.nbNode]; // Priorité des degrés a atteindre dans le cas de retrait

        // Autres stuctures
        Map<Integer, Integer> kvDegreeDiff = new HashMap<>(); // K:Degree V:Transition ratio
        Map<Integer, Integer> kvDegreeSumDiffAsc = new HashMap<>();
        Map<Integer, List<Integer>> kvDegreeNodes = new HashMap<>();
//        Map<Integer, Double> kvDdTrRatioRmv = new HashMap<>(); // K:Degree V:Transition ratio pour le retrait
        Map<Integer, Integer> kvTopXDdDiffWanted; // K:Degree V:Transition ratio - Top X
        Map<Integer, Double> kvRouletteReceveur = new LinkedHashMap<>(); // K:Degree V:sum(Transition Ratio) - Pour les noeuds qui recoivent
        Map<Integer, Double> kvRouletteHavingToMany = new LinkedHashMap<>(); // K:Degree V:sum(Transition Ratio) - Pour les noeuds qui ajoutent
        Map<Integer, Double> kvRouletteRelinker = new LinkedHashMap<>(); // K:Degree V:sum(Transition Ratio) - Pour les noeuds qui ajoutent

        // Variables
        List<Integer> nodesIndex;
//        Integer nbEdgeRemaining = target.nbEdges;
        boolean again = true;

        // Variables de fonctionnenemnt
        int nodeOneIndex = -1;
        int nodeTwoIndex;
        int nodeThreeIndex;
        int degreeTmp;
        Node nodeTmp;
        Node nodeOne;
        Node nodeTwo;
        Node nodeThree;
        Edge edge;

        // Some initialisation
        ddwanted = target.DistribDegree;
        ddActual = new int[target.nbNode];
        for (Node node : graphUnderWork.getEachNode()) {
            ddActual[node.getDegree()]++;
        }

        while (again) {

            majWeightRelink(target, ddwanted, ddActual, ddDiff, kvDegreeDiff, kvDegreeSumDiffAsc);
            majDegreeNode(graph, kvDegreeNodes);
            kvRouletteHavingToMany.clear();
            kvRouletteReceveur.clear();
            kvRouletteRelinker.clear();

            // On sort Question tag
            kvTopXDdDiffWanted = kvDegreeDiff.entrySet().stream()
                    .filter(e -> e.getValue() < 0)
                    .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                    .limit(3) // Pour l'instant on prend le plus nécessaire
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            // Pour chaque degré du top X qui a trop de lien
            for (Integer integ : kvTopXDdDiffWanted.keySet()) {
                // On prend la liste des noeuds possédant ce degré
                nodesIndex = kvDegreeNodes.get(integ);

                // on ajoute tout ces index de noeuds de degré integ avec leur importance
                // de transition
                for (Integer index : nodesIndex)
                    kvRouletteHavingToMany.put(index, 1.);
            }

            // Nombre de retrait pour le meme lien
            for (int i = 0; i < 1; i++) {
                nodeOneIndex = Toolz.getRandomObjectWeighed(kvRouletteHavingToMany);
                nodeOne = graph.getNode(nodeOneIndex);

                if(nodeOne.getDegree() == 0)
                    continue;

                // On cherche un noeud auquel est connecté ce noeud qui en a trop
                edge = Toolz.getRandomElement(nodeOne.getEdgeSet());
                nodeTwo = edge.getNode0() == nodeOne? edge.getNode1(): edge.getNode0();
                nodeTwoIndex = nodeTwo.getIndex();

                // On cherche un noeud auquel n'est pas connecté node 2
                kvRouletteReceveur.clear();
                // On rempli l'ensemble des noeuds possible pour un ajout de lien
                for (Node aNode : graph.getNodeSet()) {
                    if (aNode != nodeTwo) { // si pas le noeud courant
                        // Check si le lien n'existe pas
                        if (graph.getEdge(aNode + ":" + nodeTwoIndex) == null
                                && graph.getEdge(nodeTwoIndex + ":" + aNode) == null) {


                            // Ajout dans une keylist "roulette"
                            kvRouletteReceveur.put(aNode.getIndex(), Math.pow(target.nbNode + kvDegreeDiff.get(aNode.getDegree()), 2)/target.nbNode);//aNode.getDegree() != 0? 1./aNode.getDegree():1);
                        }
                    }
                }

                if(kvRouletteReceveur.size() == 0)
                    continue;

                nodeThreeIndex = Toolz.getRandomObjectWeighed(kvRouletteReceveur);

//                indexTmp = kvDegreeNodes
                nodeThree = graph.getNode(nodeThreeIndex);

                if(nodeThree.getDegree() != 0) {
                    try {
                        degreeTmp = nodeThree.getDegree() - 1;

                        for (Integer integer : kvDegreeNodes.get(degreeTmp)) {
                            if (graph.getEdge(integer + ":" + nodeTwoIndex) == null
                                    && graph.getEdge(nodeTwoIndex + ":" + integer) == null) {

                                nodeTmp = graph.getNode(integer);
                                nodeThreeIndex = nodeTmp.getIndex();
                                break;
                            }
                        }
                    }catch(NullPointerException npe){

                    }
                }

                modifyEdge(graph, nodeOneIndex,nodeTwoIndex,ddActual, false);
                modifyEdge(graph, nodeThreeIndex, nodeTwoIndex, ddActual, true);
            }

            //endregion



//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException ie) {
//
//            }


            again = false;
            for (int i = 0; i < ddwanted.length; i++) {
                if (ddwanted[i] != ddActual[i]) {
                    again = true;
                }
            }

        }
    };

    LinkAdder addDD = (target, graph) -> {

        //region Structures utilisées pour savoir ou relinker des edges

        int[] ddwanted, ddActual; // distribution de degrée voulue et actuelle
        int[] ddDiff; // Difference entre wanted et actual
        Map<Integer, Integer> kvDegreeDiff = new LinkedHashMap<>(target.nbNode); // k:degree v:diff de wanted
        Map<Integer, Integer> kvDegreeSumDiffAsc = new LinkedHashMap<>(target.nbNode); // k:degree v:sum des diff ascendante
        Map<Integer, Integer> kvNodeDecay = new HashMap<>(target.nbNode);
        //endregion

        double rltAddMax;

        // Autres stuctures

        Map<Integer, Double> kvRouletteReceveur = new LinkedHashMap<>(); // K:Degree V:sum(Transition Ratio) - Pour les noeuds qui recoivent
        Map<Integer, Double> kvRouletteAjouteur = new LinkedHashMap<>(); // K:Degree V:sum(Transition Ratio) - Pour les noeuds qui ajoutent

        Map<Integer, List<Integer>> kvIndexNodes = new HashMap<>(); // K:Degree V:Liste d'index des nodes@this degree

        Map<Integer, Integer> kvTopXA; // K:Degree V:Transition ratio - Top X
        Map<Integer, Double> kvTopXB; // K:Degree V:Transition ratio - Top X
        Map<Integer, Double> kvTopXC; // K:Degree V:Transition ratio - Top X

        // Variables
        List<Integer> nodesIndexForRmv;
        List<Integer> nodesCandidateToAdd = new ArrayList<>();

        boolean again = true;
        boolean supFind = false;

        // Variables de fonctionnenemnt
        double addedRlt;
        double selectedRlt;

        int nodeOneIndex;
        int degreeDiff;

        Node nodeActif;
        Node nodeTournant;
        Node nodeToRmvEdge;
        Node nodeNewAdd = null;
        Edge edge;

        //region Some initialisation
        ddwanted = target.DistribDegree;
        ddActual = new int[target.nbNode];
        ddDiff = new int[target.nbNode];

        for (Node node : graphUnderWork.getEachNode()) {
            ddActual[node.getDegree()]++;
        }
        //endregion

        while (again) {

            //region re init variable a clear
            kvRouletteAjouteur.clear();
            kvRouletteReceveur.clear();
            rltAddMax = 0;
            nodeOneIndex = 0;
            nodesCandidateToAdd.clear();
            //endregion

            //region MaJ des valeurs des variables attennantes a l'état du réseau
            majWeightRelink(target, ddwanted, ddActual, ddDiff, kvDegreeDiff, kvDegreeSumDiffAsc);
            majDegreeNode(graph, kvIndexNodes);

            //Top X des besoins en ajout de noeud
            kvTopXA = kvDegreeDiff.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                    // .limit(target.nbNode/10)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
//
//            // Top X des besoin en retrait de noeud
//            kvTopXB = kvDdDiffRatio.entrySet().stream()
//                    .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
//                    .limit(target.nbNode/5)
//                    .collect(Collectors.toMap(
//                            Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
//
//            // Top X des sum des diff wanted - actual. Si positif, on manque de noeud au dessus
//            kvTopXC = kvDdTrRatio.entrySet().stream()
//                    .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
//                    .collect(Collectors.toMap(
//                            Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            // endregion

            // On prend la valeur la plus basse, c a d celle qui a besoin de perdre en lien
            int degreeToChange = kvTopXA.keySet().iterator().next();
            // On prend la liste des noeuds possédants ce degrée
            List<Integer> nodesWithDegree = kvIndexNodes.get(degreeToChange);

            int nbChangement = ddDiff[degreeToChange];
            int nbDown = kvDegreeSumDiffAsc.get(degreeToChange);
            int nbUp = nbChangement - nbDown;

            nodesCandidateToAdd.clear();
            supFind = false;
            nodeToRmvEdge = null;

            // on va élaguer ce niveau de degrée pour le mettre a ce qu'il faudrait
            for (Integer integer : nodesWithDegree) {
                nodeActif = graphUnderWork.getNode(integer);
                // ON VA RELINKER DEPUIS CE NOEUD VERS UN NOEUD QUI EN A BESOIN

                // Si la sum des éléments précédents est inférieur 0 il faut ajouter un lien a l'actif depuis un degré inf
                if (nbDown < 0) {
                    // On collecte l'ensemble des noeuds au degré inférieur qui veulent relinker vers l'actif
                    for (int i = 0; i < nodeActif.getDegree(); i++) {
                        nodesCandidateToAdd.addAll(kvIndexNodes.get(i));
                    }
                    for (Integer nodeIndexEnDessous : nodesCandidateToAdd) {
                        nodeTournant = graphUnderWork.getNode(nodeIndexEnDessous);
                        // On regarde si le degree de ce noeud a besoin d'un ajout
                        degreeDiff = kvDegreeDiff.get(nodeTournant.getDegree());
                        // if(degreeDiff )
                    }


                }


                // on regarde a qui il est connecté
                for (Edge edge1 : nodeActif.getEachEdge()) {
                    nodeTournant = edge1.getNode1() == nodeActif ? edge1.getNode0() : edge1.getNode1();


                    // Dans le cas ou on a un degré qui a besoin d'etre diminuer, on peut supprimer ce lien
//                    if(degreeDiff < 0){
//                        supFind = true;
//                        nodeToRmvEdge = nodeTournant;
//                        break;
//                    }
                }

                // si on a trouvé parmi les noeuds associés a ce noeud [de plus haut degré de diff] une suppr. possible
                if (supFind) {

                }
            }


        }


    };

    LinkAdder trveddAdd = (target, graph) -> {
        // structures utilisées pour liste de priorité
        int[] ddwanted, ddActual; // distribution de degrée voulue et actuelle
        double[] ddDiffRatio = new double[target.nbNode]; // % de diff entre la DD voulu et en construction
        double[] ddTransitionRatioAdd = new double[target.nbNode]; // Priorité des degrés a atteindre
        double[] ddTransitionRatioRmv = new double[target.nbNode]; // Priorité des degrés a atteindre dans le cas de retrait

        // Autres stuctures
        Map<Integer, Double> kvDdTrRatio = new HashMap<>(); // K:Degree V:Transition ratio
        Map<Integer, Double> kvDdTrRatioRmv = new HashMap<>(); // K:Degree V:Transition ratio pour le retrait
        Map<Integer, Double> kvTopXDdTrRatio; // K:Degree V:Transition ratio - Top X
        Map<Integer, Double> kvRouletteReceveur = new LinkedHashMap<>(); // K:Degree V:sum(Transition Ratio) - Pour les noeuds qui recoivent
        double rltAddMax;
        Map<Integer, Double> kvRouletteAjouteur = new LinkedHashMap<>(); // K:Degree V:sum(Transition Ratio) - Pour les noeuds qui ajoutent
        Map<Integer, List<Integer>> kvIndexNodes = new HashMap<>(); // K:Degree V:Liste d'index des nodes@this degree

        // Variables
        List<Integer> nodesIndex;
        Integer nbEdgeRemaining = target.nbEdges;
        boolean again = true;

        // Variables de fonctionnenemnt
        double addedRlt;
        double selectedRlt;
        int nodeOneIndex;
        int nodeTwoIndex;
        Node nodeOne;
        Node nodeTwo;
        Edge edge;
        boolean needToRemove = false;
        int nbedgeToAdd;

        // Some initialisation
        ddwanted = target.DistribDegree;
        ddActual = new int[target.nbNode];
        ddActual[0] = target.nbNode; // initialisation de la dd du réseau a vide

        graph.display();

        //        Stream<Map.Entry<Integer,Double>> sorted;
        //        sorted = kvDdTrRatio.entrySet().stream().sorted(Map.Entry.comparingByValue());

        while (again) {

            needToRemove = true;
            kvRouletteAjouteur.clear();
            kvRouletteReceveur.clear();
            rltAddMax = 0;
            nodeOneIndex = 0;
            nbedgeToAdd = ((int) Math.ceil((nbEdgeRemaining * (.05)))) * 2;

            majWeight(target, graph, ddwanted, ddActual, ddDiffRatio, ddTransitionRatioAdd, kvDdTrRatio, ddTransitionRatioRmv, kvDdTrRatioRmv);
            majDegreeNode(graph, kvIndexNodes);

            // On sort Question tag
            kvTopXDdTrRatio = kvDdTrRatio.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(target.nbNode / 10)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            //region CREATION D'UNE KV NOEUDS/PRIORITE

            // Pour chaque degré du top X qui a besoin de lien
            for (Integer integ : kvTopXDdTrRatio.keySet()) {
                // On prend la liste des noeuds possédant ce degré
                nodesIndex = kvIndexNodes.get(integ);
                // Si aucun noeud n'a ce degré, on prend les noeuds de degré inférieur
                while (nodesIndex == null && integ >= 0) {
                    nodesIndex = kvIndexNodes.get(--integ);
                }
                // si tjrs rien, on passe au degré suivant de la liste
                if (nodesIndex == null)
                    continue;
                // TODO Implique qu'on ajoute qu'un lien par degré a chaque itéraltion
                // or il peut y avoir de grande disparité de besoin.

                // on ajoute tout ces index de noeuds de degré integ avec leur importance
                // de transition
                for (Integer index : nodesIndex) {
                    rltAddMax += kvDdTrRatio.get(integ);
                    kvRouletteAjouteur.put(index, rltAddMax);
                }
            }

            //endregion

            //region REPETER X FOIS SELECTION D'UN NOEUD D'AJOUT
            for (int i = 0; i < nbedgeToAdd; i++) {
                int sltBla = (int) (rltAddMax * 10000);
                selectedRlt = Toolz.getRandomNumber((sltBla) / 10000);

                for (Integer integer : kvRouletteAjouteur.keySet()) {
                    if (kvRouletteAjouteur.get(integer) >= selectedRlt) { // bingo loto
                        nodeOneIndex = integer;
                        break;
                    }
                }

                nodeOne = graph.getNode(nodeOneIndex);

                // On cherche un noeud auquel il n'est pas connecté et qui est aussi en besoin de lien
                addedRlt = 0;
                kvRouletteReceveur.clear();
                // On rempli l'ensemble des noeuds possible pour un ajout de lien
                for (Node aNode : graph.getNodeSet()) {
                    if (aNode != nodeOne) { // si pas le noeud courant
                        // Check si le lien n'existe pas
                        if (graph.getEdge(aNode + ":" + nodeOneIndex) == null
                                && graph.getEdge(nodeOneIndex + ":" + aNode) == null) {
                            // Ajout dans une keylist "roulette"
                            kvRouletteReceveur.put(aNode.getIndex(), addedRlt += kvDdTrRatio.get(aNode.getIndex()));
                        }
                    }
                }

                // Tirage d'un node de la keylist

                int slt = (int) (addedRlt * 10000);
                selectedRlt = Toolz.getRandomNumber(slt) / 10000;
                nodeTwoIndex = 0;

                for (Integer integer : kvRouletteReceveur.keySet()) {
                    if (kvRouletteReceveur.get(integer) >= selectedRlt) { // bingo loto
                        nodeTwoIndex = integer;
                        break;
                    }
                }

                modifyEdge(graph, nodeOneIndex, nodeTwoIndex, ddActual, true);
                nbEdgeRemaining--;
                needToRemove = false;
            }

            //endregion

            // Dans le cas ou il reste des edges a ajouter
            if (nbEdgeRemaining != 0 && needToRemove) {
                while (nbEdgeRemaining != 0) {
                    for (Integer integer : kvDdTrRatio.keySet()) { // degré qui a le plus besoin d'un up
                        // On prend la liste des noeuds possédant ce degré
                        nodesIndex = kvIndexNodes.get(integer);
                        for (Integer nodeIndex : nodesIndex) {

                        }
                    }

                }
            }

            // si on a ajouté tout les noeuds, on va en virer de ceux qui en ont le moins besoin
            if (nbEdgeRemaining == 0 || needToRemove) {
                int nbRetrait = Toolz.getRandomNumber(graph.getNodeCount() / 20);

                for (int i = 0; i < nbRetrait; i++) {
                    kvTopXDdTrRatio = kvDdTrRatioRmv.entrySet().stream()
                            .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
//                        .limit(1)
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                    // On a l'ordre index qui a le moins besoin d'un ajout
                    for (Integer integer : kvTopXDdTrRatio.keySet()) {
                        nodesIndex = kvIndexNodes.get(integer);
                        if (nodesIndex == null)
                            continue;

                        nodeOneIndex = Toolz.getRandomElement(nodesIndex);
                        nodeOne = graph.getNode(nodeOneIndex);

                        if (nodeOne.getDegree() > 0) {
                            // On trouve un edge qui possede ce noeud
                            edge = nodeOne.getEdge(nodeOne.getDegree() - 1);
                            nodeTwo = edge.getNode1() == nodeOne ? edge.getNode0() : edge.getNode1();
                            modifyEdge(graph, nodeOne.getIndex(), nodeTwo.getIndex(), ddActual, false);
                            nbEdgeRemaining++;
                        }
                    }
                }

            }

//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException ie) {
//
//            }


            again = false;
            for (int i = 0; i < ddwanted.length; i++) {
                if (ddwanted[i] != ddActual[i]) {
                    again = true;
                }
            }

        }
    };

    public void majWeightRelink(TargetStructure target, int[] ddwanted, int[] ddActual, int[] ddDiff,
                                Map<Integer, Integer> kvDegreeDiff, Map<Integer, Integer> kvDegreeSumDiffAsc) {
        /** Si trop de noeud, mettre (en frac?) 5%
         *  Si manque un noeud frac de 30% + 70% ?
         *
         */
        double pourcent;
        int diff;
        int sum = 0;

        // Liste de pourcentage de manquement pour atteindre le bon # noeud par degré
        for (int i = 0; i < target.nbNode; i++) {
            // Si la diff est négative, on a trop de lien. pourcentage "négatif". Si positif, il en manque. Si zero, on en a le bon nombre. Wanted
            // est à +1 pour pouvoir diviser directement
            diff = ddwanted[i] - ddActual[i];
            ddDiff[i] = diff;
            sum += diff;

            kvDegreeDiff.put(i, diff);
            kvDegreeSumDiffAsc.put(i, sum);
        }


    }

    public void majWeight(TargetStructure target, Graph graph, int[] ddwanted, int[] ddActual, double[] ddDiff, double[] ddTransitionRatio, Map<Integer, Double> kvDdTrRatio,
                          double[] ddTransitionRatioRmv, Map<Integer, Double> kvDdTrRatioRmv) {
        /** Si trop de noeud, mettre (en frac?) 5%
         *  Si manque un noeud frac de 30% + 70% ?
         *
         */
        double pourcent;
        int diff;
        int fracToMany = 5;
        int fracNeedMore = 30;
        int fracFine = 15;
        int offsetNotEnought = 100 - fracToMany - fracNeedMore;

        // Liste de pourcentage de manquement pour atteindre le bon # noeud par degré
        for (int i = 0; i < target.nbNode; i++) {
            // Si la diff est négative, on a trop de lien. pourcentage "négatif". Si positif, il en manque. Si zero, on en a le bon nombre. Wanted
            // est à +1 pour pouvoir diviser directement
            diff = ddwanted[i] - ddActual[i];

            // Si le noeud a trop de lien
            if (diff < 0) {
                pourcent = (-1. / diff) * fracToMany; // plus la diff est grand plus on arriver vers 0%
            }
            // Si le noeud manque de lien
            else if (diff > 0) {
                pourcent = ((double) diff / ddwanted[i]) * fracNeedMore + offsetNotEnought; // Ratio a quel point il manque de noeud
            }
            // Si on a le bon nombre de noeud
            else {
                pourcent = fracFine;
            }

            ddDiff[i] = pourcent;

            // Tableau de transition
            // Pour la premiere case, donc les noeuds a degré un, ca sera de tte facon rempli en premier avec le premier ajout

            if (i == 0) { // sera mis a jour a la prochaine itération ( inutile )?
                ddTransitionRatio[i] = fracFine; // Fine pour l'ajout
                ddTransitionRatioRmv[i] = 0; // Fine pour le retrait
            } else {
                // on utilise le ratio du degré courant et du suivant pour déterminer le bénéfice d'un ajout de lien
                // donc en pratique du courant et du précédent pour déterminer la valeur sur le précédent
                // a quelle point on manque du ratio courant - a quelle point le précédent est réalisé
                // aucune valeur a 0%
                pourcent = ((double) (ddDiff[i - 1] + ddDiff[i]) / 2);
                ddTransitionRatio[i - 1] = pourcent;
                ddTransitionRatioRmv[i] = pourcent;
            }
            // sur la derniere itération on set aussi le pourcentage courant
            if (i == target.nbNode - 1) {
                ddTransitionRatio[i] = fracFine; // Utilisé pour évalué a qui s'ajouter
                pourcent = ((double) (ddDiff[i - 1] + ddDiff[i]) / 2);
                ddTransitionRatioRmv[i] = pourcent;
            }
        }

        kvDdTrRatio.clear();

        for (int i = 0; i < ddTransitionRatio.length; i++) {
            kvDdTrRatio.put(i, ddTransitionRatio[i]);
            kvDdTrRatioRmv.put(i, ddTransitionRatioRmv[i]);
        }
    }

    /**
     * Liste des degrée des noeuds, la clef étant le degrée
     *
     * @param graph
     * @param kvIndexNodes
     */
    public void majDegreeNode(Graph graph, Map<Integer, List<Integer>> kvIndexNodes) {
        kvIndexNodes.clear();
        for (Node node : graph) {
            Toolz.addElementInMap(kvIndexNodes, node.getDegree(), node.getIndex());
        }
    }

    public void modifyEdge(Graph graph, int nodeOneIndex, int nodeTwoIndex, int[] ddActual, boolean addRatherThanRemove) {
        if (addRatherThanRemove) {
            ddActual[graph.getNode(nodeOneIndex).getDegree()]--;
            ddActual[graph.getNode(nodeTwoIndex).getDegree()]--;
            // Ajout du lien
            graph.addEdge(nodeOneIndex + ":" + nodeTwoIndex, nodeOneIndex, nodeTwoIndex, false);
            ddActual[graph.getNode(nodeOneIndex).getDegree()]++;
            ddActual[graph.getNode(nodeTwoIndex).getDegree()]++;

        } else {
            ddActual[graph.getNode(nodeOneIndex).getDegree()]--;
            ddActual[graph.getNode(nodeTwoIndex).getDegree()]--;
            if (graph.getEdge(nodeOneIndex + ":" + nodeTwoIndex) != null)
                graph.removeEdge(nodeOneIndex + ":" + nodeTwoIndex);
            else
                graph.removeEdge(nodeTwoIndex + ":" + nodeOneIndex);
            // Ajout du lien
            ddActual[graph.getNode(nodeOneIndex).getDegree()]++;
            ddActual[graph.getNode(nodeTwoIndex).getDegree()]++;

        }
    }

    public boolean graphNotLinked(Graph graph, int nodeIndexOne, int nodeIndexTwo) {
        return (graph.getEdge(nodeIndexOne + ":" + nodeIndexTwo) == null
                && graph.getEdge(nodeIndexTwo + ":" + nodeIndexOne) == null);
    }

    /**
     * Classe qui contient un "décompte" des propriétés a atteindre
     */
    class TargetStructure {
        public TargetStructure() {

        }

        int nbNode;
        int nbEdges;
        int[] DistribDegree;
        double ccAvailable;
    }
}


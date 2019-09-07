/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn;

import com.mycompany.complexity.tool.mvn.Nodes.Edge;
import com.mycompany.complexity.tool.mvn.Nodes.Node;
import edu.uci.ics.jung.graph.Tree;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

/**
 *
 * @author helenocampos
 */
public class GraphAnalysis {

    private Node graphRoot;
    private Node graphExit;
    private LinkedList<Path> paths;


    public GraphAnalysis(Node root, Node exit) {
        this.graphRoot = root;
        this.graphExit = exit;
        paths = new LinkedList<>();
    }

    public void analyzeGraph() {
        int id = 1;

        Stack<Node> basisPathNodes = new Stack<>();
        Stack<Node> predicateNodes = new Stack();
        Stack<Node> allPredicateNodes = new Stack();
        traverseGraph(graphRoot, basisPathNodes, graphExit, false, predicateNodes, allPredicateNodes);

        paths.add(new Path(id++, basisPathNodes));
        Stack<Node> lastPathNodes = basisPathNodes;
        while (predicateNodes.size() > 0) {
            Node predicateNode = predicateNodes.pop();

            Stack<Node> derivatedPathNodes = copyPathUntilNode(lastPathNodes, graphRoot, predicateNode);
            Node nodeAfterPredicate = getNodeAfter(lastPathNodes, predicateNode);
            Node flippedNode = flipPredicateNode(predicateNode, nodeAfterPredicate);

            if (Node.contains(basisPathNodes, flippedNode.getId())) {
                if (flippedNode != graphExit) {
                    derivatedPathNodes.addAll(copyPathUntilNode(basisPathNodes, flippedNode, predicateNode));
                }else{
                    derivatedPathNodes.add(flippedNode);
                }
            } else {
                traverseGraph(flippedNode, derivatedPathNodes, graphExit, false, predicateNodes, allPredicateNodes);
            }

            paths.add(new Path(id++, derivatedPathNodes));
            lastPathNodes = derivatedPathNodes;
        }
        

    }

    private Stack<Node> copyPathUntilNode(Stack<Node> basePath, Node starterNode, Node lastNode) {
        Stack<Node> newPath = new Stack<>();
        Iterator iterator = basePath.listIterator();
        Node node = getNextNode(iterator);

        while (node != starterNode && node != null) {
            node = getNextNode(iterator);
        }
        while (node != lastNode && node != null) {
            newPath.push(node);
            node = getNextNode(iterator);
        }

        if (node != null) {
            newPath.push(node);
        }

        return newPath;
    }

    private Node getNodeAfter(Stack<Node> nodes, Node node) {
        if (node != null) {
            Iterator iterator = nodes.listIterator();
            Node nextNode = getNextNode(iterator);
            while (nextNode != node && nextNode != null) {
                nextNode = getNextNode(iterator);
            }
            return getNextNode(iterator);
        }
        return null;
    }

    private Node flipPredicateNode(Node predicate, Node actualNext) {
        if (actualNext == predicate.getLeft()) {
            return predicate.getRight();
        } else {
            return predicate.getLeft();
        }
    }

    private Node getNextNode(Iterator iterator) {
        if (iterator.hasNext()) {
            return (Node) iterator.next();
        } else {
            return null;
        }
    }

    private boolean traverseGraph(Node node, Stack<Node> path, Node exitNode, boolean exitNodeReached, Stack<Node> predicateNodes, Stack<Node> allPredicateNodes) {
        // traverse the graph looking for if nodes without right
        if (node != null) {
            path.push(node);

            if (Node.isPredicate(node)) {
                if (!allPredicateNodes.contains(node)) {
                    predicateNodes.push(node);
                    allPredicateNodes.push(node);
                }
            }

            if (node.equals(exitNode)) {
                return true;
            } else {
                Node left = node.getLeft();
                if (left != null) {
                    if (!path.contains(left) || Node.isLoopNode(left)) {
                        exitNodeReached=traverseGraph(left, path, exitNode, exitNodeReached, predicateNodes, allPredicateNodes);
                    }
                }
                if (!exitNodeReached) {
                    Node right = node.getRight();
                    if (right != null) {
                        if (!path.contains(right)) {
                           exitNodeReached=traverseGraph(right, path, exitNode, exitNodeReached, predicateNodes, allPredicateNodes);
                        }
                    }
                }
                return exitNodeReached;
            }

        }
        return true;

    }

//    private void printPaths() {
//        for (Path path : paths) {
//            System.out.println("Path " + path.getId());
//            for (Node node : path.getNodes()) {
//                System.out.print(node.getId() + " ");
//            }
//            System.out.println(" ");
//        }
//    }

    public LinkedList<String> getPathsString() {
        LinkedList<String> pathsString = new LinkedList<>();
        for (Path path : paths) {
            String pathString = "";
            for (Node node : path.getNodes()) {
                pathString = pathString.concat(node.getId() + " ");

            }
            pathsString.add(pathString);
        }

        return pathsString;
    }

    public LinkedList<Path> getPaths() {
        return this.paths;
    }

}

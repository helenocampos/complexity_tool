/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn.Nodes;

import com.github.javaparser.ast.stmt.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author helenocampos
 */
public class Node implements Comparable<Node> {

    private int id;
    private Node left;
    private Node right;
    private NodeType type;
    private boolean rendered = false;
    private boolean hasElse = false;
    private boolean hasBreak = false;
    private Node parent;
    private boolean locked = false;
    private Statement baseStatement;
    private int degree = 0;
    
    /**
     * @return the locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * @param locked the locked to set
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Statement getBaseStatement() {
        return baseStatement;
    }

    public String getStatementText() {
        if (this.type.equals(this.type.EXIT)) {
            return "Exit Node";
        }
        String wholeStatement = this.baseStatement.toStringWithoutComments();
        return wholeStatement.substring(0, wholeStatement.indexOf("\n"));
    }

    public void setBaseStatement(Statement baseStatement) {
        this.baseStatement = baseStatement;
    }

    public boolean HasBreak() {
        return hasBreak;
    }

    public void setHasBreak(boolean hasBreak) {
        this.hasBreak = hasBreak;
    }

    @Override
    public int compareTo(Node o) {
        if (o instanceof Node) {
            Node node = (Node) o;
            if (this.id > node.getId()) {
                return 1;
            } else if (this.id < node.getId()) {
                return -1;
            }
        }
        return 0;
    }

    public enum NodeType {

        IF,
        BLOCK,
        CASE_BLOCK,
        CASE,
        FOR,
        FOREACH,
        WHILE,
        DO,
        LOOP_EXIT,
        EXIT;
    }

    public Node(int id, NodeType type, Statement stmt, int degree) {
        this.id = id;
        this.type = type;
        this.baseStatement = stmt;
        this.degree = degree;
    }

    public Node() {

    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "nodeId: " + id + " " + this.baseStatement.toString();
    }

    public static Node getNode(List<Node> nodes, int id) {
        for (Node node : nodes) {
            if (node.getId() == id) {
                return node;
            }
        }
        return null;
    }

    public boolean isRendered() {
        return rendered;
    }

    public void setRendered(boolean rendered) {
        this.rendered = rendered;
    }

    public void setHasElse(boolean hasElse) {
        this.hasElse = hasElse;
    }

    public boolean hasElse() {
        return hasElse;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public static boolean isChild(Node parent, Node node) {
        while (parent != null) {
            if (node.getParent().equals(parent)) {
                return true;
            } else {
                parent = node.getParent();
            }
        }
        return false;
    }

    public static boolean isPredicate(Node node) {
        if (node != null) {
            switch (node.getType()) {
                case CASE:
                case IF:
                case WHILE:
                case DO:
                case FOR:
                case FOREACH:
                    return true;
            }
            return false;
        } else {
            return false;
        }
    }

    public static boolean isLoopNode(Node node) {
        String className = node.getClass().getSimpleName();
        List<String> classes = Arrays.asList("WhileNode", "ForNode", "DoNode", "ForEachNode");
        return classes.contains(className);

    }

    public static boolean contains(Stack<Node> nodes, int id) {
        for (Node node : nodes) {
            if (node.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public String getPredicateText() {
        return baseStatement.toStringWithoutComments();
    }

    public static Node getNextNode(Stack<Node> nodes, Node targetNode) {
        Iterator<Node> nodesIterator = nodes.listIterator();
        while (nodesIterator.hasNext()) {
            Node node = nodesIterator.next();
            if (node == targetNode) {
                return nodesIterator.next();
            }
        }
        return null;
    }

    //Counts the number of parents couting +1 if child Node is right and -1 if child Node is left
    public static int getNumberOfParents(List<Node> nodes, Node actual) {
        int numberOfParents = 0;
        Iterator<Node> nodesIterator = nodes.listIterator();
        while (nodesIterator.hasNext()) {
            Node node = nodesIterator.next();
            if (node.getLeft() != null && node.getLeft().equals(actual)) {
                numberOfParents--;
            }
            if (node.getRight() != null && node.getRight().equals(actual)) {
                numberOfParents++;
            }
        }
        return numberOfParents;
    }

    public static List<Node> resetNodesRenderingStates(List<Node> nodes) {
        for (Node node : nodes) {
            if (node.isLocked()) {
                node.setLocked(false);
            }

            if (node.isRendered()) {
                node.setRendered(false);
            }
        }
        return nodes;
    }

    public static List<Node> getIncidentNodes(List<Node> nodes, Node node) {
        List<Node> incidentNodes = new LinkedList<>();
        for (Node actualNode : nodes) {
            if (!actualNode.equals(node)) {
                if (actualNode.getLeft() != null && actualNode.getLeft().equals(node)) {
                    if (!incidentNodes.contains(actualNode)) {
                        incidentNodes.add(actualNode);
                    }
                }
                if (actualNode.getRight() != null && actualNode.getRight().equals(node)) {
                    if (!incidentNodes.contains(actualNode)) {
                        incidentNodes.add(actualNode);
                    }
                }
            }
        }
        return incidentNodes;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public void increaseDegree() {
        this.degree++;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn.Nodes;

import java.util.Stack;

/**
 *
 * @author helenocampos
 */
public class Edge{

    private int id;
    private Node sourceNode;
    private Node destinationNode;

    public Edge(int id, Node sourceNode, Node destinationNode) {
        this.id = id;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(Node sourceNode) {
        this.sourceNode = sourceNode;
    }

    public Node getDestinationNode() {
        return destinationNode;
    }

    public void setDestinationNode(Node destinationNode) {
        this.destinationNode = destinationNode;
    }

    public static boolean contains(Edge edge, Stack<Node> nodes) {
        for (int i = 0; i < nodes.size() - 1; i++) {
            Node sourceNode = nodes.get(i);
            Node destinationNode = nodes.get(i + 1);
            if (edge.getSourceNode().getId() == sourceNode.getId()
                    && edge.getDestinationNode().getId() == destinationNode.getId()) {
                return true;
            }

        }
        return false;
    }

   
}

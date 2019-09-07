/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn;

import com.mycompany.complexity.tool.mvn.Nodes.Node;
import java.util.LinkedList;
import java.util.Stack;

/**
 *
 * @author helenocampos
 */
public class Path {

    private int id;
    private Stack<Node> nodes;

    public Path(int id, Stack<Node> nodes){
        this.id =id;
        this.nodes = nodes;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Stack<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Stack<Node> nodes) {
        this.nodes = nodes;
    }

    public String getPathString() {
        String pathString = this.getId()+": ";
        for (Node node : nodes) {
            pathString = pathString.concat(node.getId()+" ");
        }
        return pathString;
    }

    @Override
    public String toString() {
        return getPathString();
    }
    
    private LinkedList<Node> getPredicateNodes(){
        LinkedList<Node> predicates = new LinkedList<>();
        for(Node node: nodes){
            if(Node.isPredicate(node)){
                predicates.add(node);
            }
        }
        return predicates;
    }
    
    public String[][] getPredicateData(){
        LinkedList<Node> predicates = getPredicateNodes();
        String[][] predicateData = new String[predicates.size()][3];
        int count=0;
        
        for(Node node: predicates){
            predicateData[count][0]=Integer.toString(node.getId());
            predicateData[count][1]=node.getPredicateText();
            predicateData[count][2]=getCondition(node);
            count++;
        }
        return predicateData;
    }
    
    private String getCondition(Node predicate){
        Node nextNode = Node.getNextNode(nodes, predicate);
        if(nextNode!=null){
            if(nextNode==predicate.getLeft()){
                return "TRUE";
            }else if(nextNode==predicate.getRight()){
                return "FALSE";
            }
        }
        return " - ";
    }
}

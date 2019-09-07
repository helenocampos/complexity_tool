/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn.Nodes;

import com.github.javaparser.ast.stmt.Statement;

/**
 *
 * @author Heleno
 */
public abstract class LoopNode extends Node{

    private Node exitNode;

    public LoopNode(int id, NodeType nodeType, Statement statement, int degree){
        super(id, nodeType, statement, degree);
    }
    
    public Node getExitNode() {
        return exitNode;
    }

    public void setExitNode(Node exitNode) {
        this.exitNode = exitNode;
    }
    

    
    
   
    
    
}

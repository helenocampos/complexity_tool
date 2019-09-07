/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn.Nodes;

import com.github.javaparser.ast.stmt.Statement;

/**
 *
 * @author helenocampos
 */
public class LoopExitNode extends Node{
    private Node loopNode;

    public LoopExitNode(int id, Node.NodeType type, Statement statement, int degree) {
        super(id, type, statement, degree);
    }
    
    public LoopExitNode(int id, Node loopNode){
        this.loopNode = loopNode;
        this.setId(id);
        this.setType(NodeType.LOOP_EXIT);
    }
    
    public Node getLoopNode() {
        return loopNode;
    }

    public void setLoopNode(Node loopNode) {
        this.loopNode = loopNode;
    }
    
    @Override
    public String toString(){
       return "nodeId: " + this.getId() + "  , LOOP_EXIT_NODE "+" loopNode id: "+loopNode.getId(); 
    }
    
    public String getStatementText() {
        return "Loop Exit Node, loop Node: " + loopNode.getId();
    }
    
     public Statement getBaseStatement() {
        return this.loopNode.getBaseStatement();
    }
    
}

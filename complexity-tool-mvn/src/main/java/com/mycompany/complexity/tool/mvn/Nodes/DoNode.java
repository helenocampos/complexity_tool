/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn.Nodes;

import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.Statement;

/**
 *
 * @author helenocampos
 */
public class DoNode extends Node{
    private Node rootNode;
     public DoNode(int id, NodeType type,Statement statement, int degree) {
        super(id, type, statement, degree);
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }
     
     public String getStatementText() {
        String wholeStatement = this.getBaseStatement().toStringWithoutComments();
        String[] lines = wholeStatement.split("\n");
        return lines[lines.length-1];
     }
     
       public String getPredicateText(){
           DoStmt dostmt = (DoStmt) getBaseStatement();
           
          return dostmt.getCondition().toString();
      }
}

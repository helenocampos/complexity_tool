/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn.Nodes;

import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;

/**
 *
 * @author helenocampos
 */
public class SwitchCaseNode extends Node{
    public SwitchCaseNode(int id, NodeType type,Statement statement, int degree) {
        super(id, type, statement, degree);
    }
      public String getPredicateText(){
          SwitchEntryStmt casestmt = (SwitchEntryStmt)getBaseStatement();
          
          return casestmt.getLabel().toString();
      }
}

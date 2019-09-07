/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn.Nodes;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;

/**
 *
 * @author helenocampos
 */
public class IfNode extends Node {

    private boolean hasElse = false;
    private Expression condition;
    
    public IfNode(int id, NodeType type, Statement statement, int degree) {
        super(id, type, statement, degree);
    }

    public IfNode() {

    }

    public void setHasElse(boolean hasElse) {
        this.hasElse = hasElse;
    }

    public boolean hasElse() {
        return hasElse;
    }

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        if(condition!=null){
//            return "nodeId: " + getId() + " " + condition.toString();
            return "if ( "+condition.toString()+" )";
        }else{
            return super.toString();
        }
        
    }
    
      public String getStatementText(){
        String text = "if ("+condition+")";
        return text;
    }
      
      public String getPredicateText(){
          return condition.toString();
      }

}

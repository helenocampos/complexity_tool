/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn.refactorer;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.IfStmt;

/**
 *
 * @author helenocampos
 */
public class IfRefactorContainer {
   private Expression condition;
   private IfStmt createdIf;
   private String lastCall;

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public IfStmt getCreatedIf() {
        return createdIf;
    }

    public void setCreatedIf(IfStmt createdIf) {
        this.createdIf = createdIf;
    }

    public String getLastCall() {
        return lastCall;
    }

    public void setLastCall(String lastCall) {
        this.lastCall = lastCall;
    }
   
   
}

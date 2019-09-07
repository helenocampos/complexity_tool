/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.IfStmt;

import java.util.ArrayList;

/**
 *
 * @author helenocampos
 */
public class ConditionContainer {
    private IfStmt ifstmt;
    private ArrayList<Expression> conditions;

    public ConditionContainer(IfStmt ifstmt){
        this.conditions = new ArrayList<>();
        this.ifstmt = ifstmt;
    }
    
    public IfStmt getIfstmt() {
        return ifstmt;
    }

    public void setIfstmt(IfStmt ifstmt) {
        this.ifstmt = ifstmt;
    }

    public ArrayList<Expression> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<Expression> conditions) {
        this.conditions = conditions;
    }
    
    public void parseEqualsConditions(){
        if(ifstmt!= null){
            parseCondition(ifstmt.getCondition());
        }
    }
    
    private void parseCondition(Expression condition) {
        if (condition.getClass().equals(BinaryExpr.class)) {
            BinaryExpr binaryCondition = (BinaryExpr) condition;
            if (binaryCondition.getOperator().equals(BinaryExpr.Operator.equals)) {
                conditions.add(condition);
            }else{
                parseCondition(binaryCondition.getLeft());
                parseCondition(binaryCondition.getRight());
            }
        } else if (condition.getClass().equals(EnclosedExpr.class)) {
            EnclosedExpr enclosedCondition = (EnclosedExpr) condition;
            parseCondition(enclosedCondition.getInner());
        }
    }
    
    public int getConditionIndex(Expression condition){
        return conditions.indexOf(condition);
    }

}

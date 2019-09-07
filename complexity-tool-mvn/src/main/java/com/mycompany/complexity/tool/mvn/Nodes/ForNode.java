/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn.Nodes;

import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.Statement;

/**
 *
 * @author Heleno
 */
public class ForNode extends LoopNode {

    private ForStmt forstmt;

    public ForNode(int id, Node.NodeType type, Statement statement, int degree) {
        super(id, type, statement, degree);
    }

    /**
     * @return the forstmt
     */
    public ForStmt getForstmt() {
        return forstmt;
    }

    /**
     * @param forstmt the forstmt to set
     */
    public void setForstmt(ForStmt forstmt) {
        this.forstmt = forstmt;
    }

    public String getPredicateText() {
        return getForstmt().getCompare().toString();
    }

}

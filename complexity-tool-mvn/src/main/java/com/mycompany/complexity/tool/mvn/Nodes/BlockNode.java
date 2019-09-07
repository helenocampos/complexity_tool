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
public class BlockNode extends Node {

    private boolean hasBreak;
    private boolean hasReturn;

    public BlockNode(int id, NodeType type, Statement statement, int degree) {
        super(id, type, statement, degree);
    }

    public boolean getHasReturn() {
        return this.isHasReturn();
    }

    public void setHasReturn(boolean hasReturn) {
        this.hasReturn = hasReturn;
    }

    public boolean isHasBreak() {
        return hasBreak;
    }

    public void setHasBreak(boolean hasBreak) {
        this.hasBreak = hasBreak;
    }

    @Override
    public String toString() {
        String blockText = this.getBaseStatement().toStringWithoutComments();
        if (blockText.length() > 50) {
            return blockText.substring(0, 50).concat("  [...] ");
        }
        return blockText;
    }

    @Override
    public String getStatementText() {
        return this.getBaseStatement().toStringWithoutComments();
    }

    public boolean isHasReturn() {
        return hasReturn;
    }

}

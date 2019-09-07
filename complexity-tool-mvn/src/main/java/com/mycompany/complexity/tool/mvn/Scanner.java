/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.System.in;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public class Scanner {

    private Parser parser = null;
    private HashMap<String,Type> variablesTable = new HashMap<>();
    private HashMap<String,Type> privateVariablesTable = new HashMap<>();


    public Scanner(Parser parser) {
        this.parser = parser;
    }

    public void scan(String fileName) {
        try {
            FileInputStream in = new FileInputStream(fileName);
            CompilationUnit cu;
            cu = JavaParser.parse(in);
            new MethodVisitor().visit(cu, null);
        } catch (FileNotFoundException e) {
            System.out.println(fileName + " not found.");
        } catch (ParseException e) {
            System.out.println("Parse exception. Details: " + e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                System.out.println("IO Exception. Details: " + e.getMessage());
            }
        }

    }

    /**
     * @return the variablesTable
     */
    public HashMap<String,Type> getVariablesTable() {
        return variablesTable;
    }

    public HashMap<String, Type> getPrivateVariablesTable() {
        return privateVariablesTable;
    }

    private class MethodVisitor extends VoidVisitorAdapter {

//        @Override
//        public void visit(MethodDeclaration n, Object arg) {
//            int stmtCount = n.getBody().getStmts().size();
//            for (Statement stmt : n.getBody().getStmts()) {
//                parser.parseManager(stmt, --stmtCount);
//
//            }
//            parser.connectClosingNode(new ReturnStmt(n.getEndLine(), 1, n.getEndLine(), 1, null));
//        }

        @Override
        public void visit(VariableDeclarationExpr n, Object arg) {
            List<VariableDeclarator> myVars = n.getVars();
            for (VariableDeclarator vars : myVars) {
                System.out.println("Variable Name: " + vars.getId().getName() + " Type: " + n.getType());
                getVariablesTable().put(vars.getId().getName(), n.getType());
            }
        }
    }

    public void scanMethod(MethodDeclaration n, HashMap<String,Type> privateVariables) {
        List<Parameter> myParameterVars = n.getParameters();
        for (Parameter vars : myParameterVars) {
            System.out.println("Variable Name: " + vars.getId().getName() + " Type: " + vars.getType());
            getVariablesTable().put(vars.getId().getName(), vars.getType());
        }
        for (String key : privateVariables.keySet()) {
            getPrivateVariablesTable().put(key, privateVariables.get(key));
        }
        int stmtCount = n.getBody().getStmts().size();
        for (Statement stmt : n.getBody().getStmts()) {
            parser.parseManager(stmt, --stmtCount);
        }
        if (parser.getRoot() == null) {
            parser.setRoot(parser.createBlockNode(n.getBody()));
        }
        new MethodVisitor().visit(n, null);
        parser.connectClosingNode(new ReturnStmt(n.getEndLine(), 1, n.getEndLine(), 1, null));
    }

}

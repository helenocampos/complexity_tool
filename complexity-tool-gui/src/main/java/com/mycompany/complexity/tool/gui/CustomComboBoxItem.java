package com.mycompany.complexity.tool.gui;


import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;
import com.mycompany.complexity.tool.mvn.App;
import java.util.HashMap;
import javax.swing.JTextArea;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Heleno
 */
public class CustomComboBoxItem {
    private MethodDeclaration method;
    private App app;

    public CustomComboBoxItem(MethodDeclaration method,JTextArea codeArea, JTextArea statementArea, HashMap<String,Type> methodsTable, HashMap<String,Type> privateVariables){
        app = new App();
        app.processMethod(method, codeArea, statementArea, methodsTable, privateVariables);
        this.method = method;
    }
    
    /**
     * @return the method
     */
    public MethodDeclaration getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(MethodDeclaration method) {
        this.method = method;
    }
    
    public String toString(){
        return getMethod().getName();
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;
import com.mycompany.complexity.tool.mvn.Nodes.Node;
import com.mycompany.complexity.tool.mvn.refactorer.MethodRefactorer;
import com.mycompany.complexity.tool.mvn.tester.Tester;
import java.util.HashMap;
import javax.swing.JTextArea;

/**
 *
 * @author helenocampos
 */
public class App {

    private Parser parser;
    private Renderer renderer;
    private Renderer optimizedRenderer = null;
    private String code;
    private String codeOptmized;
    private String tests;
    private GraphAnalysis analysis;
    private GraphAnalysis optimizedAnalysis;
    private boolean optimizingSugestions = false;

    public void processMethod(MethodDeclaration n, JTextArea codeArea, JTextArea statementArea, HashMap<String,Type> methodsTable, HashMap<String,Type> privateVariables) {
        parser = new Parser(false);
        Scanner scanner = new Scanner(parser);
        scanner.scanMethod(n, privateVariables);
        renderer = new Renderer(parser);
        renderer.renderGraph(codeArea, statementArea, n);
        setAnalysis(new GraphAnalysis(parser.getRoot(), parser.getExitNode()));
        getAnalysis().analyzeGraph();
        setCode(n.toStringWithoutComments());
        
        /*
            Temporary pattern analysis needs to duplicate parser
        
        */
        Parser secondaryParser = new Parser(false);
        Scanner secondaryScanner = new Scanner(secondaryParser);
        secondaryScanner.scanMethod(n, privateVariables);
        setOptimizingSugestions(PatternAnalysis.analize(secondaryParser.getConditionals(),secondaryParser.getNodes(), scanner.getVariablesTable(), scanner.getPrivateVariablesTable(), methodsTable));
        Node.resetNodesRenderingStates(secondaryParser.getNodes());
        optimizedRenderer = new Renderer(secondaryParser);
        optimizedRenderer.renderGraph(codeArea, statementArea, n);
        setOptimizedAnalysis(new GraphAnalysis(secondaryParser.getRoot(), secondaryParser.getExitNode()));
        getOptimizedAnalysis().analyzeGraph();
        String refactoredMethod = MethodRefactorer.getRefactoredMethod(n.toStringWithoutComments(), parser, secondaryParser);
        setCodeOptmized(refactoredMethod);
        String unitTests = Tester.generateTests(secondaryScanner, this, n);
        setTests(unitTests);
        
        
//        List<Statement> t = n.getBody().getStmts();
//        System.out.println("Method: "+t.toString());
//        LinkedList<Node> s = secondaryParser.getConditionals();
//        System.out.println("Condicionals: "+s.toString());
//        ArrayList<Node> m = secondaryParser.getNodes();
//        System.out.println("Nodes: "+m.toString());
    }
   
    public String getTests() {
        return tests;
    }

    public void setTests(String tests) {
        this.tests = tests;
    }
    
    public Parser getParser() {
        return this.parser;
    }

    public Renderer getRenderer() {
        return this.renderer;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public GraphAnalysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(GraphAnalysis analysis) {
        this.analysis = analysis;
    }

    /**
     * @return the optimizedRenderer
     */
    public Renderer getOptimizedRenderer() {
        return optimizedRenderer;
    }

    /**
     * @param optimizedRenderer the optimizedRenderer to set
     */
    public void setOptimizedRenderer(Renderer optimizedRenderer) {
        this.optimizedRenderer = optimizedRenderer;
    }

    public boolean hasOptimizingSugestions() {
        return optimizingSugestions;
    }

    public void setOptimizingSugestions(boolean optimizingSugestions) {
        this.optimizingSugestions = optimizingSugestions;
    }

    public GraphAnalysis getOptimizedAnalysis() {
        return optimizedAnalysis;
    }

    public void setOptimizedAnalysis(GraphAnalysis optimizedAnalysis) {
        this.optimizedAnalysis = optimizedAnalysis;
    }

    public String getCodeOptmized() {
        return codeOptmized;
    }

    public void setCodeOptmized(String codeOptmized) {
        this.codeOptmized = codeOptmized;
    }
}

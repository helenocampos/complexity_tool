/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn.tester;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.mycompany.complexity.tool.mvn.App;
import com.mycompany.complexity.tool.mvn.Nodes.BlockNode;
import com.mycompany.complexity.tool.mvn.Nodes.IfNode;
import com.mycompany.complexity.tool.mvn.Nodes.Node;
import static com.mycompany.complexity.tool.mvn.Nodes.Node.NodeType.EXIT;
import com.mycompany.complexity.tool.mvn.Path;
import com.mycompany.complexity.tool.mvn.Scanner;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author Gabriel Felix and Nathan Manera Magalhães
 */
public class Tester {

    public static String generateTests(Scanner secondaryScanner, App app, MethodDeclaration m) {
        String typeVariable = "com.mycompany.complexity.tool.mvn.tester.Variable";
        String testCode = "";
        String methodName = m.getNameExpr().getName();
        String typeMethodReturn = m.getType().toString();
        if (!typeMethodReturn.equals("void")) {
            TypeDeclaration td = (TypeDeclaration) m.getParentNode();
            String className = td.getName();
            String[][] predicateData;
            LinkedList<Path> path = app.getOptimizedAnalysis().getPaths();
            String var = setNameVar(className);
            testCode += "import org.junit.Test;\n"
                    + "import static org.junit.Assert.*;\n"
                    + "import org.junit.Before;\n"
                    + "\n"
                    + "public class " + className + "Test {\n"
                    + "\n"
                    + "    " + className + " " + var + ";\n"
                    + "\n"
                    + "    @Before\n"
                    + "    public void setUp() {\n"
                    + "        " + var + " = new " + className + "();\n"
                    + "    }\n";
            for (int c = 0; c < path.size(); c++) {
                predicateData = path.get(c).getPredicateData();
                ArrayList<String> inputs = new ArrayList<>();
                ArrayList<String> inputsParameter = new ArrayList<>();
                ArrayList<String> inputsPrivate = new ArrayList<>();
                for (Parameter p : m.getParameters()) {
                    String variable = p.getId().getName();
                    inputsParameter = setInput(inputsParameter, variable,
                            p.getType() + " " + variable + " = " + "/*insert value*/;");
                }
                for (String variable : secondaryScanner.getPrivateVariablesTable().keySet()) {
                    inputsPrivate = setInput(inputsPrivate, capitalizeFirstLetter(variable),
                            var + ".set" + capitalizeFirstLetter(variable) + "(/*insert value*/);");
                }
                for (String[] pD : predicateData) {
                    Node n = getNodeById(Integer.parseInt(pD[0]), path.get(c).getNodes());
                    if (n instanceof IfNode) {
                        Expression expr = ((IfNode) n).getCondition();
//                        String text = "// to (" + pD[1] + ") be " + pD[2];
                        String variable = "";
                        String value;
                        String operator = "";
                        String boundaryValue;
                        if (expr instanceof BinaryExpr) {
                            BinaryExpr exprBinary = (BinaryExpr) expr;
                            variable = exprBinary.getLeft().toString();
                            value = exprBinary.getRight().toString();
                            boundaryValue = "/*boundary value*/";
                            if (pD[2].equals("TRUE")) {
                                operator = exprBinary.getOperator().toString();
                            } else if (pD[2].equals("FALSE")) {
                                operator = "not_" + exprBinary.getOperator().toString();
                            }
                        } else {
                            UnaryExpr ue = null;
                            if (expr instanceof UnaryExpr) {
                                ue = (UnaryExpr) expr;
                            } else if ((expr instanceof NameExpr || expr instanceof MethodCallExpr)
                                    && expr.getParentNode() instanceof UnaryExpr) {
                                ue = (UnaryExpr) expr.getParentNode();
                            }
                            operator = (ue == null || ue.getOperator() == null ? "equals" : "notEquals");
                            if (pD[2].equals("FALSE")) {
                                operator = "not_" + operator;
                            }
                            value = operator.equals("not_equals") || operator.equals("notEquals") ? "false" : "true";
                            if (ue != null) {
                                if (ue.getExpr() instanceof MethodCallExpr) {
                                    variable = ((MethodCallExpr) ue.getExpr()).getName() + "()";
                                } else if (ue.getExpr() instanceof NameExpr) {
                                    variable = ((NameExpr) ue.getExpr()).getName();
                                }
                            }
                            boundaryValue = value;
                        }
                        Type t = secondaryScanner.getVariablesTable().get(variable);
                        t = t == null ? secondaryScanner.getPrivateVariablesTable().get(variable) : t;
                        String input = "";
                        String typeName = "";
                        if (t instanceof PrimitiveType) {
                            typeName = ((PrimitiveType) t).getType().toBoxedType().getName();
                        } else if (t instanceof ReferenceType) {
                            typeName = ((ReferenceType) t).getType().toString();
                        }
                        TypeVariable tV = typeName.equals("") ? null : typeVariableFactory(typeVariable + typeName);
                        boundaryValue = tV != null ? tV.getBoundaryValue(operator, value) : boundaryValue;
                        if (t != null) {
                            if (variableIsParameter(m.getParameters(), variable)) {
                                inputsParameter = setInput(inputsParameter, variable,
                                        t + " " + variable + " = " + boundaryValue + "; "/* + text*/);
                            } else if (secondaryScanner.getPrivateVariablesTable().get(variable) != null) {
                                inputsPrivate = setInput(inputsPrivate, capitalizeFirstLetter(variable),
                                        var + ".set" + capitalizeFirstLetter(variable) + "(" + boundaryValue + "); "/* + text*/);
                            } else {
                                input = "// " + t + " " + variable + " = " + boundaryValue + "; "/* + text*/;
                            }
                        } else {
                            if (expr instanceof BinaryExpr) {
                                BinaryExpr be = (BinaryExpr) expr;
                                if (be.getRight() instanceof LiteralExpr) {
                                    String s = ((LiteralExpr) be.getRight()).getClass().getSimpleName();
                                    tV = typeVariableFactory(typeVariable + s.substring(0, s.indexOf("LiteralExpr")));
                                    boundaryValue = tV != null ? tV.getBoundaryValue(operator, value) : boundaryValue;
                                }
                            }
                            input = "// " + (variable.equals("") ? pD[1] : variable) + " = " + boundaryValue + " "/* + text*/;
                        }
                        inputs = input.equals("") ? inputs : setInput(inputs, variable, input);
                    }
                }
                String output = getPathOutput(m, path, c);
                testCode += "\n    @Test"
                        + "\n    public void test" + "_" + (c + 1) + "_" + methodName + "() {"
                        + "\n        " + printInputs(inputs) + printInputs(inputsParameter) + printInputs(inputsPrivate)
                        + typeMethodReturn + " output = " + output + ";"
                        + "\n        assertEquals(output, " + var + "." + methodName
                        + "(" + printParameters(m.getParameters()) + "));"
                        + "\n    }"
                        + "\n";
            }
            testCode += "\n}";
        } else {
            testCode = "/*\nMethod (" + methodName + ") is Void type (without return statement).\n*/";
        }
        return testCode;
    }

    private static String getPathOutput(MethodDeclaration m, LinkedList<Path> path, int c) {
        String output = "/*insert output*/";
        String returnNameExpr = "";
        for (Statement s : m.getBody().getStmts()) {
            if (s instanceof ReturnStmt) {
                ReturnStmt r = (ReturnStmt) s;
                if (r.getExpr() instanceof NameExpr) {
                    returnNameExpr = ((NameExpr) r.getExpr()).getName();
                } else {
                    output = r.getExpr().toString();
                }
                break;
            }
        }
        for (Node node : path.get(c).getNodes()) {
            if (node instanceof BlockNode && node.getLeft().getType().equals(EXIT)) {
                BlockNode bn = (BlockNode) node;
                if (bn.getBaseStatement() instanceof BlockStmt) {
                    for (Statement s : ((BlockStmt) bn.getBaseStatement()).getStmts()) {
                        if (s instanceof ReturnStmt) {
                            output = ((ReturnStmt) s).getExpr().toString();
                            break;
                        } else if (!returnNameExpr.equals("") && s instanceof ExpressionStmt) {
                            if (((ExpressionStmt) s).getExpression() instanceof AssignExpr) {
                                AssignExpr ae = (AssignExpr) ((ExpressionStmt) s).getExpression();
                                if (ae.getTarget() instanceof NameExpr
                                        && ae.getOperator().toString().equals("assign")
                                        && ((NameExpr) ae.getTarget()).getName().equals(returnNameExpr)) {
                                    output = ae.getValue().toString();
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
        return output;
    }

    private static String setNameVar(String className) {
        String varName = "";
        for (char c : className.toCharArray()) {
            if (Character.isUpperCase(c)) {
                varName += Character.toLowerCase(c);
            }
        }
        return varName.equals("") ? className.substring(0, 1) : varName;
    }

    private static String capitalizeFirstLetter(String original) {
        return original == null || original.length() == 0 ? original : original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    private static boolean variableIsParameter(List<Parameter> lp, String variable) {
        for (Parameter p : lp) {
            if (p.getId().getName().equals(variable)) {
                return true;
            }
        }
        return false;
    }

    private static String printParameters(List<Parameter> lp) {
        String params = "";
        for (int i = 0; i < lp.size(); i++) {
            params += lp.get(i).getId();
            if (i < lp.size() - 1) {
                params += ", ";
            }
        }
        return params;
    }

    private static String printInputs(ArrayList<String> inputs) {
        String p = "";
        for (String input : inputs) {
            p += input + "\n        ";
        }
        return p;
    }

    private static ArrayList<String> setInput(ArrayList<String> inputs, String variable, String input) {
        for (int i = 0; i < inputs.size(); i++) {
            if (inputs.get(i).contains(variable)) {
                inputs.set(i, input);
                return inputs;
            }
        }
        inputs.add(input);
        return inputs;
    }

    private static TypeVariable typeVariableFactory(String className) {
        Object o = instanciate(className);
        return !(o instanceof TypeVariable) ? null : (TypeVariable) o;
    }

    private static Object instanciate(String className) {
        try {
            return Class.forName(className).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            return null;
        }
    }

    private static Node getNodeById(Integer id, Stack<Node> nodes) {
        for (Node node : nodes) {
            if (node.getId() == id) {
                return node;
            }
        }
        return null;
    }
}
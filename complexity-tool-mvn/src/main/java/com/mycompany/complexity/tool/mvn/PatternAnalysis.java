/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.Type;
import com.mycompany.complexity.tool.mvn.Nodes.IfNode;
import com.mycompany.complexity.tool.mvn.Nodes.Node;
import com.mycompany.complexity.tool.mvn.refactorer.ConditionRefactorer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author helenocampos
 */
public abstract class PatternAnalysis {

    static HashMap<String, Type> variablesTable;
    static HashMap<String, Type> privateVariablesTable;
    static HashMap<String, Type> methodsTable;

    //TODO: make sure variable being analysed is not changed throughout the code
    public static boolean analize(LinkedList<Node> conditionals, ArrayList<Node> nodes, HashMap<String, Type> variables, HashMap<String,Type> privateVariables, HashMap<String, Type> methods) {
        variablesTable = variables;
        privateVariablesTable = privateVariables;
        methodsTable = methods;
        normalizeConditinals(conditionals);
        return checkPattern(conditionals, nodes);
    }

    //method to normalize conditionals
    //left side of expression always a variable
    //right side can be a variable or a value
    private static void normalizeConditinals(LinkedList<Node> conditionals) {
        for (Node node : conditionals) {
            // all the nodes are IFs, safety checking anyway
            if (node.getBaseStatement() instanceof IfStmt) {
                IfStmt ifstmt = (IfStmt) node.getBaseStatement();
                BinaryExpr conditionExpression = ConditionRefactorer.getBinaryCondition(ifstmt.getCondition());

                if (conditionExpression != null) {
                    if ((!(conditionExpression.getLeft() instanceof NameExpr) && (!(conditionExpression.getLeft() instanceof MethodCallExpr)))
                            && ((conditionExpression.getRight() instanceof NameExpr) || (conditionExpression.getRight() instanceof MethodCallExpr))) {    // left side of the condition expression is not a variable and right is, thus need to be inverted
                        Expression originalLeft = conditionExpression.getLeft();
                        Expression originalRight = conditionExpression.getRight();
                        conditionExpression.setLeft(originalRight);
                        conditionExpression.setRight(originalLeft);
                        conditionExpression.setOperator(invertOperator(conditionExpression.getOperator()));

                    }
                }

            }
        }
    }

    private static Operator invertOperator(Operator operator) {
        switch (operator) {
            case less:
                return Operator.greater;
            case lessEquals:
                return Operator.greaterEquals;
            case greater:
                return Operator.less;
            case greaterEquals:
                return Operator.lessEquals;
            default:
                return operator;
        }
    }

    private static LinkedList<ConditionsChain> getConditionClusters(LinkedList<Node> conditionals) {
        Collections.sort(conditionals);
        Stack<Node> conditionalsStack = linkedListToStack(conditionals);
        LinkedList<ConditionsChain> globalIntervals = new LinkedList<>();
//        LinkedList<Node> localIntervals;
        ConditionsChain localIntervals;

        while (!conditionalsStack.isEmpty()) {
            localIntervals = new ConditionsChain();
            Node actualCondition = conditionalsStack.pop();

            boolean first = true;
            boolean isFullCoverage = false;     //need more testings
            while (continueToRightNode((IfNode) actualCondition)) {
                if (first) {
                    localIntervals.addCondition(actualCondition);
                    first = false;
                }
                if (!Interval.checkCoverage(conditionsToIntervalsList(localIntervals))) {     //need more testings
                    localIntervals.addCondition(actualCondition.getRight());
                    conditionalsStack.remove(actualCondition.getRight());
                    isFullCoverage = false;     //need more testings
                } else {                        //need more testings
                    isFullCoverage = true;      //need more testings
                }                               //need more testings
                actualCondition = actualCondition.getRight();

            }
            if (!localIntervals.getConditions().isEmpty()) {
                globalIntervals.add(localIntervals);
                LinkedList<Node> l = localIntervals.getConditions();    //need more testings
                Node plc = l.get(l.size() - 2);                         //need more testings
                Node lc = l.get(l.size() - 1);                          //need more testings
                if (localIntervals.checkConsistency()
                        || (isFullCoverage && (plc.getRight() != lc || plc.getLeft().getRight() != lc)) //need more testings
                ){  
                    addList(globalIntervals, localIntervals);
                }
            }
        }
        return globalIntervals;
    }

    private static void addList(LinkedList<ConditionsChain> globalIntervals, ConditionsChain newList) {
        boolean merged = false;
        int count = 0;
        for (ConditionsChain interval : globalIntervals) {
            if (newList.getConditions().containsAll(interval.getConditions())) {
                globalIntervals.set(count, newList);
                merged = true;
            }
            count++;
        }
        if (!merged) {
            globalIntervals.add(newList);
        }
    }

    /* conditions to be true
     right node is an IF condition without ELSE (except when the ELSE is followed by an IF or exit  node (IF/ELSE chain))
     the condition tests the same variable as the actual node
     left has to be a NameExpr and the name must be equal
     the condition expression  right side's type is like the actual node's 
     */
    public static boolean continueToRightNode(IfNode node) {
        if (node.getRight() != null) {
            if (node.getRight().getType().equals(Node.NodeType.IF)) {
                IfNode rightNode = (IfNode) node.getRight();
                if (!rightNode.hasElse() || isIfElseChain(rightNode) || rightNode.getRight().getId() == rightNode.getLeft().getId()) {
                    BinaryExpr rightNodeCondition = ConditionRefactorer.getBinaryCondition(rightNode.getCondition());
                    if (rightNodeCondition != null) {
                        BinaryExpr nodeCondition = ConditionRefactorer.getBinaryCondition(node.getCondition());
                        if (nodeCondition != null) {
                            if ((nodeCondition.getOperator().toString().equals("equals") && rightNodeCondition.getOperator().toString().equals("notEquals"))
                                    || (nodeCondition.getOperator().toString().equals("notEquals") && rightNodeCondition.getOperator().toString().equals("equals"))) {
                                if (nodeCondition.getLeft().equals(rightNodeCondition.getLeft())) {
                                    if (!nodeCondition.getRight().equals(rightNodeCondition.getRight())) {
                                        return false;
                                    }
                                }
                            }
                            if (nodeCondition.getLeft() instanceof NameExpr) {
                                NameExpr leftSideExpression = (NameExpr) nodeCondition.getLeft();
                                if (rightNodeCondition.getLeft() instanceof NameExpr) {
                                    NameExpr rightNodeleftSideExpression = (NameExpr) rightNodeCondition.getLeft();
                                    if (rightNodeleftSideExpression.getName().equals(leftSideExpression.getName())) {
                                        normalizeTypes(nodeCondition, rightNodeCondition);
                                        if (nodeCondition.getRight().getClass().equals(rightNodeCondition.getRight().getClass())) {
                                            return true;
                                        }
                                    }
                                }
                            } else if ((nodeCondition.getLeft() instanceof MethodCallExpr)) {
                                MethodCallExpr leftSideExpression = (MethodCallExpr) nodeCondition.getLeft();
                                if (rightNodeCondition.getLeft() instanceof MethodCallExpr) {
                                    MethodCallExpr rightNodeleftSideExpression = (MethodCallExpr) rightNodeCondition.getLeft();
//                                    if(rightNodeleftSideExpression.getArgs().isEmpty() && leftSideExpression.getArgs().isEmpty()){
                                    if (rightNodeleftSideExpression.getName().equals(leftSideExpression.getName())) {
                                        if (methodsTable.containsKey(leftSideExpression.getName()) && methodsTable.containsKey(rightNodeleftSideExpression.getName())) {
                                            normalizeTypes(nodeCondition, rightNodeCondition);
                                            if (nodeCondition.getRight().getClass().equals(rightNodeCondition.getRight().getClass())) {
                                                return true;
                                            }
                                        }
                                    }
//                                    }else if(!rightNodeleftSideExpression.getArgs().isEmpty() && !leftSideExpression.getArgs().isEmpty()){
//                                        if ((rightNodeleftSideExpression.getName() + rightNodeleftSideExpression.getArgs().toString()).equals(leftSideExpression.getName() + leftSideExpression.getArgs().toString())) {
//                                            if(methodsTable.containsKey(leftSideExpression.getName()) && methodsTable.containsKey(rightNodeleftSideExpression.getName())){
//                                                normalizeTypes(nodeCondition, rightNodeCondition);
//                                                if (nodeCondition.getRight().getClass().equals(rightNodeCondition.getRight().getClass())) {
//                                                    return true;
//                                                }
//                                            }
//                                        }
//                                    }else{
//                                        return false;
//                                    }
                                }
                            }
                        }
                    } else {
                        UnaryExpr unaryRightNode = ConditionRefactorer.getUnaryCondition(rightNode.getCondition());
                        if (unaryRightNode != null) {
                            UnaryExpr unaryNode = ConditionRefactorer.getUnaryCondition(node.getCondition());
                            if (unaryNode != null) {
                                if (unaryNode.getExpr() instanceof NameExpr) {
                                    NameExpr unaryNodeExpr = (NameExpr) unaryNode.getExpr();
                                    if (unaryRightNode.getExpr() instanceof NameExpr) {
                                        NameExpr unaryRighNodeExpr = (NameExpr) unaryRightNode.getExpr();
                                        if (unaryNodeExpr.getName().equals(unaryRighNodeExpr.getName())) {
                                            return true;
                                        }
                                    }
                                } else if (unaryNode.getExpr() instanceof MethodCallExpr) {
                                    MethodCallExpr unaryNodeExpr = (MethodCallExpr) unaryNode.getExpr();
                                    if (unaryRightNode.getExpr() instanceof MethodCallExpr) {
                                        MethodCallExpr unaryRighNodeExpr = (MethodCallExpr) unaryRightNode.getExpr();
//                                        if(unaryNodeExpr.getArgs().isEmpty() && unaryRighNodeExpr.getArgs().isEmpty()){
                                        if (unaryNodeExpr.getName().equals(unaryRighNodeExpr.getName())) {
                                            return true;
                                        }
//                                        }else if((!unaryNodeExpr.getArgs().isEmpty() && !unaryRighNodeExpr.getArgs().isEmpty())){
//                                            if (unaryNodeExpr.getScope().equals(unaryRighNodeExpr.getScope()) && unaryNodeExpr.getName().equals(unaryRighNodeExpr.getName()) && unaryNodeExpr.getArgs().get(0).equals(unaryRighNodeExpr.getArgs().get(0))) {
//                                                return true;
//                                            }
//                                        }else{
//                                            return false;
//                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;

    }

    /*
     Checks if the node has ELSE and is followed by another IF inside this else
     */
    private static boolean isIfElseChain(IfNode node) {
        if (node.hasElse()) {
            if (node.getRight().getType().equals(Node.NodeType.IF)
                    || node.getRight().getType().equals(Node.NodeType.EXIT)) {
                return true;
            }
        }
        return false;
    }

    private static Stack<Node> linkedListToStack(LinkedList<Node> list) {
        Stack<Node> conditionalsStack = new Stack<>();
        Iterator<Node> listIterator = list.iterator();
        while (listIterator.hasNext()) {
            Node node = listIterator.next();
            conditionalsStack.push(node);
        }

        return conditionalsStack;
    }

    private static LinkedList<Interval> conditionsToIntervalsList(ConditionsChain conditions) {
        LinkedList<Interval> intervals = new LinkedList<>();
        for (Node node : conditions.getConditions()) {
            Interval interval = conditionToInterval(node);
            if (interval != null) {
                intervals.add(interval);
            }
        }
        return intervals;
    }

    private static Interval conditionToInterval(Node condition) {
        if (condition instanceof IfNode) {
            IfNode ifNode = (IfNode) condition;
            BinaryExpr binaryExpr = ConditionRefactorer.getBinaryCondition(ifNode.getCondition());
            if (binaryExpr != null) {
                if (binaryExpr.getRight().getClass().equals(IntegerLiteralExpr.class)) {
                    IntegerLiteralExpr intConditionExpr = (IntegerLiteralExpr) binaryExpr.getRight();
                    return new Interval(binaryExpr.getOperator().toString(), Integer.parseInt(intConditionExpr.getValue()), condition);
                } else if (binaryExpr.getRight().getClass().equals(NameExpr.class)) {
                    NameExpr conditionNameExpr = (NameExpr) binaryExpr.getRight();
                    return new Interval(binaryExpr.getOperator().toString(), conditionNameExpr, condition);
                } else if (binaryExpr.getRight().getClass().equals(LongLiteralExpr.class)) { //TODO: no needed
                    LongLiteralExpr longConditionExpr = (LongLiteralExpr) binaryExpr.getRight();
                    return new Interval(binaryExpr.getOperator().toString(), Long.parseLong(longConditionExpr.getValue()), condition);
                } else if (binaryExpr.getRight().getClass().equals(DoubleLiteralExpr.class)) {
                    DoubleLiteralExpr doubleConditionExpr = (DoubleLiteralExpr) binaryExpr.getRight();
                    return new Interval(binaryExpr.getOperator().toString(), Double.parseDouble(doubleConditionExpr.getValue()), condition);
                } else if (binaryExpr.getRight().getClass().equals(BooleanLiteralExpr.class)) {
                    BooleanLiteralExpr booleanConditionExpr = (BooleanLiteralExpr) binaryExpr.getRight();
                    return new Interval(binaryExpr.getOperator().toString(), booleanConditionExpr.getValue(), condition);
                } else if (binaryExpr.getRight().getClass().equals(StringLiteralExpr.class)) {
                    StringLiteralExpr stringConditionExpr = (StringLiteralExpr) binaryExpr.getRight();
                    return new Interval(binaryExpr.getOperator().toString(), stringConditionExpr.getValue(), condition);
                } else if (binaryExpr.getRight().getClass().equals(MethodCallExpr.class)) {
                    MethodCallExpr methodExpr = (MethodCallExpr) binaryExpr.getRight();
                    return new Interval(binaryExpr.getOperator().toString(), methodExpr, condition);
                }
            } else {
                UnaryExpr unaryExpr = ConditionRefactorer.getUnaryCondition(ifNode.getCondition());
                if (unaryExpr.getExpr() instanceof NameExpr) {
                    NameExpr nameExpr = (NameExpr) unaryExpr.getExpr();
                    if (unaryExpr.getOperator() == null) {
                        return new Interval("equals", nameExpr.getName(), condition);
                    } else {
                        return new Interval("notEquals", nameExpr.getName(), condition);
                    }
                } else if (unaryExpr.getExpr() instanceof MethodCallExpr) {
                    MethodCallExpr methodExpr = (MethodCallExpr) unaryExpr.getExpr();
                    if (unaryExpr.getOperator() == null) {
                        return new Interval("equals", methodExpr.getName(), condition);
                    } else {
                        return new Interval("notEquals", methodExpr.getName(), condition);
                    }
                }
            }
        }
        return null;

    }

    private static boolean checkPattern(LinkedList<Node> conditionals, ArrayList<Node> nodes) {
        boolean changed = false;
        LinkedList<ConditionsChain> conditionsClusters = getConditionClusters(conditionals);
        int count = 0;
        boolean updated = false;
        while (conditionsClusters.size() > 0) {
            ConditionsChain conditionCluster = conditionsClusters.get(count);
            conditionCluster.setNextNodeOutsideScope();
            LinkedList<Interval> actualInterval = conditionsToIntervalsList(conditionCluster);

            if (Interval.checkCoverage(actualInterval)) {
                if (actualInterval.size() > 1) {
                    updated = true;
                    if (!changed) {
                        changed = true;
                    }
                    IfNode preLastCondition = (IfNode) actualInterval.get(actualInterval.size() - 2).getNode();
                    IfNode lastCondition = (IfNode) actualInterval.get(actualInterval.size() - 1).getNode();
                    removeNode(lastCondition, preLastCondition, nodes, conditionCluster.getNextNodeOutsideScope());
                    if (!preLastCondition.hasElse()) {
                        preLastCondition.setHasElse(true);
                        lastCondition.setDegree(preLastCondition.getDegree() + 1);
                    }
                    conditionals.remove(lastCondition);
                    conditionsClusters = getConditionClusters(conditionals);
                    count = 0;
                }
            } else {
                count++;
            }

            if (count >= conditionsClusters.size()) {
                if (!updated) {
                    break;
                } else {
                    count = 0;
                    updated = false;
                }
            }
        }
        return changed;
    }

    private static void removeNode(Node toRemoveNode, Node parentNode, ArrayList<Node> nodes, Node nextNodeOutsideScope) {
        parentNode.setRight(toRemoveNode.getLeft());
        List<Node> incidentNodes = Node.getIncidentNodes(nodes, toRemoveNode);
        for (Node node : incidentNodes) {
            if (!node.equals(parentNode) && !node.equals(toRemoveNode)) {
                if (node.getLeft().equals(toRemoveNode)) {
                    if (toRemoveNode.getLeft().getId() == toRemoveNode.getRight().getId()) {  //Check if it is valid
                        node.setLeft(toRemoveNode.getLeft().getLeft());                     //
                    } else {                                                                  //
    //                    node.setLeft(toRemoveNode.getRight());
                        node.setLeft(nextNodeOutsideScope);
                    }                                                                       //
                } else {
                    if (node.hasElse()) { // toRemoveNode is inside an else
                        node.setHasElse(false);
                    }
                    if (!node.getRight().equals(toRemoveNode.getRight())) {
                        node.setRight(nextNodeOutsideScope);
                    } else {
                        node.setRight(null);
                    }
                }
            }
        }
        nodes.remove(toRemoveNode);
    }

    /*
     This method is a workaround for a problem with Javaparser API.
     When the decimal part of a value is .0 (e.g. 30.0) javaparser parses it as an Integer value.
    
     */
    private static void normalizeTypes(BinaryExpr firstExpr, BinaryExpr secondExpr) {
        checkTypes(firstExpr);
        checkTypes(secondExpr);

    }

    private static void checkTypes(BinaryExpr expression) {
        String varType = "";
        if (expression.getLeft() instanceof NameExpr) {
            NameExpr leftVarName = (NameExpr) expression.getLeft();
            Type t = variablesTable.get(leftVarName.getName());
            varType = (t == null) ? privateVariablesTable.get(leftVarName.getName()).toString() :  t.toString();
        } else if (expression.getLeft() instanceof MethodCallExpr) {
            MethodCallExpr leftVarName = (MethodCallExpr) expression.getLeft();
            varType = methodsTable.get(leftVarName.getName()).toString();
        }
        switch (varType) {
            case "int":
            case "long":
                if (expression.getRight() instanceof DoubleLiteralExpr) {
                    DoubleLiteralExpr doubleValue = (DoubleLiteralExpr) expression.getRight();
                    expression.setRight(new IntegerLiteralExpr(doubleValue.getBeginLine(), doubleValue.getBeginColumn(), doubleValue.getEndLine(), doubleValue.getEndColumn(), doubleValue.getValue()));
                }
                break;
            case "double":
            case "float":
                if (expression.getRight() instanceof IntegerLiteralExpr) {
                    IntegerLiteralExpr intValue = (IntegerLiteralExpr) expression.getRight();
                    expression.setRight(new DoubleLiteralExpr(intValue.getBeginLine(), intValue.getBeginColumn(), intValue.getEndLine(), intValue.getEndColumn(), intValue.getValue()));
                }
                break;
        }
    }
}
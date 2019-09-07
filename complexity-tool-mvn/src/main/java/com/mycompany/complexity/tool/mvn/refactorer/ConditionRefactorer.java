/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn.refactorer;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author helenocampos
 */
public class ConditionRefactorer {

    private Stack<IfStmt> conditionsNextMarkers;
    private ArrayList<IfStmt> checkedIfStmts;

    public ConditionRefactorer() {
        conditionsNextMarkers = new Stack<>();
        checkedIfStmts = new ArrayList<>();
    }

    public Stack<IfStmt> getConditionsNextMarker() {
        return this.conditionsNextMarkers;
    }

    public void setConditionsNextMarker(Stack<IfStmt> conditionsNextMarkers) {
        this.conditionsNextMarkers = conditionsNextMarkers;
    }

    public IfRefactorContainer parseCondition(Expression condition) {
        IfRefactorContainer container = new IfRefactorContainer();
        container.setCondition(condition);
        if (condition.getClass().equals(BinaryExpr.class)) {
            BinaryExpr binaryCondition = (BinaryExpr) condition;
            if (binaryCondition.getOperator().equals(BinaryExpr.Operator.and)) {
                container.setCreatedIf(parseAndCondition(binaryCondition));
                container.setLastCall("and");
            } else if (binaryCondition.getOperator().equals(BinaryExpr.Operator.or)) {
                container.setCreatedIf(parseOrCondition(binaryCondition));
                container.setLastCall("or");
            } else if (isSimpleCondition(binaryCondition)) {
                container.setCondition(binaryCondition);
            }
        } else if (condition.getClass().equals(EnclosedExpr.class)) {
            EnclosedExpr enclosedCondition = (EnclosedExpr) condition;
            container = parseCondition(enclosedCondition.getInner());
        } else if (condition.getClass().equals(MethodCallExpr.class)) {
            container.setCondition((MethodCallExpr) condition);
        } else if (condition.getClass().equals(UnaryExpr.class)) {
            UnaryExpr unaryExpr = (UnaryExpr) condition;
            container.setCondition(unaryExpr.getExpr());
        } else if (condition.getClass().equals(NameExpr.class)) {
            NameExpr nameExpr = (NameExpr) condition;
            container.setCondition(nameExpr);
        }

        return container;
    }

    public static boolean isSimpleCondition(Expression condition) {
        if (condition.getClass().equals(MethodCallExpr.class)) {
            return true;
        } else if (condition.getClass().equals(BinaryExpr.class)) {
            BinaryExpr binaryExpr = (BinaryExpr) condition;
            return !binaryExpr.getOperator().equals(BinaryExpr.Operator.and)
                    && !binaryExpr.getOperator().equals(BinaryExpr.Operator.or);
        } else if (condition.getClass().equals(UnaryExpr.class)) {
            return true;
        } else if (condition.getClass().equals(NameExpr.class)) {
            return true;
        } else {
            try {
                throw new Exception("REVER SIMPLE CONDITION, TALVEZ EXISTA UM CASO NAO TRATADO DE PARSING");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
        return false;

    }

    private IfStmt parseAndCondition(BinaryExpr condition) {
        IfRefactorContainer left = parseCondition(condition.getLeft());
        IfRefactorContainer right = parseCondition(condition.getRight());
        IfStmt ifstmt = new IfStmt();

        if (left.getCondition().getClass().equals(BinaryExpr.class)) {
            BinaryExpr leftBinary = (BinaryExpr) left.getCondition();

            if (right.getCondition().getClass().equals(BinaryExpr.class)) {
                BinaryExpr rightBinary = (BinaryExpr) right.getCondition();
                if (isSimpleCondition(leftBinary)) {

                    ifstmt = parseAndRightSide(condition, leftBinary, rightBinary, right);

                } else {
                    if (isSimpleCondition(rightBinary)) {
                        //left is not simple condition but right is
                        parseAndRight(condition);
                        ifstmt = left.getCreatedIf();
                    } else {
                        //neither left nor right is simple condition
                        ifstmt = parseAndWithTwoReturns(left.getCreatedIf(), right.getCreatedIf());
                    }
                }
            } else if (right.getCondition().getClass().equals(MethodCallExpr.class)
                    || right.getCondition().getClass().equals(NameExpr.class)) {
                if (isSimpleCondition(leftBinary)) {
                    ifstmt = parseAndRightSide(condition, leftBinary, right.getCondition(), right);
                } else {
                    parseAndRight(condition);
                    ifstmt = left.getCreatedIf();
                }
            }

        } else if (left.getCondition().getClass().equals(MethodCallExpr.class)) {
            MethodCallExpr leftMethod = (MethodCallExpr) left.getCondition();
            if (right.getCondition().getClass().equals(MethodCallExpr.class)
                    || right.getCondition().getClass().equals(NameExpr.class)) {
                ifstmt = parseSimpleAnd(condition);
            } else if (right.getCondition().getClass().equals(BinaryExpr.class)) {
                BinaryExpr rightBinary = (BinaryExpr) right.getCondition();
                ifstmt = parseAndRightSide(condition, leftMethod, rightBinary, right);
            }

        } else if (left.getCondition().getClass().equals(NameExpr.class)) {
            NameExpr leftName = (NameExpr) left.getCondition();
            if (right.getCondition().getClass().equals(MethodCallExpr.class)
                    || right.getCondition().getClass().equals(NameExpr.class)) {
                ifstmt = parseSimpleAnd(condition);
            } else if (right.getCondition().getClass().equals(BinaryExpr.class)) {
                BinaryExpr rightBinary = (BinaryExpr) right.getCondition();
                ifstmt = parseAndRightSide(condition, leftName, rightBinary, right);
            }
        }

        System.out.println("Parsing AND condition: ");
        System.out.println(left.getCondition().toString());
        System.out.println(condition.getOperator().toString());
        System.out.println(right.getCondition().toString());
        return ifstmt;

    }

    private IfStmt parseAndRightSide(Expression condition, Expression left, Expression right, IfRefactorContainer rightContainer) {
        IfStmt ifstmt;
        if (isSimpleCondition(right)) {
            //left and right are simple conditions
            ifstmt = parseSimpleAnd((BinaryExpr) condition);

        } else {
            //left is a simple condition, right is not
            ifstmt = parseAndLeft(left, rightContainer.getCreatedIf());
        }
        return ifstmt;
    }

    /*
     SIMPLE AND RULES
     Example: x==1 && a==2
        
     if(left condition){
     if(right condition){
     space for the next condition parsing
     }
     }
        
     */
    private IfStmt parseSimpleAnd(BinaryExpr condition) {
        System.out.println("Simple and");
        IfStmt ifstmt = new IfStmt();
        ifstmt.setCondition(condition.getLeft());
        IfStmt innerIf = new IfStmt();
        innerIf.setCondition(condition.getRight());
        conditionsNextMarkers.push(innerIf);
        ifstmt.setThenStmt(innerIf);
        return ifstmt;
    }

    /*
        
     WHEN THE LEFT CONDITION IS A SIMPLE CONDITION AND THE RIGHT IS THE RETURN
     WITH AN IF STATEMENT
     Example: a==1 && (x==3 || x==2)
     RULE:
     if(left condition){
     right created if
     }   
        
     */
    private IfStmt parseAndLeft(Expression condition, IfStmt rightIf) {
        System.out.println("And left");
        IfStmt ifstmt = new IfStmt();
        ifstmt.setCondition(condition);
        ifstmt.setThenStmt(rightIf);
        return ifstmt;
    }

    /*
     WHEN THE RIGHT CONDITION IS A SIMPLE CONDITION AND THE LEFT IS THE RETURN
     WITH AN IF STATEMENT
     Example: (x==3 || x==2) && a==1
     RULE:
     Insert the following code on the existants spaces for next parsing 
     if(right condition){
     space for next condition parsing
     }
     */
    private IfStmt parseAndRight(BinaryExpr condition) {
        System.out.println("And right");
        IfStmt ifstmt = new IfStmt();
        ifstmt.setCondition(condition.getRight());
        insertNextConditions(ifstmt);
        conditionsNextMarkers.push(ifstmt);
        return ifstmt;
    }

    /*
    
     SIMPLE OR RULES
     Example: a==1 || a==2
     if(left condition){
     space for next condition parsing
     }else{
     if(right condition){
     space for next condition parsing
     }
     }
    
     */
    private IfStmt parseSimpleOr(BinaryExpr condition) {
        System.out.println("Simple or");
        IfStmt ifstmt = new IfStmt();
        ifstmt.setCondition(condition.getLeft());
        conditionsNextMarkers.push(ifstmt);
        IfStmt innerIf = new IfStmt();
        innerIf.setCondition(condition.getRight());
        conditionsNextMarkers.push(innerIf);
        ifstmt.setElseStmt(innerIf);

        return ifstmt;
    }

    /*
     WHEN THE LEFT CONDITION IS A SIMPLE CONDITION AND THE RIGHT IS THE RETURN 
     WITH AN IF STATEMENT
     Example: x==1 || (x==2 && a==3)
     RULES:
     if(left condition){
     space for next condition parsing
     }else{
     right created if
     }
     */
    private IfStmt parseOrLeft(Expression condition, IfStmt rightIf) {
        System.out.println("Or left");
        IfStmt ifstmt = new IfStmt();
        ifstmt.setCondition(condition);
        conditionsNextMarkers.push(ifstmt);
        ifstmt.setElseStmt(rightIf);
        return ifstmt;
    }

    /*
     WHEN THE RIGHT CONDITION IS A SIMPLE CONDITION AND THE LEFT IS THE RETURN
     OF AN AND PARSING, TYPE IS A IF STATEMENT 
     Example: (x==1 && a==2) || x==2
     RULE:
     add an else in every if of the left using the following rule:
     else{
     if(right condition){
     space for next condition parsing
     }
     }
     */
    private IfStmt parseOrRightAfterAnd(BinaryExpr condition, IfStmt leftIf) {
        System.out.println("Or right after and");
        IfStmt ifstmt = new IfStmt();
        ifstmt.setCondition(condition.getRight());
        conditionsNextMarkers.push(ifstmt);
//        leftIf.setElseStmt(ifstmt);
        checkedIfStmts.clear();
        traverseIfStmt(leftIf, ifstmt);
        checkedIfStmts.clear();
        return leftIf;
    }

    // traverse the if statement applying the parse or RIGHT after AND rule
    // for all IFs inside the ifstmt that dont have an else statement, make the newIf the else statement of this if
    private void traverseIfStmt(IfStmt ifstmt, IfStmt newIf) {
        if (!checkedIfStmts.contains(ifstmt)) {
            checkedIfStmts.add(ifstmt);
            if (ifstmt.getThenStmt() != null) {
                IfStmt inner = (IfStmt) ifstmt.getThenStmt();
                traverseIfStmt(inner, newIf);
            }
            if (ifstmt.getElseStmt() != null) {
                IfStmt inner = (IfStmt) ifstmt.getElseStmt();
                traverseIfStmt(inner, newIf);
            } else {
                ifstmt.setElseStmt(newIf);
            }
        }
    }

    /*
     WHEN THE RIGHT CONDITION IS A SIMPLE CONDITION AND THE LEFT IS THE RETURN
     OF AN OR PARSING, TYPE IS A IF STATEMENT
     Example: (x==1 || x==2) || a==2
     RULE: 
     get the else of the if of the left
     get the if inside this else
     create an else for this if
     insert the right condition inside this else
     else{
     if(right condition){
     space for next condition parsing
     }
     }
     */
    private IfStmt parseOrRightAfterOr(BinaryExpr condition, IfStmt leftIf) {
        System.out.println("Or right after or");
        IfStmt ifstmt = new IfStmt();
        ifstmt.setCondition(condition.getRight());
        conditionsNextMarkers.push(ifstmt);
        IfStmt innerIf = (IfStmt) leftIf.getElseStmt();
        innerIf.setElseStmt(ifstmt);
        return leftIf;
    }

    /*
     WHEN THE RIGHT CONDITION IS THE RETURN OF ANOTHER CONDITION PARSING AND THE
     LEFT TOO
     Example: (x==1 && a==2) || (x==2 && a==3)
     RULE:
     get the else of the if on the left
     if he hasnt an else,  put the right side as the else
     if he has:
     get the if inside this else
     create an else for this if
     insert the right inside this else
     else{
     right created if
     }
     */
    private IfStmt parseOrWithTwoReturns(IfStmt leftIf, IfStmt rightIf) {
        System.out.println("Or two returns");

        if (leftIf.getElseStmt() != null) {
            IfStmt innerIf = (IfStmt) leftIf.getElseStmt();
            innerIf.setElseStmt(rightIf);
        } else {
            leftIf.setElseStmt(rightIf);
            IfStmt leftIfThen = (IfStmt) leftIf.getThenStmt();
            if (leftIfThen != null) {
                if (leftIfThen.getElseStmt() == null) {
                    leftIfThen.setElseStmt(rightIf);
                }
            }
        }

        return leftIf;
    }

    /*
     WHEN THE RIGHT CONDITION IS THE RETURN OF ANOTHER CONDITION PARSING AND THE
     LEFT TOO
     Example: (x==1 || a==2) && (b==2 || c==3)
     RULE:
     the return of the right expression parsing enters on the existants spaces 
     for next parsings of the return of the left expression
     */
    private IfStmt parseAndWithTwoReturns(IfStmt leftIf, IfStmt rightIf) {
        System.out.println("And two returns");
        insertNextConditionsSpecifiedIf(rightIf, leftIf);
        return leftIf;
    }

    private void insertNextConditions(IfStmt ifstmt) {
        while (!conditionsNextMarkers.empty()) {
            IfStmt marker = conditionsNextMarkers.pop();
            marker.setThenStmt(ifstmt);

        }
    }

    private void insertNextConditionsSpecifiedIf(IfStmt ifstmt, IfStmt target) {
        //target is where I want to put
        //ifstmt is what I want to put
        Stack<IfStmt> auxiliaryStack = new Stack<>();
        while (!conditionsNextMarkers.empty()) {
            IfStmt marker = conditionsNextMarkers.pop();
            boolean found = traverseIfTree(marker, target, ifstmt);
            if (!found) {
                auxiliaryStack.push(marker);
            }

        }
        while (!auxiliaryStack.empty()) {
            conditionsNextMarkers.push(auxiliaryStack.pop());
        }

    }

    private boolean traverseIfTree(IfStmt target, IfStmt root, IfStmt newIf) {
        boolean found = false;
        if (root != null) {
            if (root.equals(target)) {
                target.setThenStmt(newIf);
                return true;
            }

            if (root.getThenStmt() != null) {
                if (root.getThenStmt().equals(target)) {
                    target.setThenStmt(newIf);
                    return true;
                } else {
                    found = traverseIfTree(target, (IfStmt) root.getThenStmt(), newIf);
                }
            }
            if (root.getElseStmt() != null) {
                if (root.getElseStmt().equals(target)) {
                    target.setThenStmt(newIf);
                    return true;
                } else {
                    found = traverseIfTree(target, (IfStmt) root.getElseStmt(), newIf);
                }
            }
        }
        return found;
    }
    
    public static BinaryExpr getBinaryCondition(Expression condition) {
        BinaryExpr conditionExpression = null;
        if (condition instanceof BinaryExpr) {
            conditionExpression = (BinaryExpr) condition;
        } else {
            if (condition instanceof EnclosedExpr) {
                EnclosedExpr enclosedExpr = (EnclosedExpr) condition;
                if (enclosedExpr.getInner() instanceof BinaryExpr) {
                    conditionExpression = (BinaryExpr) enclosedExpr.getInner();
                }
            }
        }
        return conditionExpression;
    }
    
    public static UnaryExpr getUnaryCondition(Expression condition) {
        UnaryExpr conditionExpression = null;
        if (condition instanceof UnaryExpr) {
            conditionExpression = (UnaryExpr) condition;
        } else {
            if (condition instanceof EnclosedExpr) {
                EnclosedExpr enclosedExpr = (EnclosedExpr) condition;
                if (enclosedExpr.getInner() instanceof UnaryExpr) {
                    conditionExpression = (UnaryExpr) enclosedExpr.getInner();
                }
            } else {
                if (condition instanceof NameExpr || condition instanceof MethodCallExpr){
                    conditionExpression = new UnaryExpr(condition, null);
                }
            }
        }
        return conditionExpression;
    }

    private IfStmt parseOrCondition(BinaryExpr condition) {
        IfRefactorContainer left = parseCondition(condition.getLeft());
        IfRefactorContainer right = parseCondition(condition.getRight());
        IfStmt ifstmt = new IfStmt();

        if (isSimpleCondition(left.getCondition())) {
            //left is simple
            ifstmt = parseSimpleOrAndLeft(left, right, condition);
        } else {
            if (isSimpleCondition(right.getCondition())) {
                //left is not simple condition but right is
                if (left.getLastCall().equals("or")) {
                    ifstmt = parseOrRightAfterOr(condition, left.getCreatedIf());
                } else {
                    ifstmt = parseOrRightAfterAnd(condition, left.getCreatedIf());
                }

            } else {
                //neither left nor right is simple condition
                ifstmt = parseOrWithTwoReturns(left.getCreatedIf(), right.getCreatedIf());
            }
        }

//        if (left.getCondition().getClass().equals(BinaryExpr.class)) {
//            BinaryExpr leftBinary = (BinaryExpr) left.getCondition();
//
//            if (right.getCondition().getClass().equals(BinaryExpr.class)) {
//                BinaryExpr rightBinary = (BinaryExpr) right.getCondition();
//                if (isSimpleCondition(leftBinary)) {
//                    //left is simple
//                    parseSimpleOrAndLeft(left, right, condition);
//
//                } else {
//                    if (isSimpleCondition(rightBinary)) {
//                        //left is not simple condition but right is
//                        if (left.getLastCall().equals("or")) {
//                            ifstmt = parseOrRightAfterOr(condition, left.getCreatedIf());
//                        } else {
//                            ifstmt = parseOrRightAfterAnd(condition, left.getCreatedIf());
//                        }
//
//                    } else {
//                        //neither left nor right is simple condition
//                        ifstmt = parseOrWithTwoReturns(left.getCreatedIf(), right.getCreatedIf());
//                    }
//                }
//            }
//
//        } else if (left.getCondition().getClass().equals(MethodCallExpr.class)
//                || left.getCondition().getClass().equals(NameExpr.class)) {
//            //left is simple
//            parseSimpleOrAndLeft(left, right, condition);
//        }

        System.out.println("Parsing OR condition: ");
//        BinaryExpr binaryLeft = (BinaryExpr) left;
        System.out.println(left.getCondition().toString());
        System.out.println(condition.getOperator().toString());
//        BinaryExpr binaryRight = (BinaryExpr) right;
        System.out.println(right.getCondition().toString());
        return ifstmt;
    }

    private IfStmt parseSimpleOrAndLeft(IfRefactorContainer left, IfRefactorContainer right, BinaryExpr condition) {
        IfStmt ifstmt = null;

        if (isSimpleCondition(right.getCondition())) {
            //left and right are simple
            ifstmt = parseSimpleOr(condition);
        } else {
            // left is simple, right is not
            ifstmt = parseOrLeft(left.getCondition(), right.getCreatedIf());
        }
        return ifstmt;
    }

}

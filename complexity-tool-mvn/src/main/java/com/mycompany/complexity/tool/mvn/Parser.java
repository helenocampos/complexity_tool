/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.mycompany.complexity.tool.mvn.Nodes.BlockNode;
import com.mycompany.complexity.tool.mvn.Nodes.BreakNode;
import com.mycompany.complexity.tool.mvn.Nodes.DoNode;
import com.mycompany.complexity.tool.mvn.Nodes.ForEachNode;
import com.mycompany.complexity.tool.mvn.Nodes.ForNode;
import com.mycompany.complexity.tool.mvn.Nodes.IfNode;
import com.mycompany.complexity.tool.mvn.Nodes.LoopExitNode;
import com.mycompany.complexity.tool.mvn.Nodes.LoopNode;
import com.mycompany.complexity.tool.mvn.Nodes.Node;
import static com.mycompany.complexity.tool.mvn.Nodes.Node.NodeType.BLOCK;
import static com.mycompany.complexity.tool.mvn.Nodes.Node.NodeType.CASE;
import static com.mycompany.complexity.tool.mvn.Nodes.Node.NodeType.DO;
import static com.mycompany.complexity.tool.mvn.Nodes.Node.NodeType.EXIT;
import static com.mycompany.complexity.tool.mvn.Nodes.Node.NodeType.FOR;
import static com.mycompany.complexity.tool.mvn.Nodes.Node.NodeType.FOREACH;
import static com.mycompany.complexity.tool.mvn.Nodes.Node.NodeType.IF;
import static com.mycompany.complexity.tool.mvn.Nodes.Node.NodeType.WHILE;
import com.mycompany.complexity.tool.mvn.Nodes.ReturnNode;
import com.mycompany.complexity.tool.mvn.Nodes.SwitchCaseNode;
import com.mycompany.complexity.tool.mvn.Nodes.WhileNode;
import com.mycompany.complexity.tool.mvn.refactorer.ConditionRefactorer;
import com.mycompany.complexity.tool.mvn.refactorer.IfRefactorContainer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author helenocampos
 */
public class Parser {

    private Node root;
    private ArrayList<Node> nodes;
    private Node lastParent;
    private Node lastSwitchCase;
    private Node exitNode;
    private int lastNodeId;
    private int nodeDegree = 0;
    private Stack<IfStmt> conditionsNextMarkers;
    private ArrayList<IfNode> ifSimpleConditions;
    private ArrayList<IfStmt> checkedIfStmts;
    // key: original ifstmt,    value: refactored ifstmt
    private HashMap<IfStmt, IfStmt> refactoredIfStmts;
    private Stack<Node> breakNodes;
    private Stack<Node> breakScope;
    private Stack<Node> futureConnections;
    private Stack<Node> returnNodes;
    private LinkedList<Node> conditionals;
    
//    private boolean lockNewNodes;
    private boolean mockParser;

    public boolean isMockParser() {
        return this.mockParser;
    }

    public Parser(boolean mockParser) {
        root = null;
        nodes = new ArrayList<>();
        lastParent = null;
        lastNodeId = 0;
        lastSwitchCase = null;
        conditionsNextMarkers = new Stack<>();
        ifSimpleConditions = new ArrayList<>();
        checkedIfStmts = new ArrayList<>();
        refactoredIfStmts = new HashMap<>();
//        lockNewNodes = false;
        this.mockParser = mockParser;
        breakNodes = new Stack<>();
        breakScope = new Stack<>();
        futureConnections = new Stack<>();
        returnNodes = new Stack<>();
        conditionals = new LinkedList<>();
    }

    public void parseManager(Statement stmt, int stmtCount) {
        // TODO: test another approach on missingLinks method, instead of traverse the tree each time, check the nodes list (check if it is complete)
        //TODO: delete this method, use the direct parsing from scanner instead
        if (checkStatement(stmt)) {
            Stack<Node> missingLinks = new Stack<>();
            Node parent = null;
            if (getRoot() == null) {
                Node parsedNode = parse(stmt, lastParent, "");
                if (parsedNode.getId() != 0) {
                    setRoot(parsedNode);
                    parent = getRoot();
                    if (getRoot().getClass().equals(DoNode.class)) {
                        DoNode doNodeRoot = (DoNode) getRoot();
                        setRoot(doNodeRoot.getRootNode());
                    }
                }

            } else {
                parent = lastParent;

                if (parent.getRight() == null) {
                    parent = parse(stmt, parent, "right");
                } else {
                    parent = parse(stmt, lastSwitchCase, "right");
//                    connectMissingLinksLeft(missingLinks, parent);
                }
                if (parent != null && !parent.getClass().equals(ReturnNode.class)) {
                    connectFutureConnections(parent);
                    processBreakNodes(parent, parent);
//                    if (stmtCount != 0) {   //stmtCount=0 on the last parsing
                    performIfandLeavesConnections(lastParent, parent);
//                    }

                }

            }
            for (Node node : nodes) {
                checkLateralLink(node);
            }
            lastParent = parent;
        }

    }

    private Node checkExitNode(Node exitNode) {
        if (exitNode.getClass().equals(ForNode.class)
                || (exitNode.getClass().equals(WhileNode.class))
                || (exitNode.getClass().equals(ForEachNode.class))) {
            LoopNode node = (LoopNode) exitNode;
            return node.getExitNode();
        }
        return exitNode;
    }

    private void processBreakNodes(Node exitNode, Node processLimitNode) {
        exitNode = checkExitNode(exitNode);
        while (breakNodes.size() > 0) {
            Node breakNode = breakNodes.pop();
            Node caseParent = getCaseParent(breakNode);
            Node nextCase = caseParent.getRight();
            removeAllCaseConnections(caseParent, caseParent, nextCase);
            connectLeavesToTarget(caseParent, exitNode, caseParent);
            Stack<Node> ifNodes = getIFNodesWithoutRightWithLimit(caseParent, new Stack<Node>(), caseParent, processLimitNode);
            connectMissingLinksRight(ifNodes, exitNode);
        }

    }

    private void connectLeavesToTarget(Node node, Node target, Node rootCaller) {
        // connects all the leaves to the target parameter

        if (node != null) {
            if (node.getId() < target.getId()) {
                if (node.getLeft() == null && node.getRight() == null) {
                    node.setLeft(target);
                    return;
                }
                Node left = node.getLeft();
                if (left != null) {

                    if (left.getId() > node.getId()) {          // prevent stack overflow on going back on the "tree"
                        connectLeavesToTarget(left, target, rootCaller);
                    }

                    Node right = node.getRight();
                    if (right != null && !node.equals(rootCaller) && !right.equals(target)) {
                        if (right.getId() > node.getId()) {         // prevent stack overflow on going back on the "tree"
                            connectLeavesToTarget(right, target, rootCaller);
                        }
                    }

                }

            }
        }
    }

    private Node getCaseParent(Node node) {
        Node initialNode = node;
        while (node.getParent() != null) {
            Node parent = node.getParent();
            if (parent.getType().equals(Node.NodeType.CASE)) {
                return parent;
            } else {
                node = parent;
            }
        }
        return initialNode;
    }

    private void removeAllCaseConnections(Node node, Node rootCaller, Node nextCase) {
        //removes all switch case connections to the next case (has break)
        if (node != null) {

            Node left = node.getLeft();
            if (left != null) {
                if (nextCase != null) {
                    if (left.getId() >= nextCase.getId()) {
                        node.setLeft(null);
                    } else {
                        if (left.getId() > node.getId()) {          // prevent stack overflow on going back on the "tree"
                            removeAllCaseConnections(left, rootCaller, nextCase);
                        }
                    }

                }
                Node right = node.getRight();

                if (right != null && !node.equals(rootCaller)) {
                    if (nextCase != null) {
                        if (right.getId() >= nextCase.getId()) {
                            node.setRight(null);
                        } else {
                            if (right.getId() > node.getId()) {         // prevent stack overflow on going back on the "tree"
                                removeAllCaseConnections(right, rootCaller, nextCase);
                            }
                        }

                    }

                }

            }

        }
    }

    public Node parse(Statement stmt, Node parent, String direction) {
        Node actualNode = null;
        boolean isDoNode = false;
        DoNode doNode = null;
        if (checkStatement(stmt)) {
            String stmtClassType = stmt.getClass().getSimpleName();
            switch (stmtClassType) {
                case "IfStmt":
                    actualNode = parseIf((IfStmt) stmt);
                    break;

                case "TryStmt":
                    TryStmt trystmt = (TryStmt) stmt;
                    stmt = trystmt.getTryBlock();
                    trystmt = null;
                case "BlockStmt":
                    actualNode = parseBlock((BlockStmt) stmt, parent, direction);
                    break;

                case "SwitchStmt":
                    actualNode = parseSwitch((SwitchStmt) stmt);
                    connectSwitchStatements(actualNode);
                    break;

                case "ForeachStmt":
                    actualNode = parseForeach((ForeachStmt) stmt);
                    break;

                case "ForStmt":
                    actualNode = parseFor((ForStmt) stmt);
                    break;

                case "WhileStmt":
                    actualNode = parseWhile((WhileStmt) stmt);
                    break;

                case "DoStmt":
                    isDoNode = true;
                    doNode = parseDo((DoStmt) stmt, parent, direction);
                    actualNode = doNode.getRootNode();
                    break;

                case "BreakStmt":
                    actualNode = new BreakNode();
                    actualNode.setParent(parent);
                    return actualNode;

                case "ReturnStmt":
                    actualNode = new ReturnNode();
                    actualNode.setParent(parent);
                    actualNode.setType(Node.NodeType.EXIT);
                    return actualNode;

            }
            if (parent != null) {
                switch (direction) {
                    case "left":
                        parent.setLeft(actualNode);
                        break;
                    case "right":
                        parent.setRight(actualNode);
                        break;
                }
            }
            
            actualNode.setParent(parent);
            checkLateralLink(actualNode);
        }
        if (!isDoNode) {
            return actualNode;
        } else {
            return doNode;
        }

    }

    private void connectFutureConnections(Node target) {
        while (futureConnections.size() > 0) {
            Node futureConnection = futureConnections.pop();
            if (futureConnection.getType().equals(Node.NodeType.BLOCK)) {
                futureConnection.setLeft(target);
            } else {
                futureConnection.setRight(target);
            }

        }
    }

    private void connectSwitchStatements(Node caseNode) {

        if (caseNode != null) {
            Node nextCaseNode = caseNode.getRight();
            caseNode.setRight(null);
            Stack<Node> missingLinks = getMostRecentChildren(caseNode, new Stack<Node>(), null, caseNode);
            caseNode.setRight(nextCaseNode);

            Stack<Node> ifNodesWithoutRight = getIFNodesWithoutRight(caseNode, new Stack<Node>(), caseNode);

            if (nextCaseNode != null) {

                if (nextCaseNode.getClass().equals(SwitchCaseNode.class)) {
                    connectMissingLinksLeft(missingLinks, nextCaseNode.getLeft());
                    connectMissingLinksRight(ifNodesWithoutRight, nextCaseNode.getLeft());

                } else {
                    connectMissingLinksLeft(missingLinks, nextCaseNode);
                    connectMissingLinksRight(ifNodesWithoutRight, nextCaseNode);
                }
            } else {
                futureConnections.addAll(missingLinks);
                futureConnections.addAll(ifNodesWithoutRight);
            }
            connectSwitchStatements(nextCaseNode);
        }
    }

    private Stack<Node> getIFNodesWithoutRight(Node node, Stack<Node> ifNodes, Node rootCaller) {
        // traverse the graph looking for if nodes without right

        if (node != null) {
            if (node.getType().equals(Node.NodeType.IF)) {
                if (node.getRight() == null) {
                    ifNodes.push(node);
                }

            } else {

                Node left = node.getLeft();
                if (left != null) {
                    if (left.getId() > node.getId()) {          // prevent stack overflow on going back on the "tree"
                        ifNodes = getIFNodesWithoutRight(node.getLeft(), ifNodes, rootCaller);
                    }
                }
                Node right = node.getRight();

                if (right != null && !node.equals(rootCaller)) {
                    if (right.getId() > node.getId()) {         // prevent stack overflow on going back on the "tree"
                        ifNodes = getIFNodesWithoutRight(node.getRight(), ifNodes, rootCaller);
                    }
                }
            }

        }

        return ifNodes;

    }

    private Stack<Node> getNonBlockNodesWithoutRight(Node node, Stack<Node> ifNodes, Node rootCaller, Stack<Node> checkedNodes) {
        // traverse the graph looking for non block nodes without right

        if (node != null) {
            if (!checkedNodes.contains(node)) {
                checkedNodes.push(node);
                if (!node.getType().equals(Node.NodeType.BLOCK) && node.getRight() == null
                        && !node.getType().equals(Node.NodeType.EXIT)
                        && !node.getType().equals(Node.NodeType.LOOP_EXIT)) {
                    if (!ifNodes.contains(node)) {
                        ifNodes.push(node);
                    }
                }

                Node left = node.getLeft();
                if (left != null) {
                    if (left.getId() > node.getId()) {          // prevent stack overflow on going back on the "tree"
                        ifNodes = getNonBlockNodesWithoutRight(node.getLeft(), ifNodes, rootCaller, checkedNodes);
                    }
                }
                Node right = node.getRight();

                if (right != null && !node.equals(rootCaller)) {
                    if (right.getId() > node.getId()) {         // prevent stack overflow on going back on the "tree"
                        ifNodes = getNonBlockNodesWithoutRight(node.getRight(), ifNodes, rootCaller, checkedNodes);
                    }
                }

            }
        }
        return ifNodes;

    }

    private Stack<Node> getIFNodesWithoutRightWithLimit(Node node, Stack<Node> ifNodes, Node rootCaller, Node limitNode) {
        // traverse the graph looking for if nodes without right until the limitNode is reached

        if (node != null) {
//            if (node.getType().equals(Node.NodeType.IF) ) {
//                if (node.getRight() == null) {
            if (!node.getType().equals(Node.NodeType.BLOCK) && node.getRight() == null
                    && !node.getType().equals(Node.NodeType.EXIT)
                    && !node.getType().equals(Node.NodeType.LOOP_EXIT)) {
                ifNodes.push(node);
//                }

            }

            Node left = node.getLeft();
            if (left != null && !left.equals(limitNode)) {
                if (left.getId() > node.getId()) {          // prevent stack overflow on going back on the "tree"
                    ifNodes = getIFNodesWithoutRightWithLimit(left, ifNodes, rootCaller, limitNode);
                }
            }
            Node right = node.getRight();

            if (right != null && !right.equals(limitNode)) {
                if (right.getId() > node.getId()) {         // prevent stack overflow on going back on the "tree"
                    ifNodes = getIFNodesWithoutRightWithLimit(right, ifNodes, rootCaller, limitNode);
                }
            }

        }

        return ifNodes;

    }

    private Node instanciateNode(Node.NodeType type, Statement stmt) {
        Node node = null;
        node = getNodeByStatement(stmt);
        if (node != null) {
            return node;
        }

        switch (type) {
            case IF:
                node = new IfNode(++lastNodeId, Node.NodeType.IF, stmt, nodeDegree);
                break;
            case BLOCK:
                node = new BlockNode(++lastNodeId, Node.NodeType.BLOCK, stmt, nodeDegree+1);
                break;

            case CASE:
                node = new SwitchCaseNode(++lastNodeId, Node.NodeType.CASE, stmt, nodeDegree);
                break;

            case FOR:
                node = new ForNode(++lastNodeId, Node.NodeType.FOR, stmt, nodeDegree);
                break;

            case FOREACH:
                node = new ForEachNode(++lastNodeId, Node.NodeType.FOREACH, stmt, nodeDegree);
                break;

            case WHILE:
                node = new WhileNode(++lastNodeId, Node.NodeType.WHILE, stmt, nodeDegree);
                break;

            case DO:
                node = new DoNode(++lastNodeId, Node.NodeType.DO, stmt, nodeDegree);
                break;

            case EXIT:
                node = new Node(++lastNodeId, Node.NodeType.EXIT, null, nodeDegree);
                break;

        }
        getNodes().add(node);

        return node;
    }

    private Node instanciateExitNode(Node loopNode) {
        for (Node node : nodes) {
            if (node.getType().equals(Node.NodeType.LOOP_EXIT)) {
                LoopExitNode loopExitNode = (LoopExitNode) node;
                if (loopExitNode.getLoopNode() == loopNode) {
                    return node;
                }
            }
        }
        Node exitNode = new LoopExitNode(++lastNodeId, loopNode);
        getNodes().add(exitNode);
        return exitNode;
    }

    private Node getNodeByStatement(Statement statement) {
        for (Node node : nodes) {
//            if (node.getClass().equals(IfNode.class)) {
//                IfNode ifnode = (IfNode) node;
//                if (ifnode.getCondition() != null) {
//                    return ifnode;
//                }
//            }
            if (node.getBaseStatement() != null) {
                if (node.getBaseStatement() == statement) {
                    return node;
                }
            }
        }
        return null;
    }

    private Node instanciateIfNode(Statement stmt, Expression condition, int id, boolean considerStatement) {
        Node node = null;
        if(considerStatement){
            node = getNodeByStatementOrCondition(stmt, condition);
        }else{
            node = getNodeByCondition(condition);
        }
        
        if (node != null) {
            return node;
        }
        if (id == 0) {
            node = new IfNode(++lastNodeId, Node.NodeType.IF, stmt, nodeDegree);
        } else {
            node = new IfNode(id, Node.NodeType.IF, stmt, nodeDegree);
            ++lastNodeId;

        }

        getNodes().add(node);

        if (condition != null) {
            IfNode ifnode = (IfNode) node;
            ifnode.setCondition(condition);
        }

        return node;
    }

    private Node getNodeByCondition(Expression condition) {
        for (Node node : nodes) {
            if (node.getClass().equals(IfNode.class)) {
                IfNode ifnode = (IfNode) node;
                if (ifnode.getCondition() != null) {
                    if (ifnode.getCondition() == condition) {
                        return ifnode;
                    }
                }
            }
        }
        return null;
    }
    
    private Node getNodeByStatementOrCondition(Statement statement, Expression condition) {
        for (Node node : nodes) {
            if (node.getClass().equals(IfNode.class)) {
                IfNode ifnode = (IfNode) node;
                if (ifnode.getCondition() != null) {
                    if (ifnode.getCondition() == condition) {
                        return ifnode;
                    }
                }
            }
            if (node.getBaseStatement() != null) {
                if (node.getBaseStatement() == statement) {
                    return node;
                }
            }
        }
        return null;
    }

    private IfNode parseIf(IfStmt ifstmt) {

        ifstmt = preParseCondition(ifstmt);

        IfNode node = (IfNode) instanciateIfNode(ifstmt, ifstmt.getCondition(), 0, true);
        node.setDegree(++nodeDegree);
        System.out.println(ifstmt.getCondition().toString() + " " + node.getId());
        if (ifstmt.getThenStmt() != null) {
            parse(ifstmt.getThenStmt(), node, "left");
        }

        if (ifstmt.getElseStmt() != null) {
            node.setHasElse(true);
            parse(ifstmt.getElseStmt(), node, "right");

        }
        if(!conditionals.contains(node)){
            conditionals.add(node);
        }
        nodeDegree--;
        return node;
    }

    private void processOriginalNodes(IfStmt ifstmt, IfStmt createdIf) {
        ifSimpleConditions.clear();
        traverseConditionTree(ifstmt.getCondition(), lastNodeId, createdIf);
        processIfSimpleConditions(ifSimpleConditions);
        Parser mockParserObj = new Parser(true);
        mockParserObj.lastNodeId = lastNodeId;
        mockParserObj.setNodes(nodes);
        mockParserObj.parseManager(ifstmt.getThenStmt(), 1);
        lastNodeId = mockParserObj.lastNodeId;
    }

    private void processIfSimpleConditions(List<IfNode> conditions) {
        for (IfNode node : conditions) {
            instanciateIfNode(node.getBaseStatement(), node.getCondition(), node.getId(), false);
        }
    }
    
    private IfStmt preParseCondition(IfStmt ifstmt) {
//        if (ifstmt.getCondition().getClass().equals(BinaryExpr.class) && !isMockParser()) {
        BinaryExpr condition = ConditionRefactorer.getBinaryCondition(ifstmt.getCondition());
        if (condition!=null) {
            if (!ConditionRefactorer.isSimpleCondition(condition)) {
                IfStmt ifstmtHash = refactoredIfStmts.get(ifstmt);
                if (ifstmtHash != null) {
                    return ifstmtHash;
                } else {
                    System.out.println("Parsing " + ifstmt.getCondition());
                    ConditionRefactorer refactorer = new ConditionRefactorer();
                    refactorer.setConditionsNextMarker(conditionsNextMarkers);
                    IfRefactorContainer container = refactorer.parseCondition(ifstmt.getCondition());
                    conditionsNextMarkers = refactorer.getConditionsNextMarker();
                    System.out.println("CONDITION TREE TRAVERSE");

                    checkedIfStmts.clear();
                    parseElseAfterRefactor(container.getCreatedIf(), ifstmt.getElseStmt());
                    checkedIfStmts.clear();

                    insertLastsNexts(ifstmt.getThenStmt());
                    System.out.println(container.getCreatedIf().toString());
                    System.out.println(" ----------  ");
                    System.out.println("");
                    if (!isMockParser()) {
                        processOriginalNodes(ifstmt,container.getCreatedIf());
                    }

                    refactoredIfStmts.put(ifstmt, container.getCreatedIf());
                    return container.getCreatedIf();
                }
            }

        }

        return ifstmt;
    }

    // apply the else rule.
    // insert the statement inside every if that dont have an else of the ifstmt
    private void parseElseAfterRefactor(IfStmt ifstmt, Statement stmt) {
        if (!checkedIfStmts.contains(ifstmt)) {
            checkedIfStmts.add(ifstmt);
            if (ifstmt.getThenStmt() != null) {
                IfStmt inner = (IfStmt) ifstmt.getThenStmt();
                parseElseAfterRefactor(inner, stmt);
            }
            if (ifstmt.getElseStmt() != null) {
                IfStmt inner = (IfStmt) ifstmt.getElseStmt();
                parseElseAfterRefactor(inner, stmt);
            } else {
                ifstmt.setElseStmt(stmt);
            }
        }
    }

    private void insertLastsNexts(Statement stmt) {
        while (!conditionsNextMarkers.empty()) {
            IfStmt marker = conditionsNextMarkers.pop();
            marker.setThenStmt(stmt);

        }
    }

    //add the simple conditions of an expression to the ifSimpleConditions list
    //return the id of the last condition added (represents the id of the created node)
    private int traverseConditionTree(Expression condition, int id, IfStmt createdIf) {
        BinaryExpr binaryCondition = ConditionRefactorer.getBinaryCondition(condition);
        if (binaryCondition!=null) {
            if (ConditionRefactorer.isSimpleCondition(binaryCondition)) {
                id = instanciateNodeForSimpleCondition(id, condition, createdIf);
            } else {
                if (binaryCondition.getLeft() != null) {
                    id = traverseConditionTree(binaryCondition.getLeft(), id, createdIf);
                }

                if (binaryCondition.getRight() != null) {
                    id = traverseConditionTree(binaryCondition.getRight(), id, createdIf);
                }
            }

        } else if (condition.getClass().equals(EnclosedExpr.class)) {
            EnclosedExpr enclosedCondition = (EnclosedExpr) condition;
            id = traverseConditionTree(enclosedCondition.getInner(), id, createdIf);
        } else if (condition.getClass().equals(MethodCallExpr.class)
                || condition.getClass().equals(UnaryExpr.class)) {
            id = instanciateNodeForSimpleCondition(id, condition, createdIf);
        }

        return id;

    }

    private int instanciateNodeForSimpleCondition(int id, Expression condition, IfStmt createdIf) {
        IfNode node = (IfNode) getNodeByStatementOrCondition(createdIf, condition);
        if (node == null) {
            node = new IfNode(++id, Node.NodeType.IF, createdIf, nodeDegree);
            node.setCondition(condition);
        }
        ifSimpleConditions.add(node);
        return id;
    }

    public void performIfandLeavesConnections(Node starterNode, Node targetNode) {
        if (targetNode != null) {
            Stack<Node> missingLinks = getLeaves(starterNode, targetNode, new Stack<Node>(), starterNode, new Stack<Node>());
            connectMissingLinksLeft(missingLinks, targetNode);
            Stack<Node> ifNodes = getIFNodesWithoutRightWithLimit(starterNode, new Stack<Node>(), starterNode, targetNode);
//            Stack<Node> ifNodes = getNonBlockNodesWithoutRight(starterNode, new Stack<Node>(), starterNode, new Stack<Node>());
            connectMissingLinksRight(ifNodes, targetNode);
        }
    }

    public void performFinalConnections(Node starterNode, Node targetNode) {
        if (targetNode != null) {
            Stack<Node> missingLinks = getLeaves(starterNode, targetNode, new Stack<Node>(), starterNode, new Stack<Node>());
            connectMissingLinksLeft(missingLinks, targetNode);
            Stack<Node> nonBlockNodes = getNonBlockNodesWithoutRight(starterNode, new Stack<Node>(), targetNode, new Stack<Node>());
            connectMissingLinksRight(nonBlockNodes, targetNode);
        }
    }


    /*
     parseBlock()
     parse each statement of the block
     in case that after the end of this parse there is no inner node created, 
     create a node to represent the block as a whole
     return the root

     */
    private Node parseBlock(BlockStmt blockstmt, Node parent, String direction) {

        Node actualNode = null;
        Node rootBlockNode = null;
        boolean hasReturn = false;
        if (blockstmt.getStmts() != null) {
            for (Statement stmt2 : blockstmt.getStmts()) {
                if (actualNode == null) {
                    actualNode = parse(stmt2, parent, direction);
                    if (actualNode != null) {
                        if (actualNode.getClass().equals(ReturnNode.class)) {
                            hasReturn = true;
                        } else {
                            rootBlockNode = actualNode;
                        }

                    }
                } else {
                    Node auxNode = null;
                    if (actualNode.getRight() == null) {
                        auxNode = parse(stmt2, actualNode, "right");
                    } else {
                        auxNode = parse(stmt2, actualNode, "");
                    }
                    if (auxNode != null) {
                        if (auxNode.getClass().equals(BreakNode.class)) {
                            if (breakScope.size() > 0) {
                                breakNodes.push(breakScope.lastElement());
                            }
                        } else if (auxNode.getClass().equals(ReturnNode.class)) {
                            hasReturn = true;
                        } else {
                            if (auxNode.getType().equals(Node.NodeType.DO)) {
                                DoNode doNode = (DoNode) auxNode;
                                performIfandLeavesConnections(actualNode, doNode.getRootNode());
                            } else {
                                performIfandLeavesConnections(actualNode, auxNode);
                            }
                            actualNode = auxNode;
                        }

                    }
                }
            }
        }
        if (rootBlockNode == null || rootBlockNode.getClass().equals(BreakNode.class)) {
            //no inner node has been created, create a node to represent the whole block
            BreakNode breakNode = (BreakNode) rootBlockNode;
            rootBlockNode = (BlockNode) instanciateNode(Node.NodeType.BLOCK, blockstmt);
            rootBlockNode.setDegree(nodeDegree+1);
            if (breakNode != null) {
                breakNodes.push(rootBlockNode);
            }

            if (hasReturn) {
                if (!returnNodes.contains(rootBlockNode)) {
                    returnNodes.push(rootBlockNode);
                }
            }
//            System.out.println(blockstmt.getStmts().get(0).toString() + " " + rootBlockNode.getId());

        }
        if (rootBlockNode.getType().equals(Node.NodeType.DO)) {
            DoNode doNode = (DoNode) rootBlockNode;
            rootBlockNode = doNode.getRootNode();
        }
        return rootBlockNode;
    }

    private SwitchCaseNode parseSwitch(SwitchStmt switchstmt) {
        boolean first = true;
        SwitchCaseNode rootCase = null;
        SwitchCaseNode actualNode = null;
        Node internalParent;

        for (SwitchEntryStmt entry : switchstmt.getEntries()) {  // each entry is a case of the switch

            internalParent = actualNode;
            actualNode = (SwitchCaseNode) instanciateNode(Node.NodeType.CASE, entry);

            if (first) {
                rootCase = actualNode;
                first = false;
            }

            if (internalParent != null) {
                if (internalParent.getType().equals(Node.NodeType.CASE)) {
                    internalParent.setRight(actualNode);
                }
            }

            if (entry.getLabel() != null) {
                System.out.println("case " + entry.getLabel().toString() + " " + actualNode.getId());
            } else {
                System.out.println("default: " + actualNode.getId());
            }

            BlockStmt blockStmt = new BlockStmt(entry.getStmts());

            breakScope.push(actualNode);
            parse(blockStmt, actualNode, "left");
            breakScope.pop();

            System.out.println("");
            lastSwitchCase = actualNode;
        }
        return rootCase;
    }

    private Node parseFor(ForStmt forstmt) {
        ForNode forNode = (ForNode) instanciateNode(Node.NodeType.FOR, forstmt);
        forNode.setForstmt(forstmt);
        return parseLoop(forNode, forstmt.getBody());
    }

    private Node parseForeach(ForeachStmt forstmt) {
        ForEachNode forNode = (ForEachNode) instanciateNode(Node.NodeType.FOREACH, forstmt);
        forNode.setForstmt(forstmt);
        return parseLoop(forNode, forstmt.getBody());
    }

    private void connectInnerLoopNodes(Node topNodeFromBreakNodes, Node target) {
        if (!breakNodes.empty()) {
            Node breakNode = breakNodes.lastElement();
            if (breakNode.getType() != Node.NodeType.CASE && !Node.isLoopNode(target)) {
                while (breakNode != topNodeFromBreakNodes) {
                    breakNode.setLeft(target);
                    breakNode.setRight(null);
//                System.out.println("Aplicando a regra para : " + breakNode.getId());
                    breakNodes.pop();
                    if (!breakNodes.empty()) {
                        breakNode = breakNodes.lastElement();
                    } else {
                        breakNode = null;
                    }

                }
            }
        }
    }

    private Node parseWhile(WhileStmt whilestmt) {
        WhileNode whileNode = (WhileNode) instanciateNode(Node.NodeType.WHILE, whilestmt);
        return parseLoop(whileNode, whilestmt.getBody());
    }

    private Node parseLoop(LoopNode node, Statement nodeBody) {
        System.out.println("loopNode id: " + node.getId());
        Node topNodeFromBreakNodes = null;
        if (!breakNodes.empty()) {
            topNodeFromBreakNodes = breakNodes.lastElement();
        }

        Node nodeChild = parse(nodeBody, node, "left");
        Node exitNode = instanciateExitNode(node);
        node.setExitNode(exitNode);
        performIfandLeavesConnections(nodeChild, exitNode);
        connectInnerLoopNodes(topNodeFromBreakNodes, node);

        if (nodeChild.getRight() == null && !nodeChild.getType().equals(Node.NodeType.BLOCK)) {
            nodeChild.setRight(exitNode);
        }

        exitNode.setLeft(node);
        return node;
    }

    private DoNode parseDo(DoStmt dostmt, Node parent, String direction) {

        Node topNodeFromBreakNodes = null;
        if (!breakNodes.empty()) {
            topNodeFromBreakNodes = breakNodes.lastElement();
        }

        Node doBlockNode = parse(dostmt.getBody(), parent, direction);

        DoNode doNode = (DoNode) instanciateNode(Node.NodeType.DO, dostmt); //  is the condition in the exit (while)
        doNode.setRootNode(doBlockNode);

        performIfandLeavesConnections(doBlockNode, doNode);
        connectInnerLoopNodes(topNodeFromBreakNodes, doNode);

        doNode.setLeft(doBlockNode);

//        getNodes().add(doBlockNode);
        return doNode;
    }

    private void checkLateralLink(Node actualNode) {
        // connects the inner block of an if node with the next node (forming a triangle)
        // TODO: create documentation of the logic (including images)
        Node parent = actualNode.getParent();
        if (parent != null) {
            Node parentLeft = parent.getLeft();
            Node parentRight = parent.getRight();
            if (parentLeft != null && parentRight != null) {
                if (parent.getType() == Node.NodeType.IF) {
                    if (!parent.hasElse()) {
                        if (parentLeft.getType().equals(Node.NodeType.BLOCK)
                                && parentRight.equals(actualNode)) {
                            parentLeft.setLeft(actualNode);
                        }
                    }
                }
            }
        }
    }

    public Stack<Node> getMostRecentChildren(Node node, Stack<Node> recentChilds, Node exitNode, Node rootCaller) {
        // traverse the graph looking for nodes without childs
        if (node != null) {
            if (node.getLeft() == null && node.getRight() == null) {
                recentChilds.push(node);
            } else {

                Node left = node.getLeft();
                if (left != null) {
                    if (left.getId() > node.getId()) {          // prevent stack overflow on going back on the "tree"
                        recentChilds = getMostRecentChildren(node.getLeft(), recentChilds, exitNode, rootCaller);
                    }
                }
                Node right = node.getRight();

                if (right != null) {
                    if (right.getId() > node.getId()) {         // prevent stack overflow on going back on the "tree"
                        recentChilds = getMostRecentChildren(node.getRight(), recentChilds, exitNode, rootCaller);
                    }
                } else {
                    if (Node.isLoopNode(node) && !node.equals(rootCaller)) {
                        node.setRight(exitNode);
                    }
                }
            }

        }

        return recentChilds;
    }

    private Stack<Node> getLeaves(Node starterNode, Node finalNode, Stack<Node> leaves, Node rootCaller, Stack<Node> checkedNodes) {
        if (starterNode != null) {
            if (!checkedNodes.contains(starterNode)) {
                checkedNodes.push(starterNode);
                if (starterNode.getLeft() == null && starterNode.getRight() == null) {
                    leaves.push(starterNode);
                } else {

                    Node left = starterNode.getLeft();

                    if (left != null && !left.equals(finalNode) && !left.equals(rootCaller)) {

                        leaves = getLeaves(left, finalNode, leaves, rootCaller, checkedNodes);

                    }

                    Node right = starterNode.getRight();

                    if (right != null && !right.equals(rootCaller) && !right.equals(finalNode)) {

                        leaves = getLeaves(right, finalNode, leaves, rootCaller, checkedNodes);

                    }

//                if (right != null && !right.equals(rootCaller.getRight()) && !right.equals(finalNode)) {
                }
            }
        }
        return leaves;
    }

    public void connectMissingLinksLeft(Stack<Node> missingLinks, Node target) {
        while (missingLinks.size() > 0) {
            Node node = missingLinks.pop();
            node.setLeft(target);
        }
    }

    public void connectMissingLinksRight(Stack<Node> missingLinks, Node target) {
        while (missingLinks.size() > 0) {
            Node node = missingLinks.pop();
            node.setRight(target);
        }
    }

    private boolean checkStatement(Statement stmt) {
        //checks if the statement needs to be evaluated
        String className = stmt.getClass().getSimpleName();
        List<String> classes = Arrays.asList("IfStmt", "BlockStmt", "SwitchStmt", "ForStmt",
                "WhileStmt", "DoStmt", "TryStmt", "ForeachStmt", "BreakStmt", "ReturnStmt");
        return classes.contains(className);
    }

    protected void connectClosingNode(Statement mockStatement) {
        Node closingNode = instanciateNode(Node.NodeType.EXIT, null);
        closingNode.setBaseStatement(mockStatement);
        setExitNode(closingNode);

        while (returnNodes.size() > 0) {
            Node returnNode = returnNodes.pop();
            returnNode.setLeft(closingNode);
        }
        performFinalConnections(getRoot(), closingNode);

    }

    public Node createBlockNode(Statement stmt) {
        return parseBlock((BlockStmt) stmt, root, "");
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public Node getLastParent() {
        return lastParent;
    }

    public void setLastParent(Node lastParent) {
        this.lastParent = lastParent;
    }

    public Node getExitNode() {
        return exitNode;
    }

    public void setExitNode(Node exitNode) {
        this.exitNode = exitNode;
    }
    
    /**
     * @return the conditionals
     */
    public LinkedList<Node> getConditionals() {
        return conditionals;
    }

    /**
     * @param conditionals the conditionals to set
     */
    public void setConditionals(LinkedList<Node> conditionals) {
        this.conditionals = conditionals;
    }

}

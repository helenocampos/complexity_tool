/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn;

import com.mycompany.complexity.tool.mvn.Nodes.IfNode;
import com.mycompany.complexity.tool.mvn.Nodes.Node;
import java.util.LinkedList;

/**
 *
 * @author helenocampos
 */
public class ConditionsChain {

    private LinkedList<Node> conditions;

    // the next node outside the scope of the conditions chain
    private Node nextNodeOutsideScope;

    public ConditionsChain() {
        conditions = new LinkedList<>();
        nextNodeOutsideScope = null;
    }

    public void addCondition(Node condition) {
        getConditions().add(condition);
    }

    public LinkedList<Node> getConditions() {
        return conditions;
    }

    public void setConditions(LinkedList<Node> conditions) {
        this.conditions = conditions;
    }

    public Node getNextNodeOutsideScope() {
        return nextNodeOutsideScope;
    }

    // find the next node outside the scope of the chain
    public void setNextNodeOutsideScope() {
        if (conditions.size() > 0) {
            Node lastCondition = conditions.get(conditions.size() - 1);
            if (!lastCondition.hasElse()) {
                if (lastCondition.getRight() != null) {
                    nextNodeOutsideScope = lastCondition.getRight();
                } else {
                    nextNodeOutsideScope = lastCondition.getLeft();
                }
            } else {
                nextNodeOutsideScope = findNextNodeOutsideScope(lastCondition);
            }

        }
    }

    public Node findNextNodeOutsideScope(Node node) {
        if (node.getLeft() != null) {
            if (node.getLeft().getDegree() < node.getDegree()) {
                //found the node outside of the initial scope
                return node;
            } else {
                //go deeper
                return findNextNodeOutsideScope(node.getLeft());
            }
        } else {
            //reached the EXIT node
            return node;
        }
    }

    /*
        Checks if the last condition in the cluster has an if to the right
        If true, the cluster isn't valid. There can't be transformations in the middle of a chain
    */
    public boolean checkConsistency() {
        Node lastNode = conditions.get(conditions.size() - 1);
        if (lastNode.getRight() != null) {
            IfNode ifnode = (IfNode) lastNode;
            if (PatternAnalysis.continueToRightNode(ifnode)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String conditions = "";
        if (this.conditions != null) {

            for (Node node : this.conditions) {
                if (node instanceof IfNode) {
                    conditions += node.getPredicateText() + " ;";
                }
            }
        }
        return conditions;
    }

}

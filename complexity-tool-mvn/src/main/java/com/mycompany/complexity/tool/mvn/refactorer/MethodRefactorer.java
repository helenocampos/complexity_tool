package com.mycompany.complexity.tool.mvn.refactorer;

import com.mycompany.complexity.tool.mvn.Nodes.Node;
import static com.mycompany.complexity.tool.mvn.Nodes.Node.NodeType.EXIT;
import static com.mycompany.complexity.tool.mvn.Nodes.Node.NodeType.IF;
import com.mycompany.complexity.tool.mvn.Parser;
import java.util.ArrayList;

/**
 *
 * @author Nathan Manera Magalhães
 */
public class MethodRefactorer {
    public static String getRefactoredMethod(String m, Parser parser, Parser secondaryParser){
        ArrayList<Node> originalNodes = new ArrayList<>();
        ArrayList<Node> optmizedNodes = new ArrayList<>();
        originalNodes = getNodesByRoot(parser.getRoot(), originalNodes);
        optmizedNodes = getNodesByRoot(secondaryParser.getRoot(), optmizedNodes);
        ArrayList<Node> excludedNodes = new ArrayList<>();
        ArrayList<Node> analizedNodes = new ArrayList<>();
        ArrayList<String> ifStrings = new ArrayList<>();
        ArrayList<String> ifStringsID = new ArrayList<>();
        ArrayList<String> ifStringsStmts = new ArrayList<>();
        for (Node atualNode : originalNodes) {
            if(!checkIfNodeExists(atualNode.getId(), optmizedNodes)){
                excludedNodes.add(atualNode);
            }
        }
        excludedNodes = sortNodesByDecreasingDegree(excludedNodes);
        String e = "";
        if(!excludedNodes.isEmpty()){
            for (Node atualNode : excludedNodes) {
                if(atualNode.getType().equals(IF) && !analizedNodes.contains(atualNode)){
                ifStrings = getAllIfStrings(m, ifStrings, ifStringsStmts, ifStringsID, sortNodesByID(originalNodes), analizedNodes);
                    if(containsPredicateText(m, atualNode.getPredicateText())){
                        String ifStrStmt = getIfString(atualNode.getPredicateText(), ifStringsStmts, ifStringsID, atualNode.getId());
                        String newIfStrStmt;
                        boolean changed = false;
                        if(ifStrStmt.contains("&&") || ifStrStmt.contains("||")){
                            newIfStrStmt = refactoreIfStringStmt(ifStrStmt, atualNode);
                            m = replaceIfStatement(m, ifStrStmt, newIfStrStmt, null, ifStrings, ifStringsID);
                            changed = true;
                        }
                        if(changed == true){
                            ifStrings.removeAll(ifStrings);
                            ifStringsID.removeAll(ifStringsID);
                            ifStringsStmts.removeAll(ifStringsStmts);
                            ifStrings = getAllIfStrings(m, ifStrings, ifStringsStmts, ifStringsID, sortNodesByID(originalNodes), analizedNodes);
                        }
                        Node nodeOptmized = getNodeByRightNode(atualNode, optmizedNodes, excludedNodes);
                        Node nodeOriginal = null;
                        if(nodeOptmized != null){
                            nodeOriginal = getNodeById(nodeOptmized.getId(), originalNodes);
                        }else if(atualNode.getType().equals(IF) && (excludedNodes.contains(atualNode.getLeft()) || excludedNodes.contains(atualNode.getRight()))){
                            ifStrStmt = getIfString(atualNode.getPredicateText(), ifStringsStmts, ifStringsID, atualNode.getId());
                            m = replaceIfStatement(m, ifStrStmt, "", null, ifStrings, ifStringsID);
                            analizedNodes = getInsideNodes(atualNode, null, analizedNodes, false);
                            String msg = "\n/*\nCONDITION: ("+atualNode.getPredicateText()+")\n"
                                    + " IS UNNECESSARY AND UNREACHABLE\n*/";
                            if(!e.contains(msg)){
                                e += msg;
                            }
                        }else{
                            e += "\n//NOT CORRECTLY REFACTORED 1";
                        }
                        if(nodeOptmized != null && nodeOriginal != null){
                            if(!nodeOriginal.hasElse() && nodeOptmized.hasElse()){
                                m = replaceIfStatement(m, getIfString(atualNode.getPredicateText(), ifStrings, ifStringsID, atualNode.getId()), "else ", atualNode, ifStrings, ifStringsID);
                            }else{
                                m = replaceIfStatement(m, getIfString(atualNode.getPredicateText(), ifStrings, ifStringsID, atualNode.getId()), "", atualNode, ifStrings, ifStringsID);
                            }
                            analizedNodes.add(atualNode);
                        }
                    }else{
                        e += "\n//NOT CORRECTLY REFACTORED 2";
                    }
                    ifStrings.removeAll(ifStrings);
                    ifStringsID.removeAll(ifStringsID);
                    ifStringsStmts.removeAll(ifStringsStmts);
                }else if(!analizedNodes.contains(atualNode)){
                    analizedNodes.add(atualNode);
                }
            }
            m = searchUnreachableElse(m);
            getAllIfStrings(m, ifStrings, ifStringsStmts, ifStringsID, sortNodesByID(originalNodes), analizedNodes);
            if((checkIfIdWasAdded(ifStringsID, "NaN"))){
                e += "\n//ERROR IN REFACTORING";
            }
        }
        return m+=e;
    }
    private static ArrayList<Node> sortNodesByDecreasingDegree(ArrayList<Node> nodes){
        nodes = sortNodesByID(nodes);
        int higherDegree = 0;
        ArrayList<Node> sortedNodes = new ArrayList<>();
        for (Node node : nodes) {
            if(node.getDegree() > higherDegree){
                higherDegree = node.getDegree();
            }
        }
        ArrayList<Node> t = new ArrayList<>();
        while(higherDegree > 0){
            for (Node node : nodes) {
                if(node.getDegree() == higherDegree){
                    t.add(node);
                }
            }
            sortedNodes.addAll(sortNodesByID(t));
            t.removeAll(t);
            higherDegree--;
        }
        return sortedNodes;
    }
    private static ArrayList<Node> sortNodesByID(ArrayList<Node> nodes){
        ArrayList<Node> sortedNodes = new ArrayList<>();
        int higherID = 0;
        boolean t = true;
        while(t){
            t = false;
            for (Node node : nodes) {
                if(!sortedNodes.contains(node)){
                    if(node.getId() > higherID){
                        sortedNodes.add(node);
                        higherID = node.getId();
                    }else if(node.getId() < higherID){
                        boolean s = true;
                        while(s){
                            s = false;
                            if(sortedNodes.size() > 1){
                                sortedNodes.remove(sortedNodes.size()-1);
                                higherID = sortedNodes.get(sortedNodes.size()-1).getId();
                            }else if(sortedNodes.size() == 1){
                                sortedNodes.remove(sortedNodes.size()-1);
                                higherID = 0;
                            }
                            if(node.getId() > higherID){
                                sortedNodes.add(node);
                                higherID = node.getId();
                                t = true;
                            }else{
                                s = true;
                            }
                        }
                        break;
                    }
                }
            }
        }
        return sortedNodes;
    }
    private static String searchUnreachableElse(String m){
        CharSequence Else = "else";
        CharSequence If = "if";
        boolean repeat = true;
        while(repeat){
            int countElse = 0;
            int countIf = 0;
            ArrayList<Integer> elseStart = new ArrayList<>();
            elseStart.add(-1);
            String before;
            String after;
            CharSequence method = m;
            repeat = false;
            ArrayList<Boolean> foundedElse = new ArrayList<>();
            ArrayList<Boolean> foundedBrace = new ArrayList<>();
            foundedElse.add(false);
            foundedBrace.add(false);
            int countBraces = 0;
            for (int i = 0; i < method.length(); i++) {
                //System.out.println("|"+method.charAt(i)+"| |"+i+"|");
                if(method.charAt(i) == '{' && !itIsBetweenQuoteMarks(method, "{", i)){
                    if(foundedElse.get(countBraces) && elseStart.get(countBraces) == -1){
                        foundedBrace.set(countBraces, true);
                    }
                    countBraces++;
                    elseStart.add(-1);
                    foundedElse.add(false);
                    foundedBrace.add(false);
                }else if(method.charAt(i) == '}' && !itIsBetweenQuoteMarks(method, "}", i)){
                    countBraces--;
                    elseStart.remove(elseStart.size()-1);
                    foundedElse.remove(foundedElse.size()-1);
                    foundedBrace.remove(foundedBrace.size()-1);
                }
                if(method.charAt(i) == Else.charAt(countElse)){
                    if(foundedElse.get(countBraces) && elseStart.get(countBraces) == -1){
                        elseStart.set(countBraces, i);
                    }
                    if(countElse == Else.length()-1){ 
                        if(!itIsBetweenQuoteMarks(method, (String) Else, i-(Else.length()+1))){
                            if(foundedElse.get(countBraces) && elseStart.get(countBraces) != -1){
                                m = (String) method;
                                before = m.substring(0, lastPositionOfBlankSpace((elseStart.get(countBraces))-1, "end", m));
                                if(foundedBrace.get(countBraces)){
                                    after = m.substring(findIfCloseBraces(m, (elseStart.get(countBraces)+(Else.length()-1)), true)+1, method.length());
                                }else{
                                    after = m.substring((elseStart.get(countBraces)+(Else.length()-1))+1, method.length());
                                }
                                m = before.concat(after);
                                repeat = true;
                                break;
                            }else{
                                foundedElse.set(countBraces, true);
                            }
                        }else{
                            elseStart.set(countBraces, -1);
                        }
                        countElse = 0;
                    }else if(countElse < Else.length()-1){
                        countElse++;
                    }
                }else{
                    if(foundedElse.get(countBraces)){
                        elseStart.set(countBraces, -1);
                    }
                    countElse = 0;
                }
                if(elseStart.get(countBraces) == -1 && foundedElse.get(countBraces)){
                    if(method.charAt(i) == If.charAt(countIf)){
                        if(countIf == If.length()-1){ 
                            if(!itIsBetweenQuoteMarks(method, (String) If, i-(If.length()+1))){
                                foundedElse.set(countBraces, false);
                                foundedBrace.set(countBraces, false);
                            }else{
                                countIf = 0;
                            }
                        }else if(countIf < If.length()-1){
                            countIf++;
                        }
                    }else{
                        countIf = 0;
                    }
                }
            }
        }
        return m;
    }
    private static String refactoreIfStringStmt(String ifStringStmt, Node atualNode){
        String condition = atualNode.getPredicateText();
        boolean conditionIsInsideExtraParenthesis = true;
        int c = 0;
        while(conditionIsInsideExtraParenthesis){
            c++;
            CharSequence ifStrStmt = ifStringStmt;
            conditionIsInsideExtraParenthesis = false;
            int countP = 0;
            int countC = 0;
            int posAND = -1;
            int posOR = -1;
            int conditionStart = -1;
            int conditionEnd = -1;
            int parenthesisStart = -1;
            int parenthesisEnd = -1;
            int blockStart = -1;
            int blockEnd = -1;
            for (int i = 0; i < ifStrStmt.length(); i++) {
                //-----------------[Get Parenthesis position]-------------------
                if(ifStrStmt.charAt(i) == '(' && !itIsBetweenQuoteMarks(ifStrStmt, "(", i)){
                    if(countP == 0 && parenthesisStart == -1){
                        parenthesisStart = i;
                    }
                    countP++;
                }else if(ifStrStmt.charAt(i) == ')' && !itIsBetweenQuoteMarks(ifStrStmt, ")", i)){
                    countP--;
                    if(parenthesisStart > -1 && parenthesisEnd == -1 && countP == 0){   
                        parenthesisEnd = i;   
                    }
                }
                if((parenthesisStart > -1 && parenthesisEnd > -1) && (conditionStart == -1 && conditionEnd == -1)){
                    parenthesisStart = -1;
                    parenthesisEnd = -1;
                }
                //-----------------[Get Condition position]---------------------
                if(ifStrStmt.charAt(i) == condition.charAt(countC)){
                    if(conditionStart == -1 && countC == 0){
                        conditionStart = i;
                        conditionIsInsideExtraParenthesis = countP > c;
                    }else if(conditionEnd == -1 && countC == (condition.length()-1)){
                        conditionEnd = i+1;
                        conditionIsInsideExtraParenthesis = countP > c;
                    }
                    if(countC < (condition.length()-1)){
                        countC++;
                    }
                }else{
                    countC = 0;
                    if(conditionEnd == -1 && conditionStart != -1){
                        conditionStart = -1;
                    }
                }
                //------------------[Get Operators position]--------------------
                if(countP == c){
                    if(ifStrStmt.charAt(i) == "&&".charAt(0) && ifStrStmt.charAt(i+1) == "&&".charAt(1) && !itIsBetweenQuoteMarks(ifStrStmt, "&&", i)){
                        if(conditionStart == -1 && conditionEnd == -1){
                            posAND = i;
                        }else if((conditionStart > -1) && (conditionEnd > -1 && conditionEnd < i) && (posAND < conditionStart-1)){
                            posAND = i;
                        }
                    }else if(ifStrStmt.charAt(i) == "||".charAt(0) && ifStrStmt.charAt(i+1) == "||".charAt(1) && !itIsBetweenQuoteMarks(ifStrStmt, "||", i)){
                        if(conditionStart == -1 && conditionEnd == -1){
                            posOR = i;
                        }else if(conditionStart > -1 && (conditionEnd > -1 && conditionEnd < i) && (posOR < conditionStart-1)){
                            posOR = i;
                        }
                    }
                }
                //----------------[Get Block Position]--------------------------
                if(countP == 0 && (conditionStart > -1 && conditionEnd > -1) && (blockStart == -1 && blockEnd == -1)){
                    blockStart = i+1;
                    blockEnd = findIfCloseBraces((String) ifStrStmt, i+1, false);
                    break;
                }
            }
            ifStringStmt = (String) ifStrStmt;
            String before = "";
            String replacement = "";
            String block = ifStringStmt.substring(blockStart, blockEnd+1);
            String after = "";
            if(posAND > -1 && posOR > -1){
                if(posAND < conditionStart){
                    if(posOR < conditionStart){
                        if(posAND < posOR){
                            posAND = -1;
                        }else if(posAND > posOR){
                            posOR = -1;
                        }
                    }else if(posOR > conditionEnd){
                        posOR = -1;
                    }
                }else if(posAND > conditionEnd){
                    if(posOR < conditionStart){
                        posAND = -1;
                    }else if(posOR > conditionEnd){
                        if(posAND < posOR){
                            posOR = -1;
                        }else if(posAND > posOR){
                            posAND = -1;
                        }
                    }
                }
            }
            if(posAND > -1){
                before = ifStringStmt.substring(0, lastPositionOfBlankSpace(posAND-1, "end", ifStringStmt))+")";
                replacement = " if ";
                after = "("+ifStringStmt.substring(lastPositionOfBlankSpace(posAND+2, "start", ifStringStmt), ifStringStmt.length());
            }else if(posOR > -1){
                before = ifStringStmt.substring(0, lastPositionOfBlankSpace(posOR-1, "end", ifStringStmt))+")";
                replacement = " "+block+" else if ";
                after = "("+ifStringStmt.substring(lastPositionOfBlankSpace(posOR+2, "start", ifStringStmt), ifStringStmt.length());
            }
            if(conditionIsInsideExtraParenthesis && c > 0 && (posAND > -1 || posOR > -1)){
                before = removeUnnecessaryParenthesis(before);
                after = removeUnnecessaryParenthesis(after);
                c--;
            }
            if(!before.equals("") && !after.equals("")){
                ifStringStmt = before.concat(replacement.concat(after));
            }
        }
        return ifStringStmt;
    }
    private static String removeUnnecessaryParenthesis(String str){
        int parenthesisStart = -1;
        int parenthesisEnd = -1;
        boolean repeat = true;
        while(repeat){
            parenthesisStart = -1;
            parenthesisEnd = -1;
            int countP = 0;
            CharSequence m = str;
            for (int i = 0; i < m.length(); i++) {
                if(m.charAt(i) == '(' && !itIsBetweenQuoteMarks(m, "(", i)){
                    if(countP == 0 && parenthesisStart == -1 && parenthesisEnd == -1 ){
                        parenthesisStart = i;
                    }
                    countP++;
                }else if(m.charAt(i) == ')' && !itIsBetweenQuoteMarks(m, ")", i)){
                    countP--;
                    if(countP == 0 && parenthesisStart != -1 && parenthesisEnd == -1 ){
                        parenthesisEnd = i;
                    }
                }
            }
            str = (String) m;
            if(countP > 0){
                str = str+")";
                repeat = true;
            }else if(countP < 0){
                str = "("+str;
                repeat = true;
            }else{
                repeat = false;
            }
        }
        if(parenthesisStart != -1 && parenthesisEnd != -1){
            String before;
            String middle;
            String after;
            before = str.substring(0, parenthesisStart);
            middle = str.substring(parenthesisStart+1, parenthesisEnd);
            after = str.substring(parenthesisEnd+1, str.length());
            str = before.concat(middle.concat(after));
        }
        return str;
    }
    private static String replaceIfStatement(String m, String ifstmt, String replacement, Node atualNode, ArrayList<String> ifStr, ArrayList<String> ifStrID){
        CharSequence str = ifstmt;
        CharSequence method = m;
        String before;
        String after;
        int count = 0;
        for (int i = 0; i < method.length(); i++) {
            if(method.charAt(i) == str.charAt(count)){
                count++;
                if(count == str.length()){
                    String id = getIfStringID(ifstmt, ifStr, ifStrID);
                    if(atualNode != null && !id.contains(Integer.toString(atualNode.getId()))){
                        ifStr.remove(getIfString(ifstmt, ifStr, ifStrID, atualNode.getId()));
                        ifStrID.remove(id);
                        count = 0;
                    }else{
                        m = (String) method;
                        before = m.substring(0, (i-count)+1);
                        after = m.substring((i+1), method.length());
                        m = before.concat(replacement.concat(after));
                        break;
                    }
                }
            }else if(method.charAt(i) == str.charAt(0)){
                count = 1;
            }else{
                count = 0;
            }
        }
        return m;
    }
    private static Integer lastPositionOfBlankSpace(int i, String position, String m){
        CharSequence ms = m;
        if(position.equals("start")){
            while(i < ms.length()){
                if(ms.charAt(i) == ' '){
                    i++;
                }else{
                    break;
                }
            }
        }else if(position.equals("end")){
            while(i > 0){
                if(ms.charAt(i) == ' '){
                    i--;
                }else{
                    i++;
                    break;
                }
            }
        }
        return i;
    }
    private static boolean containsPredicateText(String s, String value){
        return !s.contains("\""+value+"\"") && !s.contains("\'"+value+"\'") && (s.contains("("+value+")") || s.contains("("+value+" ") || s.contains(" "+value+")") || s.contains(" "+value+" "));
    }
    private static ArrayList<String> getAllIfStrings(String s, ArrayList<String> ifStrings, ArrayList<String> ifStringsStmts, ArrayList<String> ifStringsID, ArrayList<Node> nodes, ArrayList<Node> analizedNodes){
        String n = s;
        while(n.contains("if")){
            n = n.substring(n.indexOf("if"));
            int lp = findIfCloseParentheses(n, 0)+1;
            int lb = findIfCloseBraces(n, 0, false)+1;
            String ifString = n.substring(n.indexOf("if"), lp);
            String ifStringStmt = n.substring(n.indexOf("if"), lb);
            boolean founded = false;
            for (Node node : nodes) {
                if(ifString.contains(node.getStatementText()) && ((!ifString.contains("&&") && !ifString.contains("||")) || (ifString.contains("\'&&\'") || ifString.contains("\'||\'") || ifString.contains("\"&&\"") || ifString.contains("\"||\""))) && !checkIfIdWasAdded(ifStringsID, Integer.toString(node.getId())) && !analizedNodes.contains(node)){
                    ifStringsID.add(Integer.toString(node.getId()));
                    System.out.println("| "+ifStringsID.get(ifStringsID.size()-1)+" |\t| "+ifString+" |");
                    ifStrings.add(ifString);
                    ifStringsStmts.add(ifStringStmt);
                    founded = true;
                    break;
                }else{
                    founded = false;
                }
            }
            if(!founded){
                String ids = "";
                ArrayList<String> readedNodes = new ArrayList<>();
                for (Node node : nodes) {
                    if(containsPredicateText(ifString, node.getPredicateText())){
                        if(!readedNodes.contains(node.getPredicateText()) && !checkIfIdWasAdded(ifStringsID, Integer.toString(node.getId()))){
                            if(!analizedNodes.contains(node)){
                                ids += Integer.toString(node.getId())+" ";
                                readedNodes.add(node.getPredicateText());
                            }else{
                                ids += "NaN:("+Integer.toString(node.getId())+") ";
                            }
                        }
                    }
                }
                if (!ids.equals("")){ 
                    ifStringsID.add(ids);
                    System.out.println("| "+ifStringsID.get(ifStringsID.size()-1)+" |\t| "+ifString+" |");
                    ifStrings.add(ifString);
                    ifStringsStmts.add(ifStringStmt);
                }
            }
            n = n.substring(lp);
        }
        System.out.println("----------");
        return ifStrings;
    }
    private static boolean checkIfIdWasAdded(ArrayList<String> ifStringsID, String id){
        for (String ifStrID : ifStringsID) {
            if(ifStrID.contains(id)){
                return true;
            }
        }
        return false;
    }
    private static String getIfString(String s, ArrayList<String> ifStrings, ArrayList<String> ifStringsID, Integer atualNodeID){
        ArrayList<String> ifStrs = new ArrayList<>();
        ArrayList<String> ifStrsID = new ArrayList<>();
        int count = 0;
        for (String ifString : ifStrings) {
            if(ifString.contains(s)){
                ifStrs.add(ifString);
                ifStrsID.add(ifStringsID.get(count));
            }
            count++;
        }
        if(ifStrs.size() == 1){
            return ifStrs.get(0);
        }else if(ifStrs.size() > 1){
            int count2 = 0;
            for (String ifStrID : ifStrsID) {
                if(ifStrID.contains(Integer.toString(atualNodeID))){
                    return ifStrs.get(count2);
                }
                count2++;
            }
        }
        return s;
    }
    private static String getIfStringID(String s, ArrayList<String> ifStrings, ArrayList<String> ifStringsID){
        int count = 0;
        if(ifStrings.size() == ifStringsID.size()){
            for (String ifString : ifStrings) {
                if(ifString.contains(s)){
                    return ifStringsID.get(count);
                }
                count++;
            }
        }
        return "-1";
    }
    private static boolean checkIfNodeExists(Integer id, ArrayList<Node> optmizedNodes){
        for (Node atualNode : optmizedNodes) {
            if(atualNode.getId() == id){
                return true;
            }
        }
        return false;
    }
    private static Node getNodeById(Integer id, ArrayList<Node> nodes){
        for (Node node : nodes) {
            if(node.getId() == id){
                return node;
            }
        }
        return null;
    }
    private static Node getNodeByRightNode(Node atualNode, ArrayList<Node> optmizedNodes, ArrayList<Node> excludedNodes){
        Node nodeL = atualNode.getLeft();
        ArrayList<Node> insideNodes = new ArrayList<>();
        insideNodes = getInsideNodes(atualNode, null, insideNodes, false);
        if(excludedNodes.contains(nodeL)){
            for (Node insideNode : insideNodes) {
                if(!excludedNodes.contains(insideNode)){
                    nodeL = insideNode;
                    break;
                }
            }
        }
        if(nodeL == null || nodeL.getType().equals(EXIT)){
            return null;
        }else{
            for (Node node : optmizedNodes) {
                if(node.getRight() != null && node.getRight().getId() == nodeL.getId()){
                    return node;
                }
            }
        }
        return null;
    }
    private static ArrayList<Node> getNodesByRoot(Node nodeRoot, ArrayList<Node> nodes){ 
        nodes.add(nodeRoot);
        if(nodeRoot.getLeft() != null && !nodes.contains(nodeRoot.getLeft())){
            nodes = getNodesByRoot(nodeRoot.getLeft(), nodes);
            if(nodeRoot.getRight()!= null && !nodes.contains(nodeRoot.getRight())){
                nodes = getNodesByRoot(nodeRoot.getRight(), nodes);
            }
        }else if(nodeRoot.getRight()!= null && !nodes.contains(nodeRoot.getRight())){
            nodes = getNodesByRoot(nodeRoot.getRight(), nodes);
        }else if(nodeRoot.getLeft() == null && nodeRoot.getRight() == null){
            return nodes;
        }
        return nodes;
    }
    private static ArrayList<Node> getInsideNodes(Node atualNode, Node rootNode, ArrayList<Node> nodes, boolean degree){
        if(nodes == null){
            nodes = new ArrayList<>();
        }
        if(rootNode == null){
            rootNode = atualNode;
            nodes.add(atualNode);
        }else if((atualNode.getDegree() <= rootNode.getDegree() && !rootNode.hasElse()) || (atualNode.getDegree()+1 <= rootNode.getDegree() && rootNode.hasElse())){
            degree = true;
        }else if(!degree){
            nodes.add(atualNode);
        }
        if(atualNode.getLeft() != null && !degree && atualNode.getLeft().getId() != rootNode.getRight().getId() && !nodes.contains(atualNode.getLeft())){
            nodes = getInsideNodes(atualNode.getLeft(), rootNode, nodes, degree);
            if(atualNode.getRight()!= null && !degree && atualNode.getRight().getId() != rootNode.getRight().getId() && !nodes.contains(atualNode.getRight())){
                nodes = getInsideNodes(atualNode.getRight(), rootNode, nodes, degree);
            }
        }else if(atualNode.getRight()!= null && !degree && atualNode.getRight().getId() != rootNode.getRight().getId() && !nodes.contains(atualNode.getRight())){
            nodes = getInsideNodes(atualNode.getRight(), rootNode, nodes, degree);
        }else if((atualNode.getLeft() == null && atualNode.getRight() == null)){
            return nodes;
        }
        return nodes;
    }
    private static int findIfCloseParentheses(String n, int k){
        int i = k;
        CharSequence p = n;
        int count = 0;
        for (int j = i; j < p.length(); j++) {
            if(p.charAt(j) == '(' && !itIsBetweenQuoteMarks(p, "(", j)){
                count++;
            }else if(p.charAt(j) == ')' && !itIsBetweenQuoteMarks(p, ")", j)){
                count--;
                if(count == 0){
                    i = j;
                    break;
                }
            }
        }
        return i;
    }
    private static int findIfCloseBraces(String n, int k, boolean e){
        int i = k;
        CharSequence p = n;
        int count = 0;
        boolean bracesFounded = false;
        for (int j = i; j < p.length(); j++) {
            if(e && !bracesFounded && p.charAt(i) == 'i' && p.charAt(i+1) == 'f' && !itIsBetweenQuoteMarks(n, (String) "if", i)){
                break;
            }
            if(p.charAt(j) == '{' && !itIsBetweenQuoteMarks(p, "{", j)){
                count++;
                bracesFounded = true;
            }else if(p.charAt(j) == '}' && !itIsBetweenQuoteMarks(p, "}", j) && bracesFounded){
                count--;
                if(count == 0){
                    k = j;
                    break;
                }
            }else if(p.charAt(j) == '\n' && !itIsBetweenQuoteMarks(p, "\n", j) && count == 0 && i == k && !bracesFounded){
                i = j+1;
                break;
            }
        }
        // NOT TESTED YET !!!
        if(!bracesFounded && !e){
            boolean foundedSomething = false;
            for (int j = i; j < p.length(); j++) {
                if(!foundedSomething && p.charAt(j) != ' ' && p.charAt(j) != '\r' && p.charAt(j) != '\n'){
                    foundedSomething = true;
                }
                if(foundedSomething && p.charAt(j) == ';' && !itIsBetweenQuoteMarks(p, ";", j)){
                    k = j+1;
                    break;
                }else if(!foundedSomething && p.charAt(j) == '\n' && !itIsBetweenQuoteMarks(p, "\n", j)){
                    k = j;
                    break;
                }
            }
        }
        return k;
    }
    private static boolean itIsBetweenQuoteMarks(CharSequence m, String value, int i){
        if(i-1 >= 0 && i+1 < m.length()){
            return (m.charAt(i-1) == '\"' && m.charAt(i+value.length()) == '\"') || (m.charAt(i-1) == '\'' && m.charAt(i+value.length()) == '\'');
        }else{
            return false;
        }
    }
}
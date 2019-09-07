package com.mycompany.complexity.tool.mvn;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.mycompany.complexity.tool.mvn.Nodes.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author LuísRogério
 */
public class Interval implements Comparable<Interval> {

    private double start;
    private double end;
    private boolean openStart;
    private boolean openEnd;
    private String type;
    private String sign;
    private Node node;
    private List<Interval> complements;

    /*Main Construtor initialization with neutral values */
    public Interval() {
        this.start = Double.NaN;
        this.end = Double.NaN;
        this.openStart = false;
        this.openEnd = false;
        this.node = null;
        this.complements = new ArrayList<>();
    }

    /*Construtor for Integer values */
    public Interval(String sign, int value, Node node) {
        this.type = "Integer";
        this.sign = getSignByName(sign);
        this.node = node;
        complements = new ArrayList<>();
        if (this.getSign().equals("<") || this.getSign().equals("<=")) {
            this.start = Double.NEGATIVE_INFINITY;
            if (this.getSign().equals("<")) {
                this.openEnd = true;
            }
            this.end = value;
        } else if (this.getSign().equals(">") || this.getSign().equals(">=")) {
            this.end = Double.POSITIVE_INFINITY;
            if (this.getSign().equals(">")) {
                this.openStart = true;
            }
            this.start = value;
        } else if (this.getSign().equals("==")) {
            this.start = value;
            this.end = value;
        } else if (this.getSign().equals("!=")) {
            this.start = Double.NEGATIVE_INFINITY;
            this.openEnd = true;
            this.end = value;
            Interval complement = new Interval();
            complement.openStart = true;
            complement.start = value;
            complement.type = this.type;
            complement.sign = this.sign;
            complement.end = Double.POSITIVE_INFINITY;
            complements.add(complement);
        }
    }

    /*Construtor for Long values */
    public Interval(String sign, long value, Node node) {
        this.type = "Long";
        this.sign = getSignByName(sign);
        this.node = node;
        complements = new ArrayList<>();
        if (this.getSign().equals("<") || this.getSign().equals("<=")) {
            this.start = Double.NEGATIVE_INFINITY;
            if (sign.equals("<")) {
                this.openEnd = true;
            }
            this.end = value;
        } else if (this.getSign().equals(">") || this.getSign().equals(">=")) {
            this.end = Double.POSITIVE_INFINITY;
            if (this.getSign().equals(">")) {
                this.openStart = true;
            }
            this.start = value;
        } else if (this.getSign().equals("==")) {
            this.start = value;
            this.end = value;
        } else if (this.getSign().equals("!=")) {
            this.start = Double.NEGATIVE_INFINITY;
            this.openEnd = true;
            this.end = value;
            Interval complement = new Interval();
            complement.openStart = true;
            complement.start = value;
            complement.type = this.type;
            complement.sign = this.sign;
            complement.end = Double.POSITIVE_INFINITY;
            complements.add(complement);
        }
    }

    /*Construtor for Short values */
    public Interval(String sign, short value, Node node) {
        this.type = "Short";
        this.sign = getSignByName(sign);
        this.node = node;
        complements = new ArrayList<>();
        if (this.getSign().equals("<") || this.getSign().equals("<=")) {
            this.start = Double.NEGATIVE_INFINITY;
            if (this.getSign().equals("<")) {
                this.openEnd = true;
            }
            this.end = value;
        } else if (this.getSign().equals(">") || this.getSign().equals(">=")) {
            this.end = Double.POSITIVE_INFINITY;
            if (this.getSign().equals(">")) {
                this.openStart = true;
            }
            this.start = value;
        } else if (this.getSign().equals("==")) {
            this.start = value;
            this.end = value;
        } else if (this.getSign().equals("!=")) {
            this.start = Double.NEGATIVE_INFINITY;
            this.openEnd = true;
            this.end = value;
            Interval complement = new Interval();
            complement.openStart = true;
            complement.start = value;
            complement.type = this.type;
            complement.sign = this.sign;
            complement.end = Double.POSITIVE_INFINITY;
            complements.add(complement);
        }
    }

    /*Construtor for Double values */
    public Interval(String sign, double value, Node node) {
        this.type = "Double";
        this.sign = getSignByName(sign);
        this.node = node;
        complements = new ArrayList<>();
        if (this.getSign().equals("<") || this.getSign().equals("<=")) {
            this.start = Double.NEGATIVE_INFINITY;
            if (this.getSign().equals("<")) {
                this.openEnd = true;
            }
            this.end = value;
        } else if (this.getSign().equals(">") || this.getSign().equals(">=")) {
            this.end = Double.POSITIVE_INFINITY;
            if (this.getSign().equals(">")) {
                this.openStart = true;
            }
            this.start = value;
        } else if (this.getSign().equals("==")) {
            this.start = value;
            this.end = value;
        } else if (this.getSign().equals("!=")) {
            this.start = Double.NEGATIVE_INFINITY;
            this.openEnd = true;
            this.end = value;
            Interval complement = new Interval();
            complement.openStart = true;
            complement.start = value;
            complement.type = this.type;
            complement.sign = this.sign;
            complement.end = Double.POSITIVE_INFINITY;
            complements.add(complement);
        }
    }
    
    /*Construtor for Methods names, default value = 0*/
    public Interval(String sign, MethodCallExpr value, Node node) {
        this.type = "Method";
        this.sign = getSignByName(sign);
        this.node = node;
        complements = new ArrayList<>();
        if (this.getSign().equals("<") || this.getSign().equals("<=")) {
            this.start = Double.NEGATIVE_INFINITY;
            if (this.getSign().equals("<")) {
                this.openEnd = true;
            }
            this.end = 0;
        } else if (this.getSign().equals(">") || this.getSign().equals(">=")) {
            this.end = Double.POSITIVE_INFINITY;
            if (this.getSign().equals(">")) {
                this.openStart = true;
            }
            this.start = 0;
        } else if (this.getSign().equals("==")) {
            this.start = 0;
            this.end = 0;
        } else if (this.getSign().equals("!=")) {
            this.start = Double.NEGATIVE_INFINITY;
            this.openEnd = true;
            this.end = 0;
            Interval complement = new Interval();
            complement.openStart = true;
            complement.start = 0;
            complement.type = this.type;
            complement.sign = this.sign;
            complement.end = Double.POSITIVE_INFINITY;
            complements.add(complement);
        }
    }

    /*Construtor for Variables names, default value = 0*/
    public Interval(String sign, NameExpr value, Node node) {
        this.type = "Variable";
        this.sign = getSignByName(sign);
        this.node = node;
        complements = new ArrayList<>();
        if (this.getSign().equals("<") || this.getSign().equals("<=")) {
            this.start = Double.NEGATIVE_INFINITY;
            if (this.getSign().equals("<")) {
                this.openEnd = true;
            }
            this.end = 0;
        } else if (this.getSign().equals(">") || this.getSign().equals(">=")) {
            this.end = Double.POSITIVE_INFINITY;
            if (this.getSign().equals(">")) {
                this.openStart = true;
            }
            this.start = 0;
        } else if (this.getSign().equals("==")) {
            this.start = 0;
            this.end = 0;
        } else if (this.getSign().equals("!=")) {
            this.start = Double.NEGATIVE_INFINITY;
            this.openEnd = true;
            this.end = 0;
            Interval complement = new Interval();
            complement.openStart = true;
            complement.start = 0;
            complement.type = this.type;
            complement.sign = this.sign;
            complement.end = Double.POSITIVE_INFINITY;
            complements.add(complement);
        }
    }
    
    /*Construtor for Strings values*/
    public Interval(String sign, String value, Node node) {
        this.type = "String";
        this.sign = getSignByName(sign);
        this.node = node;
        complements = new ArrayList<>();
        if (this.getSign().equals("==")) {
            this.start = -1;
            this.end = 1;
        } else if (this.getSign().equals("!=")) {
            this.start = Double.NEGATIVE_INFINITY;
            this.openEnd = true;
            this.end = -1;
            Interval complement = new Interval();
            complement.openStart = true;
            complement.start = 1;
            complement.type = this.type;
            complement.sign = this.sign;
            complement.end = Double.POSITIVE_INFINITY;
            complements.add(complement);
        }
    }

    /*Construtor for Boolean values*/
    public Interval(String sign, boolean value, Node node) {
        this.type = "Boolean";
        this.sign = getSignByName(sign);
        this.node = node;
        complements = new ArrayList<>();
        if(this.getSign().equals("==")){
            if (value == true) {
                this.start = -1;
                this.end = 1;
            } else if (value == false) {
                this.start = Double.NEGATIVE_INFINITY;
                this.openEnd = true;
                this.end = -1;
                Interval complement = new Interval();
                complement.openStart = true;
                complement.start = 1;
                complement.type = this.type;
                complement.sign = this.sign;
                complement.end = Double.POSITIVE_INFINITY;
                complements.add(complement);
            }
        }else if(this.getSign().equals("!=")){
            if (value == false) {
                this.start = -1;
                this.end = 1;
            } else if (value == true){
                this.start = Double.NEGATIVE_INFINITY;
                this.openEnd = true;
                this.end = -1;
                Interval complement = new Interval();
                complement.openStart = true;
                complement.start = 1;
                complement.type = this.type;
                complement.sign = this.sign;
                complement.end = Double.POSITIVE_INFINITY;
                complements.add(complement);
            }
        }
    }

    /*Given sign name in extensive returns symbol */
    private String getSignByName(String sign) {
        String name = "";
        if (sign.equals("notEquals")) {
            return "!=";
        }
        if (sign.equals("equals")) {
            return "==";
        }
        if (sign.equals("lessEquals")) {
            return "<=";
        }
        if (sign.equals("less")) {
            return "<";
        }
        if (sign.equals("greaterEquals")) {
            return ">=";
        }
        if (sign.equals("greater")) {
            return ">";
        }
        return name;
    }

    /*Verifies if a List of Intervals have the same type in all intervals*/
    public static boolean checkHomogeny(List<Interval> intervals) throws DifferentTypeException {
        String listType;
        listType = intervals.get(0).getType();
        for (Interval intervalo : intervals) {
            if (!listType.equals(intervalo.getType())) {
                throw new DifferentTypeException("The list contains elements of different types!");
            }
        }
        return true;
    }

    /*Returns type of a List of Intervals */
    public static String getListType(List<Interval> intervals) {
        try {
            checkHomogeny(intervals);
        } catch (DifferentTypeException e) {
            e.printStackTrace();
        }
        return intervals.get(0).getType();
    }

    /*Checks if a set of intervals covers from minus infinity to infinity (Full-Coverage)*/
    public static boolean checkCoverage(List<Interval> intervals) {
        if (intervals.size() > 0) {
            Interval coverages = new Interval();
            for (Interval interval : intervals) {
                Interval x;
                x = interval;
                coverages.getComplements().add(x);
                if (!interval.getComplements().isEmpty()) {
                    x = new Interval();
                    x = interval.getComplements().get(0);
                    coverages.getComplements().add(x);
                }
            }
            Collections.sort(coverages.getComplements());
            Interval mainCoverage = new Interval();
            String listType = getListType(coverages.getComplements());
            double increment;
            if (listType.equals("Integer") || listType.equals("Short") || listType.equals("Long")) {
                increment = 1;
            } else {
                increment = 0;
            }
            for (Interval complement
                    : coverages.getComplements()) {
                if (mainCoverage.getComplements().isEmpty()) {
                    if (Double.isNaN(mainCoverage.getStart()) && Double.isNaN(mainCoverage.getEnd())) {
                        if (complement.getStart() != Double.NEGATIVE_INFINITY) {
                            return false;
                        }
                        mainCoverage = complement;
                    } else {
                        if (complement.getSign().equals("!=")) {
                            if (mainCoverage.isOpenEnd()) {
                                if (mainCoverage.getEnd() > complement.getEnd()) {
                                    return true;
                                } else {
                                    mainCoverage = complement;
                                }

                            } else {
                                if (mainCoverage.getEnd() >= complement.getEnd()) {
                                    return true;
                                } else {
                                    mainCoverage = complement;
                                }
                            }
                        }
                        if (complement.getSign().equals("==")) {
                            if (mainCoverage.isOpenEnd()) {
                                if (mainCoverage.getEnd() == complement.getStart()) {
                                    mainCoverage.setOpenEnd(false);
                                }
                            } else {
                                if (mainCoverage.getEnd() + increment == complement.getStart()) {
                                    mainCoverage.setEnd(complement.getStart());
                                }
                            }
                        }
                        if (complement.getSign().equals("<") || complement.getSign().equals("<=")) {
                            if (mainCoverage.isOpenEnd()) {
                                if (mainCoverage.getEnd() <= complement.getEnd()) {
                                    mainCoverage = complement;
                                }
                            } else {
                                if (mainCoverage.getEnd() <= complement.getEnd() + increment) {
                                    mainCoverage.setEnd(complement.getStart() + increment);
                                }
                            }
                        }
                        if (complement.getSign().equals(">")) {
                            if (mainCoverage.isOpenEnd()) {
                                if(mainCoverage.getNode().hasElse()){
                                    if (mainCoverage.getEnd() - increment > complement.getStart()) {
                                        return true;
                                    }else if (mainCoverage.getEnd() - increment == complement.getStart() && increment == 1) {
                                        return true;
                                    }
                                }else{
                                    if (mainCoverage.getEnd() - increment == complement.getStart() && increment == 1) {
                                        return true;
                                    }
                                }
                            } else {
                                if(mainCoverage.getNode().hasElse()){
                                    if (mainCoverage.getEnd() >= complement.getStart()) {
                                        return true;
                                    }
                                }else{
                                    if (mainCoverage.getEnd() == complement.getStart()) {
                                        return true;
                                    }
                                }
                            }
                        }
                        if (complement.getSign().equals(">=")) {
                            if (mainCoverage.isOpenEnd()) {
                                if(mainCoverage.getNode().hasElse()){
                                    if (mainCoverage.getEnd() >= complement.getStart()) {
                                        return true;
                                    }
                                }else{
                                    if (mainCoverage.getEnd() == complement.getStart()) {
                                        return true;
                                    }
                                }
                            } else {
                                if(mainCoverage.getNode().hasElse()){
                                    if (mainCoverage.getEnd() + increment >= complement.getStart()) {
                                        return true;
                                    }
                                }else{
                                    if (mainCoverage.getEnd() + increment == complement.getStart() && increment == 1) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (mainCoverage.getEnd() < complement.getEnd() && complement.getSign().equals("<")) {
                        return true;
                    }
                    if (mainCoverage.getEnd() <= complement.getEnd() && complement.getSign().equals("<=")) {
                        return true;
                    }
                    if (mainCoverage.getEnd() > complement.getStart() && complement.getSign().equals(">")) {
                        return true;
                    }
                    if (mainCoverage.getEnd() >= complement.getStart() && complement.getSign().equals(">=")) {
                        return true;
                    }
                    if (mainCoverage.getEnd() == complement.getStart() && complement.getSign().equals("==")) {
                        return true;
                    }
                    if (mainCoverage.getEnd() == complement.getStart() && complement.getSign().equals("!=")) {
                        if(!mainCoverage.getComplements().isEmpty()){
                            if(mainCoverage.getComplements().get(0).getStart() == complement.getEnd()){
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        } else {
            return false;
        }

    }
    
    /*For Strings, Booleans and Unary expressions*/
    public int compareToEqualsAndNotEquals(Interval o){
        int t = 0;
        if ("==".equals(this.getSign()) || "!=".equals(this.getSign())){ 
            if("==".equals(o.getSign()) || "!=".equals(o.getSign())){
                if (this.getEnd() == o.getEnd()){
                    t = 0;
                }else{
                    if (this.getEnd() > o.getEnd()) {
                        t = 1;
                    }
                    if (this.getEnd() < o.getEnd()) {
                        t = -1;
                    }
                }
            }
        }
//        System.out.println(o.getSign()+": "+o.getStart()+" -> "+o.getEnd());
//        System.out.println(this.getSign()+": "+this.getStart()+" -> "+this.getEnd());
//        System.out.println(t+"\n");
        return t;
    }

    /*Sorts Intervals*/
    @Override
    public int compareTo(Interval o) {
        if((o.getType().equals("Boolean") && this.getType().equals("Boolean")) || (o.getType().equals("String") && this.getType().equals("String"))){
            return compareToEqualsAndNotEquals(o);
        }else if(!o.getType().equals("Boolean") && !this.getType().equals("Boolean") && (!o.getType().equals("String") && !this.getType().equals("String"))){
            if ("!=".equals(this.getSign()) || "==".equals(this.getSign())) {
                if ("<".equals(o.getSign()) || "<=".equals(o.getSign()) || "!=".equals(o.getSign())) {
                    if (this.getEnd() > o.getEnd()) {
                        return 1;
                    }
                    if (this.getEnd() < o.getEnd()) {
                        return -1;
                    }
                    return 1;
                }else if ("==".equals(o.getSign())) {
                    return -1;
                }else if (">".equals(o.getSign()) || ">=".equals(o.getSign())) {
                    if ("!=".equals(this.getSign())) {
                        return -1;
                    } else {
                        if (this.getEnd() > o.getEnd()) {
                            return 1;
                        }
                        if (this.getEnd() < o.getEnd()) {
                            return -1;
                        }
                        return -1;
                    }
                }
            } else if ("<".equals(this.getSign()) || "<=".equals(this.getSign())) {
                if (">".equals(o.getSign()) || ">=".equals(o.getSign())) {
                    return -1;
                } else {
                    if (this.getEnd() > o.getEnd()) {
                        return 1;
                    }
                    if (this.getEnd() < o.getEnd()) {
                        return -1;
                    }
                    return -1;
                }
            } else {
                if (this.getEnd() > o.getEnd()) {
                    return 1;
                }
                if (this.getEnd() < o.getEnd()) {
                    return -1;
                }
                return 0;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        double value = 0;
        if (this.getSign().equals("!=")) {
            value = this.getEnd();
        }
        if (this.getSign().equals("==")) {
            value = this.getStart();
        }
        if (this.getSign().equals("<=")) {
            value = this.getEnd();
        }
        if (this.getSign().equals("<")) {
            value = this.getEnd();
        }
        if (this.getSign().equals(">=")) {
            value = this.getStart();
        }
        if (this.getSign().equals(">")) {
            value = this.getStart();
        }
        return this.getSign() + " " + value;
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    public boolean isOpenStart() {
        return openStart;
    }

    public void setOpenStart(boolean openStart) {
        this.openStart = openStart;
    }

    public boolean isOpenEnd() {
        return openEnd;
    }

    public void setOpenEnd(boolean openEnd) {
        this.openEnd = openEnd;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public List<Interval> getComplements() {
        return complements;
    }

    public void setComplements(List<Interval> complements) {
        this.complements = complements;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    private static class DifferentTypeException extends Exception {

        public DifferentTypeException() {
        }

        private DifferentTypeException(String e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

}

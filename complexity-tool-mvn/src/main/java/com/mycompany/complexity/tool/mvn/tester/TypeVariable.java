/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn.tester;

/**
 *
 * @author Nathan Manera Magalhães
 * @param <T>
 */
public abstract class TypeVariable<T>{
    protected String getSign(String operator){
        switch (operator) {
            case "less":
            case "not_greaterEquals":
                return "NEGATIVE";
            case "greater":
            case "not_lessEquals":
                return "POSITIVE";
            case "not_equals":
            case "notEquals":
                return "NOT_NEUTRAL";
            default:
                return "";
        }
    }
    
    protected boolean isNumber(String value){
        String c = "0123456789.-";
        for (int i = 0; i < value.length(); i++) {
            if(c.indexOf(value.charAt(i)) == -1){
                return false;
            }
        }
        return true;
    }
    
    protected abstract T getBoundary(String operator);
    public abstract String getBoundaryValue(String operator, String value);
}

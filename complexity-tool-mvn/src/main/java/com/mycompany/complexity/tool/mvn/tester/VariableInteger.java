/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.complexity.tool.mvn.tester;

/**
 *
 * @author Nathan Manera Magalhães
 */
public class VariableInteger extends TypeVariable<Integer>{

    @Override
    protected Integer getBoundary(String operator) {
        switch (getSign(operator)) {
            case "NEGATIVE":
                return -1;
            case "POSITIVE":
                return 1;
            case "NOT_NEUTRAL":
                return 1;
            default:
                return 0;
        }
    }
    
    @Override
    public String getBoundaryValue(String operator, String value) {
        return isNumber(value) ? Integer.toString(Integer.parseInt(value) + getBoundary(operator)) : 
            value + (getBoundary(operator) > 0 ? "+" + Integer.toString(getBoundary(operator)) 
            : getBoundary(operator) < 0 ? Integer.toString(getBoundary(operator)) : "");
    }
    
}

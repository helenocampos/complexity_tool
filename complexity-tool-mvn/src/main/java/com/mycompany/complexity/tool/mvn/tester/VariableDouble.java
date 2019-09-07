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
public class VariableDouble extends TypeVariable<Double>{

    @Override
    protected Double getBoundary(String operator) {
        switch (getSign(operator)) {
            case "NEGATIVE":
                return -0.1d;
            case "POSITIVE":
                return 0.1d;
            case "NOT_NEUTRAL":
                return 0.1d;
            default:
                return 0d;
        }
    }

    @Override
    public String getBoundaryValue(String operator, String value) {
        return isNumber(value) ? Double.toString(Double.parseDouble(value) + getBoundary(operator)) : 
            value + (getBoundary(operator) > 0 ? "+" + Double.toString(getBoundary(operator)) 
            : getBoundary(operator) < 0 ? Double.toString(getBoundary(operator)) : "");
    }
    
}

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
public class VariableFloat extends TypeVariable<Float>{

    @Override
    protected Float getBoundary(String operator) {
        switch (getSign(operator)) {
            case "NEGATIVE":
                return -0.1f;
            case "POSITIVE":
                return 0.1f;
            case "NOT_NEUTRAL":
                return 0.1f;
            default:
                return 0f;
        }
    }
    
    @Override
    public String getBoundaryValue(String operator, String value) {
        return isNumber(value) ? Float.toString(Float.parseFloat(value) + getBoundary(operator)) : 
            value + (getBoundary(operator) > 0 ? "+" + Float.toString(getBoundary(operator)) 
            : getBoundary(operator) < 0 ? Float.toString(getBoundary(operator)) : "");
    }
    
}

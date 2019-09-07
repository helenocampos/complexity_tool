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
public class VariableBoolean extends TypeVariable<Boolean>{
    
    @Override
    protected Boolean getBoundary(String operator) {
        return !getSign(operator).equals("NOT_NEUTRAL");
    }
    
    @Override
    public String getBoundaryValue(String operator, String value) {
        return Boolean.toString(getBoundary(operator));
    }
    
}

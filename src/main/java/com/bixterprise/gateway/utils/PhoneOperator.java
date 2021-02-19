
package com.bixterprise.gateway.utils;

/**
 * This enum define the set of Known phone operator, Actually only Cameroon's is known
 * MTN -> MTN Cameroon
 * ORANGE -> Orange
 * NEXTTEL -> NEXTTEL
 * UNKNOWN -> UNKNOWN
 * @author haranov
 */
public enum PhoneOperator {
    
    MTN("MTN"), ORANGE("Orange"), NEXTTEL("NEXTTEL"), UNKNOWN("UNKNOWN");
    
    String name;
    
    PhoneOperator(String name){
        this.name = name;
    }
    
    @Override
    public String toString(){return this.name;}

}

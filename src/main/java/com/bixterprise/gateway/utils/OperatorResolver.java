package com.bixterprise.gateway.utils;

import com.bixterprise.gateway.domain.AutomateAgents;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class OperatorResolver as named, it is generaly used to check phone operator
 * @author haranov
 */
public class OperatorResolver {
    
    /**
     * Filter agents that have same phone operator than phoneNumber param
     * @param agents Set of Agent
     * @param phoneNumber client phone number
     * @return Set of Agent that can resolve operation
     * @see Set
     * @see AutomateAgents
     * @see PhoneOperator
     */
    public static List<AutomateAgents> resolve(List<AutomateAgents> agents, String phoneNumber){
        List<AutomateAgents> result = new LinkedList<>();
        String spn = removeCountryCode(phoneNumber);
        agents
            .stream()
            .filter((a) -> (a.getPhoneOperator().name.equalsIgnoreCase(identifyNetwork(spn).toString())))
            .forEachOrdered((a) -> {
                result.add(a);
            });
        return result;
    }
    
    /**
     * remove country dial code on specific phone number
     * @param phoneNumber
     * @return phone number without country dial code
     */
    public static String removeCountryCode(String phoneNumber){
        
        /**
         * On supprimer les espaces, le code pays (237 | +237)
         */
        String number = phoneNumber.replaceAll("\\s+", "").replaceAll("\\+237", "");
        
        if(number.startsWith("237"))
            number = number.replaceFirst("237", "");
        return number;
    }
    
    private static PhoneOperator identifyNetwork(String phone){
        PhoneOperator po = http.getPhoneOperator(phone);
        return po;
    }
}

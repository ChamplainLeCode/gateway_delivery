/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bixterprise.gateway.service;

import com.bixterprise.gateway.domain.AgentActivity;
import com.bixterprise.gateway.domain.AutomateAgents;
import com.bixterprise.gateway.repository.AgentActivityRepository;
import com.bixterprise.gateway.repository.AgentRepository;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 *
 * @author champlain
 */
@Service
public class AgentService {
    
    

    @Autowired
    AgentRepository agentRepository;    
    
    @Autowired
    AgentActivityRepository agentActivityRepositority;
    
    
    public HashMap login(AutomateAgents a){
    
        
        AutomateAgents loggedUser = agentRepository.doLogin(a.getPhone(), a.getImei());
        //System.out.println("Login de "+a.toJSONString()+"\nIsONline = "+loggedUser.getIs_online());
		
		
		if(loggedUser != null) {
			if(a.getFcm_token() == null) {
				HashMap obj = new HashMap();
				obj.put("code", "100");
				obj.put("label", "FCM_Token is missing");
				HashMap res = new HashMap();
				res.put("status", false);
				res.put("message", 1001);
				res.put("errors", obj);
				return res;
			}
			loggedUser.setFcm_token(a.getFcm_token());
			loggedUser.setIsOnline(true);
			loggedUser.setUpdatedAt(Calendar.getInstance().getTime());
			agentRepository.save(loggedUser);
			
			/**
			 * On enregistre son activité
			 */
			AgentActivity activ = new AgentActivity();
			activ.setAgent(loggedUser);
			activ.setLog(a.getLog());
			activ.setContext("LOGIN");
			activ.setCreatedAt(Calendar.getInstance().getTime());
			agentActivityRepositority.save(activ);
			
			HashMap map = new HashMap();
			
			map.put("status", true);
			map.put("message", 1000);
			map.put("data", loggedUser.toJSONString());
			return map;
		}
		HashMap obj = new HashMap(); 
		obj.put("code", "101");
		obj.put("label", "INVALID_CREDENTIAL");
		HashMap res = new HashMap();
		res.put("status", false);
		res.put("message", 1001);
		res.put("errors", obj);
		
		return res;
    }

    public List<AutomateAgents> findAll() {
        return agentRepository.findAll();
    }

    public Page<AutomateAgents> findAll(PageRequest of) {
        return agentRepository.findAll(of);
    }

    public AutomateAgents findById(String phone) {
        return agentRepository.findById(phone).orElse(null);
    }

    public AutomateAgents findByImei(String imei) {
        return agentRepository.findByImei(imei).orElse(null);
    }

    public AutomateAgents save(AutomateAgents a) {
        return agentRepository.save(a);
    }

    public AutomateAgents doLogin(String phone, String imei) {
        return agentRepository.doLogin(phone, imei);
    }

    public HashMap logout(AutomateAgents ag) {
        AutomateAgents loggedUser = doLogin(ag.getPhone(), ag.getImei());
		
		if(loggedUser != null) {
			loggedUser.setFcm_token("");
			loggedUser.setIsOnline(false);
			loggedUser.setUpdatedAt(Calendar.getInstance().getTime());
			save(loggedUser);

			/**
			 * On enregistre son activité
			 */
			AgentActivity activ = new AgentActivity();
			activ.setAgent(loggedUser);
			activ.setLog(ag.getLog());
			activ.setContext("LOGOUT");
			activ.setCreatedAt(Calendar.getInstance().getTime());
			agentActivityRepositority.save(activ);

			HashMap res = new HashMap();
			res.put("status", true);
			res.put("message", 1000);
			res.put("data", loggedUser.toJSONString());
			return res;
		}
		HashMap obj = new HashMap();
		obj.put("code", "101");
		obj.put("label", "INVALID_CREDENTIAL");
		HashMap res = new HashMap();
		res.put("status", false);
		res.put("message", 1001);
		res.put("errors", obj);
                return res;
    }
}

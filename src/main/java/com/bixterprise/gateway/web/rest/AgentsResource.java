package com.bixterprise.gateway.web.rest;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityExistsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bixterprise.gateway.repository.AgentActivityRepository;
import com.bixterprise.gateway.repository.AgentRepository;
import com.bixterprise.gateway.domain.AgentActivity;
import com.bixterprise.gateway.domain.AutomateAgents;
import com.bixterprise.gateway.domain.enums.AgentStatus;
import com.bixterprise.gateway.utils.http;

import java.util.HashMap;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/api/agents")
public class AgentsResource {


	@Value("${entity.page.max}")
	Integer maxByPage;
	
	@Autowired
	AgentRepository agentRepository;
	
	@Autowired
	AgentActivityRepository agentActivityRepository;
	
	@GetMapping("/detail/{phone}")
	public Object getUnique(@PathVariable(required = true, name = "phone") String phone)  {
		if(phone == null || phone.trim().length() < 9) {
                        HashMap obj = new HashMap();
			obj.put("code", 101);
			obj.put("label", "Phone number error");
			return obj;
		}
		Optional<AutomateAgents> o = agentRepository.findById(phone);
		if(!o.isPresent()) {
			HashMap obj = new HashMap();
			obj.put("code", 102);
			obj.put("label", "Agent not found");
			return obj;
		}
		return o.get();
	}
	
	@GetMapping("/all")
	public List<AutomateAgents> getAll(){
		return agentRepository.findAll();
	}

	@GetMapping("/all/page/{page}")
	public List<AutomateAgents> getAllByPage(@PathVariable int page){
		return agentRepository.findAll(PageRequest.of(page, maxByPage)).toList();
	}

	@PostMapping("/add")
	public Object addAgent(@RequestBody AutomateAgents a)  {

		if(a == null || a.getPhone() == null ) {
			HashMap obj = new HashMap();
			obj.put("code", "100");
			obj.put("label", "Agent phone is required");
			return obj;
		}
		if(a.getPhone().length() < 5) {
			HashMap obj = new HashMap();
			obj.put("code", "101");
			obj.put("label", "Phone number error");
			return obj;
		}

		if(a.getImei() == null || a.getImei().length() <15) { 
			HashMap obj = new HashMap();
			obj.put("code", "102");
			obj.put("label", "Imei malformed error or absent");
			return obj;
		}
		
		if(agentRepository.findById(a.getPhone()).isPresent()) {
			HashMap obj = new HashMap();
			obj.put("code", "103");
			obj.put("label", "User already exists error");
			return obj;
		}
		
		if(agentRepository.findByImei(a.getImei()).isPresent()) {
			HashMap obj = new HashMap();
			obj.put("code", "103");
			obj.put("label", "Imei already exists error");
			return obj;
		}

		a.setIsOnline(false);
		a.setFcm_token("");
		a.setSolde(0F);
                a.setStatus(AgentStatus.ACTIVATE);
		a.setCreatedAt(Calendar.getInstance().getTime());
		a.setUpdatedAt(a.getCreatedAt());
		try {
			agentRepository.save(a);
			HashMap obj = new HashMap();
			obj.put("code", 100);
			obj.put("label", "success");
                        /**
                         * On met à jour l'opérateur télephonique de l'agent
                         */
                        new Thread(){
                            public void run(){
                                http.updateOperator(a);
                                agentRepository.save(a);
                            } 
                        }.start();
			return obj;
		}catch(EntityExistsException e) {
			HashMap obj = new HashMap();
			obj.put("code", "103");
			obj.put("label", "User already exists error");
			return obj;
		}catch(Exception  e) {
			HashMap obj = new HashMap();
			obj.put("code", "100");
			obj.put("label", "General error");
                        obj.put("error", e);
			return obj;
		}
	}


	@PostMapping("/edit")
	public Object editAgent(@RequestBody AutomateAgents a)  {
		try {
			if(a == null || a.getPhone() == null) {
				HashMap obj = new HashMap();
				obj.put("code", "100");
				obj.put("label", "General error");
				return obj;
			}
			if(a.getPhone().length() < 5) {
				HashMap obj = new HashMap();
				obj.put("code", "101");
				obj.put("label", "Phone number error");
				return obj;
			}
	
			if(a.getImei().length() <15) { 
				HashMap obj = new HashMap();
				obj.put("code", "102");
				obj.put("label", "Imei malformed error");
				return obj;
			}
			Optional<AutomateAgents> age;
			if((age = agentRepository.findById(a.getPhone())).isPresent()) {
				AutomateAgents agent = age.get();
				if(agent.getIs_online()) {
					HashMap obj = new HashMap();
					obj.put("code", "103");
					obj.put("label", "Agent is online, disconnect device first");
					return obj;
				}
				if(agentRepository.findByImei(a.getImei()).isPresent()) {
					HashMap obj = new HashMap();
					obj.put("code", "103");
					obj.put("label", "Imei already used by another");
					return obj;
				}
				agent.setImei(a.getImei());
				if(a.getBalance() != null ) {
					agent.setSolde(a.getBalance());
				}
				agent.setUpdatedAt(Calendar.getInstance().getTime());
				agentRepository.save(agent);
				HashMap obj = new HashMap();
				obj.put("code", 100);
				obj.put("label", "success");
				return obj;
			}else {
				HashMap obj = new HashMap();
				obj.put("code", "104");
				obj.put("label", "Agent not found");
				return obj;
			}
		}catch(Exception  e) {
			HashMap obj = new HashMap();
			obj.put("code", "100");
			obj.put("label", "General error");
			return obj;
		}
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json")
	public HashMap login(@RequestBody AutomateAgents a) {
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
			/*
			if(loggedUser.getIs_online() == true) {
				HashMap obj = new HashMap();
				obj.put("code", "101");
				obj.put("label", "Agent already connected");
				HashMap res = new HashMap();
				res.put("status", false);
				res.put("message", 1001);
				res.put("errors", obj);
				return res;
			}
			*/
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
			agentActivityRepository.save(activ);
			
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
	

	@RequestMapping(value = "/logout", method = RequestMethod.POST, produces = "application/json")
	public HashMap logout(@RequestBody AutomateAgents a) {
		AutomateAgents loggedUser = agentRepository.doLogin(a.getPhone(), a.getImei());
		
		if(loggedUser != null) {
			loggedUser.setFcm_token("");
			loggedUser.setIsOnline(false);
			loggedUser.setUpdatedAt(Calendar.getInstance().getTime());
			agentRepository.save(loggedUser);

			/**
			 * On enregistre son activité
			 */
			AgentActivity activ = new AgentActivity();
			activ.setAgent(loggedUser);
			activ.setLog(a.getLog());
			activ.setContext("LOGOUT");
			activ.setCreatedAt(Calendar.getInstance().getTime());
			agentActivityRepository.save(activ);

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

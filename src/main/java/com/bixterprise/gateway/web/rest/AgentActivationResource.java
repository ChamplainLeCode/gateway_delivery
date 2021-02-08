package com.bixterprise.gateway.web.rest;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bixterprise.gateway.repository.AgentActivationRepository;
import com.bixterprise.gateway.repository.AgentRepository;
import com.bixterprise.gateway.domain.AgentActivation;
import com.bixterprise.gateway.domain.AutomateAgents;
import com.bixterprise.gateway.domain.enums.AgentStatus;

import java.util.HashMap;




@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/api/agents/status")
public class AgentActivationResource {

	
	@Autowired
	AgentRepository agentRepository;
	@Autowired
	AgentActivationRepository aar;
	
	@GetMapping(value = "/for/{phone}")
	public Object getStatusFor(Pageable pageable, @PathVariable AutomateAgents phone) {
		Page<AgentActivation> agts = aar.findAllByAgent(pageable, phone);
		return ResponseEntity.ok().body(agts.getContent());	
	}
	
	@PostMapping(value = "/s/{state}/{phone}")
	public Object activateAgent(@PathVariable(required = true, name = "state") String state, @PathVariable(required = true, name="phone") String phone) {
		
		if(phone.isEmpty() || phone.trim().isEmpty()) {
			HashMap obj = new HashMap();
			obj.put("code", 101);
			obj.put("codeError", "Phone can't be empty");
			return obj;
		}
		Optional<AutomateAgents> agent = agentRepository.findById(phone);
		if(!agent.isPresent()) {
			HashMap obj = new HashMap();
			obj.put("code", 102);
			obj.put("codeError", "Agent not found");
			return obj;
		}
		AgentActivation aa = new AgentActivation();
		aa.setAgent(agent.get());
		String msg;
		if(state.equalsIgnoreCase(AgentStatus.ACTIVATE.toString())){
			aa.setStatus(AgentStatus.ACTIVATE);
			agent.get().setActive();
			msg = "Agent activé";
		} else if(state.equalsIgnoreCase(AgentStatus.DEACTIVATE.toString())) {
			aa.setStatus(AgentStatus.DEACTIVATE);
			agent.get().setDeactivate();
			msg = "Agent désactivé";
		} else {
			HashMap obj = new HashMap();
			obj.put("code", 103);
			obj.put("codeError", "Operation unsupported");
			return obj;
		}
		//System.out.println(aa.toString());
		aar.save(aa);
		agentRepository.save(agent.get());
		HashMap obj = new HashMap();
		obj.put("code", 200);
		obj.put("codeError", msg);
		return obj;		
	}
}

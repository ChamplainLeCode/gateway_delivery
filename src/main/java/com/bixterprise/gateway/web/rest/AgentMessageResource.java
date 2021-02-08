package com.bixterprise.gateway.web.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bixterprise.gateway.repository.AgentMessageRepository;
import com.bixterprise.gateway.repository.AgentRepository;
import com.bixterprise.gateway.domain.AgentMessage;
import com.bixterprise.gateway.domain.AutomateAgents;
import com.bixterprise.gateway.domain.AgentMessage;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/messages")
public class AgentMessageResource {

	@Autowired AgentMessageRepository amr;
	
	@Autowired AgentRepository agRepo;
	
	@PostMapping
	public Object saveNewMessage(@RequestBody AgentMessage am) {

		if(am == null || am.getId() != null) {
			HashMap<String, Object> obj = new HashMap<String, Object>();
			obj.put("code", 101);
			obj.put("label", "Impossible d'enregistrer ce message");
			return ResponseEntity.badRequest().body(obj);
		}
		AutomateAgents ag;
		if(am.getAgent() == null || (ag = agRepo.findAutomateAgentsByPhone(am.getAgent().getPhone())) == null) {
			HashMap<String, Object> obj = new HashMap<String, Object>();
			obj.put("code", 101);
			obj.put("label", "Agent non trouvé");
			return ResponseEntity.badRequest().body(obj);
		}
		am.setAgent(ag);
		am.setDate(new Date());
		return ResponseEntity.ok(amr.save(am));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<AgentMessage> getMassage(@PathVariable Long id) {
		if(id != null) {
			Optional<AgentMessage> am = amr.findById(id);
			if(am.isPresent())
				return ResponseEntity.ok(am.get());
		}
		return ResponseEntity.badRequest().body(null);
	}
	
	
	@GetMapping("/all")
	public Object getAllByPage(Pageable page, String dd, String df){ 
		/*if(!(dd == null ^ df != null)){
			HashMap<String, Object> obj = new HashMap<>();
			obj.put("code", 101L);
			obj.put("label", "Paramètre date absent");
			return obj;
		}*/
		if(dd == null && df == null) {
			Page<AgentMessage> list = amr.findAll(page);
			HashMap<String, Object> obj = new HashMap<String, Object>();
			HashMap<String, Object> pagination = new HashMap<String, Object>();
			pagination.put("pageSize", list.getPageable().getPageSize());
			pagination.put("totalSize", list.getTotalElements());
			pagination.put("totalPageNumber", list.getTotalPages());
			pagination.put("pageNumber", list.getNumber());
			obj.put("pagination", pagination);
			obj.put("result", list.getContent());
			return obj;
		}
		Calendar ddebut = Calendar.getInstance(), dfin = Calendar.getInstance();
		try {
			SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
			if(df != null) {
				dfin.setTime(dformat.parse(df));
				dfin.set(Calendar.HOUR,23);
				dfin.set(Calendar.MINUTE,59);
				dfin.set(Calendar.SECOND,59);
			}
			if(dd != null) {
				ddebut.setTime(dformat.parse(dd));
				ddebut.set(Calendar.HOUR,0);
				ddebut.set(Calendar.MINUTE,0);
				ddebut.set(Calendar.SECOND,0);
			}
			if(df == null) {
				Page<AgentMessage> list = amr.findAllAfterDate(page, ddebut.getTime());
				HashMap<String, Object> obj = new HashMap<String, Object>();
				HashMap<String, Object> pagination = new HashMap<String, Object>();
				pagination.put("pageSize", list.getPageable().getPageSize());
				pagination.put("totalSize", list.getTotalElements());
				pagination.put("totalPageNumber", list.getTotalPages());
				pagination.put("pageNumber", list.getNumber());
				obj.put("pagination", pagination);
				obj.put("result", list.getContent());
				return obj;
			}
			if(dd == null) {
				Page<AgentMessage> list = amr.findAllBeforeDate(page, dfin.getTime());
				HashMap<String, Object> obj = new HashMap<String, Object>();
				HashMap<String, Object> pagination = new HashMap<String, Object>();
				pagination.put("pageSize", list.getPageable().getPageSize());
				pagination.put("totalSize", list.getTotalElements());
				pagination.put("totalPageNumber", list.getTotalPages());
				pagination.put("pageNumber", list.getNumber());
				obj.put("pagination", pagination);
				obj.put("result", list.getContent());
				return obj;
			}
			if(ddebut.after(dfin)) {
				HashMap<String, Object> obj = new HashMap<>();
				obj.put("code", 103L);
				obj.put("label", "La date de début ne peut être postérieure à la date de fin");
				return obj;
			}
			Page<AgentMessage> list = amr.findAllByCreatedAt(page, ddebut.getTime(), dfin.getTime());
			
			HashMap<String, Object> obj = new HashMap<String, Object>();
			HashMap<String, Object> pagination = new HashMap<String, Object>();
			pagination.put("pageSize", list.getPageable().getPageSize());
			pagination.put("totalSize", list.getTotalElements());
			pagination.put("totalPageNumber", list.getTotalPages());
			pagination.put("pageNumber", list.getNumber());
			obj.put("pagination", pagination);
			obj.put("result", list.getContent());
			return obj;
		}catch(ParseException  e) {
			HashMap<String, Object> obj = new HashMap<>();
			obj.put("code", 102L);
			obj.put("label", "Paramètre date invalide");
			return obj;
		}
		
//		return agentTransactionRepository.findAll(PageRequest.of(page, maxByPage)).toList();
	}
	

	
	
}

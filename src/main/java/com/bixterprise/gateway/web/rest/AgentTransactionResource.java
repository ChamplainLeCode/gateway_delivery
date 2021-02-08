package com.bixterprise.gateway.web.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.bixterprise.gateway.repository.AgentRepository;
import com.bixterprise.gateway.repository.AgentTransactionRepository;
import com.bixterprise.gateway.repository.TransactionActivityRepository;
import com.bixterprise.gateway.domain.AgentTransaction;
import com.bixterprise.gateway.domain.AutomateAgents;
import com.bixterprise.gateway.domain.TransactionActivity;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/api/transactions")
public class AgentTransactionResource {
	

	@Value("${entity.page.max}")
	Integer maxByPage;
	
	@Autowired
	AgentTransactionRepository agentTransactionRepository;

	@Autowired
	AgentRepository agentRepository;
	
	@Autowired
	TransactionActivityRepository tar;

	@GetMapping("/all")
	public Object getAll(String dd, String df){
		if(!(dd == null ^ df != null)){
			HashMap<String, Object> obj = new HashMap<>();
			obj.put("code", 101L);
			obj.put("label", "Paramètre date absent");
			return obj;
		}
		if(dd == null)
			return tar.findAll();
		Calendar ddebut, dfin;
		try {
			SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
						
			dfin   = Calendar.getInstance();
			dfin.setTime(dformat.parse(df));
			ddebut   = Calendar.getInstance();
			ddebut.setTime(dformat.parse(dd));
			dfin.set(Calendar.HOUR,23);
			dfin.set(Calendar.MINUTE,59);
			dfin.set(Calendar.SECOND,59);
			if(ddebut.after(dfin)) {
				HashMap<String, Object> obj = new HashMap<>();
				obj.put("code", 103L);
				obj.put("label", "La date de début ne peut être postérieure à la date de fin");
				return obj;
			}
			return tar.findAllByCreatedAt(ddebut.getTime(), dfin.getTime());
		}catch(ParseException  e) {
			HashMap<String, Object> obj = new HashMap<>();
			obj.put("code", 102L);
			obj.put("label", "Paramètre date invalide");
			return obj;
		}
	}

	@GetMapping("/{id}")
	public Object getOne(@PathVariable Long id) {
		
		if(id != null) {
			Optional<TransactionActivity> ta = tar.findById(id);
			if(ta.isPresent())
				return ResponseEntity.ok(ta.get());
		}
		return ResponseEntity.badRequest().body(null);
		
	}
	
	@GetMapping("/all/page")
	public Object getAllByPage(Pageable page, String dd, String df){ 
		/*if(!(dd == null ^ df != null)){
			HashMap<String, Object> obj = new HashMap<>();
			obj.put("code", 101L);
			obj.put("label", "Paramètre date absent");
			return obj;
		}*/
		if(dd == null && df == null) {
			Page<TransactionActivity> list = tar.findAll(page);
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
				Page<TransactionActivity> list = tar.findAllAfterDate(page, ddebut.getTime());
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
				Page<TransactionActivity> list = tar.findAllBeforeDate(page, dfin.getTime());
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
			Page<TransactionActivity> list = tar.findAllByCreatedAt(page, ddebut.getTime(), dfin.getTime());
			
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
	
	@PostMapping("/recharge")
	public Map<?, ?> addAgent(@RequestBody AgentTransaction a) {
		
		if(a == null) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "100");
			obj.put("label", "General error");
			return obj;
		}
		if(a.getAmount() <= 0) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "101");
			obj.put("label", "Amount must be greater than 0 unit");
			return obj;
		}
		if(a.getAgent() == null || a.getAgent().getPhone() == null) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "102");
			obj.put("label", "Agent phone number error");
			return obj;
		}
		
		Optional<AutomateAgents> agent = agentRepository.findById(a.getAgent().getPhone());
		if(! agent.isPresent()) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "103");
			obj.put("label", "Agent not found");
			return obj;
		}
		 
		
		
		a.setAgentPhone(agent.get());
		a.setCreatedAt(Calendar.getInstance().getTime());
		a.setDescription( a.getDescription() == null ? "RECHARGE DU COMPTE AGENT D'UN MONTANT DE "+a.getAmount()+" units" : a.getDescription());
		
		try {
			agentTransactionRepository.save(a);
			a.getAgent().setSolde(a.getAgent().getBalance()+a.getAmount());
			agentRepository.save(a.getAgent());
			
			HashMap<String, Object> obj = new HashMap<>();
			obj.put("code", 100);
			obj.put("label", "success");
			return obj;
		}catch(Exception  e) {
			HashMap<String, String> obj = new HashMap<String, String>();
			obj.put("code", "100");
			obj.put("label", "General error");
			return obj;
		}
	}

}

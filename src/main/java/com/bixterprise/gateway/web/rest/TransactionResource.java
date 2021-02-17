package com.bixterprise.gateway.web.rest;

import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bixterprise.gateway.repository.AgentRepository;
import com.bixterprise.gateway.repository.AgentTransactionRepository;
import com.bixterprise.gateway.repository.TransactionActivityRepository;
import com.bixterprise.gateway.repository.TransactionRepository;
import com.bixterprise.gateway.repository.WorkSpaceRepository;

import com.bixterprise.gateway.domain.AgentTransaction;
import com.bixterprise.gateway.domain.AutomateAgents;
import com.bixterprise.gateway.domain.TransactionActivity;
import com.bixterprise.gateway.domain.Transactions;
import com.bixterprise.gateway.domain.WorkSpace;
import com.bixterprise.gateway.domain.enums.AgentStatus;
import com.bixterprise.gateway.service.WorkSpaceService;
import com.bixterprise.gateway.utils.IQueueWriter;
import com.bixterprise.gateway.utils.IWorkerImpl;
import com.bixterprise.gateway.utils.OperatorResolver;
import com.bixterprise.gateway.utils.TransactionStatus;
import com.bixterprise.gateway.utils.http;

import java.util.HashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/commandes")
public class TransactionResource {
	public static String LOG_SEPARATOR = "$<<<>>>$";
	
	IWorkerImpl<TransactionActivity> worker;
	IQueueWriter<TransactionActivity> writer = new IQueueWriter<>();
	Logger log = LoggerFactory.getLogger(TransactionResource.class);
        
        @Autowired @Qualifier("gatewayAgentListLock") Lock agentLocker;
        
	@Autowired
	AgentRepository agentRepository;
	
	@Autowired
	TransactionRepository transactionRepository; 
	
	@Autowired
	TransactionActivityRepository transactionActivityRepository;
	
	@Autowired
	AgentTransactionRepository agentTransactionRepository;

        @Autowired
        WorkSpaceRepository workSpaceDB;
        
        @Autowired WebSocketService ws;
        
        @Autowired @Qualifier("gatewayAgentListLock") Lock agentListLock;
        

	public TransactionResource(WorkSpaceService workSpaceService) {
            this.worker = new IWorkerImpl<>(at -> {

                WorkSpace workSpace = workSpaceService.save(at);

                System.out.println("new\n\t->AT = "+at.getId()+"\n\t->AMOUNT = "+at.getAmount()+"\n\t->PHONE = "+at.getTransactionId().getReceiverPhone()+"\n\t->AGENT = "+at.getAgentPhone().getPhone()+"\n\t->WorkSpaceID = "+workSpace);
            });
            IQueueWriter.newInstance(worker, writer);
            System.out.println("Transaction activities initialized");
	}
	
    /**
     * /api/commandes/check end point used to check Transaction Status
     * @param t
     * @return Map of data with following structure
     * 	 "reference" => t.getReference();
     * 	 "activityId" => activ.getId();
     * 	 "status" => activ.getStatus();
     * 	 "log" => activ.getLog();
     * 	 "agent" =>  activ.getAgentPhone().getPhone();
     * 	 "amount" => activ.getAmount();
     */
    @GetMapping("/check")
	public Object checkCommand(@RequestBody Transactions t) {

		if(t.getReference() == null) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "101");
			obj.put("label", "No transaction Id");
			return obj;
		}
		
		Optional<Transactions> oTrans = transactionRepository.findById(t.getReference());
		if(!oTrans.isPresent()) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "102");
			obj.put("label", "No transaction found");
			return obj;
		}
		
		Transactions trans = oTrans.get();
		Set<TransactionActivity> oActiv = transactionActivityRepository
				.findByTransactionsId(trans.getReference());
		
		if(oActiv.size() == 0) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "103");
			obj.put("label", "No Transaction activity found for this transaction");
			return obj;
		}
		TransactionActivity activ = oActiv.iterator().next();
		HashMap<String, Object> res = new HashMap<String, Object>();
		res.put("reference", t.getReference());
		res.put("activityId", activ.getId());
		res.put("status", activ.getStatus());
		res.put("log", activ.getLog());
		res.put("agent", activ.getAgentPhone().getPhone());
		res.put("amount", activ.getAmount());
		return res;
	}
    
        
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
    @PostMapping("/a")
	public Object newCommandes(@RequestBody List<Transactions> transac) {  
            List<Object> t = new LinkedList();
            transac.forEach((e) -> t.add(newCommande(e)));
            
            return t;
        }
        
	@SuppressWarnings({ "unchecked", "rawtypes" })
    @PostMapping
	public Object newCommande(@RequestBody Transactions transac) {  

		if(transac.getReference() == null) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "101");
			obj.put("label", "No transaction Reference");
			return obj;
		}
		if( transac.getAmount() == null || transac.getAmount() <= 0) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "101");
			obj.put("label", "Invalid Amount");
			return obj;
		}
		if(transac.getReceiverPhone() == null || transac.getReceiverPhone().length() < 5) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "101");
			obj.put("label", "Invalid receiver phone number");
			return obj;
		}
		if(transac.getUrl() == null || transac.getUrl() == "") {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "101");
			obj.put("label", "Invalid URL");
			return obj;
		}

		Optional<Transactions> oTrans = transactionRepository.findById(transac.getReference());
		if(oTrans.isPresent()) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "102");
			obj.put("label", "Transaction already exists");
			return obj;
		}
		Transactions transaction = transactionRepository.save(transac);
		

		try { 
			HashMap o = new HashMap();
			o.put("transaction", transaction);
			o.put("amount", transaction.getAmount());
			o.put("receiver", transaction.getReceiverPhone());
			FileOutputStream log = new FileOutputStream("gateway_firebase_notification.txt", true);
			log.write(("\n\nReception d'une commande  >> "+(new Date())+" >>> "+o.toString()).getBytes());
			log.close();
		}catch(Exception e){
			
		}
		
		List<AutomateAgents> agents = new LinkedList<>();
                
                System.out.println("\n\n############################ LOCK ON AGENT ##############");
                agentListLock.lock();
                try{
                    ws.getMap().forEach((sessionId, pipeEntry) -> {
                            Object agent = ((HashMap<String, Object>)pipeEntry).get("agent");
                            if(! (agent.getClass().equals(String.class))) {
                                    AutomateAgents agt = new AutomateAgents();
                                    agt.setImei(((Map)agent).get("imei").toString());
                                    //ObjectParser.parse("{\"imei\": \""+((Map)agent).get("imei")+"\"}", AutomateAgents.class);
                                            agentRepository.findByImei(agt.getImei())
                                            .ifPresent((ag) -> {
                                                System.out.println("Agent = "+ag.getBalance()+" <=> "+transac.getAmount());
                                                if(ag.getBalance() > transac.getAmount()) agents.add(ag);
                                            });
                            } 
                    }); 
                }finally{
                    System.out.println("\n\n############################ UNLOCK ON AGENT ##############");
                    agentListLock.unlock();
                }
                
		
		//System.out.println("\n\n>>>>>>Agents = "+agents);
		
		/** 
		 * On recherche les agents ayant assez pour effectuer cette transaction
		 */
		//List<AutomateAgents> agents = agentRepository.findAgentForTransaction(transaction.getAmount());
		
		if(agents.size() > 0) {
			AutomateAgents agent = null;

			/**
			 * On crée l'activiter de la transaction
			 */
			TransactionActivity activity = new TransactionActivity(); 
			activity.setAmount(transaction.getAmount());
			activity.setCreatedAt(Calendar.getInstance().getTime());
			activity.setStatus(TransactionStatus.PENDING.toString());
			activity.setLog("Initiation de l'opération");
			activity.setTransactionId(transaction);
			activity.setUpdatedAt(activity.getCreatedAt());

					/**
                     * On exclus les agents offline
                     */
//                    agents.stream().filter((a) -> (a.getIs_online() == false)).forEachOrdered((a) -> {
//                        agents.remove(a);
//                    });
                    
                    if(agents.size() > 0){
                        /**
                         * On recherche les agents pouvent résoudre l'opération en fonction de l'opérateur
                         */
                        List<AutomateAgents> availableAgents = OperatorResolver.resolve(agents, transaction.getReceiverPhone());
                        
                        /**
                         * Si on veut rendre circulaire l'activité des agents, on va devoir
                         * les roter. mais dans ce cas on s'interesse au premier element
                         */
                        if(availableAgents.size() > 0) {
                        	for(AutomateAgents ag : availableAgents) {
                        		AgentStatus sta;
								/**
                        		 * Au cas où le status de l'agent a été modifié à DEACTIVATE pourtant l'agent était on online
                        		 * là on va check son status en BD
                        		 */
                        		if((sta = agentRepository.getStatusByPhone(ag.getPhone())) == AgentStatus.ACTIVATE) {
                        			agent = ag;
                        			break;
                        		}else{
                        			ag.setStatus(sta);
                        		}
                        	}
                        	if(agent == null)
                        		activity.setLog(activity.getLog()+LOG_SEPARATOR+"Tous les agents actifs pouvant résoudre cette transaction sont désactivés");
                        	
//                        	agent = availableAgents.get(0);
                        }else {
                        	activity.setLog(activity.getLog()+LOG_SEPARATOR+"Aucun agent actif trouvé pour opérateur téléphonique");
                        }
                    }else
                    	activity.setLog(activity.getLog()+LOG_SEPARATOR+"Aucun agent actif trouvé");
    				
                    
            /**
			 * On va charger le premier agent de la liste d'effectuer la transaction
			 */
			activity.setAgentPhone(agent);
			/**
			 * On enregistre l'activté
			 */
			if(agent == null) {
				activity.setAgentPhone(null);
				activity.setStatus(TransactionStatus.FAILURE.toString());
				activity = transactionActivityRepository.save(activity);

				HashMap obj = new HashMap();
				obj.put("transactionid", transaction.getReference());
				obj.put("statusdesc", activity.getLog());
				obj.put("amount", activity.getAmount()+"");
				obj.put("status", "200");
				
				/**
				 * On Signale à SpTransaction que la transaction n'a pu aboutir dû à l'absence d'agent disponibe
				 */
				Object response = null;
				try{
					response = http.post(transaction.getUrl(), obj);
					System.out.println("**************** GATEWAY RESPONSE FROM CALL BACK **************\n\n"+response.toString());
				}catch(Exception e){

					System.out.println(
					"\n\n********************* GATEWAY SENDING RESPONSE ERROR ****************\n\n"+
					"\t\t>>>>>>>>URL: "+transaction.getUrl()+"\n\n"+
					"\t\t>>>>>>>>Body: "+obj.toString()+"\n\n"+
					"\t\t>>>>>>>>Error = "+e.getMessage()+"\n\n"+
					"\t\t>>>>>>>>Trace = "+e.getCause()
					);
				}
				try {  
					HashMap t = new HashMap();
					t.put("activity", activity);
					FileOutputStream log = new FileOutputStream("gateway_firebase_notification.txt", true);
						log.write(("\n\nNouvelle commande Aucun agents actifs pour traiter l'opération >> "+(new Date())+" >>> Transaféré "+obj.toString()).getBytes());
						log.write(("\n\nActivité associée >> "+(new Date())+" >>> Transaféré "+t.toString()).getBytes());
					log.close();
				}catch(Exception e){
				}
				return activity;
			}
			final TransactionActivity acti = transactionActivityRepository.save(activity);
			/**
			 * On charge dans la pile d'exécution
			 */
                        (new FutureTask<>(() -> { 
                            writer.add(acti);
                            return 0;
                        })).run();

			try { 
				FileOutputStream logFirebase = new FileOutputStream("gateway_firebase_notification.txt", true);
				logFirebase.write(("\n\nTransaction ajouté dans le fil de traitement >> "+(new Date())+" >>> Transaféré "+acti.toString()).getBytes());
				logFirebase.close();
			}catch(Exception e){
				
			}
			return activity;
		}
		HashMap<String, Object> res = new HashMap<>();
		res.put("code", 102);
		res.put("label", "Aucun agent actif trouvé pour la transaction "+transac.getReference()+", solde insuffisant");
		return res;
	}
	
	@GetMapping
	public Object newCheck(@RequestBody Transactions transac) {

		if(transac.getReference() == null) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "101");
			obj.put("label", "N° de référence absent");
			return obj;
		}
		if( transac.getAmount() == null || transac.getAmount() <= 0) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "101");
			obj.put("label", "Montant invalide");
			return obj;
		}
		if(transac.getReceiverPhone() == null || transac.getReceiverPhone().length() < 5) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "101");
			obj.put("label", "Numéro de téléphone du bénéficiaire incorrect");
			return obj;
		}
		if(transac.getUrl() == null || transac.getUrl() == "") {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "101");
			obj.put("label", "Adresse url incorrecte");
			return obj;
		}

		Optional<Transactions> oTrans = transactionRepository.findById(transac.getReference());
		if(oTrans.isPresent()) {
			HashMap<String, String> obj = new HashMap<>();
			obj.put("code", "102");
			obj.put("label", "Cette transaction existe déjà");
			return obj;
		}
		Transactions transaction = transac;
		

		try { 
			HashMap o = new HashMap();
			o.put("transaction", transaction);
			o.put("amount", transaction.getAmount());
			o.put("receiver", transaction.getReceiverPhone());
			FileOutputStream log = new FileOutputStream("gateway_firebase_notification.txt", true);
			log.write(("\n\nReception d'une commande  >> "+(new Date())+" >>> "+o.toString()).getBytes());
			log.close();
		}catch(Exception e){
			
		}
		/**
		 * On recherche les agents ayant assez pour effectuer cette transaction
		 */
		List<AutomateAgents> agents = agentRepository.findAgentForTransaction(transaction.getAmount());
		
		if(agents.size() > 0) {

			/**
			 * On crée l'activiter de la transaction
			 */
			TransactionActivity activity = new TransactionActivity(); 
			activity.setAmount(transaction.getAmount());
			activity.setCreatedAt(Calendar.getInstance().getTime());
			activity.setStatus(TransactionStatus.PENDING.toString());
			activity.setLog("Initiation de l'opération");
			activity.setTransactionId(transaction);
			activity.setUpdatedAt(activity.getCreatedAt());

					/**
                     * On exclus les agents offline
                     */
                    agents.stream().filter((a) -> (a.getIs_online() == false)).forEachOrdered((a) -> {
                        agents.remove(a);
                    });
                    
                    if(agents.size() > 0){
                        /**
                         * On recherche les agents pouvent résoudre l'opération en fonction de l'opérateur
                         */
                        List<AutomateAgents> availableAgents = OperatorResolver.resolve(agents, transaction.getReceiverPhone());
                        
                        /**
                         * Si on veut rendre circulaire l'activité des agents, on va devoir
                         * les roter. mais dans ce cas on s'interesse au premier element
                         */
                        if(availableAgents.size() > 0) {
                        	HashMap response = new HashMap();
                        	response.put("code", "400");
                        	response.put("label", "Opération Possible");
                        	return response;
                        	
                        }else {
                        	HashMap response = new HashMap();
                        	response.put("code", "401");
                        	response.put("label", "Aucun agent actif trouvé pour cette opérateur téléphonique");
                        	return response;
                        }
                    }else {
                    	HashMap response = new HashMap();
                    	response.put("code", "402");
                    	response.put("label", "Aucun agent actif trouvé pour cette opération");
                    	return response;
                    }
		}
		HashMap<String, Object> res = new HashMap<>();
		res.put("code", 403);
		res.put("label", "Aucun agent trouvé pour envoyé ce montant");
		return res;
	}

	@PostMapping("/notify-transaction-receive")
	public HashMap CommandReceptionReport(@RequestBody TransactionActivity activ) {
		TransactionActivity ta = transactionActivityRepository.findById(activ.getId()).orElse(null);
		

		System.out.println(
		"\n**************** GATEWAY NOTIFY RECEIVE WITH **************\n\n"+
		"\t\t>>>>>>>>transaction: "+activ+"\n\n"+
		"\t\t>>>>>>>>Inner Activity: "+ta+"\n\n"
		);
		
		if(ta != null) {
			ta.setStatus(TransactionStatus.RUNNING.toString());
                        ta.setLog(ta.getLog()+LOG_SEPARATOR+"Transaction reçue par le mobile");
			ta = transactionActivityRepository.save(ta);
                        WorkSpace workSpace = workSpaceDB.findByActivity(ta);
                        if(workSpace != null){
                            workSpace.setStatus(TransactionStatus.RUNNING);
                            workSpaceDB.save(workSpace);
                        }
			HashMap res = new HashMap();
			res.put("status", true);
			res.put("message", "1000");
			res.put("data", ta.toHashMap());

			System.out.println(
			"\n**************** GATEWAY NOTIFY SEND BACK TO MOBILE **************\n\n"+
			"\t\t>>>>>>>>Body: "+res.toString()+"\n\n"
			);
			return res;
		}
		transactionActivityRepository.save(ta);
		HashMap res = new HashMap();
		res.put("status", false);
		res.put("message", "1001");
		res.put("data", activ.toHashMap());

		System.out.println(
		"\n**************** Process of NOTIFY ENDED **************\n\n");
		return res;
	}

	@PostMapping("/update-transaction")
	public HashMap CommandComplete(@RequestBody TransactionActivity activ)  {
		TransactionActivity ta = transactionActivityRepository.findById(activ.getId()).orElse(null);
                
		if(ta != null) {
			/**
			 * On met à jour le status de la transation qui peut être
			 * COMPLETE, ABORT, FAILED
			 */
			ta.setStatus(TransactionStatus.valueOf(activ.getStatus()).toString());
			ta.setLog(ta.getLog()+LOG_SEPARATOR+activ.getLog());
			//ta.setStatus(activ.getStatus());
			/**
			 * Pour la notification 
			 */
			
			HashMap obj = new HashMap();
			obj.put("transactionid", ta.getTransactionId().getReference());
			obj.put("statusdesc", ta.getLog());
			obj.put("amount", ta.getAmount()+"");
			obj.put("status", "200");
			/**
			 * CREATION DE LA TRANSACTION AGENT EN CAS DE TRANSACTION COMPLETE
			 */
			if(ta.getStatus() == null ? TransactionStatus.COMPLETE.toString() == null : ta.getStatus().equals(TransactionStatus.COMPLETE.toString())) {
				obj.put("status", "100");
				ta.setUpdatedAt(Calendar.getInstance().getTime());
				AgentTransaction at = new AgentTransaction();
				at.setAgentPhone(ta.getAgentPhone());
				at.setAmount(-ta.getAmount()); 
				at.setCreatedAt(Calendar.getInstance().getTime());
				at.setDescription("RETRAIT DU COMPTE AGENT D'UN MONTANT DE "+at.getAmount()+" units");
				/**
				 * MISE À JOUR DU SOLDE DE L'AGENT APRÈS
				 */
				ta.getAgentPhone().setSolde(ta.getAgentPhone().getBalance()-ta.getAmount());
				agentTransactionRepository.save(at);
				agentRepository.save(ta.getAgentPhone());
                                agentLocker.lock();
                                try{
                                    System.out.println("\n\n############################ AGENT UNLOCK ON  UPDATE TRANSACTION RESOURCE ##############");
                                    for (String key : ws.getMap().keySet()) {                                        
                                        HashMap<String, Object> entry = HashMap.class.cast(ws.getMap().get(key));
                                        HashMap<String, Object> agent = HashMap.class.cast(entry.get("agent"));
                                        if(agent.get("phone").toString().equals(ta.getAgentPhone().getPhone())){
                                            agent.put("balance", ta.getAgentPhone().getBalance());
                                            break;
                                        }
                                    }
                                }finally{
                                    System.out.println("\n\n############################ AGENT UNLOCK ON  UPDATE TRANSACTION RESOURCE ##############");
                                    agentLocker.unlock();
                                }
			}
			TransactionActivity tb = transactionActivityRepository.save(ta);
                        
                        /**
                         * On retire le job dans le workspace car on a un retour sur lui
                         */
                            WorkSpace workspace = workSpaceDB.findByActivity(ta);
                            if(workspace != null)
                                workSpaceDB.delete(workspace);
			HashMap res = new HashMap();
			res.put("status", true);
			res.put("message", "1000");
			res.put("data", tb.toHashMap());
                        FutureTask<Integer> future = new FutureTask<Integer>(() -> {
                            
                                try{
                                        Object response = http.post(tb.getTransactionId().getUrl(), obj);

                                        System.out.println(
                                        "\n**************** GATEWAY RESPONSE FROM CALL BACK **************\n\n"+
                                        "\t\t>>>>>>>>URL: "+tb.getTransactionId().getUrl()+"\n\n"+
                                        "\t\t>>>>>>>>Body: "+obj.toString()+"\n\n"+
                                        "\t\t>>>>>>>>response = "+response+"\n\n"
                                        );
                                        return 0;
                                }catch(Exception e){

                                        System.out.println(
                                        "\n\n********************* GATEWAY SENDING RESPONSE ERROR ****************\n\n"+
                                        "\t\t>>>>>>>>URL: "+tb.getTransactionId().getUrl()+"\n\n"+
                                        "\t\t>>>>>>>>Body: "+obj.toString()+"\n\n"+
                                        "\t\t>>>>>>>>Error = "+e.getMessage()+"\n\n"+
                                        "\t\t>>>>>>>>Trace = "+e.getCause()
                                        );
                                }
                                return -1;
                        });
                        future.run();
			return res;
		}

		HashMap res = new HashMap();
		res.put("status", false);
		res.put("message", "1001");
		res.put("errors", activ.toHashMap());

		try { 
			FileOutputStream logFirebase = new FileOutputStream("gateway_firebase_notification.txt", true);
			logFirebase.write(("\n\nUpdate Transaction Failed >> "+(new Date())+" >>> "+res.toString()).getBytes());
			logFirebase.close();
		}catch(Exception e){
			
		}
		return res;
	}

	@RequestMapping("/check/network")
	public Object checkNetwork(String phone) {
		HashMap res = new HashMap();
		res.put("phoneNumber", phone);
		res.put("phoneOperator", http.getPhoneOperator(OperatorResolver.removeCountryCode(phone)).toString());
		return res;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("/online")
	public Object activeUser() {
		LinkedList<HashMap> k = new LinkedList<>();
                log.debug("\n\n############################ AGENT LOCK ON ONLINE RESOURCE ##############");
                agentLocker.lock();
                try{
                    ws.getMap().forEach((sessionId, pipeEntry) -> {
                            HashMap<String, Object> m = new HashMap<>();
                            m.put("agent", ((HashMap<String, Object>)pipeEntry).get("agent"));
                            m.put("pipe", ((HashMap<String, Object>)pipeEntry).get("pipe"));
                            k.add(m);
                    });
                }finally{
                    System.out.println("\n\n############################ AGENT UNLOCK ON  ONLINE RESOURCE ##############");
                    agentLocker.unlock();
                }
		
		return k;
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bixterprise.gateway.service;

import com.bixterprise.gateway.domain.AgentTransaction;
import com.bixterprise.gateway.domain.TransactionActivity;
import com.bixterprise.gateway.domain.WorkSpace;
import com.bixterprise.gateway.repository.AgentTransactionRepository;
import com.bixterprise.gateway.repository.TransactionActivityRepository;
import com.bixterprise.gateway.repository.WorkSpaceRepository;
import com.bixterprise.gateway.service.WorkSpaceService;
import com.bixterprise.gateway.utils.TransactionStatus;
import com.bixterprise.gateway.utils.http;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

import java.util.HashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 * @author champlain
 */

@Service
public class TransactionService {
    
    
    @Autowired TransactionActivityRepository transactionActivityRepository;
    
    @Autowired AgentTransactionRepository agentTransactionRepository;

    @Autowired AgentService agentService;
    
    @Autowired WorkSpaceRepository workSpaceDB;

    @Autowired @Qualifier("gatewayAgentListLock") Lock agentListLock;

    @Autowired @Qualifier("gatewayAgentListLock") Lock agentLocker;

    public HashMap commandeReception(TransactionActivity activ){
        TransactionActivity ta = transactionActivityRepository.findById(activ.getId()).orElse(null);
		

		System.out.println(
		"\n**************** GATEWAY NOTIFY RECEIVE WITH **************\n\n"+
		"\t\t>>>>>>>>transaction: "+activ+"\n\n"+
		"\t\t>>>>>>>>Inner Activity: "+ta+"\n\n"
		);
		
		if(ta != null) {
			ta.setStatus(TransactionStatus.RUNNING.toString());
                        ta.setLog(ta.getLog()+WorkSpaceService.LOG_SEPARATOR+"Transaction reçue par le mobile");
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

    public HashMap receptionCommand(TransactionActivity activ) {
		TransactionActivity ta = transactionActivityRepository.findById(activ.getId()).orElse(null);
                
		if(ta != null) {
			/**
			 * On met à jour le status de la transation qui peut être
			 * COMPLETE, ABORT, FAILED
			 */
			ta.setStatus(TransactionStatus.valueOf(activ.getStatus()).toString());
			ta.setLog(ta.getLog()+WorkSpaceService.LOG_SEPARATOR+activ.getLog());
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
				agentService.save(ta.getAgentPhone());
                                agentLocker.lock();
                                try{
                                    System.out.println("\n\n############################ AGENT UNLOCK ON  UPDATE TRANSACTION RESOURCE ##############");
                                    for (String key : WorkSpaceService.map.keySet()) {                                        
                                        HashMap<String, Object> entry = HashMap.class.cast(WorkSpaceService.map.get(key));
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
}

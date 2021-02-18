package com.bixterprise.gateway.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bixterprise.gateway.domain.AutomateAgents;
import com.bixterprise.gateway.domain.TransactionActivity;
import com.bixterprise.gateway.domain.WorkSpace;
import com.bixterprise.gateway.service.WorkSpaceService;
import com.bixterprise.gateway.service.AgentService;
import com.bixterprise.gateway.service.TransactionService;
import com.bixterprise.gateway.utils.ObjectParser;
import com.bixterprise.gateway.utils.PhoneOperator;
import io.socket.client.Socket;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapstruct.ap.internal.util.Collections;
import org.springframework.beans.factory.annotation.Qualifier;


@Service
public class WebSocketService  {

	
	@Autowired AgentService agentService;
    
	@Autowired TransactionService transactionService;
	
        @Autowired @Qualifier("gatewayDeliverer") Socket gatewayDeliverer;
        
        @Autowired @Qualifier("gatewayAgentListLock") Lock agentListLock;
        
        @Autowired WorkSpaceService workSpaceService;
        
    
        /**
         * Lock on launchWorkSpaceThread, to insure that sigle thread will works
         */
        @Autowired @Qualifier("gatewayMtnWorkSpaceThreadLock") Lock mtnThreadLock;
        @Autowired @Qualifier("gatewayOrangeWorkSpaceThreadLock") Lock orangeThreadLock;
	
        
        public void send(WorkSpace workSpace){
            
            TransactionActivity at = workSpace.getActivity();
            
            HashMap obj = new HashMap();
            obj.put("collapse_key", "type_a");
            obj.put("to", at.getAgentPhone().getFcm_token());
                HashMap data = new HashMap();
                    data.put("id", at.getId());
                    data.put("transaction_id", at.getTransactionId().getReference());
                    data.put("client_phone", at.getTransactionId().getReceiverPhone());
                    data.put("automate_agent_phone", at.getAgentPhone().getPhone());
                    data.put("status", at.getStatus());
                    data.put("amount", at.getAmount());
                    data.put("log", at.getLog());
                        Calendar c = Calendar.getInstance();
                            c.setTime(at.getCreatedAt());
                    data.put("created_at", c.get(Calendar.YEAR)+"-"+((c.get(Calendar.MONTH) < 10 ? "0" : "")+(c.get(Calendar.MONTH)+1))+"-"+c.get(Calendar.DAY_OF_MONTH)+" "+c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE));
                HashMap arg = new HashMap();
                arg.put("data", data);
                arg.put("type", "transaction");
            obj.put("data", arg);
            obj.put("operator", workSpace.getActivity().getAgentPhone().getPhoneOperator());
            obj.put("workspace", workSpace.getId());
            try {
                /**
                 * On envoie la notification à Firebase
                 */
                /// Object res = http.firebaseNotification(obj);
                /**
                 * ON envoie la notification par la WebSocket
                 */
                
                sentToMobile(obj);
                
                FileOutputStream logFirebase = new FileOutputStream("gateway_firebase_notification.txt", true);
                logFirebase.write(("\n\nReponse Systeme >> "+(new Date())+" >>> "+obj.toString()+"\n").getBytes());
                logFirebase.close();
            }catch(Exception e){
                
            }
            
        }
        
//	public void onMessage(WebSocketSession session, BinaryMessage message) {
//		
//	}
//
//	public void onMessage(WebSocketSession session, TextMessage message) {
//
//				
//	}
//
//	public void onLogin(WebSocketSession session, HashMap message) {
//		
//		try {			
//			
//			if(String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.PING.toString()) || String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.PONG.toString())) {
//				HashMap obj = new HashMap();
//				obj.put("type", (String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.PING.toString()) ? MessageType.PONG : MessageType.PING).toString());
//				session.sendMessage(new TextMessage(obj.toString().getBytes()));				
//			} else if(String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.LOGIN.toString())) {
//				AutomateAgents agent = ObjectParser.parse(HashMap.class.cast(message.get("message")).toString(), AutomateAgents.class);
//				if(agent != null) {
//					agent.setFcm_token(session.getId()); 
//					HashMap rest = agentService.login(agent);
//					System.out.println(rest);
//					if(Boolean.class.cast(rest.get("status"))) {
//						HashMap<String, Object> pipeEntry = new HashMap<>();
//						pipeEntry.put("agent", ObjectParser.parse(HashMap.class.cast(rest.get("data")).toString(), HashMap.class));
//						pipeEntry.put("pipe", session);
//						map.put(session.getId(), pipeEntry);
//					}
//					rest.put("type", "login");
//					session.sendMessage(new TextMessage(rest.toString().getBytes()));
//				}
//				
//			}else if(String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.LOGOUT.toString())) {
//				
//				AutomateAgents agent = ObjectParser.parse(HashMap.class.cast(message.get("message")).toString(), AutomateAgents.class);
//				if(agent != null) {
//					agent.setFcm_token(session.getId());
//					HashMap rest = agentService.logout(agent);
//					//map.remove(session.getId());
//					rest.put("type", "logout");
//					session.sendMessage(new TextMessage(rest.toString().getBytes()));
//					onSocketDisconnected(session, CloseStatus.NORMAL);
//				}
//				
//			}else if(String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.NOTIFY.toString())) {
//	
//				TransactionActivity ta = ObjectParser.parse(HashMap.class.cast(message.get("message")).toString(), TransactionActivity.class);
//				if(ta != null) {
//					HashMap rest = transactionService.CommandReceptionReport(ta);
//					rest.put("type", "notify");
//					session.sendMessage(new TextMessage(rest.toString().getBytes()));
//				}
//			}else if(String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.UPDATE.toString())) {
//	
//				TransactionActivity ta = ObjectParser.parse(HashMap.class.cast(message.get("message")).toString(), TransactionActivity.class);
//				if(ta != null) {
//					HashMap rest = transactionService.CommandComplete(ta);
//					System.out.println(rest);
//					rest.put("type", "update");
//					session.sendMessage(new TextMessage(rest.toString().getBytes()));
//				}
//			}else {
//				
//			}
//		} catch(IOException e) {
//			System.out.println("Message << Fermé anormalement ");
//			onSocketDisconnected(session, CloseStatus.SESSION_NOT_RELIABLE);
//		}
//	}
//
	public static Object pushWebServiceNotification(final HashMap obj) {
//		String message = obj.get("data").toString();
//		// ObjectParser.parse(obj.get("data").toString(), HashMap.class);
//		Object pipeEntryObject =  map.get(String.class.cast(obj.get("to")));// new HashMap<>();
//		System.out.println("description = "+pipeEntryObject);
//		if(pipeEntryObject != null) {
//			@SuppressWarnings("unchecked")
//			HashMap<String, Object> pipeEntry = (HashMap<String, Object>) pipeEntryObject;
//			WebSocketSession pipe = (WebSocketSession) ((HashMap<String, Object>)pipeEntry).get("pipe");
//			System.out.println("Message out = >> "+message.toString());
//			try {
//				pipe.sendMessage(new TextMessage(message.toString().getBytes()));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		return null;
	}

    public void onAgentLogin(Object...args) {
        	if(args != null && args.length > 1){
                    AutomateAgents agent = ObjectParser.parse(JSONObject.class.cast(args[0]).toString(), AutomateAgents.class);
            LogFactory
                .getLog(WebSocketService.class)
                .debug("\n\n###################################################\n#### Agent Connected "+agent.toJSONString()+"\n###################################################");
                    
                    if(agent != null) {
                        agent.setFcm_token(String.class.cast(args[1])); 
                        HashMap rest = agentService.login(agent);
                        rest.put("pipe", agent.getFcm_token());
                        if(Boolean.class.cast(rest.get("status"))) {
                                HashMap<String, Object> pipeEntry = new HashMap<>();
                                pipeEntry.put("agent", rest.get("data"));
                                pipeEntry.put("pipe", agent.getFcm_token());
                                
                                agentListLock.lock();
                                System.out.println("\n\n############################ LOCK ON LOGIN ##############");
                                try {
                                    WorkSpaceService.map.put(agent.getFcm_token(), pipeEntry);
                                } finally {
                                    System.out.println("\n\n############################ UNLOCK ON LOGIN ##############");
                                    agentListLock.unlock();
                                }
                        }
                        gatewayDeliverer.emit("login", rest);

//                            rest.put("type", "login");
//                            session.sendMessage(new TextMessage(rest.toString().getBytes()));
                    }
                }
    }

    public void onAgentLogout(Object...args) {
        LogFactory
            .getLog(WebSocketService.class)
            .debug("\n\n###################################################\n#### Disconnect from \n###################################################");

        if(args != null && args.length > 0){
            /**
             * On récupère le 
             */
            HashMap m = HashMap.class.cast(WorkSpaceService.map.get(args[0].toString()));
            if(m != null){
                m = (HashMap) m.get("agent");
                AutomateAgents ag = new AutomateAgents();
                ag.setPhone((String) m.get("phone"));
                ag.setImei(m.get("imei").toString());
                ag.setLog("Déconnexion Agent");
                ag.setFcm_token(String.class.cast(m.get("fcm_token")));
                
                HashMap rest = agentService.logout(ag);
                if(Boolean.class.cast(rest.get("status"))) {
                    agentListLock.lock();
                    try{
                        WorkSpaceService.map.remove(ag.getFcm_token());
                    }finally{
                        agentListLock.unlock();
                    }
                }
            }
        }
        
        
    }
    
    public void onTransactionGetNextCommand(Object...args){
        
    }
    
    
    public void onTransactionGetNextCommandReceived(Object...args){
        
    }

    /**
     *
     * L'update désigne le status final de traitement de la transaction par le mobile
     * et donc il envoie l'identifiant de la transaction reçue, le status de la transaction 
     * et le message (log) renvoyé par l'opérateur
     * on transfert ces données au serveur de la gateway
     * @param params
     */
    public void onTransactionUpdate(Object...params) {
        if(params != null && params.length > 0){
            for(Object entry : params){
                try {
                    TransactionActivity ta = new TransactionActivity();
                    JSONObject m = JSONObject.class.cast(entry);
                    ta.setId(m.getLong("id"));
                    ta.setLog(m.getString("log"));
                    ta.setStatus(m.getString("status"));
                    transactionService.receptionCommand(ta);
                } catch (JSONException ex) {
                    Logger.getLogger(WebSocketService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * This function is called when Phone has successfully received transaction
     * and send back reception delivery
     * @param receptionDeliveries
     */
    public void onTransactionReceive(Object...receptionDeliveries) {
        
        if(receptionDeliveries != null && receptionDeliveries.length > 0)
            for(Object id : receptionDeliveries){
                TransactionActivity ta = new TransactionActivity(Long.parseLong(id+""));
                    HashMap rest = transactionService.commandeReception(ta);
                gatewayDeliverer.emit("commande/notify", rest);
//                    rest.put("type", "notify");
//                    session.sendMessage(new TextMessage(rest.toString().getBytes()));
            }
        
    }
//	 

    public void onOrangeThreadStarted(Object...args) {
        orangeThreadLock.lock();
        try {
            WorkSpaceService.launchOrangeWorkSpaceThread = true;
        } finally {
            orangeThreadLock.unlock();
        }
        System.out.println("\n\n########### Orange Thread Started############ \n\n");
    }
    public void onMtnThreadStarted(Object...args) {
        mtnThreadLock.lock();
        try {
            WorkSpaceService.launchMtnWorkSpaceThread = true;
        } finally {
            mtnThreadLock.unlock();
        }
        System.out.println("\n\n########### Mtn Thread Started############ \n\n");
    }
    public void onOrangeThreadStoped(Object...args) {
        orangeThreadLock.lock();
        try {
            System.out.println("\n\n########### Orange Thread Stoped############ \n\n");
            WorkSpaceService.launchOrangeWorkSpaceThread = false;
        } finally {
            orangeThreadLock.unlock();
        }
    }
    public void onMtnThreadStoped(Object...args) {
        mtnThreadLock.lock();
            try {
                System.out.println("\n\n########### MtnThread Stoped############ \n\n");
                WorkSpaceService.launchMtnWorkSpaceThread = false;
            } finally {
                mtnThreadLock.unlock();
            }
    }

    public void onAskCommandForOrange(Object[] os) {
            WorkSpace workSpace = workSpaceService.findNextForOperator(PhoneOperator.ORANGE);
            if(workSpace == null)
                return;
            send(workSpace);
        System.out.println("\n\n########## ASK TRANSACITION FOR ORANGE ###########\n\n");
    }

    public void onAskCommandForMtn(Object[] os) {
        workSpaceService.findNextForOperator(PhoneOperator.MTN);
        System.out.println("\n\n######### ASK TRANSACTION FOR MTN ########\n\n");
    }

    private void sentToMobile(HashMap command) {
        System.out.println("\n\n################### ENVOI DE LA COMMANDE ###################\n### ");
        gatewayDeliverer.emit("/command", command);
        System.out.println("###   "+command+"\n###");
        System.out.println("\n################### ENVOI DE LA COMMANDE ###################\n\n");
    }

    public void onCommandForReceived(Object...ids) {
        if(ids != null && ids.length > 0){
            for(Object id : ids){
                System.out.println("\n\n##################### WorkSpace RECEIVED ################\n### "+id+"\n##################################################");
                workSpaceService.updateWorkSpaceReceived(new Long(id+""));
            }
        }
    }

    public void serverDisconnected(Object[] os) {
        System.out.println("\n\n##################### SERVER DISCONNECTED TO DELIVERER ################\n");
            onOrangeThreadStoped(os);
            onMtnThreadStoped(os);
        System.out.println("\n\n##################### SERVER DISCONNECTED TO DELIVERER ################\n");
    }
    
}

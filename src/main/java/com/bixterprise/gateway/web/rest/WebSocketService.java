package com.bixterprise.gateway.web.rest;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.bixterprise.gateway.domain.AutomateAgents;
import com.bixterprise.gateway.domain.TransactionActivity;
import com.bixterprise.gateway.utils.ObjectParser;
import com.bixterprise.gateway.websocket.listener.WebSocketListener;
import com.bixterprise.gateway.websocket.messages.MessageType;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.HashMap;


@Service
public class WebSocketService implements WebSocketListener  {

	public static HashMap<String, Object> map = new HashMap<>();
	
	@Autowired AgentsResource agentsRestController;
    
	@Autowired TransactionResource transactionRestController;
	
	
	public HashMap<String, Object> getMap() {
		return map;
	}
	
	@Override
	public void onSocketConnected(WebSocketSession session) {

            HashMap<String, Object> pipeEntry = new HashMap<>();
            pipeEntry.put("agent", "Agent non identifié");//ObjectParser.parse(rest.getHashMap("data").toString(), HashMap.class));
            pipeEntry.put("pipe", session);
            map.put(session.getId(), pipeEntry);

            HashMap obj = new HashMap();
            obj.put("type", "info");
            try {
                session.sendMessage(new TextMessage(obj.toString().getBytes()));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
	}

	@Override
	public void onSocketDisconnected(WebSocketSession session, CloseStatus status) {
		
			Object o = map.get(session.getId());


		if(o != null) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> pipeEntry = (HashMap<String, Object>) o;
			AutomateAgents agent = ObjectParser.parse(pipeEntry.get("agent").toString(), AutomateAgents.class);
			if(agent != null) {
                            agent.setFcm_token(session.getId());		
                            agentsRestController.logout(agent);
			}	
		}
		map.remove(session.getId());
		try {
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onMessage(WebSocketSession session, BinaryMessage message) {
		
	}

	public void onMessage(WebSocketSession session, TextMessage message) {

				
	}

	public void onMessage(WebSocketSession session, HashMap message) {
		
		try {			
			
			if(String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.PING.toString()) || String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.PONG.toString())) {
				HashMap obj = new HashMap();
				obj.put("type", (String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.PING.toString()) ? MessageType.PONG : MessageType.PING).toString());
				session.sendMessage(new TextMessage(obj.toString().getBytes()));				
			} else if(String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.LOGIN.toString())) {
				AutomateAgents agent = ObjectParser.parse(HashMap.class.cast(message.get("message")).toString(), AutomateAgents.class);
				if(agent != null) {
					agent.setFcm_token(session.getId()); 
					HashMap rest = agentsRestController.login(agent);
					System.out.println(rest);
					if(Boolean.class.cast(rest.get("status"))) {
						HashMap<String, Object> pipeEntry = new HashMap<>();
						pipeEntry.put("agent", ObjectParser.parse(HashMap.class.cast(rest.get("data")).toString(), HashMap.class));
						pipeEntry.put("pipe", session);
						map.put(session.getId(), pipeEntry);
					}
					rest.put("type", "login");
					session.sendMessage(new TextMessage(rest.toString().getBytes()));
				}
				
			}else if(String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.LOGOUT.toString())) {
				
				AutomateAgents agent = ObjectParser.parse(HashMap.class.cast(message.get("message")).toString(), AutomateAgents.class);
				if(agent != null) {
					agent.setFcm_token(session.getId());
					HashMap rest = agentsRestController.logout(agent);
					//map.remove(session.getId());
					rest.put("type", "logout");
					session.sendMessage(new TextMessage(rest.toString().getBytes()));
					onSocketDisconnected(session, CloseStatus.NORMAL);
				}
				
			}else if(String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.NOTIFY.toString())) {
	
				TransactionActivity ta = ObjectParser.parse(HashMap.class.cast(message.get("message")).toString(), TransactionActivity.class);
				if(ta != null) {
					HashMap rest = transactionRestController.CommandReceptionReport(ta);
					rest.put("type", "notify");
					session.sendMessage(new TextMessage(rest.toString().getBytes()));
				}
			}else if(String.class.cast(message.get("type")).equalsIgnoreCase(MessageType.UPDATE.toString())) {
	
				TransactionActivity ta = ObjectParser.parse(HashMap.class.cast(message.get("message")).toString(), TransactionActivity.class);
				if(ta != null) {
					HashMap rest = transactionRestController.CommandComplete(ta);
					System.out.println(rest);
					rest.put("type", "update");
					session.sendMessage(new TextMessage(rest.toString().getBytes()));
				}
			}else {
				
			}
		} catch(IOException e) {
			System.out.println("Message << Fermé anormalement ");
			onSocketDisconnected(session, CloseStatus.SESSION_NOT_RELIABLE);
		}
	}

	public static Object pushWebServiceNotification(final HashMap obj) {
		String message = obj.get("data").toString();
		// ObjectParser.parse(obj.get("data").toString(), HashMap.class);
		Object pipeEntryObject =  map.get(String.class.cast(obj.get("to")));// new HashMap<>();
		System.out.println("description = "+pipeEntryObject);
		if(pipeEntryObject != null) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> pipeEntry = (HashMap<String, Object>) pipeEntryObject;
			WebSocketSession pipe = (WebSocketSession) ((HashMap<String, Object>)pipeEntry).get("pipe");
			System.out.println("Message out = >> "+message.toString());
			try {
				pipe.sendMessage(new TextMessage(message.toString().getBytes()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	    
	
}

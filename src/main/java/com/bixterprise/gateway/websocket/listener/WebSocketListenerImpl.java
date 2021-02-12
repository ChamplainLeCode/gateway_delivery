package com.bixterprise.gateway.websocket.listener;

import com.bixterprise.gateway.web.rest.WebSocketService;
import com.bixterprise.gateway.utils.ObjectParser;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import org.springframework.stereotype.Component;
//import org.springframework.web.socket.BinaryMessage;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;

@Component
public class WebSocketListenerImpl  {
  
    @Autowired
    WebSocketService wsService;
//
//	public void newTextMessage(WebSocketSession session, TextMessage message) {
//		HashMap map;
//		map = new HashMap(ObjectParser.parse(message.getPayload(), HashMap.class));
//		wsService.onMessage(session, map);
//	}
//
//	public void newBinaryMessage(WebSocketSession session, BinaryMessage message) {
//		wsService.onMessage(session, message);
//	}
//
//	public void closeConnection(WebSocketSession session, CloseStatus status) {
//		wsService.onSocketDisconnected(session, status);		
//	}
//
//	public void openConnection(WebSocketSession session) {
//		wsService.onSocketConnected(session);
//	}

     
}
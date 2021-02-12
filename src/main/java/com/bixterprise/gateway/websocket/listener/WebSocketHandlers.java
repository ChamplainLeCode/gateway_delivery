package com.bixterprise.gateway.websocket.listener;

import java.io.IOException;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
//import org.springframework.web.socket.BinaryMessage;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Component
public class WebSocketHandlers {
        
//        extends AbstractWebSocketHandler {
//	
//	@Autowired
//	WebSocketListenerImpl listener;
//	
//	@Override
//	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//		listener.closeConnection(session, status);
//		super.afterConnectionClosed(session, status);
//	}
//	 
//	@Override
//	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//		super.afterConnectionEstablished(session);
//		listener.openConnection(session);
//	}
//		
//	@Override
//	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
//	    listener.newTextMessage(session, message);
//	}
//	 
//	@Override
//	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
//	    listener.newBinaryMessage(session, message);
//	}

}

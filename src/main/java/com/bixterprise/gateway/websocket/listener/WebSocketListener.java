package com.bixterprise.gateway.websocket.listener;

import java.io.IOException;

import java.util.HashMap;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public interface WebSocketListener {

	public void onSocketConnected(WebSocketSession session);
	
	public void onSocketDisconnected(WebSocketSession session, CloseStatus status);
	
	public void onMessage(WebSocketSession session, BinaryMessage message);
	
	public void onMessage(WebSocketSession session, TextMessage message);
	
	public void onMessage(WebSocketSession session, HashMap message) throws IOException;
}

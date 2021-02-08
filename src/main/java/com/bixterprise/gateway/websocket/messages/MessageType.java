package com.bixterprise.gateway.websocket.messages;

public enum MessageType {
	
	LOGIN("login"), LOGOUT("logout"), NOTIFY("notify"), UPDATE("update"), PING("ping"), PONG("pong");
	
	public final String value;
	
	MessageType(String value){
		this.value = value;
	}
	
	public final String toString() {
		return this.value;
	}
	
	public final String inverse() {
		if(this == MessageType.PING)
			return PONG.toString();
		else if(this == MessageType.PONG)
			return PING.toString();
		return "";
	}
}

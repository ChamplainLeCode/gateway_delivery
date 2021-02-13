package com.bixterprise.gateway.websocket.messages;

public enum MessageType {
	SERVER_DISCONNECTED("disconnect"),
	LOGIN("login"), 
        LOGOUT("logout"), 
        PING("ping"), PONG("pong"), 
        NEXT("next"), NEXT_RECEIVED("next_received"),
	ORANGE_THREAD_STARTED("start/process/orange/started"),
        MTN_THREAD_STARTED("start/process/mtn/started"),
	ORANGE_THREAD_STOPED("stop/process/orange/stoped"),
        MTN_THREAD_STOPED("stop/process/mtn/stoped"),
	ORANGE_THREAD_STOP("stop/process/orange"),
        MTN_THREAD_STOP("stop/process/mtn"),
        ASK_COMMAND_FOR_ORANGE("command/for/orange"),
        ASK_COMMAND_FOR_MTN("command/for/mtn"),
        COMMAND_FOR_RECEIVED("command/received"),
        NOTIFY("command/notify"), 
        UPDATE("command/update");
        
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

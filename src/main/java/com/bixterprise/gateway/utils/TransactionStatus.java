package com.bixterprise.gateway.utils;

/**
 * TransactionStatus define set of constants status that a Gataway Transaction can have.
 * PENDING: When transaction is sent to mobile Agent
 * RUNNING: When Mobile Agent successfully receive Transaction and add to her fifo queue
 * FAILURE: When Transaction have not been complete
 * COMPLETE: When Transaction have been successful complete
 * ABORT: When Transaction have Abort
 * @author haranov
 */
public enum TransactionStatus {
	PENDING("PENDING"), RUNNING("RUNNING"),  FAILURE("FAILURE"), COMPLETE("COMPLETE"), ABORT("ABORT");
	String code;
	
	TransactionStatus(String code){
		this.code = code;
	}
        @Override
	public String toString() {
		return code;
	}
}

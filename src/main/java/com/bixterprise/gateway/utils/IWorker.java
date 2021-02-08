package com.bixterprise.gateway.utils;

/**
 * Functional interface that define task that IWorkerThreadExecutor gonna complete
 * @author haranov Champlain
 * @param <E>
 */
@FunctionalInterface
public interface IWorker<E> {
	
	public void call();
	

}


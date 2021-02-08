package com.bixterprise.gateway.utils;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 *
 * @author haranov Champlain
 * @param <T>
 * @see IQueueWriter
 * @see IWorker
 * @see IWorkerImpl
 * @see IWorkerThread
 */
public class IWorkerData<T>{
	Queue<T> queue = new LinkedList<>();
	
	public synchronized void put(T e) {
            queue.add(e);
            //System.out.println(">>>>> Size of stack = "+queue.size());
            notifyAll();
	}
	
	public synchronized T get() {
            try {
        //	System.out.println("Test = "+(queue.isEmpty() && (IWorkerThread.canWorkAgain()))+" queem = "+queue.isEmpty() );
                while((queue.isEmpty() ))// && (IWorkerThread.canWorkAgain())))
                    try {
                        wait(); 
                        if(queue.isEmpty() && (IWorkerThread.canWorkAgain()))
                            continue;
                    } catch (InterruptedException e) {
                            e.printStackTrace();
                    }
                return queue.remove();
            }catch(NoSuchElementException e) {
                    return null;
            }
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}
}
package com.bixterprise.gateway.utils;

import java.util.LinkedList;
import java.util.Queue;

import com.bixterprise.gateway.utils.GatewayInteface.IAddable;

/**
 *
 * @author haranov Champlain
 * @param <E>
 * @see IAddable
 * @see IQueue
 * @see IQueueWriter
 * @see IWorkerData
 * @see IWorkerImpl
 * @see IWorkerThread
 */
public class IQueueWriter<E> implements GatewayInteface.IAddable<E>{


	public static <T> void newInstance(IWorkerImpl<T> worker, IQueueWriter<T> writer) {
		IQueue<T> queue =  new IQueue<>(worker); 
		writer.setQueue(queue);
		worker.setQueue(queue);
		writer.setAddable((T t)->{
                    //System.out.println("Action of Type "+t.getClass()+" t = "+t+" added");
                    queue.add(t);
                    worker.wakeUp();
		});
		new IWorkerThread<String>().init();
	}
        
        


	private IAddable<E> addable;
	
	private void setAddable(GatewayInteface.IAddable<E> addable) {
		this.addable = (addable);
	}
	
	protected IAddable<E> getAddable() {
		return this.addable;
	}
	

	protected IQueue<E> fifo;
	
	public void setQueue(IQueue<E> queue) {
		fifo = queue;
	}
	
	@Override
	public void add(E e) {
		this.getAddable().add(e);
	}
	
	
	public static class IQueue<E>{

            
		protected IWorkerImpl<E> worker;
		
		protected Queue<E> fifo;
		
		protected IQueue() {
			fifo = new LinkedList<>();
		}
		
		protected IQueue(IWorkerImpl<E> worker) {
			this();
			this.worker = worker;
		}
		
		protected void add(E e) {
			fifo.add(e);
		}
		
		protected E remove() {
			return fifo.poll();
		}
		
		public boolean isEmpty() {
			return fifo.isEmpty();
		}
	}
}

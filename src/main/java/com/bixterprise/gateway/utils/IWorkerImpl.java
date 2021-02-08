package com.bixterprise.gateway.utils;


import com.bixterprise.gateway.utils.IQueueWriter.IQueue;

/**
 *  We build this as Writer/Reader Problem. this class can be consider like Pipe
 * @author haranov Champlain
 * @param <E>
 * @see IWorkerData
 * @see IWorkerThread
 * @see IWorker
 */
public class IWorkerImpl<E> implements IWorker<E>{

	protected boolean isSleeping = true;
	private IQueue<E> fifo;
	private Callback<E> callback;
	
	public IWorkerImpl(Callback<E> callback){
		this.callback = callback;
	}
	@Override
	public synchronized void call() {
				
		this.isSleeping = fifo.isEmpty();// !IWorkerThread.canWorkAgain() || fifo.isEmpty();
		
		if(!this.isSleeping)
			IWorkerThread.exec(this.getActivity(), callback);
			
		
	}
	
    /**
     * Way to get the current first task to complete
     * @return
     */
    protected E getActivity() {
            return fifo.remove();
    }
    public void setQueue(IQueue<E> e) {
            this.fifo = e;
    }

    public synchronized void wakeUp() {
            call();
    }


}

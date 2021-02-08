package com.bixterprise.gateway.utils;

import java.util.HashMap;

/**
 *  General Gateway Request executor. this class is used to manage concurrent threads, that will
 * treat all Gateway request
 * @author haranov Champlain
 * @param <E>
 * @see Thread
 * @see ThreadGroup
 */
public class IWorkerThread<E>  {
	
	static protected long number = 0;
	static protected ThreadGroup tg = new ThreadGroup("activity");
	static IWorkerData<Object> queue = new IWorkerData<>() ;
	
	Runnable scheduler;
	static Thread process;

    /**
     * Method that init All threads, actually we use 3.
     */
    public void init() { 
		scheduler = new Runnable() {
			public synchronized void  run() {
				Thread t1 = new Thread(tg, new IWorkerThread<E>().new IWorkerThreadExecutor(queue, "Worker 1"));
				Thread t2 = new Thread(tg, new IWorkerThread<E>().new IWorkerThreadExecutor(queue, "Worker 2"));
				Thread t3 = new Thread(tg, new IWorkerThread<E>().new IWorkerThreadExecutor(queue, "Worker 3"));
				t1.start();
				t2.start();
				t3.start();
			} 
		};
		process = new Thread(scheduler);
		process.start();
	}
	
	static synchronized boolean canWorkAgain (){
		return tg.activeCount() <= 3;
	}
	
	static synchronized int countProcess() {
		return tg.activeCount();
	}
	static synchronized <E> void exec(E e, Callback<E> callback) {
		HashMap<String, Object> operation = new HashMap<String, Object>();
		operation.put("data", e);
		operation.put("callback", callback); 
		queue.put(operation);
//		if(canWorkAgain())
//			process.notify();
	}
	
    /**
     * IWorkerThreadExecutor is a class that define what each executor will do.
     * In Writer/Reader proble this class can be consider as Reader.
     * @author haranov Champlain
     */
    protected final class IWorkerThreadExecutor implements Runnable{

		IWorkerData<Object> context;
		String name;
		
		@SuppressWarnings("unused")
		private IWorkerThreadExecutor() {}
		
            /**
             *  Constructor
             * @param e IWorkerData, that contains set of data (Job to do)
             * @param name String that represent this Executor
             */
            public IWorkerThreadExecutor(IWorkerData<Object> e, String name) {
                this.context = e; 
                this.name = name;
            }
            
            @SuppressWarnings("unchecked")
			@Override
            public synchronized void run() {
                while(true) {
                    try {
                        HashMap<String, Object> operation = (HashMap<String, Object>)context.get();
                        E data = (E) operation.get("data");
                        Callback<E> callback = (Callback<E>) operation.get("callback");
                        //System.out.println(" Nouveau traitement par "+ name+" de "+data);				
                        callback.call(data);
                        //System.out.println("Agent "+name+" Transaction terminé");
                        notifyAll();
                    }catch(NullPointerException e) {}
                }
            }
		
	}


}

package ch.ethz.inf.vs.actinium.plugnplay;

import java.util.LinkedList;

import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.endpoint.LocalResource;

/**
 * Inspired by
 * http://www.ibm.com/developerworks/library/j-jtp0730/index.html
 * 
 * @author Martin Lanter
 */
public class WorkQueue {
	
	private final PoolWorker thread;
	private final LinkedList<Runnable> queue;

	public WorkQueue() {
		this(null);
	}
	
	public WorkQueue(String name) {
		queue = new LinkedList<Runnable>();
		if (name==null)
			thread = new PoolWorker();
		else
			thread = new PoolWorker(name);
		//thread.start();
	}

	public void deliver(Request request, LocalResource resource) {
		synchronized (queue) {
			queue.addLast(new RequestDelivery(request, resource));
			queue.notify(); // notifyAll not required
		}
	}
	
	public void deliver(Runnable runnable) {
		synchronized (queue) {
			queue.addLast(runnable);
			queue.notify(); // notifyAll not required
		}
	}
	
	/**
	 * Starts the queue concurrently
	 */
	public void start() {
		thread.start();
	}
	
	/**
	 * Executes the queue with the thread, that calls this method
	 */
	public void execute() {
		thread.run();
	}
	
	public void stop() {
		thread.interrupt();
	}

	/*
	 * The handler for the requests of the queue
	 */
	private class PoolWorker extends Thread {
		
		private PoolWorker() {
			super();
		}
		
		private PoolWorker(String name) {
			super(name);
		}
		
		public void run() {
			Runnable r;
			while (true) {
				// wait for another task to execute
				synchronized (queue) {
					while (queue.isEmpty()) {
						try {
							queue.wait();
						} catch (InterruptedException ignored) { }
					}
					r = queue.removeFirst();
					r.run();
				}
			}
		}
	}
	
	private class RequestDelivery implements Runnable {
		
		private Request request;
		private LocalResource resource;
		
		private RequestDelivery(Request request, LocalResource resource) {
			this.request = request;
			this.resource = resource;
		}
		
		public void run() {
			/*
			 * Calls performXXX Method. If an exception occurs it must be
			 * caught, to ensure, the thread doesn't stop.
			 */
			try {
				request.dispatch(resource);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

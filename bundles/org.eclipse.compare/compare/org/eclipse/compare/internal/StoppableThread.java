/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

/**
 * Run a (cooperative) piece of code under timeout control.
 */
public abstract class StoppableThread extends Thread {
			
	boolean fStopped= false;
	Object fResult= null;
	
	private synchronized boolean isStopped2() {
		return fStopped;
	}
	
	public void run() {
		fResult= doRun();
	}
	
	private synchronized void setStopped() {
		fStopped= true;
	}
	
	abstract public Object doRun();
	
	public synchronized Object getResult(int timeout) {
		
		start();
		try {
			join(timeout);
		} catch(InterruptedException ex) {
			fResult= null;
		}
		setStopped();
		return fResult;
	}
	
	public static boolean isStopped() {
		Thread t= Thread.currentThread();
		if (t instanceof StoppableThread) {
			StoppableThread st= (StoppableThread) t;
			return st.isStopped2();
		}
		return false;
	}
}


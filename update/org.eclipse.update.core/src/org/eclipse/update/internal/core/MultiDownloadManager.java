/*
 * Created on Jun 18, 2003
 */
package org.eclipse.update.internal.core;


/**
 * MultiDownloadManager.
 * A helper class that manages the number of threads to be used for parallel downloading.
 */
public class MultiDownloadManager {
	private final static int MAX_NUM_THREADS = 5;
	private final static int WAIT_INTERVAL = 1000; // milliseconds
	private static int threadsInUse = 0;
	
	public static synchronized void releaseThread(Thread t){
		threadsInUse--;
	}
	
	public static synchronized Thread getThread(Runnable r, String threadName, ThreadGroup tg) {
		try {
			while(threadsInUse >= MAX_NUM_THREADS)
				Thread.sleep(WAIT_INTERVAL);
		} catch (InterruptedException e) {
		}
		threadsInUse++;
		return new Thread(tg, r,threadName);
	}
	
	public static void waitForThreads(ThreadGroup tg) {
		int activeThreadsCount = tg.activeCount();
		Thread[] activeThreads = new Thread[activeThreadsCount];
		int n = tg.enumerate(activeThreads);
		for (int i=0; i<n; i++) {
			try {
				activeThreads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void stopThreads(ThreadGroup tg) {
		int activeThreadsCount = tg.activeCount();
		Thread[] activeThreads = new Thread[activeThreadsCount];
		int n = tg.enumerate(activeThreads);
		for (int i=0; i<n; i++) {
			activeThreads[i].interrupt();
		}
	}
}

/*
 * Created on Jun 18, 2003
 */
package org.eclipse.update.internal.core;


/**
 * MultiDownloadManager.
 * A helper class that manages the number of threads to be used for parallel downloading.
 */
public class MultiDownloadManager {
	private final static int MAX_NUM_THREADS = 5; // hint for max threads, there may be more
	private final static int WAIT_INTERVAL = 1000; // milliseconds
	private static int threadsInUse = 0;
	
	public static synchronized void releaseThread(Thread t){
		threadsInUse--;
	}
	
	public static Thread getThread(Runnable r, String threadName, ThreadGroup tg) {
		try {
			// attempt to keep the number of download threads to less than 5
			// but because of the synchronization there could be more
			while(threadsInUse >= MAX_NUM_THREADS)
				Thread.sleep(WAIT_INTERVAL);
		} catch (InterruptedException e) {
		}
		synchronized (MultiDownloadManager.class){
			threadsInUse++;
		}
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

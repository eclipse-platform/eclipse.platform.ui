package org.eclipse.core.tests.runtime.locks;

import java.util.Random;

import org.eclipse.core.runtime.jobs.ILock;

public class TestRunnable implements Runnable {
	private ILock[] locks;
	private Random random = new Random();
	private boolean alive;
	/**
	 * This runnable will randomly acquire the given lock for
	 * random periods of time, in the given order
	 */
	public TestRunnable(ILock[] locks) {
		this.locks = locks;
		this.alive = true;
	}
	public void kill() {
		alive = false;
	}
	public void run() {
		while (alive) {
			try {
				Thread.sleep(random.nextInt(500));
			} catch (InterruptedException e) {
			}
			for (int i = 0; i < locks.length; i++) {
				locks[i].acquire();
				try {
					Thread.sleep(random.nextInt(500));
				} catch (InterruptedException e1) {
				}
			}
			//release all locks
			for (int i = locks.length; --i >= 0;) {
				locks[i].release();
			}
		}
	}
}

package org.eclipse.core.internal.utils;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class Semaphore {
	protected long notifications;
	protected Runnable runnable;
public Semaphore(Runnable runnable) {
	this.runnable = runnable;
	notifications = 0;
}
public synchronized void acquire() throws InterruptedException {
	if (Thread.interrupted())
		throw new InterruptedException();
	while (notifications <= 0)
		wait();
	notifications--;
}
public boolean equals(Object obj) {
	return (runnable == ((Semaphore) obj).runnable);
}
public Runnable getRunnable() {
	return runnable;
}
public int hashCode() {
	return runnable.hashCode();
}
public synchronized void release() {
	notifications++;
	notifyAll();
}
// for debug only
public String toString() {
	return runnable.toString();
}
}

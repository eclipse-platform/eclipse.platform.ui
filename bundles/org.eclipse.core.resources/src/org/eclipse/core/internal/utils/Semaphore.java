/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.utils;

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

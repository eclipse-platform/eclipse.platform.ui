/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

/**
 * Monitor ensuring no more than one writer working concurrently.
 * Multiple readers are allowed to perform simultaneously.
 * 
 * This class was borrowed from org.eclipse.jdt.internal.core.search.indexing. 
 */
public class ReadWriteMonitor {

	/**
	 * <0 : writing (cannot go beyond -1, i.e one concurrent writer)
	 * =0 : idle
	 * >0 : reading (number of concurrent readers)
	 */
	private int status = 0;

	private Thread writeLockowner;

	/**
	 * Concurrent reading is allowed
	 * Blocking only when already writing.
	 */
	public synchronized void enterRead() {
		if (writeLockowner == Thread.currentThread())
			return;
		while (status < 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				// ignore
			}
		}
		status++;
	}

	/**
	 * Only one writer at a time is allowed to perform
	 * Blocking only when already writing or reading.
	 */
	public synchronized void enterWrite() {
		if (writeLockowner != Thread.currentThread()) {
			while (status != 0) {
				try {
					wait();
				} catch (InterruptedException e) {
					// ignore
				}
			}
//			System.out.println(this + "lockowner:" + Thread.currentThread());
			writeLockowner = Thread.currentThread();
		}
		status--;
	}

	/**
	 * Only notify waiting writer(s) if last reader
	 */
	public synchronized void exitRead() {
		if (writeLockowner == Thread.currentThread())
			return;
		if (--status == 0)
			notifyAll();
	}

	/**
	 * When writing is over, all readers and possible
	 * writers are granted permission to restart concurrently
	 */
	public synchronized void exitWrite() {
		if (writeLockowner != Thread.currentThread())
			throw new IllegalStateException("Current owner is " + writeLockowner); //$NON-NLS-1$
		if (++status == 0) {
//			System.out.println(this + "exitWrite:" + Thread.currentThread());
			writeLockowner = null;
			notifyAll();
		}
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.hashCode());
		if (status == 0) {
			buffer.append("Monitor idle "); //$NON-NLS-1$
		} else if (status < 0) {
			buffer.append("Monitor writing "); //$NON-NLS-1$
		} else if (status > 0) {
			buffer.append("Monitor reading "); //$NON-NLS-1$
		}
		buffer.append("(status = "); //$NON-NLS-1$
		buffer.append(this.status);
		buffer.append(")"); //$NON-NLS-1$
		return buffer.toString();
	}
}

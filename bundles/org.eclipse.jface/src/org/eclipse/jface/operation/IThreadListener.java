/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.operation;

/**
 * A thread listener is an object that is interested in notification of thread changes.
 * An example usage of thread listeners is to notify a runnable of the thread that
 * it will execute it, allowing it to transfer thread-local state from the calling
 * thread before control passes to the new thread.
 * 
 * @since 3.1
 */
public interface IThreadListener {
	/**
	 * Notification that a thread change is occurring.
	 * 
	 * @param thread The new thread
	 */
	public void threadChange(Thread thread);
}

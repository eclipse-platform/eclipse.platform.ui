/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.work;

/**
 * This class is experimental and represents a work in progress.
 */
public abstract class WorkScheduler {

	/**
	 * Schedule the given runnable to run in the background
	 * 
	 * @param <T>
	 * @param taskName
	 * @param r
	 * @param flags
	 * @param delay
	 * @return
	 */
	public abstract <T> AsyncFuture<T> schedule(WorkRunnable<T> r, int flags, long delay);

}

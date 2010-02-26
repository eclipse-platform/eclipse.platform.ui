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
 * Runnable with a result, running in a WorkContext for reporting progress and responding to
 * cancellation.
 * 
 * This class is experimental and represents a work in progress.
 * 
 * @param <T>
 *            the result type
 */
public abstract class WorkRunnable<T> {

	/**
	 * Run this runnable in the given work context, returning a result.
	 * 
	 * @param workContext
	 * @return the result
	 */
	public abstract T run(WorkContext workContext);
}

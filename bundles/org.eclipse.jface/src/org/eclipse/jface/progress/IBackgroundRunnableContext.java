/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.progress;

import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * IBackgroundRunnableContext is the class used to run runnables
 * in the background with notification of completion to be sent
 * to the listener.
 */
public interface IBackgroundRunnableContext {
	/**
	* Runs the given <code>IBackgroundRunnableContext</code> in this context.
	*
	* @param runnable the runnable to run
	* @param completionListener. The listener to be updated when done.
	*
	* @exception InvocationTargetException wraps any exception or error which occurs 
	*  while running the runnable
	* @exception InterruptedException propagated by the context if the runnable 
	*  acknowledges cancelation by throwing this exception.  This should not be thrown
	*  if cancelable is <code>false</code>.
	*/
	public void run(
		IRunnableWithProgress runnable,
		IJobCompletionListener completionListener);

}

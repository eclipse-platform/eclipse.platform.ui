/**********************************************************************
Copyright (c) 2000, 2004 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.filebuffers;

/**
 * Executes <code>Runnables</code> according to a certain synchronization
 * policy. This could be that all given <code>Runnables</code> are executed in
 * a specific thread or environment.
 * 
 * @since 3.0
 */
public interface ISynchronizationContext {
	
	/**
	 * Executes the given runnable.
	 * 
	 * @param runnable the runnable to be executed
	 */
	void run(Runnable runnable);
}

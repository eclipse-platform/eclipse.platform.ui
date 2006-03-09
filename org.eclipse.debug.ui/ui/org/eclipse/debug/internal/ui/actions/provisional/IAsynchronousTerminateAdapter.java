/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.provisional;

import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;

/**
 * Adapter for terminating.
 * 
 * @since 3.2
 */
public interface IAsynchronousTerminateAdapter {

	/**
	 * Asynchronously determines whether the given element can be terminated.
	 *  
	 * @param element element
	 * @param monitor request monitor
	 */
	public void canTerminate(Object element, IBooleanRequestMonitor monitor);
	
	/**
	 * Asynchronously determines whether the given element is terminated.
	 *  
	 * @param element element
	 * @param monitor request monitor
	 */
	public void isTerminated(Object element, IBooleanRequestMonitor monitor);
	
	/**
	 * Asynchronously terminates the given elemnet.
	 *  
	 * @param element element
	 * @param monitor request monitor
	 */
	public void terminate(Object element, IAsynchronousRequestMonitor monitor);	
}

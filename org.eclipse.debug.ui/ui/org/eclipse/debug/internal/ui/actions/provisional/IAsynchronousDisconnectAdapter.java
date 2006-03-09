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
 * Adapter for disconnecting.
 * 
 * @since 3.2
 */
public interface IAsynchronousDisconnectAdapter {

	/**
	 * Asynchronously determines whether the given element can be disconnected.
	 *  
	 * @param element element
	 * @param monitor request monitor
	 */
	public void canDisconnect(Object element, IBooleanRequestMonitor monitor);
	
	/**
	 * Asynchronously determines whether the given element is disconnected.
	 *  
	 * @param element element
	 * @param monitor request monitor
	 */
	public void isDisconnected(Object element, IBooleanRequestMonitor monitor);
	
	/**
	 * Asynchronously disconnects the given element.
	 *  
	 * @param element element
	 * @param monitor request monitor
	 */
	public void disconnect(Object element, IAsynchronousRequestMonitor monitor);	
}

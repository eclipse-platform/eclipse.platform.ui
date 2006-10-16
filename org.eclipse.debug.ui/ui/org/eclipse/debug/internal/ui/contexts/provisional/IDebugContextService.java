/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts.provisional;

import org.eclipse.jface.viewers.ISelection;


/** 
 * Debug context service for a window.
 * 
 * @since 3.2
 */
public interface IDebugContextService {
	
	/**
	 * Registers for context activation notification in this service.
	 * 
	 * @param listener
	 */
	public void addDebugContextListener(IDebugContextListener listener);
	/**
	 * Unregisters for context activation notification in this service.
	 * 
	 * @param listener
	 */	
	public void removeDebugContextListener(IDebugContextListener listener);
	
	/**
	 * Registers for context activation notification in the specified part.
	 * 
	 * @param listener
	 * @param partId
	 */
	public void addDebugContextListener(IDebugContextListener listener, String partId);
	
	/**
	 * Unregisters for context activation notification in the specified part.
	 * 
	 * @param listener
	 * @param partId
	 */
	public void removeDebugContextListener(IDebugContextListener listener, String partId);
		
	/**
	 * Returns the active context in this service's window
	 * or <code>null</code>.
	 * 
	 * @return
	 */
	public ISelection getActiveContext();
	
	/**
	 * Returns the active context in the specified part or <code>null</code>.
	 * 
	 * @param partId
	 * @return
	 */
	public ISelection getActiveContext(String partId);
	
	/**
	 * Registers for post context notification. Post listeners
	 * are notified of context activation and change after all
	 * non-post listeners are notified.  
	 * 
	 * @param listener
	 * @since 3.3
	 */
	public void addPostDebugContextListener(IDebugContextListener listener);
	/**
	 * Unregisters for post context notification.
	 * 
	 * @param listener
	 * @since 3.3
	 */	
	public void removePostDebugContextListener(IDebugContextListener listener);
	
	/**
	 * Registers for post context notification in the specified part. Post listeners
	 * are notified of context activation and change after all
	 * non-post listeners are notified. 
	 * 
	 * @param listener
	 * @param partId
	 * @since 3.3
	 */
	public void addPostDebugContextListener(IDebugContextListener listener, String partId);
	
	/**
	 * Unregisters for post context notification in the specified part.
	 * 
	 * @param listener
	 * @param partId
	 * @since 3.3
	 */
	public void removePostDebugContextListener(IDebugContextListener listener, String partId);	
	
}

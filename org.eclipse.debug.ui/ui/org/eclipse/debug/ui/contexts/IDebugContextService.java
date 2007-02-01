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
package org.eclipse.debug.ui.contexts;

import org.eclipse.jface.viewers.ISelection;


/** 
 * Debug context service for a window. Clients may register for debug context
 * notification with this service. A context service is obtained from the
 * debug context manager.
 * <p>
 * Not intended to be implemented by clients.
 * </p>
 * @see IDebugContextManager
 * @since 3.3
 */
public interface IDebugContextService {
	
	/**
	 * Registers for the given listener for debug context change notification
	 * in this service's window.
	 * 
	 * @param listener debug context listener
	 */
	public void addDebugContextListener(IDebugContextListener listener);
	/**
	 * Unregisters for the given listener for debug context change notification
	 * in this service's window.
	 * 
	 * @param listener debug context listener
	 */	
	public void removeDebugContextListener(IDebugContextListener listener);
	
	/**
	 * Registers for the given debug context listener for context notification
	 * from the specified part in this service's window.
	 * 
	 * @param listener debug context listener
	 * @param partId part identifier
	 */
	public void addDebugContextListener(IDebugContextListener listener, String partId);
	
	/**
	 * Unregisters the given debug context listener for context change notification
	 * from the specified part in this service's window.
	 * 
	 * @param listener debug context listener
	 * @param partId part identifier
	 */
	public void removeDebugContextListener(IDebugContextListener listener, String partId);
		
	/**
	 * Returns the active context in this service's window
	 * or <code>null</code>.
	 * 
	 * @return active context or <code>null</code>
	 */
	public ISelection getActiveContext();
	
	/**
	 * Returns the active context in the specified part of this service's window
	 * or <code>null</code> if none.
	 * 
	 * @param partId part identifier
	 * @return active context or <code>null</code>
	 */
	public ISelection getActiveContext(String partId);
	
	/**
	 * Registers the given debug context listener for post context change notification
	 * in this service's window. Post listeners are notified of context changes after all
	 * non-post listeners are notified.  
	 * 
	 * @param listener debug context listener
	 */
	public void addPostDebugContextListener(IDebugContextListener listener);
	
	/**
	 * Unregisters the given debug context listener for post context change notification
	 * in this service's window.
	 * 
	 * @param listener debug context listener.
	 */	
	public void removePostDebugContextListener(IDebugContextListener listener);
	
	/**
	 * Registers the given debug context listener for post context change notification
	 * in the specified part of this service's window. Post listeners are notified of
	 * context changes after all non-post listeners are notified. 
	 * 
	 * @param listener debug context listener
	 * @param partId part identifier
	 */
	public void addPostDebugContextListener(IDebugContextListener listener, String partId);
	
	/**
	 * Unregisters the given debug context listener for post context change notification
	 * in the specified part of this service's window.
	 * 
	 * @param listener debug context listener
	 * @param partId part identifier
	 */
	public void removePostDebugContextListener(IDebugContextListener listener, String partId);
	
	/**
	 * Registers the given debug context provider with this service.
	 * 
	 * @param provider debug context provider
	 */
	public void addDebugContextProvider(IDebugContextProvider provider);
	
	/**
	 * Unregisters the given debug context provider from this service.
	 * 
	 * @param provider debug context provider
	 */
	public void removeDebugContextProvider(IDebugContextProvider provider);
	
}

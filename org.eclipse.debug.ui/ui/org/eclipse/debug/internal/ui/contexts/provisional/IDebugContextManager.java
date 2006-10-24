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

import org.eclipse.ui.IWorkbenchWindow;

/**
 * Manages debug context services. There is a debug context service
 * for each workbench window. Clients interested in context change
 * notification for all windows can register with the manager. 
 * <p>
 * Clients may register debug context providers with the manager.
 * </p>
 * <p>
 * Not intended to be implemented by clients.
 * </p> 
 * @since 3.3
 */
public interface IDebugContextManager {
	
	/**
	 * Registers the given debug context provider.
	 * 
	 * @param provider
	 */
	public void addDebugContextProvider(IDebugContextProvider provider);
	
	/**
	 * Unregisters the given debug context provider.
	 * 
	 * @param provider
	 */
	public void removeDebugContextProvider(IDebugContextProvider provider);		
	
	/**
	 * Registers for context activation notification in all windows.
	 * 
	 * @param listener
	 */	
	public void addDebugContextListener(IDebugContextListener listener);
	
	/**
	 * Unregisters for context activation notification in all windows.
	 * 
	 * @param listener
	 */	
	public void removeDebugContextListener(IDebugContextListener listener);
	
	/**
	 * Returns the context service for the specified window.
	 * 
	 * @param window
	 * @return context service
	 * @since 3.3
	 */
	public IDebugContextService getContextService(IWorkbenchWindow window);
}

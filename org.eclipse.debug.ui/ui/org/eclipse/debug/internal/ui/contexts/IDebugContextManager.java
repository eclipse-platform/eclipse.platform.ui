/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * A debug context drives debugging - source lookup and action enablement in the 
 * debug user interface. The context service provides notification
 * of changes in the active context specific to the workbench, a specific window, or a
 * specific part.
 * <p>
 * Cleints provide a context policy to notifiy the context service of interesting
 * contexts within a model. For example the debug platform provides a context policy
 * that maps debug events to suspended contexts.
 * </p>
 * <p>
 * Not intended to be implemented by clients.
 * </p> 
 * @since 3.2
 */
public interface IDebugContextManager {
	
	/**
	 * Registers the given debug context provider.
	 * 
	 * @param provider
	 */
	public void addDebugContextProvider(IDebugContextProvider provider);
	
	/**
	 * Deregisters the given debug context provider.
	 * 
	 * @param provider
	 */
	public void removeDebugContextProvider(IDebugContextProvider provider);	
	
	/**
	 * Registers for context activation notification in the given window.
	 * 
	 * @param listener
	 * @param window
	 */
	public void addDebugContextListener(IDebugContextListener listener, IWorkbenchWindow window);
	/**
	 * Deregisters for context activation notification in this service in the
	 * given window.
	 * 
	 * @param listener
	 * @param window
	 */	
	public void removeDebugContextListener(IDebugContextListener listener, IWorkbenchWindow window);
	
	/**
	 * Registers for context activation notification in the specified part of the
	 * specified window.
	 * 
	 * @param listener
	 * @param window
	 * @param partId
	 */
	public void addDebugContextListener(IDebugContextListener listener, IWorkbenchWindow window, String partId);
	
	/**
	 * Deregisters for context activation notification in the specified part of
	 * the specified window.
	 * 
	 * @param listener
	 * @param partId
	 */
	public void removeDebugContextListener(IDebugContextListener listener, IWorkbenchWindow window, String partId);
		
	/**
	 * Returns the active context in the given window
	 * or <code>null</code>.
	 * 
	 * @param window
	 * @return
	 */
	public ISelection getActiveContext(IWorkbenchWindow window);
	
	/**
	 * Returns the active context in the specified part of the given 
	 * window or <code>null</code>.
	 * 
	 * @param partId
	 * @return
	 */
	public ISelection getActiveContext(IWorkbenchWindow window, String partId);	
}

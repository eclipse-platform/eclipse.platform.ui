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
	 * Deregisters for context activation notification in this service.
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
	 * Deregisters for context activation notification in the specified part.
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
	
}

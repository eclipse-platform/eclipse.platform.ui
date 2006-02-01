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
package org.eclipse.debug.internal.ui.contexts.provisional;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Provides debug context context information for a part.
 * Provides change notification as the active context is changed,
 * or as the active context changes state. 
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 3.2
 */
public interface IDebugContextProvider {
	
	/**
	 * Returns the part associated with this provider.
	 * 
	 * @return the part for which active context information is being provided
	 */
	public IWorkbenchPart getPart();
	
    /**
     * Registers the given listener for context notifications.
     * 
     * @param listener context listener
     */
	public void addDebugContextListener(IDebugContextListener listener);
    
    /**
     * Deregisters the given listener for context notifications.
     * 
     * @param listener context listener
     */
	public void removeDebugContextListener(IDebugContextListener listener);
	
    /**
     * Returns the currently active context, possibly empty or <code>null</code>.
     * 
     * @return active context, possibly empty or <code>null</code>.
     */
	public ISelection getActiveContext();

}

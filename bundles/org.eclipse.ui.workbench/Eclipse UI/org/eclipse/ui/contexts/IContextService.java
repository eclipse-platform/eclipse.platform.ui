/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.contexts;

/**
 * <p>
 * TODO javadoc
 * </p>
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IContextService {

	/**
	 * Registers an IContextServiceListener instance with this context service.
	 *
	 * @param contextServiceListener the IContextServiceListener instance to register.
	 */	
	void addContextServiceListener(IContextServiceListener contextServiceListener);

	/**
	 * Returns the active context ids.
	 *
	 * @return the active context ids.
	 */
	String[] getActiveContextIds();

	/**
	 * Unregisters an IContextServiceListener instance with this context service.
	 *
	 * @param contextServiceListener the IContextServiceListener instance to unregister.
	 */
	void removeContextServiceListener(IContextServiceListener contextServiceListener);
	
	/**
	 * Sets the active context ids.
	 *
	 * @param activeContextIds the active context ids.
	 * @throws IllegalArgumentException
	 */	
	void setActiveContextIds(String[] activeContextIds)
		throws IllegalArgumentException;
}

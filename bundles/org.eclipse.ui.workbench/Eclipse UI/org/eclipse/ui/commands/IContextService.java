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

package org.eclipse.ui.commands;

import java.util.List;

/**
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * @since 3.0
 */
public interface IContextService {

	/**
	 * Registers an IContextServiceListener instance with this context service.
	 *
	 * @param ids the IContextServiceListener instance to register.
	 */	
	void addContextServiceListener(IContextServiceListener contextServiceListener);

	/**
	 * Unregisters an IContextServiceListener instance with this context service.
	 *
	 * @param ids the IContextServiceListener instance to unregister.
	 */
	void removeContextServiceListener(IContextServiceListener contextServiceListener);

	/**
	 * Returns the context ids.
	 *
	 * @return the context ids.
	 */
	List getContexts();
	
	/**
	 * Sets the context ids.
	 *
	 * @param ids the context ids.
	 */	
	void setContexts(List contextIds)
		throws IllegalArgumentException;
}

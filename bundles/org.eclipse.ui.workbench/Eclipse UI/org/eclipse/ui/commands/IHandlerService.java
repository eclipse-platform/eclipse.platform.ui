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

import java.util.SortedMap;

/**
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IHandlerService {
	
	/**
	 * Registers an IHandlerServiceListener instance with this handler service.
	 *
	 * @param ids the IHandlerServiceListener instance to register.
	 */	
	void addHandlerServiceListener(IHandlerServiceListener handlerServiceListener);

	/**
	 * Unregisters an IHandlerServiceListener instance with this handler service.
	 *
	 * @param ids the IHandlerServiceListener instance to unregister.
	 */
	void removeHandlerServiceListener(IHandlerServiceListener handlerServiceListener);
	
	/**
	 * Returns the mapping of command ids to IHandler instances.
	 * 
	 * @return the mapping of command ids to IHandler instances.
	 */
	SortedMap getHandlerMap();
	
	/**
	 * Sets the mapping of command ids to IHandler instances.
	 *
	 * @param ids the mapping of command ids to IHandler instances.
	 */	
	void setHandlerMap(SortedMap handlerMap)
		throws IllegalArgumentException;
}

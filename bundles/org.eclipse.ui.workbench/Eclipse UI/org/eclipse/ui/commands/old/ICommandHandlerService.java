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

package org.eclipse.ui.commands.old;

import java.util.SortedMap;

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
public interface ICommandHandlerService {
	
	/**
	 * Registers an IHandlerServiceListener instance with this handler service.
	 *
	 * @param handlerServiceListener the IHandlerServiceListener instance to register.
	 */	
	void addHandlerServiceListener(ICommandHandlerServiceListener handlerServiceListener);

	/**
	 * Returns the mapping of command ids to IHandler instances.
	 * 
	 * @return the mapping of command ids to IHandler instances.
	 */
	SortedMap getHandlerMap();

	/**
	 * Unregisters an IHandlerServiceListener instance with this handler service.
	 *
	 * @param handlerServiceListener the IHandlerServiceListener instance to unregister.
	 */
	void removeHandlerServiceListener(ICommandHandlerServiceListener handlerServiceListener);
		
	/**
	 * Sets the mapping of command ids to IHandler instances.
	 *
	 * @param handlerMap the mapping of command ids to IHandler instances.
	 * @throws IllegalArgumentException
	 */	
	void setHandlerMap(SortedMap handlerMap)
		throws IllegalArgumentException;
}

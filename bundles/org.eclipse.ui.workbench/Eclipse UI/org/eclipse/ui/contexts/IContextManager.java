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

import java.util.SortedSet;

/**
 * <p>
 * JAVADOC
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IContextManager {

	/**
	 * Registers an IContextManagerListener instance with this context manager.
	 *
	 * @param contextManagerListener the IContextManagerListener instance to register.
	 * @throws IllegalArgumentException
	 */	
	void addContextManagerListener(IContextManagerListener contextManagerListener)
		throws IllegalArgumentException;

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedSet getActiveContextIds();

	/**
	 * JAVADOC
	 *
	 * @param contextId
	 * @return
	 * @throws IllegalArgumentException
	 */	
	IContext getContext(String contextId)
		throws IllegalArgumentException;

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedSet getDefinedContextIds();
	
	/**
	 * Unregisters an IContextManagerListener instance with this context manager.
	 *
	 * @param contextManagerListener the IContextManagerListener instance to unregister.
	 * @throws IllegalArgumentException
	 */
	void removeContextManagerListener(IContextManagerListener contextManagerListener)
		throws IllegalArgumentException;
}

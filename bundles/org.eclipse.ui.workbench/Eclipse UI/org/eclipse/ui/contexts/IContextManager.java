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
public interface IContextManager {

	/**
	 * Registers an IContextManagerListener instance with this context manager.
	 *
	 * @param contextManagerListener the IContextManagerListener instance to register.
	 */	
	void addContextManagerListener(IContextManagerListener contextManagerListener);

	/**
	 * TODO javadoc
	 *
	 * @param contextId
	 * @return
	 */	
	IContext getContext(String contextId);

	/**
	 * TODO javadoc
	 *
	 * @return
	 */
	String[] getContextIds();
	
	/**
	 * Unregisters an IContextManagerListener instance with this context manager.
	 *
	 * @param contextManagerListener the IContextManagerListener instance to unregister.
	 */
	void removeContextManagerListener(IContextManagerListener contextManagerListener);
}

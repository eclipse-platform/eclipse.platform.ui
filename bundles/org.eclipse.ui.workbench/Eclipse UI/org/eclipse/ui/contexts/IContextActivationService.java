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
 * TODO javadoc
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
public interface IContextActivationService {

	/**
	 * TODO javadoc
	 *
	 * @param contextId
	 * @throws IllegalArgumentException
	 */	
	void activateContext(String contextId)
		throws IllegalArgumentException;

	/**
	 * Registers an IContextActivationServiceListener instance with this context activation service.
	 *
	 * @param contextActivationServiceListener the IContextActivationServiceListener instance to register.
	 * @throws IllegalArgumentException
	 */	
	void addContextActivationServiceListener(IContextActivationServiceListener contextActivationServiceListener)
		throws IllegalArgumentException;

	/**
	 * TODO javadoc
	 *
	 * @param contextId
	 * @throws IllegalArgumentException
	 */	
	void deactivateContext(String contextId)
		throws IllegalArgumentException;
		
	/**
	 * TODO javadoc
	 *
	 * @return
	 */
	SortedSet getActiveContextIds();
	
	/**
	 * Unregisters an IContextActivationServiceListener instance with this context activation services.
	 *
	 * @param contextActivationServiceListener the IContextActivationServiceListener instance to unregister.
	 * @throws IllegalArgumentException
	 */
	void removeContextActivationServiceListener(IContextActivationServiceListener contextActivationServiceListener)
		throws IllegalArgumentException;
}

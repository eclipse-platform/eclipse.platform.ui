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
 * An instance of this interface allows clients to manage context activation.
 * <p>
 * The list of active contexts in this instance is the union of the lists of
 * active contexts in all instances of <code>IContextActivationService</code>
 * added via the method <code>addContextActivationService</code>.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see ContextActivationServiceFactory
 */
public interface ICompoundContextActivationService
	extends IContextActivationService {

	/**
	 * Adds an instance of <code>IContextActivationService</code> to this
	 * instance.
	 * 
	 * @param contextActivationService
	 *            the instance to add to this instance.
	 */
	void addContextActivationService(IContextActivationService contextActivationService);

	/**
	 * Removes an instance of <code>IContextActivationService</code> from
	 * this instance.
	 * 
	 * @param contextActivationService
	 *            the instance to remove from this instance.
	 */
	void removeContextActivationService(IContextActivationService contextActivationService);
}
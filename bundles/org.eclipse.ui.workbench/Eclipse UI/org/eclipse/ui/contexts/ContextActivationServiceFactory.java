/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.contexts;

import org.eclipse.ui.internal.contexts.CompoundContextActivationService;
import org.eclipse.ui.internal.contexts.MutableContextActivationService;

/**
 * This class allows clients to broker instances of <code>IContextActivationService</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class ContextActivationServiceFactory {

	/**
	 * Creates a new instance of <code>ICompoundContextActivationService</code>.
	 * 
	 * @return a new instance of <code>ICompoundContextActivationService</code>.
	 *         Clients should not make assumptions about the concrete
	 *         implementation outside the contract of the interface. Guaranteed
	 *         not to be <code>null</code>.
	 */
	public static ICompoundContextActivationService getCompoundContextActivationService() {
		return new CompoundContextActivationService();
	}

	/**
	 * Creates a new instance of <code>IMutableContextActivationService</code>.
	 * 
	 * @return a new instance of <code>IMutableContextActivationService</code>.
	 *         Clients should not make assumptions about the concrete
	 *         implementation outside the contract of the interface. Guaranteed
	 *         not to be <code>null</code>.
	 */
	public static IMutableContextActivationService getMutableContextActivationService() {
		return new MutableContextActivationService();
	}

	private ContextActivationServiceFactory() {
	}
}

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

package org.eclipse.ui.contexts.activationservice;

import org.eclipse.ui.internal.contexts.activationservice.CompoundContextActivationService;
import org.eclipse.ui.internal.contexts.activationservice.MutableContextActivationService;

/**
 * <p>
 * This class allows clients to broker instances of <code>IContextActivationService</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IContextActivationService
 */
public final class ContextActivationServiceFactory {

	/**
	 * Creates a new instance of ICompoundContextActivationService.
	 * 
	 * @return a new instance of ICompoundContextActivationService. Clients should not
	 *         make assumptions about the concrete implementation outside the
	 *         contract of <code>ICompoundContextActivationService</code>. Guaranteed
	 *         not to be <code>null</code>.
	 */
	public static ICompoundContextActiviationService getCompoundContextActivationService() {
		return new CompoundContextActivationService();
	}

	/**
	 * Creates a new instance of IMutableContextActivationService.
	 * 
	 * @return a new instance of IMutableContextActivationService. Clients should not
	 *         make assumptions about the concrete implementation outside the
	 *         contract of <code>IMutableContextActivationService</code>. Guaranteed
	 *         not to be <code>null</code>.
	 */
	public static IMutableContextActivationService getMutableContextActivationService() {
		return new MutableContextActivationService();
	}

	private ContextActivationServiceFactory() {
	}
}

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

package org.eclipse.ui.activities;

import org.eclipse.ui.internal.activities.ActivationService;

/**
 * <p>
 * This class allows clients to broker instances of 
 * <code>IActivationService</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IActivationService
 */
public final class ActivationServiceFactory {

	/**
	 * Creates a new instance of IActivationService.
	 * 
	 * @return a new instance of IActivationService. Clients should not make 
	 *         assumptions about the concrete implementation outside the 
	 *         contract of <code>IActivationService</code>. Guaranteed not to be 
	 *         <code>null</code>.
	 */
	public static IActivationService getActivationService() {
		return new ActivationService();
	}

	/**
	 * Private constructor to ensure that <code>ActivationServiceFactory</code> 
	 * can not be instantiated. 
	 */	
	private ActivationServiceFactory() {		
	}
}

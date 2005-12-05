/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

/**
 * 
 * An extension activation listener is notified whenever the activation state
 * changese for one or more content extensions.
 * 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * @see NavigatorActivationService
 * @see NavigatorActivationService#addExtensionActivationListener(String,
 *      IExtensionActivationListener)
 * @see NavigatorActivationService#removeExtensionActivationListener(String,
 *      IExtensionActivationListener)
 */
public interface IExtensionActivationListener {
	/**
	 * @param aViewerId
	 *            The viewer id of the INavigatorContentService
	 * @param theNavigatorExtensionIds
	 *            An array of updated extension ids
	 * @param isActive
	 *            The new activation state of the extensions
	 */
	void onExtensionActivation(String aViewerId,
			String[] theNavigatorExtensionIds, boolean isActive);
}

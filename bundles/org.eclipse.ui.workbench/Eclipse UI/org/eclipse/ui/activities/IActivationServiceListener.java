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

package org.eclipse.ui.activities;

/**
 * <p>
 * An instance of <code>IActivationServiceListener</code> can be used by clients 
 * to receive notification of changes to one or more instances of 
 * <code>IActivationService</code>.
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IActivationService#addRoleManagerListener
 * @see IActivationService#removeRoleManagerListener
 * @see IActivationServiceEvent
 */
public interface IActivationServiceListener {

	/**
	 * Notifies that one or more attributes of an instance of 
	 * <code>IActivationService</code> have changed. Specific details are 
	 * described in the <code>IActivationServiceEvent</code>.
	 *
	 * @param activationServiceEvent the activity service event. Guaranteed not to 
	 * 							   be <code>null</code>.
	 */
	void activationServiceChanged(IActivationServiceEvent activationServiceEvent);
}

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
 * An instance of this interface can be used by clients to receive notification
 * of changes to one or more instances of <code>IContextActivationService</code>.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see IContextActivationService#addContextActivationServiceListener
 * @see IContextActivationService#removeContextActivationServiceListener
 */
public interface IContextActivationServiceListener {

	/**
	 * Notifies that one or more properties of an instance of <code>IContextActivationService</code>
	 * have changed. Specific details are described in the <code>ContextActivationServiceEvent</code>.
	 * 
	 * @param contextActivationServiceEvent
	 *            the context activation service event. Guaranteed not to be
	 *            <code>null</code>.
	 */
	void contextActivationServiceChanged(ContextActivationServiceEvent contextActivationServiceEvent);
}

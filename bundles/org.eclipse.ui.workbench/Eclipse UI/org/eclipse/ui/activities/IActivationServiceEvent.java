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
 * An instance of <code>IActivationServiceEvent</code> describes changes to an 
 * instance of <code>IActivationService</code>. 
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IActivationService
 * @see IActivationServiceListener#activationServiceChanged
 */
public interface IActivationServiceEvent {

	/**
	 * Returns the instance of <code>IActivationService</code> that has changed.
	 *
	 * @return the instance of <code>IActivationService</code> that has changed. 
	 * 		   Guaranteed not to be <code>null</code>.
	 */
	IActivationService getActivationService();
	
	/**
	 * TODO javadoc
	 */		
	boolean haveActiveActivityIdsChanged();
	
	/**
	 * TODO javadoc
	 */
	boolean isDisposed();
}

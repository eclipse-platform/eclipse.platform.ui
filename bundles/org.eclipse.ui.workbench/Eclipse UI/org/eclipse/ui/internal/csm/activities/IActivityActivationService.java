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

package org.eclipse.ui.internal.csm.activities;

import java.util.SortedSet;

/**
 * <p>
 * JAVADOC
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
public interface IActivityActivationService {

	/**
	 * JAVADOC
	 *
	 * @param activityId
	 */	
	void activateActivity(String activityId);

	/**
	 * Registers an IActivationServiceListener instance with this activity activation service.
	 *
	 * @param activityActivationServiceListener the IActivationServiceListener instance to register.
	 */	
	void addActivityActivationServiceListener(IActivityActivationServiceListener activityActivationServiceListener);

	/**
	 * JAVADOC
	 *
	 * @param activityId
	 */	
	void deactivateActivity(String activityId);
		
	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedSet getActiveActivityIds();
	
	/**
	 * Unregisters an IActivationServiceListener instance with this activity activation services.
	 *
	 * @param activityActivationServiceListener the IActivationServiceListener instance to unregister.
	 */
	void removeActivityActivationServiceListener(IActivityActivationServiceListener activityActivationServiceListener);
}

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

package org.eclipse.ui.internal.csm.activities.api;

import java.util.Set;
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
public interface IActivityManager {

	/**
	 * Registers an IActivityManagerListener instance with this activity manager.
	 *
	 * @param activityManagerListener the IActivityManagerListener instance to register.
	 */	
	void addActivityManagerListener(IActivityManagerListener activityManagerListener);

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	Set getActiveActivityIds();

	/**
	 * JAVADOC
	 *
	 * @param activityId
	 * @return
	 */	
	IActivity getActivity(String activityId);

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedSet getDefinedActivityIds();
	
	/**
	 * Unregisters an IActivityManagerListener instance with this activity manager.
	 *
	 * @param activityManagerListener the IActivityManagerListener instance to unregister.
	 */
	void removeActivityManagerListener(IActivityManagerListener activityManagerListener);
	
	/**
	 * JAVADOC
	 *
	 * @param activeActivityIds
	 */
	void setActiveActivityIds(Set activeActivityIds);	
}

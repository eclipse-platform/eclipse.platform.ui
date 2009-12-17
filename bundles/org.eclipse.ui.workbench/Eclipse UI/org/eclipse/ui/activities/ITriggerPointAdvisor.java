/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

import java.util.Set;

/**
 * The trigger point advisor is a mechanism provided by the workbench that is
 * consulted whenever code that is considered a trigger point is hit. It is the
 * role of the advisor to determine what, if any, activities should be enabled
 * as a consequence of this action. The advisor also has the option of vetoing
 * the operation.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * 
 * @since 3.1
 * @see org.eclipse.ui.activities.ITriggerPoint
 */
public interface ITriggerPointAdvisor {

	/**
	 * Answer whether the activities bound to the identifier should be enabled
	 * when triggered by the provided trigger point.
	 * 
	 * @param triggerPoint
	 *            the trigger point to test
	 * @param identifier
	 *            the identifier to test against the trigger point
	 * @return the set of activities that should be enabled if this the
	 *         contribution represented by this identifier is to be used. If
	 *         this is not <code>null</code>, the caller can proceed with
	 *         usage of the contribution provided that the collection of
	 *         activities is enabled. If this is <code>null</code>, the
	 *         caller should assume that the operation involving the
	 *         contribution should be aborted. If this method returns the empty
	 *         set then the operation can proceed without any changes to
	 *         activity enablement state. Please note that it is the callers
	 *         responsibility to ensure that the Set returned by this method is
	 *         actually enabled - after setting the enabled state of the
	 *         required activities the change should be verified by consulting
	 *         {@link IActivityManager#getEnabledActivityIds()}.
	 */
	Set allow(ITriggerPoint triggerPoint, IIdentifier identifier);

	/**
	 * Calculate the identifier's enabled state for a combination of activities
	 * with and without enabled when core expressions.
	 * 
	 * @param activityManager
	 *            the activity manager
	 * @param identifier
	 *            the identifier to update
	 * 
	 * @return <code>true</code> if this identifier should be enabled,
	 *         <code>false</code> otherwise
	 * @since 3.4
	 */
	boolean computeEnablement(IActivityManager activityManager, IIdentifier identifier);
}

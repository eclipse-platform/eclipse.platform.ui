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
 * An instance of this class describes changes to an instance of 
 * <code>IActivityManager</code>.  This class does not give details as to the 
 * specifics of a change, only that the given property on the source object has 
 * changed.
 * 
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see IActivityManagerListener#activityManagerChanged(ActivityManagerEvent)
 */
public final class ActivityManagerEvent {
	private IActivityManager activityManager;
	private boolean definedActivityIdsChanged;
	private boolean definedCategoryIdsChanged;
	private boolean enabledActivityIdsChanged;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param activityManager
	 *            the instance of the interface that changed.
	 * @param definedActivityIdsChanged
	 *            <code>true</code>, iff the definedActivityIds property changed.
	 * @param definedCategoryIdsChanged
	 *            <code>true</code>, iff the definedCategoryIds property changed.
	 * @param enabledActivityIdsChanged
	 *            <code>true</code>, iff the enabledActivityIds property changed.
	 */
	public ActivityManagerEvent(
		IActivityManager activityManager,
		boolean definedActivityIdsChanged,
		boolean definedCategoryIdsChanged,
		boolean enabledActivityIdsChanged) {
		if (activityManager == null)
			throw new NullPointerException();

		this.activityManager = activityManager;
		this.definedActivityIdsChanged = definedActivityIdsChanged;
		this.definedCategoryIdsChanged = definedCategoryIdsChanged;
		this.enabledActivityIdsChanged = enabledActivityIdsChanged;
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public IActivityManager getActivityManager() {
		return activityManager;
	}

	/**
	 * Returns whether or not the definedActivityIds property changed.
	 * 
	 * @return <code>true</code>, iff the definedActivityIds property changed.
	 */
	public boolean haveDefinedActivityIdsChanged() {
		return definedActivityIdsChanged;
	}

	/**
	 * Returns whether or not the definedCategoryIds property changed.
	 * 
	 * @return <code>true</code>, iff the definedCategoryIds property changed.
	 */
	public boolean haveDefinedCategoryIdsChanged() {
		return definedCategoryIdsChanged;
	}

	/**
	 * Returns whether or not the enabledActivityIds property changed.
	 * 
	 * @return <code>true</code>, iff the enabledActivityIds property changed.
	 */
	public boolean haveEnabledActivityIdsChanged() {
		return enabledActivityIdsChanged;
	}
}

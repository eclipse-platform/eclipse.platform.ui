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
 * An instance of <code>ActivityManagerEvent</code> describes changes to an
 * instance of <code>IActivityManager</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IActivityManager
 * @see IActivityManagerListener#activityManagerChanged
 */
public final class ActivityManagerEvent {

	private IActivityManager activityManager;
	private boolean definedActivityIdsChanged;
	private boolean definedCategoryIdsChanged;
	private boolean enabledActivityIdsChanged;
	private boolean enabledCategoryIdsChanged;

	/**
	 * TODO javadoc
	 * 
	 * @param activityManager
	 * @param definedActivityIdsChanged
	 * @param enabledActivityIdsChanged
	 */
	public ActivityManagerEvent(
		IActivityManager activityManager,
		boolean definedActivityIdsChanged,
		boolean enabledActivityIdsChanged,
		boolean definedCategoryIdsChanged,
		boolean enabledCategoryIdsChanged) {
		if (activityManager == null)
			throw new NullPointerException();

		this.activityManager = activityManager;
		this.definedActivityIdsChanged = definedActivityIdsChanged;
		this.enabledActivityIdsChanged = enabledActivityIdsChanged;
		this.definedCategoryIdsChanged = definedCategoryIdsChanged;
		this.enabledCategoryIdsChanged = enabledCategoryIdsChanged;
	}

	/**
	 * Returns the instance of <code>IActivityManager</code> that has
	 * changed.
	 * 
	 * @return the instance of <code>IActivityManager</code> that has
	 *         changed. Guaranteed not to be <code>null</code>.
	 */
	public IActivityManager getActivityManager() {
		return activityManager;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveDefinedActivityIdsChanged() {
		return definedActivityIdsChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveDefinedCategoryIdsChanged() {
		return definedCategoryIdsChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveEnabledActivityIdsChanged() {
		return enabledActivityIdsChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveEnabledCategoryIdsChanged() {
		return enabledCategoryIdsChanged;
	}
}

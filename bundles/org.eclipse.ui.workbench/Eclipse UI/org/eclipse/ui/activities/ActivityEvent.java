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
 * An instance of <code>ActivityEvent</code> describes changes to an instance
 * of <code>Activity</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IActivity
 * @see IActivityListener#activityChanged
 */
public final class ActivityEvent {
	private IActivity activity;
	private boolean activityActivityBindingsChanged;
	private boolean activityPatternBindingsChanged;
	private boolean definedChanged;
	private boolean descriptionChanged;
	private boolean enabledChanged;
	private boolean nameChanged;
	private boolean parentIdChanged;

	/**
	 * TODO javadoc
	 * 
	 * @param activity
	 * @param activityActivityBindingsChanged
	 * @param activityPatternBindingsChanged
	 * @param definedChanged
	 * @param descriptionChanged
	 * @param enabledChanged
	 * @param nameChanged
	 * @param parentIdChanged
	 */
	public ActivityEvent(
		IActivity activity,
		boolean activityActivityBindingsChanged,
		boolean activityPatternBindingsChanged,
		boolean definedChanged,
		boolean descriptionChanged,
		boolean enabledChanged,
		boolean nameChanged,
		boolean parentIdChanged) {
		if (activity == null)
			throw new NullPointerException();

		this.activity = activity;
		this.activityActivityBindingsChanged = activityActivityBindingsChanged;
		this.activityPatternBindingsChanged = activityPatternBindingsChanged;
		this.definedChanged = definedChanged;
		this.descriptionChanged = descriptionChanged;
		this.enabledChanged = enabledChanged;
		this.nameChanged = nameChanged;
		this.parentIdChanged = parentIdChanged;
	}

	/**
	 * Returns the instance of <code>IActivity</code> that has changed.
	 * 
	 * @return the instance of <code>IActivity</code> that has changed.
	 *         Guaranteed not to be <code>null</code>.
	 */
	public IActivity getActivity() {
		return activity;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasDefinedChanged() {
		return definedChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasDescriptionChanged() {
		return descriptionChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasEnabledChanged() {
		return enabledChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasNameChanged() {
		return nameChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasParentIdChanged() {
		return parentIdChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveActivityActivityBindingsChanged() {
		return activityActivityBindingsChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveActivityPatternBindingsChanged() {
		return activityPatternBindingsChanged;
	}
}

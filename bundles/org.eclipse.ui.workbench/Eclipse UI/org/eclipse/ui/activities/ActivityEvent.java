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
 * An instance of this class describes changes to an instance of <code>IActivity</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see IActivityListener#activityChanged
 */
public final class ActivityEvent {
	private IActivity activity;
	private boolean activityActivityBindingsChanged;
	private boolean activityPatternBindingsChanged;
	private boolean definedChanged;
	private boolean enabledChanged;
	private boolean nameChanged;
    private boolean descriptionChanged;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param activity
	 *            the instance of the interface that changed.
	 * @param activityActivityBindingsChanged
	 *            true, iff the activityActivityBindings property changed.
	 * @param activityPatternBindingsChanged
	 *            true, iff the activityPatternBindings property changed.
	 * @param definedChanged
	 *            true, iff the defined property changed.
	 * @param enabledChanged
	 *            true, iff the enabled property changed.
	 * @param nameChanged
	 *            true, iff the name property changed.
	 */
	public ActivityEvent(
		IActivity activity,
		boolean activityActivityBindingsChanged,
		boolean activityPatternBindingsChanged,
		boolean definedChanged,
		boolean enabledChanged,
		boolean nameChanged, 
		boolean descriptionChanged) {
		if (activity == null)
			throw new NullPointerException();

		this.activity = activity;
		this.activityActivityBindingsChanged = activityActivityBindingsChanged;
		this.activityPatternBindingsChanged = activityPatternBindingsChanged;
		this.definedChanged = definedChanged;
		this.enabledChanged = enabledChanged;
		this.nameChanged = nameChanged;
		this.descriptionChanged = descriptionChanged;
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public IActivity getActivity() {
		return activity;
	}

	/**
	 * Returns whether or not the defined property changed.
	 * 
	 * @return true, iff the defined property changed.
	 */
	public boolean hasDefinedChanged() {
		return definedChanged;
	}

	/**
	 * Returns whether or not the enabled property changed.
	 * 
	 * @return true, iff the enabled property changed.
	 */
	public boolean hasEnabledChanged() {
		return enabledChanged;
	}

	/**
	 * Returns whether or not the name property changed.
	 * 
	 * @return true, iff the name property changed.
	 */
	public boolean hasNameChanged() {
		return nameChanged;
	}
	
	/**
	 * Returns whether or not the description property changed.
	 * 
	 * @return true, iff the description property changed.
	 */
	public boolean hasDescriptionChanged() {
		return descriptionChanged;
	}
	
	/**
	 * Returns whether or not the activityActivityBindings property changed.
	 * 
	 * @return true, iff the activityActivityBindings property changed.
	 */
	public boolean haveActivityActivityBindingsChanged() {
		return activityActivityBindingsChanged;
	}

	/**
	 * Returns whether or not the activityPatternBindings property changed.
	 * 
	 * @return true, iff the activityPatternBindings property changed.
	 */
	public boolean haveActivityPatternBindingsChanged() {
		return activityPatternBindingsChanged;
	}
}

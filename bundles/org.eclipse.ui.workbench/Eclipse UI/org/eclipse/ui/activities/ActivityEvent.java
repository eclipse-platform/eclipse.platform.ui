/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.activities;

/**
 * An instance of this class describes changes to an instance of 
 * <code>IActivity</code>.  This class does not give details as to the 
 * specifics of a change, only that the given property on the source object has 
 * changed.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see IActivityListener#activityChanged(ActivityEvent)
 */
public final class ActivityEvent {
    private IActivity activity;

    private boolean activityRequirementBindingsChanged;

    private boolean activityPatternBindingsChanged;

    private boolean definedChanged;

    private boolean enabledChanged;
    
    private boolean defaultEnabledChanged;

    private boolean nameChanged;

    private boolean descriptionChanged;

    /**
     * Creates a new instance of this class. Since 3.1 this method will assume
     * that the default enabled state has not changed.
     * 
     * @param activity
     *            the instance of the interface that changed.
     * @param activityRequirementBindingsChanged
     *            <code>true</code>, iff the activityRequirementBindings
     *            property changed.
     * @param activityPatternBindingsChanged
     *            <code>true</code>, iff the activityPatternBindings property
     *            changed.
     * @param definedChanged
     *            <code>true</code>, iff the defined property changed.
     * @param descriptionChanged
     *            <code>true</code>, iff the description property changed.
     * @param enabledChanged
     *            <code>true</code>, iff the enabled property changed.
     * @param nameChanged
     *            <code>true</code>, iff the name property changed.
     */
    public ActivityEvent(IActivity activity,
            boolean activityRequirementBindingsChanged,
            boolean activityPatternBindingsChanged, boolean definedChanged,
            boolean descriptionChanged, boolean enabledChanged,
            boolean nameChanged) {
        
        this(activity, 
                activityRequirementBindingsChanged,
                activityPatternBindingsChanged,
                definedChanged,
                descriptionChanged,
                enabledChanged,
                nameChanged, 
                false);
    }

    /**
     * Creates a new instance of this class.
     * 
     * @param activity
     *        the instance of the interface that changed.
     * @param activityRequirementBindingsChanged
     *        <code>true</code>, iff the activityRequirementBindings property changed.
     * @param activityPatternBindingsChanged
     *        <code>true</code>, iff the activityPatternBindings property changed.
     * @param definedChanged
     *        <code>true</code>, iff the defined property changed.
     * @param descriptionChanged
     * 		  <code>true</code>, iff the description property changed.
     * @param enabledChanged
     *      <code>true</code>, iff the enabled property changed.
     * @param nameChanged
     *        <code>true</code>, iff the name property changed.
     * @param defaultEnabledChanged 
     * 		  <code>true</code>, iff the default enabled property changed.
     * @since 3.1
     */
    public ActivityEvent(IActivity activity,
            boolean activityRequirementBindingsChanged,
            boolean activityPatternBindingsChanged, boolean definedChanged,
            boolean descriptionChanged, boolean enabledChanged,
            boolean nameChanged,
            boolean defaultEnabledChanged) {
        if (activity == null) {
			throw new NullPointerException();
		}

        this.activity = activity;
        this.activityRequirementBindingsChanged = activityRequirementBindingsChanged;
        this.activityPatternBindingsChanged = activityPatternBindingsChanged;
        this.definedChanged = definedChanged;
        this.enabledChanged = enabledChanged;
        this.nameChanged = nameChanged;
        this.descriptionChanged = descriptionChanged;
        this.defaultEnabledChanged = defaultEnabledChanged;
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
     * @return <code>true</code>, iff the defined property changed.
     */
    public boolean hasDefinedChanged() {
        return definedChanged;
    }

    /**
     * Returns whether or not the enabled property changed.
     * 
     * @return <code>true</code>, iff the enabled property changed.
     */
    public boolean hasEnabledChanged() {
        return enabledChanged;
    }
    
    /**
     * Returns whether or not the default enabled property changed.
     * 
     * @return <code>true</code>, iff the default enabled property changed.
     * @since 3.1
     */
    public boolean hasDefaultEnabledChanged() {
        return defaultEnabledChanged;
    }

    /**
     * Returns whether or not the name property changed.
     * 
     * @return <code>true</code>, iff the name property changed.
     */
    public boolean hasNameChanged() {
        return nameChanged;
    }

    /**
     * Returns whether or not the description property changed.
     * 
     * @return <code>true</code>, iff the description property changed.
     */
    public boolean hasDescriptionChanged() {
        return descriptionChanged;
    }

    /**
     * Returns whether or not the activityRequirementBindings property changed.
     * 
     * @return <code>true</code>, iff the activityRequirementBindings property changed.
     */
    public boolean haveActivityRequirementBindingsChanged() {
        return activityRequirementBindingsChanged;
    }

    /**
     * Returns whether or not the activityPatternBindings property changed.
     * 
     * @return <code>true</code>, iff the activityPatternBindings property changed.
     */
    public boolean haveActivityPatternBindingsChanged() {
        return activityPatternBindingsChanged;
    }
}

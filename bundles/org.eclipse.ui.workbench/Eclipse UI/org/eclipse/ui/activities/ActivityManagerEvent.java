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

import java.util.Set;

import org.eclipse.ui.internal.util.Util;

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
     * The set of activity identifiers (strings) that were defined before the
     * change occurred. If the defined activities did not changed, then this
     * value is <code>null</code>.
     */
    private final Set previouslyDefinedActivityIds;

    /**
     * The set of category identifiers (strings) that were defined before the
     * change occurred. If the defined category did not changed, then this value
     * is <code>null</code>.
     */
    private final Set previouslyDefinedCategoryIds;

    /**
     * The set of activity identifiers (strings) that were enabled before the
     * change occurred. If the enabled activities did not changed, then this
     * value is <code>null</code>.
     */
    private final Set previouslyEnabledActivityIds;

    /**
     * Creates a new instance of this class.
     * 
     * @param activityManager
     *            the instance of the interface that changed.
     * @param definedActivityIdsChanged
     *            <code>true</code>, iff the definedActivityIds property
     *            changed.
     * @param definedCategoryIdsChanged
     *            <code>true</code>, iff the definedCategoryIds property
     *            changed.
     * @param enabledActivityIdsChanged
     *            <code>true</code>, iff the enabledActivityIds property
     *            changed.
     * @param previouslyDefinedActivityIds
     *            the set of identifiers to previously defined activities. This
     *            set may be empty. If this set is not empty, it must only
     *            contain instances of <code>String</code>. This set must be
     *            <code>null</code> if definedActivityIdsChanged is
     *            <code>false</code> and must not be null if
     *            definedActivityIdsChanged is <code>true</code>.
     * @param previouslyDefinedCategoryIds
     *            the set of identifiers to previously defined category. This
     *            set may be empty. If this set is not empty, it must only
     *            contain instances of <code>String</code>. This set must be
     *            <code>null</code> if definedCategoryIdsChanged is
     *            <code>false</code> and must not be null if
     *            definedCategoryIdsChanged is <code>true</code>.
     * @param previouslyEnabledActivityIds
     *            the set of identifiers to previously enabled activities. This
     *            set may be empty. If this set is not empty, it must only
     *            contain instances of <code>String</code>. This set must be
     *            <code>null</code> if enabledActivityIdsChanged is
     *            <code>false</code> and must not be null if
     *            enabledActivityIdsChanged is <code>true</code>.
     */
    public ActivityManagerEvent(IActivityManager activityManager,
            boolean definedActivityIdsChanged,
            boolean definedCategoryIdsChanged,
            boolean enabledActivityIdsChanged,
            final Set previouslyDefinedActivityIds,
            final Set previouslyDefinedCategoryIds,
            final Set previouslyEnabledActivityIds) {
        if (activityManager == null) {
			throw new NullPointerException();
		}

        if (!definedActivityIdsChanged && previouslyDefinedActivityIds != null) {
			throw new IllegalArgumentException();
		}

        if (!definedCategoryIdsChanged && previouslyDefinedCategoryIds != null) {
			throw new IllegalArgumentException();
		}

        if (!enabledActivityIdsChanged && previouslyEnabledActivityIds != null) {
			throw new IllegalArgumentException();
		}

        if (definedActivityIdsChanged) {
            this.previouslyDefinedActivityIds = Util.safeCopy(
                    previouslyDefinedActivityIds, String.class);
        } else {
            this.previouslyDefinedActivityIds = null;
        }

        if (definedCategoryIdsChanged) {
            this.previouslyDefinedCategoryIds = Util.safeCopy(
                    previouslyDefinedCategoryIds, String.class);
        } else {
            this.previouslyDefinedCategoryIds = null;
        }

        if (enabledActivityIdsChanged) {
            this.previouslyEnabledActivityIds = Util.safeCopy(
                    previouslyEnabledActivityIds, String.class);
        } else {
            this.previouslyEnabledActivityIds = null;
        }

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
     * Returns the activity identifiers that were previously defined.
     * 
     * @return The set of defined activity identifiers before the changed; may
     *         be empty, but never <code>null</code>. This set will only
     *         contain strings.
     */
    public final Set getPreviouslyDefinedActivityIds() {
        return previouslyDefinedActivityIds;
    }

    /**
     * Returns the category identifiers that were previously defined.
     * 
     * @return The set of defined category identifiers before the changed; may
     *         be empty, but never <code>null</code>. This set will only
     *         contain strings.
     */
    public final Set getPreviouslyDefinedCategoryIds() {
        return previouslyDefinedCategoryIds;
    }

    /**
     * Returns the activity identifiers that were previously enabled.
     * 
     * @return The set of enabled activity identifiers before the changed; may
     *         be empty, but never <code>null</code>. This set will only
     *         contain strings.
     */
    public final Set getPreviouslyEnabledActivityIds() {
        return previouslyEnabledActivityIds;
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

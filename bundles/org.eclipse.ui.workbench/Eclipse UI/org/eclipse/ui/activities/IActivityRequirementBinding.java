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
 * An instance of this interface represents a binding between two activities.
 * The relationship can be interpreted as 'activity needs requiredActivity to 
 * be enabled'.
 * Enablement of the activity requires enablement of the required activity. 
 * 
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see IActivity
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IActivityRequirementBinding extends Comparable {

    /**
     * Returns the identifier of the activity represented in this
     * binding. 
     * 
     * @return the identifier of the activity represented in this
     *         binding. Guaranteed not to be <code>null</code>.
     */
    String getActivityId();

    /**
     * Returns the identifier of the required activity represented in this
     * binding.  The enablement of the activity described by {@link #getActivityId()}
     * requires the enablement of this activity.
     * 
     * @return the identifier of the required activity represented in this
     *         binding. Guaranteed not to be <code>null</code>.
     */
    String getRequiredActivityId();
}

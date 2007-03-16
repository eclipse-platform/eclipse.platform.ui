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
 * An instance of this interface can be used by clients to receive notification
 * of changes to one or more instances of <code>IActivityManager</code>.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see IActivityManager#addActivityManagerListener(IActivityManagerListener)
 * @see IActivityManager#removeActivityManagerListener(IActivityManagerListener)
 */
public interface IActivityManagerListener {

    /**
     * Notifies that one or more properties of an instance of <code>IActivityManager</code>
     * have changed. Specific details are described in the <code>ActivityManagerEvent</code>.
     * 
     * @param activityManagerEvent
     *            the activity manager event. Guaranteed not to be <code>null</code>.
     */
    void activityManagerChanged(ActivityManagerEvent activityManagerEvent);
}

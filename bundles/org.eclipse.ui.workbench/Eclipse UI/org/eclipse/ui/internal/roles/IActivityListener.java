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
package org.eclipse.ui.internal.roles;

import java.util.EventListener;

/**
 * Listener that is notified when an Activity is changed.  This currently means
 * that the enablement of the Activity has been altered but this meaning could
 * be supplemented in the future.
 */
public interface IActivityListener extends EventListener {

    /**
     * Notify the listener of the given Event.
     * 
     * @param roleRegistryEvent
     */
    void activityChanged(ActivityEvent event);
}

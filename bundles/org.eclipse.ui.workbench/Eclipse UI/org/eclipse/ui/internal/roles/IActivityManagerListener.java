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
 * Interface for listeners that wish to listen to changes covering the entire
 * Activity system.  Will currently bind to RoleManager until we get a proper
 * ActivityManager (if we do).
 */
public interface IActivityManagerListener extends EventListener {


   /**
    * Notify the listener of the given Event.
    * 
    * @param roleRegistryEvent
    */
   void activityManagerChanged(ActivityManagerEvent event);
}

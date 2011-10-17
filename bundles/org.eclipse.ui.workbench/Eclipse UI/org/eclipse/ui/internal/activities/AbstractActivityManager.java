/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityManagerListener;

public abstract class AbstractActivityManager implements IActivityManager {
	private ListenerList activityManagerListeners;

    protected AbstractActivityManager() {
    }

    public void addActivityManagerListener(
            IActivityManagerListener activityManagerListener) {
        if (activityManagerListener == null) {
			throw new NullPointerException();
		}

        if (activityManagerListeners == null) {
			activityManagerListeners = new ListenerList();
		}

		activityManagerListeners.add(activityManagerListener);
    }

    protected void fireActivityManagerChanged(
            ActivityManagerEvent activityManagerEvent) {
        if (activityManagerEvent == null) {
			throw new NullPointerException();
		}

		if (activityManagerListeners != null) {
			Object[] listeners = activityManagerListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				((IActivityManagerListener) listeners[i])
						.activityManagerChanged(activityManagerEvent);
			}
		}
    }

    public void removeActivityManagerListener(
            IActivityManagerListener activityManagerListener) {
        if (activityManagerListener == null) {
			throw new NullPointerException();
		}

        if (activityManagerListeners != null) {
			activityManagerListeners.remove(activityManagerListener);
		}
    }
}

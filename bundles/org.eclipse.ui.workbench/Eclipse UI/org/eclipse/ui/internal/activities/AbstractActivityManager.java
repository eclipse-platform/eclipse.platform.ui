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

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityManagerListener;

public abstract class AbstractActivityManager implements IActivityManager {
    private List activityManagerListeners;

    protected AbstractActivityManager() {
    }

    public void addActivityManagerListener(
            IActivityManagerListener activityManagerListener) {
        if (activityManagerListener == null) {
			throw new NullPointerException();
		}

        if (activityManagerListeners == null) {
			activityManagerListeners = new ArrayList();
		}

        if (!activityManagerListeners.contains(activityManagerListener)) {
			activityManagerListeners.add(activityManagerListener);
		}
    }

    protected void fireActivityManagerChanged(
            ActivityManagerEvent activityManagerEvent) {
        if (activityManagerEvent == null) {
			throw new NullPointerException();
		}

        if (activityManagerListeners != null) {
			for (int i = 0; i < activityManagerListeners.size(); i++) {
				((IActivityManagerListener) activityManagerListeners.get(i))
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

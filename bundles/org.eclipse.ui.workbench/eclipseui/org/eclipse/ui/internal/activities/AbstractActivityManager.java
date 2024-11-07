/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	private ListenerList<IActivityManagerListener> activityManagerListeners;

	private boolean disposed;

	protected AbstractActivityManager() {
	}

	@Override
	public void addActivityManagerListener(IActivityManagerListener activityManagerListener) {
		if (activityManagerListener == null) {
			throw new NullPointerException();
		}

		if (activityManagerListeners == null) {
			activityManagerListeners = new ListenerList<>();
		}

		activityManagerListeners.add(activityManagerListener);
	}

	protected void fireActivityManagerChanged(ActivityManagerEvent activityManagerEvent) {
		if (activityManagerEvent == null) {
			throw new NullPointerException();
		}

		if (activityManagerListeners != null) {
			for (IActivityManagerListener listener : activityManagerListeners) {
				listener.activityManagerChanged(activityManagerEvent);
			}
		}
	}

	@Override
	public void removeActivityManagerListener(IActivityManagerListener activityManagerListener) {
		if (activityManagerListener == null) {
			throw new NullPointerException();
		}

		if (activityManagerListeners != null) {
			activityManagerListeners.remove(activityManagerListener);
		}
	}

	@Override
	public void dispose() {
		activityManagerListeners.clear();
		disposed = true;
	}

	protected boolean isDisposed() {
		return disposed;
	}

}

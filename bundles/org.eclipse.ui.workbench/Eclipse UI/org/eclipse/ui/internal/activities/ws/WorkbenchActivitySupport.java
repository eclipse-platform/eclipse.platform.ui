/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.IContributionManager;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.ActivityManagerFactory;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.activities.IMutableActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

import org.eclipse.ui.internal.WorkbenchWindow;

public class WorkbenchActivitySupport implements IWorkbenchActivitySupport {
	private IMutableActivityManager mutableActivityManager;

	public WorkbenchActivitySupport() {
		mutableActivityManager = ActivityManagerFactory.getMutableActivityManager();
		mutableActivityManager.addActivityManagerListener(new IActivityManagerListener() {

			private Set lastEnabled = new HashSet(mutableActivityManager.getEnabledActivityIds());

			/* (non-Javadoc)
			 * @see org.eclipse.ui.activities.IActivityManagerListener#activityManagerChanged(org.eclipse.ui.activities.ActivityManagerEvent)
			 */
			public void activityManagerChanged(ActivityManagerEvent activityManagerEvent) {
				Set activityIds = mutableActivityManager.getEnabledActivityIds();
				// only update the windows if we've not processed this new enablement state already.
				if (!activityIds.equals(lastEnabled)) {
					lastEnabled = new HashSet(activityIds);
					// refresh the managers on all windows.
					IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
					for (int i = 0; i < windows.length; i++) {
						if (windows[i] instanceof WorkbenchWindow) {
							WorkbenchWindow window = (WorkbenchWindow) windows[i];
							IContributionManager manager = window.getMenuBarManager();
							if (manager != null)
								manager.update(true);
							manager = window.getCoolBarManager();
							if (manager != null)
								manager.update(true);
							manager = window.getToolBarManager();
							if (manager != null)
								manager.update(true);
							manager = window.getStatusLineManager();
							if (manager != null)
								manager.update(true);
						}
					}
				}
			}
		});
	}

	public IActivityManager getActivityManager() {
		// TODO need to proxy this to prevent casts to IMutableActivityManager
		return mutableActivityManager;
	}

	public void setEnabledActivityIds(Set enabledActivityIds) {
		mutableActivityManager.setEnabledActivityIds(enabledActivityIds);
	}
}

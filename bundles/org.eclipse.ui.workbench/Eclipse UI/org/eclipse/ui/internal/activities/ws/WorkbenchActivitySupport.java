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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.ui.internal.activities.ProxyActivityManager;
import org.eclipse.ui.progress.WorkbenchJob;

public class WorkbenchActivitySupport implements IWorkbenchActivitySupport {
	private IMutableActivityManager mutableActivityManager;
	private ProxyActivityManager proxyActivityManager;

	public WorkbenchActivitySupport() {
		mutableActivityManager = ActivityManagerFactory.getMutableActivityManager();
		proxyActivityManager = new ProxyActivityManager(mutableActivityManager);
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
						    final WorkbenchWindow window = (WorkbenchWindow) windows[i];
						    WorkbenchJob job = new WorkbenchJob(window.getShell().getDisplay(), ActivityMessages.getString("ManagerJob")) { //$NON-NLS-1$

                                /* (non-Javadoc)
                                 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
                                 */
                                public IStatus runInUIThread(IProgressMonitor monitor) {
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

                                    return Status.OK_STATUS;
                                }
                            };
                            job.setSystem(true);
                            job.schedule();						    
						}
					}
				}
			}
		});
	}

	public IActivityManager getActivityManager() {
		return proxyActivityManager;
	}

	public void setEnabledActivityIds(Set enabledActivityIds) {
		mutableActivityManager.setEnabledActivityIds(enabledActivityIds);
	}
}

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

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.activities.IMutableActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.activities.ActivityManagerFactory;
import org.eclipse.ui.internal.activities.ProxyActivityManager;
import org.eclipse.ui.internal.misc.StatusUtil;

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
					
					// abort if the workbench isn't running
					if (!PlatformUI.isWorkbenchRunning())
						return;
					
					// refresh the managers on all windows.
					final IWorkbench workbench = PlatformUI.getWorkbench();					
					IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
					for (int i = 0; i < windows.length; i++) {
						if (windows[i] instanceof WorkbenchWindow) {
						    final WorkbenchWindow window = (WorkbenchWindow) windows[i];

						    final ProgressMonitorDialog dialog = new ProgressMonitorDialog(window.getShell());
						    
							final IRunnableWithProgress runnable = new IRunnableWithProgress() {
								
								/**
								 * When this operation should open a dialog
								 */
								private long openTime;
								
								/**
								 * Whether the dialog has been opened yet.
								 */
								private boolean dialogOpened = false;

								/* (non-Javadoc)
								 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
								 */
								public void run(IProgressMonitor monitor)
										throws InvocationTargetException,
										InterruptedException {
									
									openTime = System.currentTimeMillis() + workbench.getProgressService().getLongOperationTime();
									
		                        	//two work units - updating the window bars, and updating view bars
		                        	monitor.beginTask(ActivityMessages.getString("ManagerTask"), 2); //$NON-NLS-1$
		                        	
								    monitor.subTask(ActivityMessages.getString("ManagerWindowSubTask")); //$NON-NLS-1$
									
		                        	//update window managers...
									updateWindowBars(window);
									monitor.worked(1);
									
									monitor.subTask(ActivityMessages.getString("ManagerViewsSubTask")); //$NON-NLS-1$
									// update all of the (realized) views in all of the pages
									IWorkbenchPage [] pages = window.getPages();
									for (int j = 0; j < pages.length; j++) {
										IWorkbenchPage page = pages[j];
										IViewReference [] refs = page.getViewReferences();
										for (int k = 0; k < refs.length; k++) {
											IViewPart part = refs[k].getView(false);
											if (part != null) {
												updateViewBars(part);														
											}													
										}
									}	
									monitor.worked(1);
										
		        					monitor.done();
								}
								
								/**
								 * Update the managers on the given given view.
								 * 
								 * @param part the view to update
								 */
								private void updateViewBars(IViewPart part) {
									IViewSite viewSite = part.getViewSite();
									// check for badly behaving or badly initialized views
									if (viewSite == null)
										return;
									IActionBars bars = viewSite.getActionBars();
									IContributionManager manager = bars.getMenuManager();
									if (manager != null)
										updateManager(manager);
									manager = bars.getToolBarManager();
									if (manager != null)
										updateManager(manager);
									manager = bars.getStatusLineManager();
									if (manager != null)
										updateManager(manager);
								}

								/**
								 * Update the managers on the given window.
								 * 
								 * @param window the window to update
								 */
								private void updateWindowBars(final WorkbenchWindow window) {
									IContributionManager manager = window.getMenuBarManager();
									if (manager != null)
										updateManager(manager);
									manager = window.getCoolBarManager();
									if (manager != null)
										updateManager(manager);
									manager = window.getToolBarManager();
									if (manager != null)
										updateManager(manager);
									manager = window.getStatusLineManager();
									if (manager != null)
										updateManager(manager);
								}

								/**
								 * Update the given manager in the UI thread.
								 * This may also open the progress dialog if 
								 * the operation is taking too long.
								 * 
								 * @param manager the manager to update
								 */
								private void updateManager(final IContributionManager manager) {
									if (!dialogOpened 
											&& System.currentTimeMillis() > openTime) {
										dialog.open();
										dialogOpened = true;
									}
									
									manager.update(true);
								}
							};
							
							// don't open the dialog by default - that'll be
							// handled by the runnable if we take too long
							dialog.setOpenOnRun(false);
							// run this in the UI thread
							workbench.getDisplay().asyncExec(new Runnable() {
								
								/* (non-Javadoc)
								 * @see java.lang.Runnable#run()
								 */
								public void run() {
								    BusyIndicator.showWhile(workbench.getDisplay(), new Runnable() {								
										
								    	/* (non-Javadoc)
										 * @see java.lang.Runnable#run()
										 */
										public void run() {								
										    try {
										    	dialog.run(false, false, runnable);
											} catch (InvocationTargetException e) {
												log(e);
											} catch (InterruptedException e) {
												log(e);
											}																			    	
										}
								    });
								}
							});
						}
					}
				}
			}

			/**
			 * Logs an error message to the workbench log.
			 * 
			 * @param e the exception to log
			 */
			private void log(Exception e) {
				StatusUtil.newStatus(IStatus.ERROR, "Could not update contribution managers", e); //$NON-NLS-1$ 
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

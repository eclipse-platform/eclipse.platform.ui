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
package org.eclipse.ui.internal.ide;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdviser;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.UISynchronizer;
import org.eclipse.ui.internal.UIWorkspaceLock;
import org.eclipse.ui.internal.WorkbenchActionBuilder;
import org.eclipse.ui.internal.WorkbenchConfigurationInfo;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.model.WorkbenchAdapterBuilder;
import org.eclipse.update.core.SiteManager;

/**
 * IDE-specified workbench adviser which configures the workbench for use as
 * an IDE.
 * <p>
 * Note: This class replaces <code>org.eclipse.ui.internal.Workbench</code>.
 * </p>
 * 
 * @since 3.0
 */
class IDEWorkbenchAdviser extends WorkbenchAdviser {
	
	/**
	 * Special object for configuring the workbench.
	 */
	IWorkbenchConfigurer configurer;	
	
	/**
	 * Tracks whether we were autobuilding.
	 */
	private boolean autoBuild;

	/**
	 * Command line arguments.
	 */
	private String[] commandLineArgs;

	/**
	 * Table of action builders keyed by window
	 * (key type: <code>IWorkbenchWindow</code>;
	 *  value type: <code>WorkbenchActionBuilder</code>)
	 */
	private Map actionBuilders = new HashMap(1);
	
	/**
	 * Preference change listener
	 */
	private final IPropertyChangeListener preferenceChangeListener =
		new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				handlePreferenceChange(event);
			}
	};
	
	/**
	 * Cached about info. Lazily initialized by getConfigurationInfo().
	 *
	 * @return the about info
	 * @see #getConfigurationInfo
	 * @issue WorkbenchConfigurationInfo needs to be moved to this package
	 */
	private WorkbenchConfigurationInfo configurationInfo;
	
	/**
	 * Creates a new workbench adviser instance.
	 */
	IDEWorkbenchAdviser(String[] commandLineArgs) {
		super();
		this.commandLineArgs = commandLineArgs;
	}
	
	/**
	 * Returns the workbench.
	 * 
	 * @return the workbench
	 */
	IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#initialize
	 */
	public void initialize(IWorkbenchConfigurer configurer) {
		// remember for future reference
		this.configurer = configurer;		
		
		// retrieve feature and plug-in configuration info to display
		// in "about" dialogs
		boolean success = !getConfigurationInfo().readInfo();

		// establish the app name for the display
		String appName = getConfigurationInfo().getAboutInfo().getAppName();
		if (appName != null) {
			// @issue this may be too late - by the time initialize is called the Display has already been created
			Display.setAppName(appName);
		}
		
		// register workspace adapters
		WorkbenchAdapterBuilder.registerAdapters();
		
		// register resource change listener for showing the tasks view
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
			getShowTasksChangeListener(),
			IResourceChangeEvent.POST_CHANGE);

		// listen for changes to IDE-specific properties
		// @issue must sure this is the correct preference store
		getWorkbench().getPreferenceStore().addPropertyChangeListener(preferenceChangeListener);
		
		// anti-deadlocking code
		boolean avoidDeadlock = true;
		for (int i = 0; i < commandLineArgs.length; i++) {
			if (commandLineArgs[i].equalsIgnoreCase("-allowDeadlock")) //$NON-NLS-1$
				avoidDeadlock = false;
		}
		if (avoidDeadlock) {
			try {
				Display display = Display.getCurrent();
				// @issue UIWorkspaceLock should be in IDE-specific package
				UIWorkspaceLock uiLock = new UIWorkspaceLock(WorkbenchPlugin.getPluginWorkspace(), display);
				ResourcesPlugin.getWorkspace().setWorkspaceLock(uiLock);
				// @issue UISynchronizer should be in IDE-specific package
				display.setSynchronizer(new UISynchronizer(display, uiLock));
			} catch (CoreException e) {
				// @issue dump to log
				e.printStackTrace(System.out);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#preStartup()
	 */
	public void preStartup() {
		disableAutoBuild();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#postStartup()
	 */
	public void postStartup() {
		refreshFromLocal(commandLineArgs);
		enableAutoBuild();
		forceOpenPerspective();
		getConfigurationInfo().openWelcomeEditors(getWorkbench().getActiveWorkbenchWindow());
		checkUpdates();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#postShutdown
	 */
	public void postShutdown() {
		if (WorkbenchPlugin.getPluginWorkspace() != null) {
			disconnectFromWorkspace();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#preWindowOpen
	 */
	public void preWindowOpen(IWorkbenchWindowConfigurer window) {
		WorkbenchActionBuilder actionBuilder = new WorkbenchActionBuilder(window.getWindow());
		actionBuilders.put(window, actionBuilder);
		actionBuilder.buildActions();

		// include the workspace location in the title 
		// if the command line option -showlocation is specified
		// @issue find a home for this
		for (int i = 0; i < commandLineArgs.length; i++) {
			if ("-showlocation".equalsIgnoreCase(commandLineArgs[i])) { //$NON-NLS-1$
				String workspaceLocation = Platform.getLocation().toOSString();
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#postWindowClose
	 */
	public void postWindowClose(IWorkbenchWindowConfigurer window) {
		Object o = actionBuilders.remove(window);
		if (o != null) {
			((WorkbenchActionBuilder) o).dispose();
		}
	}

	/**
	 * Returns the resource change listener for noticing new errors.
	 * Processes the delta and shows the Tasks view if new errors 
	 * have appeared.  See PR 2066.
	 */ 
	private IResourceChangeListener getShowTasksChangeListener() {
		return new IResourceChangeListener() {
			public void resourceChanged(final IResourceChangeEvent event) {	
				IPreferenceStore store = getWorkbench().getPreferenceStore();
				// @issue IPreferenceConstants.SHOW_TASKS_ON_BUILD is IDE-specific and should be in IDE-specific package
				if (store.getBoolean(IPreferenceConstants.SHOW_TASKS_ON_BUILD)) {
					IMarker error = findProblemToShow(event);
					if (error != null) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								try {
									IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
									if (window != null && !window.getShell().isDisposed()) { 
										IWorkbenchPage page = window.getActivePage();
										if (page != null) {
											IViewPart tasksView= page.findView(IPageLayout.ID_PROBLEM_VIEW);
											if(tasksView == null) {
												IWorkbenchPart activePart= page.getActivePart();
												page.showView(IPageLayout.ID_PROBLEM_VIEW);
												//restore focus stolen by showing the Tasks view
												page.activate(activePart);
											} else {
												page.bringToTop(tasksView);
											}
										}
									}
								} catch (PartInitException e) {
									// @issue should not be using IDEWorkbenchPlugin to log error
									WorkbenchPlugin.log("Error bringing problem view to front", e.getStatus()); //$NON-NLS$ //$NON-NLS-1$
								}
							}
						});
					}
				}
			}
		};
	}
	
	/**
	 * Finds the first problem marker to show.
	 * Returns the first added error or warning.
	 */
	private IMarker findProblemToShow(IResourceChangeEvent event) {
		IMarkerDelta[] markerDeltas = event.findMarkerDeltas(IMarker.PROBLEM, true);
		for (int i = 0; i < markerDeltas.length; i++) {
			IMarkerDelta markerDelta = markerDeltas[i];
			if (markerDelta.getKind() == IResourceDelta.ADDED) {
				int sev = markerDelta.getAttribute(IMarker.SEVERITY, -1);
				if (sev == IMarker.SEVERITY_ERROR || sev == IMarker.SEVERITY_WARNING) {
					return markerDelta.getMarker();
				}
			}
		}
		return null;
	}
	
	/**
	 * Temporarily disables auto build.
	 */
	private void disableAutoBuild() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		
		// record setting of flag for future reference
		autoBuild = description.isAutoBuilding();
		if (autoBuild) {
			IPreferenceStore store = getWorkbench().getPreferenceStore();
			// @issue IPreferenceConstants.AUTO_BUILD is IDE-specific and should be in IDE-specific package
			store.setValue(IPreferenceConstants.AUTO_BUILD, false);
			description.setAutoBuilding(false);
			try {
				workspace.setDescription(description);
			} catch (CoreException exception) { 
				MessageDialog.openError(
					null, 
					// @issue should not access IDEWorkbenchMessages
					WorkbenchMessages.getString("Workspace.problemsTitle"),	//$NON-NLS-1$
					WorkbenchMessages.getString("Restoring_Problem"));		//$NON-NLS-1$
			}
		}
	}	

	/**
	 * Restore auto builds if temporarily disabled.
	 * Assumes that workbench windows have already been restored.
	 * <p>
	 * Use a WorkspaceModifyOperation to trigger an immediate build.
	 * See bug 6091.
	 * </p>
	 */
	private void enableAutoBuild() {
		if (autoBuild) {
			IWorkbenchWindow windows[] = getWorkbench().getWorkbenchWindows();
			Shell shell = windows[windows.length - 1].getShell();				
			try {
				WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
					protected void execute(IProgressMonitor monitor) throws CoreException {
						// @issue should not be accessing IDEWorkbenchMessages to access IDE-specific message
						monitor.setTaskName(WorkbenchMessages.getString("Workbench.autoBuild"));	//$NON-NLS-1$

						IWorkspace workspace = ResourcesPlugin.getWorkspace();
						IWorkspaceDescription description = workspace.getDescription();
						description.setAutoBuilding(true);
						workspace.setDescription(description);
					}
				};
				IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
				if (window != null)
					window.run(true, true, op);
				else
					new ProgressMonitorDialog(shell).run(true, true, op);
			} catch (InterruptedException e) {
			} catch (InvocationTargetException exception) {
				MessageDialog.openError(
					shell, 
					// @issue should not access IDEWorkbenchMessages
					WorkbenchMessages.getString("Workspace.problemsTitle"),		//$NON-NLS-1$
					WorkbenchMessages.getString("Workspace.problemAutoBuild"));	//$NON-NLS-1$
			}
			// update the preference store so that property change listener
			// get notified of preference change.
			IPreferenceStore store = getWorkbench().getPreferenceStore();
			// @issue IPreferenceConstants.AUTO_BUILD is IDE-specific and should be in IDE-specific package
			store.setValue(IPreferenceConstants.AUTO_BUILD, true);
			updateBuildActions(true);			
		}
	}

	/**
	 * Update the action bar of every workbench window to
	 * add/remove the manual build actions.
	 * 
	 * @param autoBuildSetting <code>true</code> auto build is enabled 
	 * 	<code>false</code> auto build is disabled
	 */
	private void updateBuildActions(boolean autoBuildSetting) {
		// Update the menu/tool bars for each window.
		Window[] wins = configurer.getWorkbenchWindowManager().getWindows();
		for (Iterator it = actionBuilders.keySet().iterator(); it.hasNext(); ) {
			WorkbenchActionBuilder actionBuilder = (WorkbenchActionBuilder) it.next();
			if (autoBuildSetting) {
				actionBuilder.removeManualIncrementalBuildAction();
			} else {
				actionBuilder.addManualIncrementalBuildAction();
			}
		}
	}

	/**
	 * Handles a change to a preference.
	 */
	private void handlePreferenceChange(PropertyChangeEvent event) {
		// @issue IPreferenceConstants.AUTO_BUILD is IDE-specific and should be in IDE-specific package
		if (event.getProperty().equals(IPreferenceConstants.AUTO_BUILD)) {
			// Auto build is stored in core. It is also in the preference 
			// store for use by import/export.
			IWorkspaceDescription description =	ResourcesPlugin.getWorkspace().getDescription();
			boolean autoBuildSetting = description.isAutoBuilding();
			boolean newAutoBuildSetting = getWorkbench().getPreferenceStore().getBoolean(IPreferenceConstants.AUTO_BUILD);

			if (autoBuildSetting != newAutoBuildSetting) {
				// Update the core setting.
				description.setAutoBuilding(newAutoBuildSetting);
				autoBuildSetting = newAutoBuildSetting;
				try {
					ResourcesPlugin.getWorkspace().setDescription(description);
				} catch (CoreException e) {
					// @issue should not be using IDEWorkbenchPlugin to log error
					WorkbenchPlugin.log("Error changing auto build preference setting.", e.getStatus()); //$NON-NLS-1$
				}

				// If auto build is turned on, then do a global incremental
				// build on all the projects.
				if (newAutoBuildSetting) {
					GlobalBuildAction action = new GlobalBuildAction(
						getWorkbench().getActiveWorkbenchWindow(),
						IncrementalProjectBuilder.INCREMENTAL_BUILD);
					action.doBuild();
				}
				updateBuildActions(newAutoBuildSetting);
			}
		}
	}

	private void refreshFromLocal(String[] commandLineArgs) {
		IPreferenceStore store = getWorkbench().getPreferenceStore();
		// @issue should reference REFRESH_WORKSPACE_ON_STARTUP in IDE-specific package
		boolean refresh = store.getBoolean(IPreferenceConstants.REFRESH_WORKSPACE_ON_STARTUP);
		if (!refresh)
			return;
		//Do not refresh if it was already done by core on startup.
		for (int i = 0; i < commandLineArgs.length; i++)
			if (commandLineArgs[i].equalsIgnoreCase("-refresh")) //$NON-NLS-1$
				return;
		IWorkbenchWindow windows[] = getWorkbench().getWorkbenchWindows();
		Shell shell = windows[windows.length - 1].getShell();
		ProgressMonitorDialog dlg = new ProgressMonitorDialog(shell);
		final CoreException ex[] = new CoreException[1];
		try {
			dlg.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						IContainer root = ResourcesPlugin.getWorkspace().getRoot();
						root.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					} catch (CoreException e) {
						ex[0] = e;
					}
				}
			});
			if (ex[0] != null) {
				// @issue should not reference WorkbenchMessage directly
				String errorTitle = WorkbenchMessages.getString("Workspace.problemsTitle"); //$NON-NLS-1$
				// @issue should not reference WorkbenchMessage directly
				String msg = WorkbenchMessages.getString("Workspace.problemMessage"); //$NON-NLS-1$
				ErrorDialog.openError(shell, errorTitle, msg, ex[0].getStatus());
			}
		} catch (InterruptedException e) {
			//Do nothing. Operation was canceled.
		} catch (InvocationTargetException e) {
			String msg = "InvocationTargetException refreshing from local on startup"; //$NON-NLS-1$
			// @issue should not be using IDEWorkbenchPlugin to log error
			WorkbenchPlugin.log(msg, new Status(Status.ERROR, PlatformUI.PLUGIN_ID, 0, msg, e.getTargetException()));
		}
	}

	/**
	 * Disconnect from the core workspace.
	 */
	private void disconnectFromWorkspace() {
		// save the workspace
		// @issue should not be accessing IDEWorkbenchMessages to access IDE-specific message
		final MultiStatus status = new MultiStatus(WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("ProblemSavingWorkbench"), null); //$NON-NLS-1$
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				try {
					status.merge(ResourcesPlugin.getWorkspace().save(true, monitor));
				} catch (CoreException e) {
					status.merge(e.getStatus());
				}
			}
		};
		try {
			new ProgressMonitorDialog(null).run(false, false, runnable);
		} catch (InvocationTargetException e) {
			// @issue should not be accessing IDEWorkbenchMessages to access IDE-specific message
			status.merge(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("InternalError"), e.getTargetException())); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// @issue should not be accessing IDEWorkbenchMessages to access IDE-specific message
			status.merge(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("InternalError"), e)); //$NON-NLS-1$
		}
		// @issue should not be accessing IDEWorkbenchMessages to access IDE-specific message
		ErrorDialog.openError(null, WorkbenchMessages.getString("ProblemsSavingWorkspace"), //$NON-NLS-1$
		null, status, IStatus.ERROR | IStatus.WARNING);
		if (!status.isOK()) {
			// @issue should not be using IDEWorkbenchPlugin to log error
			WorkbenchPlugin.log(WorkbenchMessages.getString("ProblemsSavingWorkspace"), status); //$NON-NLS-1$
		}
	}

	/**
	 * Checks if the -newUpdates command line argument is present
	 * and if so, opens the update manager.
	 */
	private void checkUpdates() {
		boolean newUpdates = false;
		for (int i = 0; i < commandLineArgs.length; i++) {
			if (commandLineArgs[i].equalsIgnoreCase("-newUpdates")) { //$NON-NLS-1$
				newUpdates = true;
				break;
			}
		}

		if (newUpdates) {
			try {
				SiteManager.handleNewChanges();
			} catch (CoreException ex) {
				// @issue should not be using IDEWorkbenchPlugin to log error
				WorkbenchPlugin.log("Problem opening update manager", ex.getStatus()); //$NON-NLS-1$
			}
		}
	}

	private void forceOpenPerspective() {
		if (getWorkbench().getWorkbenchWindows().length == 0) {
			// Something is wrong, there should be at least
			// one workbench window open by now.
			return;
		}

		String perspId = null;
		for (int i = 0; i < commandLineArgs.length - 1; i++) {
			if (commandLineArgs[i].equalsIgnoreCase("-perspective")) { //$NON-NLS-1$
				perspId = commandLineArgs[i + 1];
				break;
			}
		}
		if (perspId == null) {
			return;
		}
		IPerspectiveDescriptor desc = getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspId);
		if (desc == null) {
			return;
		}

		IWorkbenchWindow win = getWorkbench().getActiveWorkbenchWindow();
		if (win == null) {
			win = getWorkbench().getWorkbenchWindows()[0];
		}
		try {
			getWorkbench().showPerspective(perspId, win);
		} catch (WorkbenchException e) {
			String msg = "Workbench exception showing specified command line perspective on startup."; //$NON-NLS-1$
			// @issue should not be using IDEWorkbenchPlugin to log error
			WorkbenchPlugin.log(msg, new Status(Status.ERROR, PlatformUI.PLUGIN_ID, 0, msg, e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#isApplicationMenu
	 */
	public boolean isApplicationMenu(IWorkbenchWindowConfigurer window, String menuID) {
		WorkbenchActionBuilder a = (WorkbenchActionBuilder) actionBuilders.get(window);
		return a.isContainerMenu(menuID);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#isWorkbenchCoolItemId
	 */
	public boolean isWorkbenchCoolItemId(IWorkbenchWindowConfigurer window, String id) {
		WorkbenchActionBuilder a = (WorkbenchActionBuilder) actionBuilders.get(window);
		return a.isWorkbenchCoolItemId(id);
	}
	
	/**
	 * Returns the about info.
	 *
	 * @return the about info
	 * @issue WorkbenchConfigurationInfo needs to be moved to this package
	 */
	public WorkbenchConfigurationInfo getConfigurationInfo() {
		if(configurationInfo == null)
			configurationInfo = new WorkbenchConfigurationInfo();
		return configurationInfo;
	}
}
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
import org.eclipse.core.runtime.IAdaptable;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
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
	private static final String ACTION_BUILDER = "ActionBuilder"; //$NON-NLS-1$
	
	/**
	 * Special object for configuring the workbench.
	 */
	private IWorkbenchConfigurer configurer;	

	/**
	 * Event loop exception handler for the adviser.
	 */
	private IDEExceptionHandler exceptionHandler = null;
	
	/**
	 * Tracks whether we were autobuilding.
	 */
	private boolean autoBuild;

	/**
	 * Contains the workspace location if the -showlocation command line
	 * argument is specified, or <code>null</code> if not specified.
	 */
	private String workspaceLocation = null;
	
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
	 * Creates a new workbench adviser instance.
	 */
	IDEWorkbenchAdviser() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#initialize
	 */
	public void initialize(IWorkbenchConfigurer configurer) {
		// remember for future reference
		this.configurer = configurer;		
		
		// setup the event loop exception handler
		exceptionHandler = new IDEExceptionHandler(configurer);
		
		// register workspace adapters
		WorkbenchAdapterBuilder.registerAdapters();
		
		// register resource change listener for showing the tasks view
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
			getShowTasksChangeListener(),
			IResourceChangeEvent.POST_CHANGE);

		// get the command line arguments
		String[] cmdLineArgs = Platform.getCommandLineArgs();

		// include the workspace location in the title 
		// if the command line option -showlocation is specified
		for (int i = 0; i < cmdLineArgs.length; i++) {
			if ("-showlocation".equalsIgnoreCase(cmdLineArgs[i])) { //$NON-NLS-1$
				workspaceLocation = Platform.getLocation().toOSString();
				break;
			}
		}

		// anti-deadlocking code
		boolean avoidDeadlock = true;
		for (int i = 0; i < cmdLineArgs.length; i++) {
			if (cmdLineArgs[i].equalsIgnoreCase("-allowDeadlock")) //$NON-NLS-1$
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
				IDEWorkbenchPlugin.log("Failed to setup workspace lock.", e.getStatus()); //$NON-NLS-1$
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
		refreshFromLocal();
		enableAutoBuild();
		// listen for changes to IDE-specific preferences
		// @issue must sure this is the correct preference store
		PlatformUI.getWorkbench().getPreferenceStore().addPropertyChangeListener(preferenceChangeListener);
		forceOpenPerspective();
		// @issue opening welcome editors is split over restore wnd code and here. it can be combined there is a startup penalty (ie 2 perspective started instead of one)
		//getConfigurationInfo().openWelcomeEditors(getWorkbench().getActiveWorkbenchWindow());
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
	 * @see org.eclipse.ui.application.WorkbenchAdviser#eventLoopException
	 */
	public void eventLoopException(Throwable exception) {
		super.eventLoopException(exception);
		if (exceptionHandler != null) {
			exceptionHandler.handleException(exception);
		} else {
			if (configurer != null) {
				configurer.emergencyClose();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#preWindowOpen
	 */
	public void preWindowOpen(IWorkbenchWindowConfigurer windowConfigurer) {
		// setup the action builder to populate the toolbar and menubar
		WorkbenchActionBuilder actionBuilder = new WorkbenchActionBuilder(windowConfigurer);
		windowConfigurer.setData(ACTION_BUILDER, actionBuilder);
		actionBuilder.buildActions();
		
		// hook up the listeners to update the window title
		windowConfigurer.getWindow().addPageListener(new IPageListener () {
			public void pageActivated(IWorkbenchPage page) {
			}
			public void pageClosed(IWorkbenchPage page) {
				updateTitle(page.getWorkbenchWindow());
			}
			public void pageOpened(IWorkbenchPage page) {
			}
		});
		windowConfigurer.getWindow().addPerspectiveListener(new IPerspectiveListener() {
			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				updateTitle(page.getWorkbenchWindow());
			}
			public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
			}
		});
		windowConfigurer.getWindow().getPartService().addPartListener(new IPartListener2() {
			public void partActivated(IWorkbenchPartReference ref) {
				if (ref instanceof IEditorReference || ref.getPage().getActiveEditor() == null) {
					updateTitle(ref.getPage().getWorkbenchWindow());
				}
			}
			public void partBroughtToTop(IWorkbenchPartReference ref) {
				if (ref instanceof IEditorReference || ref.getPage().getActiveEditor() == null) {
					updateTitle(ref.getPage().getWorkbenchWindow());
				}
			}
			public void partClosed(IWorkbenchPartReference ref) {
			}
			public void partDeactivated(IWorkbenchPartReference ref) {
			}
			public void partOpened(IWorkbenchPartReference ref) {
			}
			public void partHidden(IWorkbenchPartReference ref) {
			}
			public void partVisible(IWorkbenchPartReference ref) {
			}
			public void partInputChanged(IWorkbenchPartReference ref) {
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#postWindowClose
	 */
	public void postWindowClose(IWorkbenchWindowConfigurer windowConfigurer) {
		WorkbenchActionBuilder a = (WorkbenchActionBuilder) windowConfigurer.getData(ACTION_BUILDER);
		if (a != null) {
			windowConfigurer.setData(ACTION_BUILDER, null);
			a.dispose();
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
				IPreferenceStore store = PlatformUI.getWorkbench().getPreferenceStore();
				// @issue IPreferenceConstants.SHOW_TASKS_ON_BUILD is IDE-specific and should be in IDE-specific package
				if (store.getBoolean(IPreferenceConstants.SHOW_TASKS_ON_BUILD)) {
					IMarker error = findProblemToShow(event);
					if (error != null) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								try {
									IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
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
			IPreferenceStore store = PlatformUI.getWorkbench().getPreferenceStore();
			// @issue IPreferenceConstants.AUTO_BUILD is IDE-specific and should be in IDE-specific package
			store.setValue(IPreferenceConstants.AUTO_BUILD, false);
			description.setAutoBuilding(false);
			try {
				workspace.setDescription(description);
			} catch (CoreException exception) { 
				// @issue should not access IDEWorkbenchMessages
				MessageDialog.openError(
					null, 
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
			IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
			Shell shell = windows[windows.length - 1].getShell();				
			try {
				WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
					protected void execute(IProgressMonitor monitor) throws CoreException {
						// @issue should not be accessing WorkbenchMessages to access IDE-specific message
						monitor.setTaskName(WorkbenchMessages.getString("Workbench.autoBuild"));	//$NON-NLS-1$

						IWorkspace workspace = ResourcesPlugin.getWorkspace();
						IWorkspaceDescription description = workspace.getDescription();
						description.setAutoBuilding(true);
						workspace.setDescription(description);
					}
				};
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window != null)
					window.run(true, true, op);
				else
					new ProgressMonitorDialog(shell).run(true, true, op);
			} catch (InterruptedException e) {
			} catch (InvocationTargetException exception) {
				// @issue should not access WorkbenchMessages for IDE-specific messages
				MessageDialog.openError(
					shell, 
					WorkbenchMessages.getString("Workspace.problemsTitle"),		//$NON-NLS-1$
					WorkbenchMessages.getString("Workspace.problemAutoBuild"));	//$NON-NLS-1$
			}
			// update the preference store so that property change listener
			// get notified of preference change.
			// @issue IPreferenceConstants.AUTO_BUILD is IDE-specific and should be in IDE-specific package
			IPreferenceStore store = PlatformUI.getWorkbench().getPreferenceStore();
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
		IWorkbenchWindow[] wins = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < wins.length; i++) {
			WorkbenchActionBuilder a = (WorkbenchActionBuilder) configurer.getWindowConfigurer(wins[i]).getData(ACTION_BUILDER);
			if (autoBuildSetting) {
				a.removeManualIncrementalBuildAction();
			} else {
				a.addManualIncrementalBuildAction();
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
			boolean newAutoBuildSetting = PlatformUI.getWorkbench().getPreferenceStore().getBoolean(IPreferenceConstants.AUTO_BUILD);

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
						PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
						IncrementalProjectBuilder.INCREMENTAL_BUILD);
					action.doBuild();
				}
				updateBuildActions(newAutoBuildSetting);
			}
		}
	}

	private void refreshFromLocal() {
		String[] commandLineArgs = Platform.getCommandLineArgs();
		IPreferenceStore store = PlatformUI.getWorkbench().getPreferenceStore();
		// @issue should reference REFRESH_WORKSPACE_ON_STARTUP in IDE-specific package
		boolean refresh = store.getBoolean(IPreferenceConstants.REFRESH_WORKSPACE_ON_STARTUP);
		if (!refresh)
			return;
			
		//Do not refresh if it was already done by core on startup.
		for (int i = 0; i < commandLineArgs.length; i++)
			if (commandLineArgs[i].equalsIgnoreCase("-refresh")) //$NON-NLS-1$
				return;
				
		IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
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
			// @issue should not be using WorkbenchPlugin to log error
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
		String[] commandLineArgs = Platform.getCommandLineArgs();
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
				// @issue should not be using WorkbenchPlugin to log error
				WorkbenchPlugin.log("Problem opening update manager", ex.getStatus()); //$NON-NLS-1$
			}
		}
	}

	private void forceOpenPerspective() {
		String perspId = null;
		String[] commandLineArgs = Platform.getCommandLineArgs();
		for (int i = 0; i < commandLineArgs.length - 1; i++) {
			if (commandLineArgs[i].equalsIgnoreCase("-perspective")) { //$NON-NLS-1$
				perspId = commandLineArgs[i + 1];
				break;
			}
		}
		if (perspId == null) {
			return;
		}
		IPerspectiveDescriptor desc = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspId);
		if (desc == null) {
			return;
		}

		IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (win == null) {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (windows.length > 0) {
				win = windows[0];
			}
		}
		try {
			if (win != null) {
				PlatformUI.getWorkbench().showPerspective(perspId, win);
			} else {
				PlatformUI.getWorkbench().openWorkbenchWindow(perspId, getDefaultWindowInput());
			}
		} catch (WorkbenchException e) {
			String msg = "Workbench exception showing specified command line perspective on startup."; //$NON-NLS-1$
			// @issue should not be using IDEWorkbenchPlugin to log error
			WorkbenchPlugin.log(msg, new Status(Status.ERROR, PlatformUI.PLUGIN_ID, 0, msg, e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#isApplicationMenu
	 */
	public boolean isApplicationMenu(IWorkbenchWindowConfigurer windowConfigurer, String menuID) {
		WorkbenchActionBuilder a = (WorkbenchActionBuilder) windowConfigurer.getData(ACTION_BUILDER);
		return a.isContainerMenu(menuID);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#isWorkbenchCoolItemId
	 */
	public boolean isWorkbenchCoolItemId(IWorkbenchWindowConfigurer windowConfigurer, String id) {
		WorkbenchActionBuilder a = (WorkbenchActionBuilder) windowConfigurer.getData(ACTION_BUILDER);
		return a.isWorkbenchCoolItemId(id);
	}
	
	/**
	 * Returns the about info.
	 *
	 * @return the about info
	 * @issue WorkbenchConfigurationInfo needs to be moved to this package
	 */
/*	public WorkbenchConfigurationInfo getConfigurationInfo() {
		if(configurationInfo == null)
			configurationInfo = new WorkbenchConfigurationInfo();
		return configurationInfo;
	}
*/	
	private IAdaptable getDefaultWindowInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * Updates the window title. Format will be:
	 * [pageInput -] [currentPerspective -] [editorInput -] [workspaceLocation -] productName
	 */
	private void updateTitle(IWorkbenchWindow window) {
		IWorkbenchWindowConfigurer windowConfigurer = configurer.getWindowConfigurer(window);
		
		String title = null;
		try {
			title = windowConfigurer.getWorkbenchConfigurer().getPrimaryFeatureAboutInfo().getProductName();
		} catch (WorkbenchException e) {
			// do nothing
		}
		if (title == null) {
			title = ""; //$NON-NLS-1$
		}
		
		if (workspaceLocation != null) {
			title = WorkbenchMessages.format("WorkbenchWindow.shellTitle", new Object[] { workspaceLocation, title }); //$NON-NLS-1$
		}

		IWorkbenchPage currentPage = window.getActivePage();
		if (currentPage != null) {
			IEditorPart editor = currentPage.getActiveEditor();
			if (editor != null) {
				String editorTitle = editor.getTitle();
				title = WorkbenchMessages.format("WorkbenchWindow.shellTitle", new Object[] { editorTitle, title }); //$NON-NLS-1$
			}
			IPerspectiveDescriptor persp = currentPage.getPerspective();
			String label = ""; //$NON-NLS-1$
			if (persp != null)
				label = persp.getLabel();
			IAdaptable input = currentPage.getInput();
			if (input != null && !input.equals(getDefaultWindowInput())) {
				label = currentPage.getLabel();
			}
			if (label != null && !label.equals("")) { //$NON-NLS-1$	
				title = WorkbenchMessages.format("WorkbenchWindow.shellTitle", new Object[] { label, title }); //$NON-NLS-1$
			}
		}
		
		windowConfigurer.setTitle(title);
	}
}
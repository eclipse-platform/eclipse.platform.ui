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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPluginDescriptor;
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.AboutInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdviser;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.EditorAreaDropAdapter;
import org.eclipse.ui.internal.WorkbenchActionBuilder;
import org.eclipse.ui.internal.dialogs.MessageDialogWithToggle;
import org.eclipse.ui.internal.dialogs.WelcomeEditorInput;
import org.eclipse.ui.internal.model.WorkbenchAdapterBuilder;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.part.ResourceTransfer;
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
public class IDEWorkbenchAdviser extends WorkbenchAdviser {
	private static final String ACTION_BUILDER = "ActionBuilder"; //$NON-NLS-1$
	private static final String WELCOME_EDITOR_ID = "org.eclipse.ui.internal.dialogs.WelcomeEditor"; //$NON-NLS-1$
	
	private static IDEWorkbenchAdviser workbenchAdviser = null;

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
	 * List of <code>AboutInfo</code> for all new installed
	 * features that specify a welcome perspective.
	 */
	private ArrayList welcomePerspectiveInfos = null;
	
	/**
	 * Creates a new workbench adviser instance.
	 */
	protected IDEWorkbenchAdviser() {
		super();
		if (workbenchAdviser != null) {
			throw new IllegalStateException();
		}
		workbenchAdviser = this;
	}

	/**
	 * Returns the single instance for this adviser. Can
	 * be <code>null</code> if not created yet.
	 */
	/* package */ static final IDEWorkbenchAdviser getAdviser() {
		return workbenchAdviser;
	}
	
	/**
	 * Returns the workbench configurer for the adviser. Can
	 * be <code>null</code> if adviser not initialized yet.
	 */
	/* package */ IWorkbenchConfigurer getWorkbenchConfigurer() {
		return configurer;
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

		// register shared images
		declareWorkbenchImages();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#preStartup()
	 */
	public void preStartup() {
		disableAutoBuild();
		
		// collect the welcome perspectives of the new installed features
		try {
			AboutInfo[] infos = configurer.getNewFeaturesAboutInfo();
			if (infos != null) {
				welcomePerspectiveInfos = new ArrayList(infos.length);
				for (int i = 0; i < infos.length; i++) {
					if (infos[i].getWelcomePerspectiveId() != null && infos[i].getWelcomePageURL() != null) {
						welcomePerspectiveInfos.add(infos[i]);
					}
				}
			}
		} catch (WorkbenchException e) {
			IDEWorkbenchPlugin.log("Failed to load new installed features about info.", e.getStatus()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#postStartup()
	 */
	public void postStartup() {
		refreshFromLocal();
		enableAutoBuild();
		try {
			openWelcomeEditors();
		} catch (WorkbenchException e) {
			IDEWorkbenchPlugin.log("Fail to open remaining welcome editors.", e.getStatus()); //$NON-NLS-1$
		}
		checkUpdates();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#postShutdown
	 */
	public void postShutdown() {
		if (IDEWorkbenchPlugin.getPluginWorkspace() != null) {
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
	 * @see org.eclipse.ui.application.WorkbenchAdviser#preWindowClose
	 */
	public boolean preWindowClose(IWorkbenchWindowConfigurer windowConfigurer) {
		if (!super.preWindowClose(windowConfigurer)) {
			return false;
		}
		
		// if emergency close, then don't prompt at all
		if (configurer.emergencyClosing()) {
			return true;
		}

		// when closing the last window, prompt for confirmation
		if (configurer.getWorkbench().getWorkbenchWindowCount() > 1)
			return true;

		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		boolean promptOnExit =	store.getBoolean(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW);

		if (promptOnExit) {
			String message;
			String productName = null;
			try {
				productName = configurer.getPrimaryFeatureAboutInfo().getProductName();
			} catch (WorkbenchException e) {
				IDEWorkbenchPlugin.log("Failed to access primary feature product name.", e.getStatus()); //$NON-NLS-1$
			}
			if (productName == null) {
				message = IDEWorkbenchMessages.getString("PromptOnExitDialog.message0"); //$NON-NLS-1$
			} else {
				message = IDEWorkbenchMessages.format("PromptOnExitDialog.message1", new Object[] { productName }); //$NON-NLS-1$
			}

			MessageDialogWithToggle dlg = MessageDialogWithToggle.openConfirm(
				windowConfigurer.getWindow().getShell(), 
				IDEWorkbenchMessages.getString("PromptOnExitDialog.shellTitle"), //$NON-NLS-1$,
				message,
				IDEWorkbenchMessages.getString("PromptOnExitDialog.choice"), //$NON-NLS-1$,
				false);

			if (dlg.getReturnCode() == MessageDialogWithToggle.OK) {
				store.setValue(
					IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW,
					!dlg.getToggleState());
				return true;
			} else {
				return false;
			}
		}

		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#preWindowOpen
	 */
	public void preWindowOpen(IWorkbenchWindowConfigurer windowConfigurer) {
		// setup the action builder to populate the toolbar and menubar
		WorkbenchActionBuilder actionBuilder = new WorkbenchActionBuilder(windowConfigurer);
		windowConfigurer.setData(ACTION_BUILDER, actionBuilder);
		actionBuilder.buildActions();
		
		// add the drag and drop support for the editor area
		windowConfigurer.addEditorAreaTransfer(EditorInputTransfer.getInstance());
		windowConfigurer.addEditorAreaTransfer(ResourceTransfer.getInstance());
		windowConfigurer.addEditorAreaTransfer(MarkerTransfer.getInstance());
		windowConfigurer.configureEditorAreaDropListener(new EditorAreaDropAdapter(windowConfigurer.getWindow()));
		
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
	 * @see org.eclipse.ui.application.WorkbenchAdviser#postWindowRestore
	 */
	public void postWindowRestore(IWorkbenchWindowConfigurer windowConfigurer) throws WorkbenchException {
		int index = PlatformUI.getWorkbench().getWorkbenchWindowCount() - 1;
		
		if (index >=0 && welcomePerspectiveInfos != null && index < welcomePerspectiveInfos.size()) {
			// find a page that exist in the window
			IWorkbenchPage page = windowConfigurer.getWindow().getActivePage();
			if (page == null) {
				IWorkbenchPage pages[] = windowConfigurer.getWindow().getPages();
				if (pages != null && pages.length > 0)
					page = pages[0];
			}

			// if the window does not contain a page, create one
			String perspectiveId = ((AboutInfo) welcomePerspectiveInfos.get(index)).getWelcomePerspectiveId();
			if (page == null) {
				IAdaptable root = getDefaultWindowInput();
				page = windowConfigurer.getWindow().openPage(perspectiveId, root);
			} else {
				IPerspectiveRegistry reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
				IPerspectiveDescriptor desc = reg.findPerspectiveWithId(perspectiveId);
				if (desc != null) {
					page.setPerspective(desc);
				}
			}

			// set the active page and open the welcome editor
			windowConfigurer.getWindow().setActivePage(page);
			page.openEditor(new WelcomeEditorInput((AboutInfo) welcomePerspectiveInfos.get(index)), WELCOME_EDITOR_ID, true);
		}
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
				IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
				if (store.getBoolean(IDEInternalPreferences.SHOW_TASKS_ON_BUILD)) {
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
									IDEWorkbenchPlugin.log("Error bringing problem view to front", e.getStatus()); //$NON-NLS-1$
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
			description.setAutoBuilding(false);
			try {
				workspace.setDescription(description);
			} catch (CoreException exception) { 
				MessageDialog.openError(
					null, 
					IDEWorkbenchMessages.getString("Workspace.problemsTitle"),	//$NON-NLS-1$
					IDEWorkbenchMessages.getString("Restoring_Problem"));		//$NON-NLS-1$
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
						monitor.setTaskName(IDEWorkbenchMessages.getString("Workbench.autoBuild"));	//$NON-NLS-1$

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
				MessageDialog.openError(
					shell, 
					IDEWorkbenchMessages.getString("Workspace.problemsTitle"),		//$NON-NLS-1$
					IDEWorkbenchMessages.getString("Workspace.problemAutoBuild"));	//$NON-NLS-1$
			}
		}
	}

	private void refreshFromLocal() {
		String[] commandLineArgs = Platform.getCommandLineArgs();
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		boolean refresh = store.getBoolean(IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP);
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
				ErrorDialog.openError(
					shell, 
					IDEWorkbenchMessages.getString("Workspace.problemsTitle"), //$NON-NLS-1$ 
					IDEWorkbenchMessages.getString("Workspace.problemMessage"),  //$NON-NLS-1$
					ex[0].getStatus());
			}
		} catch (InterruptedException e) {
			//Do nothing. Operation was canceled.
		} catch (InvocationTargetException e) {
			String msg = "InvocationTargetException refreshing from local on startup"; //$NON-NLS-1$
			IDEWorkbenchPlugin.log(msg, new Status(Status.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, 0, msg, e.getTargetException()));
		}
	}

	/**
	 * Disconnect from the core workspace.
	 */
	private void disconnectFromWorkspace() {
		// save the workspace
		final MultiStatus status = new MultiStatus(IDEWorkbenchPlugin.IDE_WORKBENCH, 1, IDEWorkbenchMessages.getString("ProblemSavingWorkbench"), null); //$NON-NLS-1$
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
			status.merge(new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, 1, IDEWorkbenchMessages.getString("InternalError"), e.getTargetException())); //$NON-NLS-1$
		} catch (InterruptedException e) {
			status.merge(new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, 1, IDEWorkbenchMessages.getString("InternalError"), e)); //$NON-NLS-1$
		}
		ErrorDialog.openError(
			null, 
			IDEWorkbenchMessages.getString("ProblemsSavingWorkspace"), //$NON-NLS-1$
			null, 
			status, 
			IStatus.ERROR | IStatus.WARNING);
		if (!status.isOK()) {
			IDEWorkbenchPlugin.log(IDEWorkbenchMessages.getString("ProblemsSavingWorkspace"), status); //$NON-NLS-1$
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
			} catch (CoreException e) {
				IDEWorkbenchPlugin.log("Problem opening update manager", e.getStatus()); //$NON-NLS-1$
			}
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
	 * @see org.eclipse.ui.application.WorkbenchAdviser#getDefaultWindowInput
	 */
	public IAdaptable getDefaultWindowInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser
	 */
	public String getInitialWindowPerspectiveId() {
		int index = PlatformUI.getWorkbench().getWorkbenchWindowCount() - 1;
		
		String perspectiveId = null;
		if (index >=0 && welcomePerspectiveInfos != null && index < welcomePerspectiveInfos.size()) {
			perspectiveId = ((AboutInfo) welcomePerspectiveInfos.get(index)).getWelcomePerspectiveId();
		}
		if (perspectiveId == null) {
			perspectiveId = IDE.RESOURCE_PERSPECTIVE_ID;
		}
		return perspectiveId;
	}


	/*
	 * Open the welcome editor for the primary feature or for a new installed features
	 */
	private void openWelcomeEditors() throws WorkbenchException {
		AboutInfo primaryInfo = configurer.getPrimaryFeatureAboutInfo();
		AboutInfo[] newFeaturesInfo = configurer.getNewFeaturesAboutInfo();
		
		IWorkbenchWindow window = configurer.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			if (configurer.getWorkbench().getWorkbenchWindowCount() > 0) {
				window = configurer.getWorkbench().getWorkbenchWindows()[0];
			} else {
				return;
			}
		}

		if (IDEWorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(IDEInternalPreferences.WELCOME_DIALOG)) {
			// Show the quick start wizard the first time the workbench opens.
			URL url = primaryInfo.getWelcomePageURL();
			if (url == null) {
				return;
			}
			IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(IDEInternalPreferences.WELCOME_DIALOG, false);
			openWelcomeEditor(window, new WelcomeEditorInput(primaryInfo), null);
		} else {
			// Show the welcome page for any newly installed features
			ArrayList welcomeFeatures = new ArrayList();
			for (int i = 0; i < newFeaturesInfo.length; i++) {
				if (newFeaturesInfo[i].getWelcomePageURL() != null) {
					if (newFeaturesInfo[i].getFeatureId() != null && newFeaturesInfo[i].getWelcomePerspectiveId() != null) {
						IPluginDescriptor desc = newFeaturesInfo[i].getPluginDescriptor();
						try {
							// activate the feature plugin so it can run some install code.
							if(desc != null) {
								desc.getPlugin();
							}
						} catch (CoreException e) {
						}
					}
					welcomeFeatures.add(newFeaturesInfo[i]);
				}
			}
	
			int wCount = configurer.getWorkbench().getWorkbenchWindowCount();
			for (int i = 0; i < welcomeFeatures.size(); i++) {
				AboutInfo newInfo = (AboutInfo) welcomeFeatures.get(i);
				String id = newInfo.getWelcomePerspectiveId();
				// Other editors were already opened in postWindowRestore(..)
				if (id == null || i >= wCount) {
					openWelcomeEditor(window, new WelcomeEditorInput(newInfo), id);
				}
			}
		}
	}
	
	/*
	 * Open a welcome editor for the given input
	 */
	private void openWelcomeEditor(IWorkbenchWindow window, WelcomeEditorInput input, String perspectiveId) {
		if (configurer.getWorkbench().getWorkbenchWindowCount() == 0) {
			// Something is wrong, there should be at least
			// one workbench window open by now.
			return;
		}
	
		IWorkbenchWindow win = window;
		if (perspectiveId != null) {
			try {
				win = configurer.getWorkbench().openWorkbenchWindow(perspectiveId, getDefaultWindowInput());
				if (win == null) {
					win = window;
				}
			} catch (WorkbenchException e) {
				IDEWorkbenchPlugin.log("Error opening window with welcome perspective.", e.getStatus()); //$NON-NLS-1$
				return;
			}
		}
	
		if (win == null) {
			win = configurer.getWorkbench().getWorkbenchWindows()[0];
		}
			
		IWorkbenchPage page = win.getActivePage();
		String id = perspectiveId;
		if (id == null) {
			id = configurer.getWorkbench().getPerspectiveRegistry().getDefaultPerspective();
		}
	
		if (page == null) {
			try {
				page = win.openPage(id, getDefaultWindowInput());
			} catch (WorkbenchException e) {
				ErrorDialog.openError(
					win.getShell(), 
					IDEWorkbenchMessages.getString("Problems_Opening_Page"), //$NON-NLS-1$
					e.getMessage(),
					e.getStatus());
			}
		}
		if (page == null)
			return;
	
		if (page.getPerspective() == null) {
			try {
				page = configurer.getWorkbench().showPerspective(id, win);
			} catch (WorkbenchException e) {
				ErrorDialog.openError(
					win.getShell(),
					IDEWorkbenchMessages.getString("Workbench.openEditorErrorDialogTitle"),  //$NON-NLS-1$
					IDEWorkbenchMessages.getString("Workbench.openEditorErrorDialogMessage"), //$NON-NLS-1$
					e.getStatus());
				return;
			}
		}
	
		page.setEditorAreaVisible(true);
	
		// see if we already have an editor
		IEditorPart editor = page.findEditor(input);
		if (editor != null) {
			page.activate(editor);
			return;
		}
	
		try {
			page.openEditor(input, WELCOME_EDITOR_ID);
		} catch (PartInitException e) {
			ErrorDialog.openError(
				win.getShell(),
				IDEWorkbenchMessages.getString("Workbench.openEditorErrorDialogTitle"),  //$NON-NLS-1$
				IDEWorkbenchMessages.getString("Workbench.openEditorErrorDialogMessage"), //$NON-NLS-1$
				e.getStatus());
		}
		return;
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
			title = IDEWorkbenchMessages.format("WorkbenchWindow.shellTitle", new Object[] { workspaceLocation, title }); //$NON-NLS-1$
		}

		IWorkbenchPage currentPage = window.getActivePage();
		if (currentPage != null) {
			IEditorPart editor = currentPage.getActiveEditor();
			if (editor != null) {
				String editorTitle = editor.getTitle();
				title = IDEWorkbenchMessages.format("WorkbenchWindow.shellTitle", new Object[] { editorTitle, title }); //$NON-NLS-1$
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
				title = IDEWorkbenchMessages.format("WorkbenchWindow.shellTitle", new Object[] { label, title }); //$NON-NLS-1$
			}
		}
		
		windowConfigurer.setTitle(title);
	}
	
	/**
	 * Declares all IDE-specific workbench images. This includes both "shared"
	 * images (named in {@link IDE#SharedImages IDE.SharedImages}) and
	 * internal images (named in
	 * {@link org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages
	 * IDEInternalWorkbenchImages}).
	 * 
	 * @see IWorkbenchConfigurer#declareImage
	 */
	private void declareWorkbenchImages() {

		// Subdirectory (under the package containing this class) where 16 color images are
		final String ICONS_PATH = "icons/full/";//$NON-NLS-1$
		final String PATH_CTOOL = ICONS_PATH+"ctool16/"; //Colored toolbar icons - hover.//$NON-NLS-1$
		final String PATH_ETOOL = ICONS_PATH+"etool16/"; //Enabled toolbar icons.//$NON-NLS-1$
		final String PATH_DTOOL = ICONS_PATH+"dtool16/"; //Disabled toolbar icons.//$NON-NLS-1$
	
		final String PATH_CLOCALTOOL = ICONS_PATH+"clcl16/"; //Colored local toolbar icons - hover.//$NON-NLS-1$
		//final String PATH_ELOCALTOOL = ICONS_PATH+"elcl16/"; //Enabled local toolbar icons.//$NON-NLS-1$
		//final String PATH_DLOCALTOOL = ICONS_PATH+"dlcl16/"; //Disabled local toolbar icons.//$NON-NLS-1$
	
		//final String PATH_CVIEW = ICONS_PATH+"cview16/"; //Colored view icons.//$NON-NLS-1$
		//final String PATH_EVIEW = ICONS_PATH+"eview16/"; //View icons//$NON-NLS-1$
	
		final String PATH_OBJECT = ICONS_PATH+"obj16/"; //Model object icons//$NON-NLS-1$
		//final String PATH_DND = ICONS_PATH+"dnd/";  //DND icons//$NON-NLS-1$
		final String PATH_WIZBAN = ICONS_PATH+"wizban/"; //Wizard icons//$NON-NLS-1$
	
		//final String PATH_STAT = ICONS_PATH+"stat/";
		//final String PATH_MISC = ICONS_PATH+"misc/";
		//final String PATH_OVERLAY = ICONS_PATH+"ovr16/";
	
		declareWorkbenchImage(IDE.SharedImages.IMG_TOOL_NEW_WIZARD, PATH_ETOOL+"new_wiz.gif", true); //$NON-NLS-1$
		declareWorkbenchImage(IDE.SharedImages.IMG_TOOL_NEW_WIZARD_HOVER, PATH_CTOOL+"new_wiz.gif", true); //$NON-NLS-1$
		declareWorkbenchImage(IDE.SharedImages.IMG_TOOL_NEW_WIZARD_DISABLED, PATH_DTOOL+"new_wiz.gif", true); //$NON-NLS-1$
		
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_CTOOL_IMPORT_WIZ, PATH_CTOOL+"import_wiz.gif", false); //$NON-NLS-1$
		
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_CTOOL_EXPORT_WIZ, PATH_CTOOL+"export_wiz.gif", false); //$NON-NLS-1$
	
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_CTOOL_BUILD_EXEC, PATH_ETOOL+"build_exec.gif", false); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_CTOOL_BUILD_EXEC_HOVER, PATH_CTOOL+"build_exec.gif", false); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_CTOOL_BUILD_EXEC_DISABLED, PATH_DTOOL+"build_exec.gif", false); //$NON-NLS-1$
		
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_CTOOL_SEARCH_SRC, PATH_ETOOL+"search_src.gif", false); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_CTOOL_SEARCH_SRC_HOVER, PATH_CTOOL+"search_src.gif", false); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_CTOOL_SEARCH_SRC_DISABLED, PATH_DTOOL+"search_src.gif", false); //$NON-NLS-1$
	
	//	declareImage(IDEInternalWorkbenchImages.IMG_CTOOL_REFRESH_NAV, PATH_CTOOL+"refresh_nav.gif");
	//	declareImage(IDEInternalWorkbenchImages.IMG_CTOOL_REFRESH_NAV_HOVER, PATH_CTOOL+"refresh_nav.gif");
	//	declareImage(IDEInternalWorkbenchImages.IMG_CTOOL_REFRESH_NAV_DISABLED, PATH_DTOOL+"refresh_nav.gif");
	
	//	declareImage(IDEInternalWorkbenchImages.IMG_CTOOL_STOP_NAV, PATH_CTOOL+"stop_nav.gif");
	//	declareImage(IDEInternalWorkbenchImages.IMG_CTOOL_STOP_NAV_HOVER, PATH_CTOOL+"stop_nav.gif");
	//	declareImage(IDEInternalWorkbenchImages.IMG_CTOOL_STOP_NAV_DISABLED, PATH_DTOOL+"stop_nav.gif");
	
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_CTOOL_NEXT_NAV, PATH_CTOOL+"next_nav.gif", false); //$NON-NLS-1$

		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_CTOOL_PREVIOUS_NAV, PATH_CTOOL+"prev_nav.gif", false); //$NON-NLS-1$
				
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_WIZBAN_NEW_WIZ, PATH_WIZBAN+"new_wiz.gif", false); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_WIZBAN_NEWPRJ_WIZ, PATH_WIZBAN+"newprj_wiz.gif", false); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFOLDER_WIZ, PATH_WIZBAN+"newfolder_wiz.gif", false); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFILE_WIZ, PATH_WIZBAN+"newfile_wiz.gif", false); //$NON-NLS-1$
	
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORT_WIZ, PATH_WIZBAN+"import_wiz.gif", false); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTDIR_WIZ, PATH_WIZBAN+"importdir_wiz.gif", false); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTZIP_WIZ, PATH_WIZBAN+"importzip_wiz.gif", false); //$NON-NLS-1$
	
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORT_WIZ, PATH_WIZBAN+"export_wiz.gif", false); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTDIR_WIZ, PATH_WIZBAN+"exportdir_wiz.gif", false); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTZIP_WIZ, PATH_WIZBAN+"exportzip_wiz.gif", false); //$NON-NLS-1$
	
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_WIZBAN_RESOURCEWORKINGSET_WIZ, PATH_WIZBAN+"res_workset_wiz.gif", false); //$NON-NLS-1$
	
		/* Cache the commonly used ones */
		
		declareWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT, PATH_OBJECT+"prj_obj.gif", true); //$NON-NLS-1$
		declareWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED, PATH_OBJECT+"cprj_obj.gif", true); //$NON-NLS-1$
		declareWorkbenchImage(IDE.SharedImages.IMG_OPEN_MARKER, PATH_CLOCALTOOL+"gotoobj_tsk.gif", true); //$NON-NLS-1$
			
		// task objects
		//declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_HPRIO_TSK, PATH_OBJECT+"hprio_tsk.gif");
		//declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_MPRIO_TSK, PATH_OBJECT+"mprio_tsk.gif");
		//declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_LPRIO_TSK, PATH_OBJECT+"lprio_tsk.gif");
	
		declareWorkbenchImage(IDE.SharedImages.IMG_OBJS_TASK_TSK, PATH_OBJECT+"taskmrk_tsk.gif", true); //$NON-NLS-1$
		declareWorkbenchImage(IDE.SharedImages.IMG_OBJS_BKMRK_TSK, PATH_OBJECT+"bkmrk_tsk.gif", true); //$NON-NLS-1$
	
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_OBJS_COMPLETE_TSK, PATH_OBJECT+"complete_tsk.gif", true); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_OBJS_INCOMPLETE_TSK, PATH_OBJECT+"incomplete_tsk.gif", true); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_ITEM, PATH_OBJECT+"welcome_item.gif", true); //$NON-NLS-1$
		declareWorkbenchImage(IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_BANNER, PATH_OBJECT+"welcome_banner.gif", true); //$NON-NLS-1$
	
		// synchronization indicator objects
		//declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_WBET_STAT, PATH_OVERLAY+"wbet_stat.gif");
		//declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_SBET_STAT, PATH_OVERLAY+"sbet_stat.gif");
		//declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_CONFLICT_STAT, PATH_OVERLAY+"conflict_stat.gif");
	
		// content locality indicator objects
		//declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_NOTLOCAL_STAT, PATH_STAT+"notlocal_stat.gif");
		//declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_LOCAL_STAT, PATH_STAT+"local_stat.gif");
		//declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_FILLLOCAL_STAT, PATH_STAT+"filllocal_stat.gif");
	}

	/**
	 * Declares an IDE-specific workbench image.
	 * 
	 * @param symbolicName the symbolic name of the image
	 * @param path the path of the image file; this path is relative to the base
	 * of the IDE plug-in
	 * @param shared <code>true</code> if this is a shared image, and
	 * <code>false</code> if this is not a shared image
	 * @see IWorkbenchConfigurer#declareImage
	 */
	private void declareWorkbenchImage(String symbolicName, String path, boolean shared) {
		URL url = null;
		try {
			URL URL_BASIC = Platform.getPlugin(IDEWorkbenchPlugin.IDE_WORKBENCH).getDescriptor().getInstallURL();
			url = new URL(URL_BASIC, path);
		} catch (MalformedURLException e) {
		}
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		configurer.declareImage(symbolicName, desc, shared);
	}
	
}
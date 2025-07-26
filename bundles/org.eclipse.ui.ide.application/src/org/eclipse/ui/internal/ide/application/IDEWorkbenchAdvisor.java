/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430694
 *     Christian Georgi (SAP)            - Bug 432480
 *     Patrik Suzzi <psuzzi@gmail.com>   - Bug 490700, 502050
 *     Vasili Gulevich                   - Bug 501404
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ISelectionConversionService;
import org.eclipse.ui.internal.PluginActionBuilder;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.ide.AboutInfo;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDESelectionConversionService;
import org.eclipse.ui.internal.ide.IDEWorkbenchActivityHelper;
import org.eclipse.ui.internal.ide.IDEWorkbenchErrorHandler;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.ide.undo.WorkspaceUndoMonitor;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.AbstractStatusHandler;
import org.eclipse.urischeme.AutoRegisterSchemeHandlersJob;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * IDE-specified workbench advisor which configures the workbench for use as an
 * IDE.
 * <p>
 * Note: This class replaces <code>org.eclipse.ui.internal.Workbench</code>.
 * </p>
 *
 * @since 3.0
 */
public class IDEWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String WORKBENCH_PREFERENCE_CATEGORY_ID = "org.eclipse.ui.preferencePages.Workbench"; //$NON-NLS-1$

	/**
	 * The dialog setting key to access the known installed features since the
	 * last time the workbench was run.
	 */
	private static final String INSTALLED_FEATURES = "installedFeatures"; //$NON-NLS-1$

	private static IDEWorkbenchAdvisor workbenchAdvisor = null;

	/**
	 * Ordered map of versioned feature ids -gt; info that are new for this
	 * session; <code>null</code> if uninitialized. Key type:
	 * <code>String</code>, Value type: <code>AboutInfo</code>.
	 */
	private Map<String, AboutInfo> newlyAddedBundleGroups;

	/**
	 * Array of <code>AboutInfo</code> for all new installed features that
	 * specify a welcome perspective.
	 */
	private AboutInfo[] welcomePerspectiveInfos = null;

	/**
	 * Helper for managing activities in response to workspace changes.
	 */
	private IDEWorkbenchActivityHelper activityHelper = null;

	private Listener settingsChangeListener;

	/**
	 * Support class for monitoring workspace changes and periodically
	 * validating the undo history
	 */
	private WorkspaceUndoMonitor workspaceUndoMonitor;

	/**
	 * The IDE workbench error handler.
	 */
	private AbstractStatusHandler ideWorkbenchErrorHandler;

	/**
	 * Helper class used to process delayed events.
	 */
	private final DelayedEventsProcessor delayedEventsProcessor;

	private static boolean jfaceComparatorIsSet = false;

	/**
	 * Base wait time between workspace lock attempts
	 */
	private final int workspaceWaitDelay;

	private final Listener closeListener = event -> {
		boolean doExit = IDEWorkbenchWindowAdvisor.promptOnExit(null);
		event.doit = doExit;
		if (!doExit)
			event.type = SWT.None;
	};

	/**
	 * Creates a new workbench advisor instance.
	 */
	public IDEWorkbenchAdvisor() {
		this(1000, null);
	}

	IDEWorkbenchAdvisor(int workspaceWaitDelay, DelayedEventsProcessor processor) {
		super();
		this.workspaceWaitDelay = workspaceWaitDelay;
		if (workbenchAdvisor != null) {
			throw new IllegalStateException();
		}
		workbenchAdvisor = this;

		this.delayedEventsProcessor = processor;
		Display.getDefault().addListener(SWT.Close, closeListener);
	}

	/**
	 * Creates a new workbench advisor instance supporting delayed file open.
	 * @param processor helper class used to process delayed events
	 */
	public IDEWorkbenchAdvisor(DelayedEventsProcessor processor) {
		this(1000, processor);
	}

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {

		PluginActionBuilder.setAllowIdeLogging(true);

		initResourceTracking();

		// make sure we always save and restore workspace state
		configurer.setSaveAndRestore(true);

		// register workspace adapters
		IDE.registerAdapters();

		// register shared images
		declareWorkbenchImages();

		// initialize the activity helper
		activityHelper = IDEWorkbenchActivityHelper.getInstance();

		// initialize the workspace undo monitor
		workspaceUndoMonitor = WorkspaceUndoMonitor.getInstance();

		// show Help button in JFace dialogs
		TrayDialog.setDialogHelpAvailable(true);

		// Set the default value of the preference controlling the workspace
		// name displayed in the window title.
		setWorkspaceNameDefault();

		if (!jfaceComparatorIsSet) {
			// Policy.setComparator can only be called once in Jface lifetime
			Policy.setComparator(Collator.getInstance());
			jfaceComparatorIsSet = true;
		}

		if (!Platform.inDevelopmentMode() && !JUnitTestUtil.isJunitTestRunning()) {
			new AutoRegisterSchemeHandlersJob().schedule();
		}
	}

	protected void initResourceTracking() {
		boolean trackingEnabled = Boolean.getBoolean("org.eclipse.swt.graphics.Resource.reportNonDisposed"); //$NON-NLS-1$
		if (trackingEnabled) {
			Consumer<Error> reporter = createNonDisposedReporter();
			Resource.setNonDisposeHandler(reporter);
		}
	}

	/**
	 * @return reporter instance for SWT leaks reporting, or {@code null} to disable
	 *         SWT leak reporting
	 */
	public Consumer<Error> createNonDisposedReporter() {
		return new IDENonDisposedReporter();
	}

	protected static class IDENonDisposedReporter implements Consumer<Error> {

		@Override
		public void accept(Error allocationStack) {
			IDEWorkbenchPlugin.log(null,
					StatusUtil.newStatus(IStatus.ERROR, "Not properly disposed SWT resource", //$NON-NLS-1$
							allocationStack));
		}

	}

	@Override
	public void preStartup() {
		// Register the build actions
		IProgressService service = PlatformUI.getWorkbench()
				.getProgressService();
		ImageDescriptor newImage = IDEInternalWorkbenchImages
				.getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC);
		service.registerIconForFamily(newImage,
				ResourcesPlugin.FAMILY_MANUAL_BUILD);
		service.registerIconForFamily(newImage,
				ResourcesPlugin.FAMILY_AUTO_BUILD);
	}

	@Override
	public void postStartup() {
		try {
			refreshFromLocal();
			activateProxyService();
			((Workbench) PlatformUI.getWorkbench()).registerService(
					ISelectionConversionService.class,
					new IDESelectionConversionService());

			initializeSettingsChangeListener();
			Display.getCurrent().addListener(SWT.Settings,
					settingsChangeListener);
		} finally {
			// Resume the job manager to allow background jobs to run.
			// The job manager was suspended by the IDEApplication.start method.
			Job.getJobManager().resume();
		}
	}

	/**
	 * Activate the proxy service by obtaining it.
	 */
	protected void activateProxyService() {
		Bundle bundle = Platform.getBundle("org.eclipse.ui.ide"); //$NON-NLS-1$
		Object proxyService = null;
		if (bundle != null) {
			ServiceReference<IProxyService> ref = bundle.getBundleContext().getServiceReference(IProxyService.class);
			if (ref != null)
				proxyService = bundle.getBundleContext().getService(ref);
		}
		if (proxyService == null) {
			IDEWorkbenchPlugin.log("Proxy service could not be found."); //$NON-NLS-1$
		}
	}

	/**
	 * Initialize the listener for settings changes.
	 */
	protected void initializeSettingsChangeListener() {
		settingsChangeListener = new Listener() {

			boolean currentHighContrast = Display.getCurrent()
					.getHighContrast();

			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				if (Display.getCurrent().getHighContrast() == currentHighContrast)
					return;

				currentHighContrast = !currentHighContrast;

				// make sure they really want to do this
				if (new MessageDialog(null, IDEWorkbenchMessages.SystemSettingsChange_title, null,
						IDEWorkbenchMessages.SystemSettingsChange_message, MessageDialog.NONE, 1,
						IDEWorkbenchMessages.SystemSettingsChange_yes, IDEWorkbenchMessages.SystemSettingsChange_no)
								.open() == Window.OK) {
					PlatformUI.getWorkbench().restart();
				}
			}
		};

	}

	@Override
	public void postShutdown() {
		Display.getDefault().removeListener(SWT.Close, closeListener);
		if (activityHelper != null) {
			activityHelper.shutdown();
			activityHelper = null;
		}

		if (workspaceUndoMonitor != null) {
			workspaceUndoMonitor.shutdown();
			workspaceUndoMonitor = null;
		}

		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

		final Runnable disconnectFromWorkspace = new Runnable() {

			int attempts;

			@Override
			public void run() {
				if (isWorkspaceLocked(workspace)) {
					if (attempts < 3) {
						attempts++;
						IDEWorkbenchPlugin.log(null, createErrorStatus("Workspace is locked, waiting...")); //$NON-NLS-1$
						Display.getCurrent().timerExec(workspaceWaitDelay * attempts, this);
					} else {
						IDEWorkbenchPlugin.log(null, createErrorStatus("Workspace is locked and can't be saved.")); //$NON-NLS-1$
					}
					return;
				}
				disconnectFromWorkspace();
			}

			IStatus createErrorStatus(String exceptionMessage) {
				return new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
						IDEWorkbenchMessages.ProblemsSavingWorkspace, new IllegalStateException(exceptionMessage));
			}
		};

		// postShutdown may be called while workspace is locked, for example -
		// during file save operation, see
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=501404.
		// Disconnect is postponed until workspace is unlocked to prevent
		// deadlock between background thread launched by
		// disconnectFromWorkspace() and current thread
		// WARNING: this condition makes code that relies on synchronous
		// disconnect very hard to discover and test
		if (workspace != null) {
			if (isWorkspaceLocked(workspace)) {
				Display.getCurrent().asyncExec(disconnectFromWorkspace);
			} else {
				disconnectFromWorkspace.run();
			}
		}
		// This completes workbench lifecycle.
		// Another advisor can now be created for a new workbench instance.
		workbenchAdvisor = null;
	}

	protected boolean isWorkspaceLocked(IWorkspace workspace) {
		ISchedulingRule currentRule = Job.getJobManager().currentRule();
		return currentRule != null && currentRule.isConflicting(workspace.getRoot());
	}

	@Override
	public boolean preShutdown() {
		Display.getCurrent().removeListener(SWT.Settings,
				settingsChangeListener);
		return super.preShutdown();
	}

	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new IDEWorkbenchWindowAdvisor(this, configurer);
	}

	/**
	 * Return true if the intro plugin is present and false otherwise.
	 *
	 * @return boolean
	 */
	public boolean hasIntro() {
		return getWorkbenchConfigurer().getWorkbench().getIntroManager()
				.hasIntro();
	}

	protected void refreshFromLocal() {
		String[] commandLineArgs = Platform.getCommandLineArgs();
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore();
		boolean refresh = store
				.getBoolean(IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP);
		if (!refresh) {
			return;
		}

		// Do not refresh if it was already done by core on startup.
		for (String commandLineArg : commandLineArgs) {
			if (commandLineArg.equalsIgnoreCase("-refresh")) { //$NON-NLS-1$
				return;
			}
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IContainer root = workspace.getRoot();

		// We should try to use RefreshManager which avoids multiple refresh requests
		// for same objects
		if (workspace instanceof Workspace) {
			Workspace wsp = (Workspace) workspace;
			if (!wsp.isCrashed()) {
				// Only refresh if no crash happened before: the workspace itself
				// triggers refresh in that case, see Workspace.open()
				wsp.getRefreshManager().refresh(root);
			}
		} else {
			Job job = new WorkspaceJob(IDEWorkbenchMessages.Workspace_refreshing) {
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					root.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					return Status.OK_STATUS;
				}
			};
			job.setRule(root);
			job.schedule();
		}
	}

	protected static class CancelableProgressMonitorWrapper extends
			ProgressMonitorWrapper {
		private double total = 0;
		private ProgressMonitorJobsDialog dialog;

		CancelableProgressMonitorWrapper(IProgressMonitor monitor,
				ProgressMonitorJobsDialog dialog) {
			super(monitor);
			this.dialog = dialog;
		}

		@Override
		public void internalWorked(double work) {
			super.internalWorked(work);
			total += work;
			updateProgressDetails();
		}

		@Override
		public void worked(int work) {
			super.worked(work);
			total += work;
			updateProgressDetails();
		}

		@Override
		public void beginTask(String name, int totalWork) {
			super.beginTask(name, totalWork);
			subTask(IDEWorkbenchMessages.IDEWorkbenchAdvisor_preHistoryCompaction);
		}

		private void updateProgressDetails() {
			if (!isCanceled() && Math.abs(total - 4.0) < 0.0001 /* right before history compacting */) {
				subTask(IDEWorkbenchMessages.IDEWorkbenchAdvisor_cancelHistoryPruning);
				dialog.setCancelable(true);
			}
			if (Math.abs(total - 5.0) < 0.0001 /* history compacting finished */) {
				subTask(IDEWorkbenchMessages.IDEWorkbenchAdvisor_postHistoryCompaction);
				dialog.setCancelable(false);
			}
		}
	}

	protected static class CancelableProgressMonitorJobsDialog extends
			ProgressMonitorJobsDialog {

		public CancelableProgressMonitorJobsDialog(Shell parent) {
			super(parent);
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			registerCancelButtonListener();
		}

		public void registerCancelButtonListener() {
			cancel.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					subTaskLabel.setText(""); //$NON-NLS-1$
				}
			});
		}
	}

	/**
	 * Disconnect from the core workspace.
	 *
	 * Locks workspace in a background thread, should not be called while
	 * holding any workspace locks.
	 */
	protected void disconnectFromWorkspace() {
		// save the workspace
		final MultiStatus status = new MultiStatus(IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
				IDEWorkbenchMessages.ProblemSavingWorkbench);
		try {
			final ProgressMonitorJobsDialog p = new CancelableProgressMonitorJobsDialog(
					null);

			final boolean applyPolicy = ResourcesPlugin.getWorkspace()
					.getDescription().isApplyFileStatePolicy();

			IRunnableWithProgress runnable = monitor -> {
				try {
					if (applyPolicy)
						monitor = new CancelableProgressMonitorWrapper(monitor, p);

					status.merge(((Workspace) ResourcesPlugin.getWorkspace()).save(true, true, monitor));
				} catch (CoreException e) {
					status.merge(e.getStatus());
				}
			};

			p.run(true, false, runnable);
		} catch (InvocationTargetException e) {
			status
					.merge(new Status(IStatus.ERROR,
							IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
							IDEWorkbenchMessages.InternalError, e
									.getTargetException()));
		} catch (InterruptedException e) {
			status.merge(new Status(IStatus.ERROR,
					IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
					IDEWorkbenchMessages.InternalError, e));
		}
		if (!status.isOK()) {
			ErrorDialog.openError(null,
					IDEWorkbenchMessages.ProblemsSavingWorkspace, null, status,
					IStatus.ERROR | IStatus.WARNING);
			IDEWorkbenchPlugin.log(
					IDEWorkbenchMessages.ProblemsSavingWorkspace, status);
		}
	}

	@Override
	public IAdaptable getDefaultPageInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	public String getInitialWindowPerspectiveId() {
		int index = PlatformUI.getWorkbench().getWorkbenchWindowCount() - 1;

		String perspectiveId = null;
		AboutInfo[] welcomeInfos = getWelcomePerspectiveInfos();
		if (index >= 0 && welcomeInfos != null && index < welcomeInfos.length) {
			perspectiveId = welcomeInfos[index].getWelcomePerspectiveId();
		}
		if (perspectiveId == null) {
			perspectiveId = IDE.RESOURCE_PERSPECTIVE_ID;
		}
		return perspectiveId;
	}

	/**
	 * Returns the map of versioned feature ids -&gt; info object for all installed
	 * features. The format of the versioned feature id (the key of the map) is
	 * featureId + ":" + versionId.
	 *
	 * @return map of versioned feature ids -&gt; info object (key type:
	 *         <code>String</code>, value type: <code>AboutInfo</code>)
	 * @since 3.0
	 */
	protected Map<String, AboutInfo> computeBundleGroupMap() {
		// use tree map to get predicable order
		Map<String, AboutInfo> ids = new TreeMap<>();

		IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
		for (IBundleGroupProvider provider : providers) {
			IBundleGroup[] groups = provider.getBundleGroups();
			for (IBundleGroup group : groups) {
				AboutInfo info = new AboutInfo(group);

				String version = info.getVersionId();
				version = version == null ? "0.0.0" //$NON-NLS-1$
						: new Version(version).toString();
				String versionedFeature = group.getIdentifier() + ":" + version; //$NON-NLS-1$

				ids.put(versionedFeature, info);
			}
		}

		return ids;
	}

	/**
	 * Returns the ordered map of versioned feature ids -&gt; AboutInfo that are
	 * new for this session.
	 *
	 * @return ordered map of versioned feature ids (key type:
	 *         <code>String</code>) -&gt; infos (value type:
	 *         <code>AboutInfo</code>).
	 */
	public Map<String, AboutInfo> getNewlyAddedBundleGroups() {
		if (newlyAddedBundleGroups == null) {
			newlyAddedBundleGroups = createNewBundleGroupsMap();
		}
		return newlyAddedBundleGroups;
	}

	/**
	 * Updates the old features setting and returns a map of new features.
	 */
	protected Map<String, AboutInfo> createNewBundleGroupsMap() {
		// retrieve list of installed bundle groups from last session
		IDialogSettings settings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(IDE.class)).getDialogSettings();
		String[] previousFeaturesArray = settings.getArray(INSTALLED_FEATURES);

		// get a map of currently installed bundle groups and store it for next
		// session
		Map<String, AboutInfo> bundleGroups = computeBundleGroupMap();
		String[] currentFeaturesArray = new String[bundleGroups.size()];
		bundleGroups.keySet().toArray(currentFeaturesArray);
		settings.put(INSTALLED_FEATURES, currentFeaturesArray);

		// remove the previously known from the current set
		if (previousFeaturesArray != null) {
			for (String previousFeature : previousFeaturesArray) {
				bundleGroups.remove(previousFeature);
			}
		}

		return bundleGroups;
	}

	/**
	 * Sets the default value of the preference controlling the workspace name
	 * displayed in the window title to the name of the workspace directory.
	 * This preference cannot be set in the preference initializer because the
	 * workspace directory may not be known when the preference initializer is
	 * called.
	 */
	protected static void setWorkspaceNameDefault() {
		IPreferenceStore preferences = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		String workspaceNameDefault = preferences.getDefaultString(IDEInternalPreferences.WORKSPACE_NAME);
		if (workspaceNameDefault != null && !workspaceNameDefault.isEmpty())
			return; // Default is set in a plugin customization file - don't change it.
		IPath workspaceDir = Platform.getLocation();
		if (workspaceDir == null)
			return;
		String workspaceName = workspaceDir.lastSegment();
		if (workspaceName == null)
			return;
		preferences.setDefault(IDEInternalPreferences.WORKSPACE_NAME, workspaceName);
	}

	/**
	 * Declares all IDE-specific workbench images. This includes both "shared"
	 * images (named in {@link org.eclipse.ui.ide.IDE.SharedImages}) and internal images (named in
	 * {@link org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages}).
	 *
	 * @see IWorkbenchConfigurer#declareImage
	 */
	protected void declareWorkbenchImages() {

		final String ICONS_PATH = "$nl$/icons/full/";//$NON-NLS-1$
		// Local toolbar icons
		final String PATH_ELOCALTOOL = ICONS_PATH + "elcl16/"; //$NON-NLS-1$
		// Toolbar icons
		final String PATH_ETOOL = ICONS_PATH + "etool16/"; //$NON-NLS-1$
		// Model objects
		final String PATH_OBJECT = ICONS_PATH + "obj16/"; //$NON-NLS-1$
		// Wizard icons
		final String PATH_WIZBAN = ICONS_PATH + "wizban/"; //$NON-NLS-1$
		// View icons
		final String PATH_EVIEW= ICONS_PATH + "eview16/"; //$NON-NLS-1$

		Bundle ideBundle = Platform.getBundle(IDEWorkbenchPlugin.IDE_WORKBENCH);

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC, PATH_ETOOL
						+ "build_exec.svg", false); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_HOVER,
				IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_DISABLED,
				PATH_ETOOL + "build_exec.svg", false); //$NON-NLS-1$

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC, PATH_ETOOL
						+ "search_src.svg", false); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC_HOVER,
				IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC_DISABLED,
				PATH_ETOOL + "search_src.svg", false); //$NON-NLS-1$

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_ETOOL_NEXT_NAV, PATH_ETOOL
						+ "next_nav.svg", //$NON-NLS-1$
				false);

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_ETOOL_PREVIOUS_NAV, PATH_ETOOL
						+ "prev_nav.svg", //$NON-NLS-1$
				false);

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_WIZBAN_NEWPRJ_WIZ, PATH_WIZBAN
						+ "newprj_wiz.svg", //$NON-NLS-1$
				false);
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFOLDER_WIZ,
				PATH_WIZBAN + "newfolder_wiz.svg", false); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFILE_WIZ, PATH_WIZBAN
						+ "newfile_wiz.svg", //$NON-NLS-1$
				false);

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTDIR_WIZ,
				PATH_WIZBAN + "importdir_wiz.svg", false); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTZIP_WIZ,
				PATH_WIZBAN + "importzip_wiz.svg", false); //$NON-NLS-1$

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTDIR_WIZ,
				PATH_WIZBAN + "exportdir_wiz.svg", false); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTZIP_WIZ,
				PATH_WIZBAN + "exportzip_wiz.svg", false); //$NON-NLS-1$

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_WIZBAN_RESOURCEWORKINGSET_WIZ,
				PATH_WIZBAN + "workset_wiz.svg", false); //$NON-NLS-1$

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_DLGBAN_SAVEAS_DLG, PATH_WIZBAN
						+ "saveas_wiz.svg", //$NON-NLS-1$
				false);

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_DLGBAN_QUICKFIX_DLG, PATH_WIZBAN
						+ "quick_fix.svg", //$NON-NLS-1$
				false);

		declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT,
				PATH_OBJECT + "prj_obj.svg", true); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle,
				IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED, PATH_OBJECT
						+ "cprj_obj.svg", //$NON-NLS-1$
				true);
		declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OPEN_MARKER,
				PATH_ELOCALTOOL + "gotoobj_tsk.svg", true); //$NON-NLS-1$


		// Quick fix icons
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_ELCL_QUICK_FIX_ENABLED,
				IDEInternalWorkbenchImages.IMG_DLCL_QUICK_FIX_DISABLED,
				PATH_ELOCALTOOL + "smartmode_co.svg", true); //$NON-NLS-1$

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_WARNING,
				PATH_OBJECT + "quickfix_warning_obj.svg", true); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_ERROR,
				PATH_OBJECT + "quickfix_error_obj.svg", true); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_INFO,
				PATH_OBJECT + "quickfix_info_obj.svg", true); //$NON-NLS-1$


		// task objects
		declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_TASK_TSK,
				PATH_OBJECT + "taskmrk_tsk.svg", true); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_BKMRK_TSK,
				PATH_OBJECT + "bkmrk_tsk.svg", true); //$NON-NLS-1$

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_OBJS_COMPLETE_TSK, PATH_OBJECT
						+ "complete_tsk.svg", //$NON-NLS-1$
				true);
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_OBJS_INCOMPLETE_TSK, PATH_OBJECT
						+ "incomplete_tsk.svg", //$NON-NLS-1$
				true);
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_ITEM, PATH_OBJECT
						+ "welcome_item.svg", //$NON-NLS-1$
				true);
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_BANNER, PATH_OBJECT
						+ "welcome_banner.svg", //$NON-NLS-1$
				true);
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_OBJS_ERROR_PATH, PATH_OBJECT
						+ "error_tsk.svg", //$NON-NLS-1$
				true);
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_OBJS_WARNING_PATH, PATH_OBJECT
						+ "warn_tsk.svg", //$NON-NLS-1$
				true);
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_OBJS_INFO_PATH, PATH_OBJECT
						+ "info_tsk.svg", //$NON-NLS-1$
				true);

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_LCL_FLAT_LAYOUT, PATH_ELOCALTOOL
						+ "flatLayout.svg", //$NON-NLS-1$
				true);
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_LCL_HIERARCHICAL_LAYOUT,
				PATH_ELOCALTOOL + "hierarchicalLayout.svg", true); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEM_CATEGORY,
				PATH_ETOOL + "problem_category.svg", true); //$NON-NLS-1$

		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW,
				PATH_EVIEW + "problems_view.svg", true); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_ERROR,
				PATH_EVIEW + "problems_view_error.svg", true); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_WARNING,
				PATH_EVIEW + "problems_view_warning.svg", true); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle,
				IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_INFO,
				PATH_EVIEW + "problems_view_info.svg", true); //$NON-NLS-1$
	}

	/**
	 * Declares an IDE-specific workbench image.
	 *
	 * @param symbolicName
	 *            the symbolic name of the image
	 * @param path
	 *            the path of the image file; this path is relative to the base
	 *            of the IDE plug-in
	 * @param shared
	 *            <code>true</code> if this is a shared image, and
	 *            <code>false</code> if this is not a shared image
	 * @see IWorkbenchConfigurer#declareImage
	 */
	protected void declareWorkbenchImage(Bundle ideBundle, String symbolicName,
			String path, boolean shared) {
		declareWorkbenchImage(ideBundle, symbolicName, null, path, shared);
	}

	private void declareWorkbenchImage(Bundle ideBundle, String symbolicName, String disabledSymbolicName, String path,
			boolean shared) {
		URL url = FileLocator.find(ideBundle, IPath.fromOSString(path), null);
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		getWorkbenchConfigurer().declareImage(symbolicName, desc, shared);
		if (disabledSymbolicName != null) {
			ImageDescriptor disabledDescriptor = ImageDescriptor.createWithFlags(desc, SWT.IMAGE_DISABLE);
			getWorkbenchConfigurer().declareImage(disabledSymbolicName, disabledDescriptor, shared);
		}
	}

	@Override
	public String getMainPreferencePageId() {
		// indicate that we want the Workench preference page to be prominent
		return WORKBENCH_PREFERENCE_CATEGORY_ID;
	}

	/**
	 * Returns the location specified in command line when -showlocation is
	 * defined. Otherwise returns null
	 *
	 * @return may return null
	 */
	public String getCommandLineLocation() {
		IEclipseContext context = getWorkbenchConfigurer().getWorkbench().getService(IEclipseContext.class);
		return context != null ? (String) context.get(E4Workbench.FORCED_SHOW_LOCATION) : null;
	}

	/**
	 * Returns the location to show in the window title, depending on a
	 * {@link IDEInternalPreferences#SHOW_LOCATION} user preference. Note that
	 * this may be overridden by the '-showlocation' command line argument.
	 *
	 * @return the location string, or <code>null</code> if the location is not
	 *         being shown
	 */
	public String getWorkspaceLocation() {
		String location = getCommandLineLocation();
		// read command line, which has priority
		if (location != null) {
			return location;
		}
		// read the preferences
		if (IDEWorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(IDEInternalPreferences.SHOW_LOCATION)) {
			// show the full location
			return Platform.getLocation().toOSString();
		}
		return null;
	}

	/**
	 * @return the welcome perspective infos, or <code>null</code> if none or
	 *         if they should be ignored due to the new intro being present
	 */
	public AboutInfo[] getWelcomePerspectiveInfos() {
		if (welcomePerspectiveInfos == null) {
			// support old welcome perspectives if intro plugin is not present
			if (!hasIntro()) {
				Map<String, AboutInfo> m = getNewlyAddedBundleGroups();
				ArrayList<AboutInfo> list = new ArrayList<>(m.size());
				for (AboutInfo info : m.values()) {
					if (info != null && info.getWelcomePerspectiveId() != null
							&& info.getWelcomePageURL() != null) {
						list.add(info);
					}
				}
				welcomePerspectiveInfos = new AboutInfo[list.size()];
				list.toArray(welcomePerspectiveInfos);
			}
		}
		return welcomePerspectiveInfos;
	}

	@Override
	public synchronized AbstractStatusHandler getWorkbenchErrorHandler() {
		if (ideWorkbenchErrorHandler == null) {
			ideWorkbenchErrorHandler = new IDEWorkbenchErrorHandler(
					getWorkbenchConfigurer());
		}
		return ideWorkbenchErrorHandler;
	}

	@Override
	public void eventLoopIdle(Display display) {
		if (delayedEventsProcessor != null)
			delayedEventsProcessor.catchUp(display);
		super.eventLoopIdle(display);
	}
}

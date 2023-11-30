/*******************************************************************************
 * Copyright (c) 2003, 2020 IBM Corporation and others.
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
 *     Helmut J. Haigermoser -  Bug 359838 - The "Workspace Unavailable" error
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422954
 *     Christian Georgi (SAP) - Bug 423882 - Warn user if workspace is newer than IDE
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 427393, 455162
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 514355
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application;

import static org.eclipse.jface.util.Util.isValid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.ChooseWorkspaceData;
import org.eclipse.ui.internal.ide.ChooseWorkspaceDialog;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * The "main program" for the Eclipse IDE.
 *
 * @since 3.0
 */
public class IDEApplication implements IApplication, IExecutableExtension {

	/**
	 * The name of the folder containing metadata information for the workspace.
	 */
	public static final String METADATA_FOLDER = ".metadata"; //$NON-NLS-1$

	private static final String VERSION_FILENAME = "version.ini"; //$NON-NLS-1$

	// Use the branding plug-in of the platform feature since this is most likely
	// to change on an update of the IDE.
	private static final String WORKSPACE_CHECK_REFERENCE_BUNDLE_NAME = "org.eclipse.platform"; //$NON-NLS-1$
	private static final Version WORKSPACE_CHECK_REFERENCE_BUNDLE_VERSION;
	static {
		Bundle bundle = Platform.getBundle(WORKSPACE_CHECK_REFERENCE_BUNDLE_NAME);
		WORKSPACE_CHECK_REFERENCE_BUNDLE_VERSION = bundle != null ? bundle.getVersion() : null/*not installed*/;
	}

	private static final String WORKSPACE_CHECK_REFERENCE_BUNDLE_NAME_LEGACY = "org.eclipse.core.runtime"; //$NON-NLS-1$
	private static final String WORKSPACE_CHECK_LEGACY_VERSION_INCREMENTED = "2"; //$NON-NLS-1$   legacy version=1

	/**
	 * Return value when the user wants to retry loading the current workspace
	 */
	private static final int RETRY_LOAD = 0;

	/**
	 * A special return code that will be recognized by the PDE launcher and used to
	 * show an error dialog if the workspace is locked.
	 */
	private static final Integer EXIT_WORKSPACE_LOCKED = Integer.valueOf(15);

	/**
	 * The ID of the application plug-in
	 */
	public static final String PLUGIN_ID = "org.eclipse.ui.ide.application"; //$NON-NLS-1$

	/**
	 * Creates a new IDE application.
	 */
	public IDEApplication() {
		// There is nothing to do for IDEApplication
	}

	@Override
	public Object start(IApplicationContext appContext) throws Exception {
		// Suspend the job manager to prevent background jobs from running. This
		// is done to reduce resource contention during startup.
		// The job manager will be resumed by the
		// IDEWorkbenchAdvisor.postStartup method.
		Job.getJobManager().suspend();

		Display display = createDisplay();
		// processor must be created before we start event loop
		DelayedEventsProcessor processor = new DelayedEventsProcessor(display);

		try {

			// look and see if there's a splash shell we can parent off of
			Shell shell = WorkbenchPlugin.getSplashShell(display);
			if (shell != null) {
				// should should set the icon and message for this shell to be the
				// same as the chooser dialog - this will be the guy that lives in
				// the task bar and without these calls you'd have the default icon
				// with no message.
				shell.setText(ChooseWorkspaceDialog.getWindowTitle());
				shell.setImages(Window.getDefaultImages());
			}

			Object instanceLocationCheck = checkInstanceLocation(shell, appContext.getArguments());
			if (instanceLocationCheck != null) {
				WorkbenchPlugin.unsetSplashShell(display);
				return instanceLocationCheck;
			}

			// create the workbench with this advisor and run it until it exits
			// N.B. createWorkbench remembers the advisor, and also registers
			// the workbench globally so that all UI plug-ins can find it using
			// PlatformUI.getWorkbench() or AbstractUIPlugin.getWorkbench()
			int returnCode = PlatformUI.createAndRunWorkbench(display,
					new IDEWorkbenchAdvisor(processor));

			// the workbench doesn't support relaunch yet (bug 61809) so
			// for now restart is used, and exit data properties are checked
			// here to substitute in the relaunch return code if needed
			if (returnCode != PlatformUI.RETURN_RESTART) {
				return EXIT_OK;
			}

			// if the exit code property has been set to the relaunch code, then
			// return that code now, otherwise this is a normal restart
			return EXIT_RELAUNCH.equals(Integer.getInteger(Workbench.PROP_EXIT_CODE)) ? EXIT_RELAUNCH
					: EXIT_RESTART;
		} finally {
			if (display != null) {
				display.dispose();
			}
			Location instanceLoc = Platform.getInstanceLocation();
			if (instanceLoc != null)
				instanceLoc.release();
		}
	}

	/**
	 * Creates the display used by the application.
	 *
	 * @return the display used by the application
	 */
	protected Display createDisplay() {
		return PlatformUI.createDisplay();
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {
		// There is nothing to do for IDEApplication
	}

	/**
	 * Return <code>null</code> if a valid workspace path has been set and an exit code otherwise.
	 * Prompt for and set the path if possible and required.
	 *
	 * @param applicationArguments the command line arguments
	 * @return <code>null</code> if a valid instance location has been set and an exit code
	 *         otherwise
	 */
	@SuppressWarnings("rawtypes")
	protected Object checkInstanceLocation(Shell shell, Map applicationArguments) {
		// -data @none was specified but an ide requires workspace
		Location instanceLoc = Platform.getInstanceLocation();
		if (instanceLoc == null) {
			MessageDialog
					.openError(
							shell,
							IDEWorkbenchMessages.IDEApplication_workspaceMandatoryTitle,
							IDEWorkbenchMessages.IDEApplication_workspaceMandatoryMessage);
			return EXIT_OK;
		}

		boolean force = false;

		// -data "/valid/path", workspace already set
		if (instanceLoc.isSet()) {
			// make sure the meta data version is compatible (or the user has
			// chosen to overwrite it).
			ReturnCode result = checkValidWorkspace(shell, instanceLoc.getURL());
			if (result == ReturnCode.EXIT) {
				return EXIT_OK;
			}
			if (result == ReturnCode.VALID) {
				// at this point its valid, so try to lock it and update the
				// metadata version information if successful
				try {
					if (instanceLoc.lock()) {
						writeWorkspaceVersion();
						return null;
					}

					// we failed to create the directory.
					// Two possibilities:
					// 1. directory is already in use
					// 2. directory could not be created
					File workspaceDirectory = new File(instanceLoc.getURL().getFile());
					if (workspaceDirectory.exists()) {
						if (isDevLaunchMode(applicationArguments)) {
							return EXIT_WORKSPACE_LOCKED;
						}
						MessageDialog.openError(
								shell,
								IDEWorkbenchMessages.IDEApplication_workspaceCannotLockTitle,
								NLS.bind(IDEWorkbenchMessages.IDEApplication_workspaceCannotLockMessage, workspaceDirectory.getAbsolutePath()));
					} else {
						MessageDialog.openError(
								shell,
								IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetTitle,
								IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetMessage);
					}
				} catch (IOException e) {
					IDEWorkbenchPlugin.log("Could not obtain lock for workspace location", //$NON-NLS-1$
							e);
					MessageDialog
					.openError(
							shell,
							IDEWorkbenchMessages.InternalError,
							e.getMessage());
				}
				return EXIT_OK;
			}
			if (result == ReturnCode.INVALID) {
				force = true;
			}
		}

		// -data @noDefault or -data not specified => prompt and set
		// -data is specified but invalid according to checkValidWorkspace(): re-launch
		ChooseWorkspaceData launchData = new ChooseWorkspaceData(instanceLoc.getDefault());

		boolean parentShellVisible = false;
		if (isValid(shell)) {
			parentShellVisible = shell.getVisible();
			// bug 455162, bug 427393: hide the splash if the workspace
			// prompt dialog should be opened
			if (parentShellVisible && launchData.getShowDialog()) {
				shell.setVisible(false);
			}
		}

		int returnValue = -1;
		URL workspaceUrl = null;
		while (true) {
			if (returnValue != RETRY_LOAD) {
				try {
					workspaceUrl = promptForWorkspace(shell, launchData, force);
				} catch (OperationCanceledException e) {
					// Chosen workspace location was not compatible, select default one
					launchData = new ChooseWorkspaceData(instanceLoc.getDefault());

					// Bug 551260: ignore 'use default location' setting on retries. If the user has
					// no opportunity to set another location it would only fail again and again and
					// again.
					force = true;
					continue;
				}
			}
			if (workspaceUrl == null) {
				return EXIT_OK;
			}

			// if there is an error with the first selection, then force the
			// dialog to open to give the user a chance to correct
			force = true;

			try {
				if (instanceLoc.isSet()) {
					// restart with new location
					return Workbench.setRestartArguments(workspaceUrl.getFile());
				}

				// the operation will fail if the url is not a valid
				// instance data area, so other checking is unneeded
				if (instanceLoc.set(workspaceUrl, true)) {
					launchData.writePersistedData();
					writeWorkspaceVersion();
					return null;
				}
			} catch (IllegalStateException e) {
				MessageDialog
						.openError(
								shell,
								IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetTitle,
								IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetMessage);
				return EXIT_OK;
			} catch (IOException e) {
				MessageDialog
				.openError(
						shell,
						IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetTitle,
						IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetMessage);
			}

			// by this point it has been determined that the workspace is
			// already in use -- force the user to choose again
			MessageDialog dialog = new MessageDialog(null, IDEWorkbenchMessages.IDEApplication_workspaceInUseTitle,
					null, NLS.bind(IDEWorkbenchMessages.IDEApplication_workspaceInUseMessage, workspaceUrl.getFile()),
					MessageDialog.ERROR, 1, IDEWorkbenchMessages.IDEApplication_workspaceInUse_Retry,
					IDEWorkbenchMessages.IDEApplication_workspaceInUse_Choose);
			// the return value influences the next loop's iteration
			returnValue = dialog.open();
			// Remember the locked workspace as recent workspace
			launchData.writePersistedData();
		}
	}

	@SuppressWarnings("rawtypes")
	private static boolean isDevLaunchMode(Map args) {
		// see org.eclipse.pde.internal.core.PluginPathFinder.isDevLaunchMode()
		if (Boolean.getBoolean("eclipse.pde.launch")) //$NON-NLS-1$
			return true;
		return args.containsKey("-pdelaunch"); //$NON-NLS-1$
	}

	/**
	 * Open a workspace selection dialog on the argument shell, populating the
	 * argument data with the user's selection. Perform first level validation
	 * on the selection by comparing the version information. This method does
	 * not examine the runtime state (e.g., is the workspace already locked?).
	 *
	 * @param force
	 *            setting to true makes the dialog open regardless of the
	 *            showDialog value
	 * @return An URL storing the selected workspace or null if the user has
	 *         canceled the launch operation.
	 */
	private URL promptForWorkspace(Shell shell, ChooseWorkspaceData launchData,
			boolean force) {
		URL url = null;

		do {
			showChooseWorkspaceDialog(shell, launchData, force);

			String instancePath = launchData.getSelection();
			if (instancePath == null) {
				return null;
			}

			// the dialog is not forced on the first iteration, but is on every
			// subsequent one -- if there was an error then the user needs to be
			// allowed to fix it
			force = true;

			// 70576: don't accept empty input
			if (instancePath.length() <= 0) {
				MessageDialog
				.openError(
						shell,
						IDEWorkbenchMessages.IDEApplication_workspaceEmptyTitle,
						IDEWorkbenchMessages.IDEApplication_workspaceEmptyMessage);
				continue;
			}

			// create the workspace if it does not already exist
			File workspace = new File(instancePath);
			if (!workspace.exists()) {
				workspace.mkdir();
			}

			try {
				// Don't use File.toURL() since it adds a leading slash that Platform does not
				// handle properly.  See bug 54081 for more details.
				String path = workspace.getAbsolutePath().replace(
						File.separatorChar, '/');
				url = new URL("file", null, path); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				MessageDialog
						.openError(
								shell,
								IDEWorkbenchMessages.IDEApplication_workspaceInvalidTitle,
								IDEWorkbenchMessages.IDEApplication_workspaceInvalidMessage);
				continue;
			}
			ReturnCode result = checkValidWorkspace(shell, url);
			if (result == ReturnCode.INVALID) {
				throw new OperationCanceledException("Invalid workspace location: " + url); //$NON-NLS-1$
			}
			if (result == ReturnCode.EXIT) {
				return null;
			}
			return url;
		} while (true);
	}

	/**
	 * Show the choose workspace dialog to the user (if needed).
	 * @param shell      parentShell the parent shell for this dialog
	 * @param launchData launchData the launch data from past launches
	 * @param force      true if the dialog should be opened regardless of the value
	 *                   of the show dialog checkbox
	 */
	protected void showChooseWorkspaceDialog(Shell shell, ChooseWorkspaceData launchData, boolean force) {
		new ChooseWorkspaceDialog(shell, launchData, false, true) {
			@Override
			protected Shell getParentShell() {
				// Bug 429308: Make workspace selection dialog visible
				// in the task manager of the OS
				return null;
			}

		}.prompt(force);
	}

	/**
	 * Result of the {@link IDEApplication#checkValidWorkspace(Shell, URL)}
	 * operation
	 */
	public enum ReturnCode {
		/** valid workspace */
		VALID,
		/** invalid workspace */
		INVALID,
		/** exit application */
		EXIT
	}

	/**
	 * Return true if the argument directory is ok to use as a workspace and false
	 * otherwise. A version check will be performed, and a confirmation box may be
	 * displayed on the argument shell if an older version is detected.
	 *
	 * @return {@link ReturnCode#VALID} if the argument URL is ok to use as a
	 *         workspace, {@link ReturnCode#INVALID} otherwise or
	 *         {@link ReturnCode#EXIT} to exit application.
	 */
	protected ReturnCode checkValidWorkspace(Shell shell, URL url) {
		// a null url is not a valid workspace
		if (url == null) {
			return ReturnCode.INVALID;
		}

		if (WORKSPACE_CHECK_REFERENCE_BUNDLE_VERSION == null) {
			// no reference bundle installed, no check possible
			return ReturnCode.VALID;
		}

		int versionCompareResult = compareWorkspaceAndIdeVersions(url);

		// equality test is required since any version difference (newer
		// or older) may result in data being trampled
		if (versionCompareResult == 0) {
			return ReturnCode.VALID;
		}

		// At this point workspace has been detected to be from a version
		// other than the current ide version -- find out if the user wants
		// to use it anyhow.
		int severity;
		String title;
		String message;
		if (versionCompareResult < 0) {
			// Workspace < IDE. Update must be possible without issues,
			// so only inform user about it.
			severity = MessageDialog.INFORMATION;
			title = IDEWorkbenchMessages.IDEApplication_versionTitle_olderWorkspace;
			message = NLS.bind(IDEWorkbenchMessages.IDEApplication_versionMessage_olderWorkspace, url.getFile());
		} else {
			// Workspace > IDE. It must have been opened with a newer IDE version.
			// Downgrade might be problematic, so warn user about it.
			severity = MessageDialog.WARNING;
			title = IDEWorkbenchMessages.IDEApplication_versionTitle_newerWorkspace;
			message = NLS.bind(IDEWorkbenchMessages.IDEApplication_versionMessage_newerWorkspace, url.getFile());
		}

		IPersistentPreferenceStore prefStore = new ScopedPreferenceStore(ConfigurationScope.INSTANCE, IDEWorkbenchPlugin.IDE_WORKBENCH);
		boolean keepOnWarning = prefStore.getBoolean(IDEInternalPreferences.WARN_ABOUT_WORKSPACE_INCOMPATIBILITY);
		if (keepOnWarning) {
			LinkedHashMap<String, Integer> buttonLabelToId = new LinkedHashMap<>();
			buttonLabelToId.put(IDEWorkbenchMessages.IDEApplication_version_continue, IDialogConstants.YES_ID);
			buttonLabelToId.put(IDEWorkbenchMessages.IDEApplication_version_switch, IDialogConstants.RETRY_ID);
			buttonLabelToId.put(IDEWorkbenchMessages.IDEApplication_version_exit, IDialogConstants.CLOSE_ID);
			MessageDialogWithToggle dialog = new MessageDialogWithToggle(shell, title, null, message, severity,
					buttonLabelToId, 0, IDEWorkbenchMessages.IDEApplication_version_doNotWarnAgain, false) {
				@Override
				protected Shell getParentShell() {
					// Bug 429308: Make workspace selection dialog visible
					// in the task manager of the OS
					return null;
				}
			};
			// hide splash if any
			if (isValid(shell)) {
				shell.setVisible(false);
			}

			int returnCode = dialog.open();
			if (returnCode == IDialogConstants.RETRY_ID || returnCode == SWT.DEFAULT) {
				return ReturnCode.INVALID;
			}
			if (returnCode == IDialogConstants.CLOSE_ID) {
				return ReturnCode.EXIT;
			}
			keepOnWarning = !dialog.getToggleState();
			try {
				prefStore.setValue(IDEInternalPreferences.WARN_ABOUT_WORKSPACE_INCOMPATIBILITY, keepOnWarning);
				prefStore.save();
			} catch (IOException e) {
				IDEWorkbenchPlugin.log("Error writing to configuration preferences", //$NON-NLS-1$
					new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, e.getMessage(), e));
			}
		}
		return ReturnCode.VALID;
	}

	/**
	 * Compares the version of the workspace with the specified URL, to the version
	 * of the running IDE.
	 *
	 * @param url The URL of the workspace.
	 *
	 * @return
	 *         <ul>
	 *         <li>A negative integer if the workspace has a version less than that
	 *         of the IDE.</li>
	 *         <li>A positive integer if the IDE has a version greater than that of
	 *         the IDE.</li>
	 *         <li>{@code 0} if the IDE has version equal to the IDE, or if the
	 *         workspace has no version at all.</li>
	 *         </ul>
	 */
	protected int compareWorkspaceAndIdeVersions(URL url) {
		Version version = readWorkspaceVersion(url);
		// if the version could not be read, then there is not any existing
		// workspace data to trample, e.g., perhaps its a new directory that
		// is just starting to be used as a workspace
		if (version == null) {
			return 0;
		}

		final Version ide_version = toMajorMinorVersion(WORKSPACE_CHECK_REFERENCE_BUNDLE_VERSION);
		Version workspace_version = toMajorMinorVersion(version);
		return workspace_version.compareTo(ide_version);
	}

	/**
	 * @return Look at the argument URL for the workspace's version information.
	 *         Return that version if found and null otherwise.
	 */
	protected static Version readWorkspaceVersion(URL workspace) {
		File versionFile = getVersionFile(workspace, false);
		if (versionFile == null || !versionFile.exists()) {
			return null;
		}

		try {
			// Although the version file is not spec'ed to be a Java properties
			// file, it happens to follow the same format currently, so using
			// Properties to read it is convenient.
			Properties props = new Properties();
			try (FileInputStream is = new FileInputStream(versionFile)) {
				props.load(is);
			}

			String versionString = props.getProperty(WORKSPACE_CHECK_REFERENCE_BUNDLE_NAME);
			if (versionString != null) {
				return Version.parseVersion(versionString);
			}
			versionString= props.getProperty(WORKSPACE_CHECK_REFERENCE_BUNDLE_NAME_LEGACY);
			if (versionString != null) {
				return Version.parseVersion(versionString);
			}
			return null;
		} catch (IOException e) {
			IDEWorkbenchPlugin.log("Could not read version file " + versionFile, new Status( //$NON-NLS-1$
					IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
					IStatus.ERROR,
					e.getMessage() == null ? "" : e.getMessage(), //$NON-NLS-1$
					e));
			return null;
		} catch (IllegalArgumentException e) {
			IDEWorkbenchPlugin.log("Could not parse version in " + versionFile, new Status( //$NON-NLS-1$
					IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
					IStatus.ERROR,
					e.getMessage() == null ? "" : e.getMessage(), //$NON-NLS-1$
					e));
			return null;
		}
	}

	/**
	 * Write the version of the metadata into a known file overwriting any
	 * existing file contents. Writing the version file isn't really crucial,
	 * so the function is silent about failure
	 */
	private static void writeWorkspaceVersion() {
		if (WORKSPACE_CHECK_REFERENCE_BUNDLE_VERSION == null) {
			// no reference bundle installed, no check possible
			return;
		}

		Location instanceLoc = Platform.getInstanceLocation();
		if (instanceLoc == null || instanceLoc.isReadOnly()) {
			return;
		}

		File versionFile = getVersionFile(instanceLoc.getURL(), true);
		if (versionFile == null) {
			return;
		}

		Properties props = new Properties();

		// write new property
		props.setProperty(WORKSPACE_CHECK_REFERENCE_BUNDLE_NAME, WORKSPACE_CHECK_REFERENCE_BUNDLE_VERSION.toString());

		// write legacy property with an incremented version,
		// so that pre-4.4 IDEs will also warn about the workspace
		props.setProperty(WORKSPACE_CHECK_REFERENCE_BUNDLE_NAME_LEGACY, WORKSPACE_CHECK_LEGACY_VERSION_INCREMENTED);

		try (OutputStream output = new FileOutputStream(versionFile)) {
			props.store(output, null);
		} catch (IOException e) {
			IDEWorkbenchPlugin.log("Could not write version file", //$NON-NLS-1$
					StatusUtil.newError(e));
		}
	}

	/**
	 * The version file is stored in the metadata area of the workspace. This
	 * method returns an URL to the file or null if the directory or file does
	 * not exist (and the create parameter is false).
	 *
	 * @param create
	 *            If the directory and file does not exist this parameter
	 *            controls whether it will be created.
	 * @return An url to the file or null if the version file does not exist or
	 *         could not be created.
	 */
	protected static File getVersionFile(URL workspaceUrl, boolean create) {
		if (workspaceUrl == null) {
			return null;
		}

		try {
			// make sure the directory exists
			File metaDir = new File(workspaceUrl.getPath(), METADATA_FOLDER);
			if (!metaDir.exists() && (!create || !metaDir.mkdir())) {
				return null;
			}

			// make sure the file exists
			File versionFile = new File(metaDir, VERSION_FILENAME);
			if (!versionFile.exists()
					&& (!create || !versionFile.createNewFile())) {
				return null;
			}

			return versionFile;
		} catch (IOException e) {
			// cannot log because instance area has not been set
			return null;
		}
	}

	/**
	 * @return the major and minor parts of the given version
	 */
	protected static Version toMajorMinorVersion(Version version) {
		return new Version(version.getMajor(), version.getMinor(), 0);
	}

	@Override
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(() -> {
			if (!display.isDisposed())
				workbench.close();
		});
	}
}

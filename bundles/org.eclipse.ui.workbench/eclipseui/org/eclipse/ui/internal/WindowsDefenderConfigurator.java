/*******************************************************************************
 * Copyright (c) 2023, 2024 Hannes Wellmann and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hannes Wellmann - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import static org.eclipse.ui.internal.WorkbenchPlugin.PI_WORKBENCH;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.LinkFactory;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.prefs.BackingStoreException;

/**
 * A start-up event handler that, if running on Windows, suggests the user to
 * exclude the current installation directory from being scanned by the Windows
 * Defender, if it is active on the computer.
 */
@Component(service = EventHandler.class)
@EventTopics(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class WindowsDefenderConfigurator implements EventHandler {
	private static final String PREFERENCE_EXCLUDED_INSTALLATION_PATH = "windows.defender.excluded.path"; //$NON-NLS-1$
	private static final String PREFERENCE_STARTUP_CHECK_APP = "windows.defender.startup.check.app"; //$NON-NLS-1$
	public static final String PREFERENCE_STARTUP_CHECK_SKIP = "windows.defender.startup.check.skip"; //$NON-NLS-1$
	public static final boolean PREFERENCE_STARTUP_CHECK_SKIP_DEFAULT = false;

	@Reference
	protected IPreferencesService preferences;

	@Override
	public void handleEvent(Event event) {
		if (runStartupCheck()) {
			Job job = Job.create(WorkbenchMessages.WindowsDefenderConfigurator_statusCheck, m -> {
				SubMonitor monitor = SubMonitor.convert(m, 10);
				Optional<Path> installLocation = getInstallationLocation();
				if (installLocation.isPresent()) {
					String checkedPath = getPreference(ConfigurationScope.INSTANCE)
							.get(PREFERENCE_EXCLUDED_INSTALLATION_PATH, ""); //$NON-NLS-1$
					if (!checkedPath.isBlank() && installLocation.get().equals(Path.of(checkedPath))) {
						return; // This installation has already been checked at the current location
					}
				}
				monitor.worked(1);
				runExclusionCheck(monitor.split(9), installLocation);
			});
			job.setSystem(true);
			job.schedule();
		}
	}

	protected boolean runStartupCheck() {
		if (Platform.OS.isWindows() && !Platform.inDevelopmentMode()) {
			if (preferences.getBoolean(PI_WORKBENCH, PREFERENCE_STARTUP_CHECK_SKIP,
					PREFERENCE_STARTUP_CHECK_SKIP_DEFAULT, null)) {
				return false;
			}
			String permittedApp = preferences.getString(PI_WORKBENCH, PREFERENCE_STARTUP_CHECK_APP,
					"org.eclipse.ui.ide.workbench", null); //$NON-NLS-1$
			return permittedApp.equals(getRunningApplicationId());
		}
		return false;
	}

	private static String getRunningApplicationId() {
		@SuppressWarnings("restriction")
		String appId = System.getProperty(org.eclipse.core.internal.runtime.InternalPlatform.PROP_APPLICATION);
		if (appId != null) {
			return appId;
		}
		IProduct product = Platform.getProduct();
		return product != null ? product.getApplication() : null;
	}

	/**
	 * Opens the dialog to run the exclusion, regardless of any preference.
	 *
	 * @return {@code true} if this installation is now excluded, {@code false} if
	 *         Windows Defender is inactive and null if the process was aborted.
	 */
	public static Boolean runCheckEnforced(IProgressMonitor m) throws CoreException {
		Optional<Path> installLocation = getInstallationLocation();
		return runExclusionCheck(m, installLocation);
	}

	private enum HandlingOption {
		EXECUTE_EXCLUSION, IGNORE_THIS_INSTALLATION;
	}

	/**
	 * Performs the exclusion of this installation from Windows defender.
	 *
	 * @return {@code true} if this installation is now excluded, {@code false} if
	 *         Windows Defender is inactive and null if the process was aborted.
	 */
	private static Boolean runExclusionCheck(IProgressMonitor m, Optional<Path> installLocation) throws CoreException {
		SubMonitor monitor = SubMonitor.convert(m, 5);
		if (!isWindowsDefenderServiceRunning(monitor.split(1)) || !isWindowsDefenderActive(monitor.split(1))) {
			return Boolean.FALSE;
		}
		Display display = Display.getDefault();
		HandlingOption decision = askForDefenderHandlingDecision(display);
		if (decision == HandlingOption.EXECUTE_EXCLUSION) {
			if (isExclusionTamperProtectionEnabled(monitor.split(1))) {
				display.syncExec(() -> MessageDialog.openError(null, "Exclusion failed", //$NON-NLS-1$
						bindProductName(WorkbenchMessages.WindowsDefenderConfigurator_exclusionFailed_Protected)));
				savePreference(ConfigurationScope.INSTANCE, PREFERENCE_STARTUP_CHECK_SKIP, "true"); //$NON-NLS-1$
				return null; // Consider selection as 'aborted' and don't show the dialog again on startup
			}
			try {
				WindowsDefenderConfigurator.excludeDirectoryFromScanning(monitor.split(2));
				savePreference(ConfigurationScope.INSTANCE, PREFERENCE_EXCLUDED_INSTALLATION_PATH,
						installLocation.map(Path::toString).orElse("")); //$NON-NLS-1$
			} catch (IOException e) {
				display.syncExec(() -> MessageDialog.openError(null, "Exclusion failed", //$NON-NLS-1$
						bindProductName(WorkbenchMessages.WindowsDefenderConfigurator_exclusionFailed)));
			}
		} else if (decision == HandlingOption.IGNORE_THIS_INSTALLATION) {
			savePreference(ConfigurationScope.INSTANCE, PREFERENCE_STARTUP_CHECK_SKIP, "true"); //$NON-NLS-1$
		}
		return decision == HandlingOption.EXECUTE_EXCLUSION ? Boolean.TRUE : null;
	}

	private static HandlingOption askForDefenderHandlingDecision(Display display) {
		String message = bindProductName(WorkbenchMessages.WindowsDefenderConfigurator_exclusionCheckMessage);

		return display.syncCall(() -> {
			HandlingOption[] choice = new HandlingOption[] { null };
			MessageDialog dialog = new MessageDialog(display.getActiveShell(),
					WorkbenchMessages.WindowsDefenderConfigurator_statusCheck, null, message, MessageDialog.INFORMATION,
					0, IDialogConstants.PROCEED_LABEL, IDialogConstants.CANCEL_LABEL) {

				@Override
				protected Control createCustomArea(Composite parent) {
					ButtonFactory radioButtonFactory = WidgetFactory.button(SWT.RADIO | SWT.WRAP);

					Button performExclusion = radioButtonFactory
							.text(bindProductName(WorkbenchMessages.WindowsDefenderConfigurator_performExclusionChoice))
							.onSelect(e -> {
								choice[0] = HandlingOption.EXECUTE_EXCLUSION;
								getButton(0).setEnabled(true);
							}).create(parent);
					Point size = performExclusion.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					GridDataFactory.swtDefaults().hint((int) (size.x * 0.6), (int) (size.y * 2.3)).indent(0, 5)
							.applyTo(performExclusion);

					Button keepScanning = radioButtonFactory
							.text(bindProductName(
									WorkbenchMessages.WindowsDefenderConfigurator_ignoreThisInstallationChoice))
							.onSelect(e -> {
								choice[0] = HandlingOption.IGNORE_THIS_INSTALLATION;
								getButton(0).setEnabled(true);
							}).create(parent);
					GridDataFactory.swtDefaults().indent(0, 5).applyTo(keepScanning);

					if (!PlatformUI.isWorkbenchRunning()) {
						return parent; // Only show the link to the preferences if the workbench is available
					}
					LinkFactory.newLink(SWT.WRAP)
							.text(WorkbenchMessages.WindowsDefenderConfigurator_detailsAndOptionsLinkText)
							.onSelect((e -> {
								PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null,
										"org.eclipse.ui.preferencePages.Startup", null, null); //$NON-NLS-1$
								dialog.setBlockOnOpen(false);
								dialog.open();
								this.setReturnCode(Window.CANCEL);
								this.close();
							})).create(parent);

					return parent;
				}

				@Override
				protected void createButtonsForButtonBar(Composite parent) {
					super.createButtonsForButtonBar(parent);
					getButton(0).setEnabled(false);
					getButton(1).forceFocus(); // prevents auto-focusing one of the radio-buttons which selects them
				}
			};
			int open = dialog.open();
			return open == Window.OK ? choice[0] : null;
		});
	}

	public static String bindProductName(String message) {
		String name = Optional.ofNullable(Platform.getProduct()).map(IProduct::getName).orElse("this application"); //$NON-NLS-1$
		return NLS.bind(message, name);
	}

	public static IEclipsePreferences getPreference(IScopeContext instance) {
		return instance.getNode(PI_WORKBENCH);
	}

	public static void savePreference(IScopeContext scope, String key, String value) throws CoreException {
		IEclipsePreferences preferences = getPreference(scope);
		if (value != null) {
			preferences.put(key, value);
		} else {
			preferences.remove(key);
		}
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			throw new CoreException(Status.error("Failed to safe preference " + preferences, e)); //$NON-NLS-1$
		}
	}

	private static Optional<Path> getInstallationLocation() {
		// For install location see:
		// https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fmisc%2Fruntime-options.html&anchor=locations
		try {
			Location installLocation = Platform.getConfigurationLocation();
			URI location = URIUtil.toURI(installLocation.getURL());
			return Optional.of(Path.of(location)); // assume location has a file-URL
		} catch (URISyntaxException e) { // ignore
		}
		return Optional.empty();
	}

	private static List<Path> getExecutablePath() {
		@SuppressWarnings("restriction")
		String eclipseLauncher = System.getProperty(org.eclipse.osgi.internal.location.EquinoxLocations.PROP_LAUNCHER);
		return List.of(Path.of(eclipseLauncher));
	}

	private static boolean isExclusionTamperProtectionEnabled(IProgressMonitor monitor) {
		// https://learn.microsoft.com/en-us/microsoft-365/security/defender-endpoint/manage-tamper-protection-intune?view=o365-worldwide#how-to-determine-whether-antivirus-exclusions-are-tamper-protected-on-a-windows-device
		try { // Query the Windows Registry
			List<String> result = runPowershell(monitor, "-Command", //$NON-NLS-1$
					"Get-ItemPropertyValue -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows Defender\\Features' -Name 'TPExclusions'"); //$NON-NLS-1$
			return result.size() == 1 && "1".equals(result.get(0)); //$NON-NLS-1$
		} catch (IOException e) {
			return false;
		}
	}

	private static boolean isWindowsDefenderServiceRunning(IProgressMonitor monitor) {
		// https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.management/get-service?view=powershell-7.4
		// https://learn.microsoft.com/en-us/dotnet/api/system.serviceprocess.servicecontrollerstatus?view=dotnet-plat-ext-8.0
		try {
			List<String> result = runPowershell(monitor, "-Command", "(Get-Service 'WinDefend').Status"); //$NON-NLS-1$ //$NON-NLS-2$
			return result.size() == 1 && "Running".equalsIgnoreCase(result.get(0)); //$NON-NLS-1$
		} catch (IOException e) {
			String message = e.getMessage();
			if (message != null
					&& message.startsWith("Cannot run program \"" + POWERSHELL_EXE + "\": CreateProcess error=5")) { //$NON-NLS-1$//$NON-NLS-2$
				// error code 5 means ERROR_ACCESS_DENIED:
				// https://learn.microsoft.com/en-us/windows/win32/debug/system-error-codes--0-499-
				// Without permission to launch powershell we can't do anything and stay silent
				return false;
			}
			ILog.get().error("Failed to obtain 'WinDefend' service state", e); //$NON-NLS-1$
			return false;
		}
	}

	private static boolean isWindowsDefenderActive(IProgressMonitor monitor) throws CoreException {
		// https://learn.microsoft.com/en-us/powershell/module/defender/get-mpcomputerstatus?view=windowsserver2019-ps
		try {
			List<String> lines = runPowershell(monitor, "-Command", "(Get-MpComputerStatus).AMRunningMode"); //$NON-NLS-1$ //$NON-NLS-2$
			String onlyLine = lines.size() == 1 ? lines.get(0) : "error"; //$NON-NLS-1$
			return switch (onlyLine.toLowerCase(Locale.ENGLISH).strip()) {
			// Known values as listed in
			// https://learn.microsoft.com/en-us/microsoft-365/security/defender-endpoint/microsoft-defender-antivirus-windows#use-powershell-to-check-the-status-of-microsoft-defender-antivirus
			// "not running" status appears to be undocumented (https://github.com/eclipse-platform/eclipse.platform.ui/issues/2447)
			case "sxs passive mode", "passive mode", "not running", "" -> false; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			case "normal", "edr block mode" -> true; //$NON-NLS-1$//$NON-NLS-2$
			default -> throw new IOException("Process terminated with unexpected result:\n" + String.join("\n", lines)); //$NON-NLS-1$//$NON-NLS-2$
			};
		} catch (IOException e) {
			throw new CoreException(Status.error(WorkbenchMessages.WindowsDefenderConfigurator_statusCheckFailed, e));
		}
	}

	public static String createAddExclusionsPowershellCommand(String extraSeparator) {
		List<Path> paths = getExecutablePath();
		// For detailed explanations about how to add new exclusions see:
		// https://learn.microsoft.com/en-us/powershell/module/defender/add-mppreference?view=windowsserver2019-ps
		String excludedPaths = paths.stream().map(Path::toString).map(p -> '"' + p + '"')
				.collect(Collectors.joining(',' + extraSeparator));
		return "Add-MpPreference -ExclusionProcess " + extraSeparator + excludedPaths; //$NON-NLS-1$
	}

	private static void excludeDirectoryFromScanning(IProgressMonitor monitor) throws IOException {
		String exclusionsCommand = createAddExclusionsPowershellCommand(""); //$NON-NLS-1$
		// In order to change the Windows Defender configuration a powershell with
		// Administrator privileges is needed and therefore from a basic powershell
		// a second one with elevated rights is started and runs the
		// add-exclusions-command. For a detailed explanation of the Start-process
		// parameters see
		// https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.management/start-process?view=powershell-7.4#parameters
		//
		// In order to avoid quoting when passing a command through multiple
		// process-calls/command line processors, the command is passed as
		// base64-encoded string to the elevated second powershell.
		// For details about the -EncodedCommand argument see (and the EXAMPLES section)
		// https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_powershell_exe#-encodedcommand-base64encodedcommand
		String encodedCommand = Base64.getEncoder()
				.encodeToString(exclusionsCommand.getBytes(StandardCharsets.UTF_16LE)); // encoding as specified
		runPowershell(monitor, // Launch child powershell with administrator privileges
				"Start-Process", "powershell", "-Verb", "RunAs", "-Wait", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"-ArgumentList", "'-EncodedCommand " + encodedCommand + "'"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}

	private static final String POWERSHELL_EXE = "powershell.exe"; //$NON-NLS-1$

	private static List<String> runPowershell(IProgressMonitor monitor, String... arguments) throws IOException {
		return runProcess(Stream.concat(Stream.of(POWERSHELL_EXE), Arrays.stream(arguments)).toList(), monitor);
	}

	private static List<String> runProcess(List<String> command, IProgressMonitor monitor) throws IOException {
		ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();
		Process process = new ProcessBuilder(command).start();
		Future<List<String>> processLines = newSingleThreadExecutor.submit(() -> {
			try (BufferedReader inputReader = process.inputReader();) {
				return inputReader.lines().filter(l -> !l.isBlank()).map(String::strip).toList();
			}
		});
		newSingleThreadExecutor.shutdown();
		try {
			while (!processLines.isDone()) {
				if (monitor.isCanceled()) {
					process.destroy();
					process.descendants().forEach(ProcessHandle::destroy);
					processLines.cancel(true);
					throw new OperationCanceledException();
				}
				Thread.onSpinWait();
				Thread.sleep(5);
			}
			if (process.isAlive()) {
				process.destroyForcibly();
				process.descendants().forEach(ProcessHandle::destroyForcibly);
				throw new IOException("Process timed-out and it was attempted to forcefully terminate it"); //$NON-NLS-1$
			} else if (process.exitValue() != 0) {
				throw new IOException("Process failed with exit-code " + process.exitValue()); //$NON-NLS-1$
			}
			return processLines.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new OperationCanceledException();
		} catch (ExecutionException e) {
			throw new IOException(e.getCause());
		}
	}

}

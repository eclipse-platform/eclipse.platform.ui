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
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
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
	private static final String WINDOWS_DEFENDER_EXCLUDED_INSTALLATION_PATH = "windows.defender.excluded.path"; //$NON-NLS-1$
	public static final String PREFERENCE_SKIP = "windows.defender.check.skip"; //$NON-NLS-1$
	public static final boolean PREFERENCE_SKIP_DEFAULT = false;

	@Reference
	private IPreferencesService preferences;

	@Override
	public void handleEvent(Event event) {
		if (runStartupCheck()) {
			Job job = Job.create(WorkbenchMessages.WindowsDefenderConfigurator_statusCheck, m -> {
				SubMonitor monitor = SubMonitor.convert(m, 10);
				if (preferences.getBoolean(PI_WORKBENCH, PREFERENCE_SKIP, PREFERENCE_SKIP_DEFAULT, null)) {
					return;
				}
				Optional<Path> installLocation = getInstallationLocation();
				if (installLocation.isPresent()) {
					String checkedPath = getPreference(ConfigurationScope.INSTANCE)
							.get(WINDOWS_DEFENDER_EXCLUDED_INSTALLATION_PATH, ""); //$NON-NLS-1$
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

	private static boolean runStartupCheck() {
		if (Platform.isRunning() && Platform.OS.isWindows() && !Platform.inDevelopmentMode()) {
			IProduct product = Platform.getProduct();
			if (product != null) {
				return "org.eclipse.ui.ide.workbench".equals(product.getApplication()); //$NON-NLS-1$
			}
		}
		return false;
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
		SubMonitor monitor = SubMonitor.convert(m, 3);
		if (!isWindowsDefenderActive(monitor.split(1))) {
			return Boolean.FALSE;
		}

		HandlingOption decision = askForDefenderHandlingDecision();
		if (decision != null) {
			switch (decision) {
			case EXECUTE_EXCLUSION -> {
				try {
					WindowsDefenderConfigurator.excludeDirectoryFromScanning(monitor.split(2));
					savePreference(ConfigurationScope.INSTANCE, WINDOWS_DEFENDER_EXCLUDED_INSTALLATION_PATH,
							installLocation.map(Path::toString).orElse("")); //$NON-NLS-1$
				} catch (IOException e) {
					PlatformUI.getWorkbench().getDisplay()
							.syncExec(() -> MessageDialog.openError(null, "Exclusion failed", //$NON-NLS-1$
									bindProductName(WorkbenchMessages.WindowsDefenderConfigurator_exclusionFailed)));
				}
			}
			case IGNORE_THIS_INSTALLATION -> savePreference(ConfigurationScope.INSTANCE, PREFERENCE_SKIP, "true"); //$NON-NLS-1$
			}
		}
		return decision == HandlingOption.EXECUTE_EXCLUSION ? Boolean.TRUE : null;
	}

	private static HandlingOption askForDefenderHandlingDecision() {
		String message = bindProductName(WorkbenchMessages.WindowsDefenderConfigurator_exclusionCheckMessage);

		return PlatformUI.getWorkbench().getDisplay().syncCall(() -> {
			HandlingOption[] choice = new HandlingOption[] { null };
			MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(),
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
		return NLS.bind(message, Platform.getProduct().getName());
	}

	public static IEclipsePreferences getPreference(IScopeContext instance) {
		return instance.getNode(PI_WORKBENCH);
	}

	public static void savePreference(IScopeContext scope, String key, String value) throws CoreException {
		IEclipsePreferences preferences = getPreference(scope);
		preferences.put(key, value);
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
			return Optional.of(Path.of(installLocation.getURL().toURI())); // assume location has a file-URL
		} catch (URISyntaxException e) { // ignore
		}
		return Optional.empty();
	}

	private static List<Path> getExecutablePath() {
		@SuppressWarnings("restriction")
		String eclipseLauncher = System.getProperty(org.eclipse.osgi.internal.location.EquinoxLocations.PROP_LAUNCHER);
		return List.of(Path.of(eclipseLauncher));
	}

	private static boolean isWindowsDefenderActive(IProgressMonitor monitor) throws CoreException {
		// https://learn.microsoft.com/en-us/powershell/module/defender/get-mpcomputerstatus
		List<String> command = List.of("powershell.exe", "-Command", "(Get-MpComputerStatus).AMRunningMode"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		try {
			List<String> lines = runProcess(command, monitor);
			String onlyLine = lines.size() == 1 ? lines.get(0) : ""; //$NON-NLS-1$
			return switch (onlyLine) {
			// Known values as listed in
			// https://learn.microsoft.com/en-us/microsoft-365/security/defender-endpoint/microsoft-defender-antivirus-windows#use-powershell-to-check-the-status-of-microsoft-defender-antivirus
			case "SxS Passive Mode", "Passive mode" -> false; //$NON-NLS-1$ //$NON-NLS-2$
			case "Normal", "EDR Block Mode" -> true; //$NON-NLS-1$//$NON-NLS-2$
			default -> throw new IOException("Process terminated with unexpected result:\n" + String.join("\n", lines)); //$NON-NLS-1$//$NON-NLS-2$
			};
		} catch (IOException e) {
			throw new CoreException(Status.error(WorkbenchMessages.WindowsDefenderConfigurator_statusCheckFailed, e));
		}
	}

	public static String createAddExclusionsPowershellCommand(String extraSeparator) {
		List<Path> paths = getExecutablePath();
		// For detailed explanations about how to read existing exclusions and how to
		// add new ones see:
		// https://learn.microsoft.com/en-us/powershell/module/defender/add-mppreference
		// https://learn.microsoft.com/en-us/powershell/module/defender/get-mppreference
		//
		// For .NET's stream API called LINQ see:
		// https://learn.microsoft.com/en-us/dotnet/api/system.linq.enumerable
		String excludedPaths = paths.stream().map(Path::toString).map(p -> '"' + p + '"')
				.collect(Collectors.joining(',' + extraSeparator));
		final String exclusionType = "ExclusionProcess"; //$NON-NLS-1$
		return String.join(';' + extraSeparator, "$exclusions=@(" + extraSeparator + excludedPaths + ')', //$NON-NLS-1$
				"$existingExclusions=[Collections.Generic.HashSet[String]](Get-MpPreference)." + exclusionType, //$NON-NLS-1$
				"if($existingExclusions -eq $null) { $existingExclusions = New-Object Collections.Generic.HashSet[String] }", //$NON-NLS-1$
				"$exclusionsToAdd=[Linq.Enumerable]::ToArray([Linq.Enumerable]::Where($exclusions,[Func[object,bool]]{param($ex)!$existingExclusions.Contains($ex)}))", //$NON-NLS-1$
				"if($exclusionsToAdd.Length -gt 0){ Add-MpPreference -" + exclusionType + " $exclusionsToAdd }"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static void excludeDirectoryFromScanning(IProgressMonitor monitor) throws IOException {
		String exclusionsCommand = createAddExclusionsPowershellCommand(""); //$NON-NLS-1$
		// In order to change the Windows Defender configuration a powershell with
		// Administrator privileges is needed and therefore from a basic powershell
		// a second one with elevated rights is started and runs the
		// add-exclusions-command. For a detailed explanation of the Start-process
		// parameters see
		// https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.management/start-process#parameters
		//
		// In order to avoid quoting when passing a command through multiple
		// process-calls/command line processors, the command is passed as
		// base64-encoded string to the elevated second powershell.
		// For details about the -EncodedCommand argument see (and the EXAMPLES section)
		// https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_powershell_exe#-encodedcommand-base64encodedcommand
		String encodedCommand = Base64.getEncoder()
				.encodeToString(exclusionsCommand.getBytes(StandardCharsets.UTF_16LE)); // encoding as specified
		List<String> command = List.of("powershell.exe", //$NON-NLS-1$
				// Launch child powershell with administrator privileges
				"Start-Process", "powershell", "-Verb", "RunAs", "-Wait", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"-ArgumentList", "'-EncodedCommand " + encodedCommand + "'"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		runProcess(command, monitor);
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
				throw new IOException("Process timed-out and it was attempted to forcefully termiante it"); //$NON-NLS-1$
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

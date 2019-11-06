/*******************************************************************************
 * Copyright (c) 2019 Paul Pazderski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Paul Pazderski - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.console;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.tests.AbstractDebugTest;
import org.eclipse.debug.tests.TestUtil;
import org.eclipse.debug.tests.launching.LaunchConfigurationTests;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.ConsoleColorProvider;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;

/**
 * Tests the ProcessConsole.
 */
public class ProcessConsoleTests extends AbstractDebugTest {
	/**
	 * Number of received log messages with severity error while running a
	 * single test method.
	 */
	private final AtomicInteger loggedErrors = new AtomicInteger(0);

	/** Listener to count error messages in {@link ConsolePlugin} log. */
	private final ILogListener errorLogListener = new ILogListener() {
		@Override
		public void logging(IStatus status, String plugin) {
			if (status.matches(IStatus.ERROR)) {
				loggedErrors.incrementAndGet();
			}
		}
	};

	public ProcessConsoleTests() {
		super(ProcessConsoleTests.class.getSimpleName());
	}

	public ProcessConsoleTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		loggedErrors.set(0);
		Platform.addLogListener(errorLogListener);
	}

	@Override
	protected void tearDown() throws Exception {
		Platform.removeLogListener(errorLogListener);
		super.tearDown();

		assertEquals("Test triggered errors.", 0, loggedErrors.get());
	}

	/**
	 * Test if two byte UTF-8 characters get disrupted on there way from process
	 * console to the runtime process.
	 * <p>
	 * This test starts every two byte character on an even byte offset.
	 * </p>
	 */
	public void testUTF8InputEven() throws Exception {
		// 5000 characters result in 10000 bytes which should be more than most
		// common buffer sizes.
		processConsoleUTF8Input("", 5000);
	}

	/**
	 * Test if two byte UTF-8 characters get disrupted on there way from process
	 * console to the runtime process.
	 * <p>
	 * This test starts every two byte character on an odd byte offset.
	 * </p>
	 */
	public void testUTF8InputOdd() throws Exception {
		// 5000 characters result in 10000 bytes which should be more than most
		// common buffer sizes.
		processConsoleUTF8Input("+", 5000);
	}

	/**
	 * Shared code for the UTF-8 input tests.
	 * <p>
	 * Send some two byte UTF-8 characters through process console user input
	 * stream to mockup process and check if the input got corrupted on its way.
	 * </p>
	 *
	 * @param prefix an arbitrary prefix inserted before the two byte UTF-8
	 *            characters. Used to move the other characters to specific
	 *            offsets e.g. a prefix of one byte will produce an input string
	 *            where every two byte character starts at an odd offset.
	 * @param numTwoByteCharacters number of two byte UTF-8 characters to send
	 *            to process
	 */
	public void processConsoleUTF8Input(String prefix, int numTwoByteCharacters) throws Exception {
		final String input = prefix + String.join("", Collections.nCopies(numTwoByteCharacters, "\u00F8"));
		final MockProcess mockProcess = new MockProcess(input.getBytes(StandardCharsets.UTF_8).length, testTimeout);
		try {
			final ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
			launch.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, StandardCharsets.UTF_8.toString());
			final IProcess process = DebugPlugin.newProcess(launch, mockProcess, "testUtf8Input");
			@SuppressWarnings("restriction")
			final org.eclipse.debug.internal.ui.views.console.ProcessConsole console = new org.eclipse.debug.internal.ui.views.console.ProcessConsole(process, new ConsoleColorProvider(), StandardCharsets.UTF_8.toString());
			try {
				console.initialize();
				console.getInputStream().appendData(input);
				mockProcess.waitFor(testTimeout, TimeUnit.MILLISECONDS);
			} finally {
				console.destroy();
			}
		} finally {
			mockProcess.destroy();
		}

		final String receivedInput = new String(mockProcess.getReceivedInput(), StandardCharsets.UTF_8);
		assertEquals(input, receivedInput);
	}

	/**
	 * Test if InputReadJob can be canceled.
	 * <p>
	 * Actually tests cancellation for all jobs of
	 * <code>ProcessConsole.class</code> family.
	 * </p>
	 */
	public void testInputReadJobCancel() throws Exception {
		final MockProcess mockProcess = new MockProcess(MockProcess.RUN_FOREVER);
		try {
			final IProcess process = mockProcess.toRuntimeProcess("testInputReadJobCancel");
			@SuppressWarnings("restriction")
			final org.eclipse.debug.internal.ui.views.console.ProcessConsole console = new org.eclipse.debug.internal.ui.views.console.ProcessConsole(process, new ConsoleColorProvider());
			try {
				console.initialize();
				@SuppressWarnings("restriction")
				final Class<?> jobFamily = org.eclipse.debug.internal.ui.views.console.ProcessConsole.class;
				assertTrue("Input read job not started.", Job.getJobManager().find(jobFamily).length > 0);
				Job.getJobManager().cancel(jobFamily);
				TestUtil.waitForJobs(getName(), 0, 1000);
				assertEquals("Input read job not canceled.", 0, Job.getJobManager().find(jobFamily).length);
			} finally {
				console.destroy();
			}
		} finally {
			mockProcess.destroy();
		}
	}

	/**
	 * Test console finished notification with standard process console.
	 */
	public void testProcessTerminationNotification() throws Exception {
		TestUtil.log(IStatus.INFO, getName(), "Process terminates after Console is initialized.");
		processTerminationTest(null, false);
		TestUtil.log(IStatus.INFO, getName(), "Process terminates before Console is initialized.");
		processTerminationTest(null, true);
	}

	/**
	 * Test console finished notification if process standard input is feed from
	 * file.
	 */
	public void testProcessTerminationNotificationWithInputFile() throws Exception {
		File inFile = DebugUIPlugin.getDefault().getStateLocation().addTrailingSeparator().append("testStdin.txt").toFile();
		boolean fileCreated = inFile.createNewFile();
		assertTrue("Failed to prepare input file.", fileCreated);
		try {
			ILaunchConfigurationType launchType = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(LaunchConfigurationTests.ID_TEST_LAUNCH_TYPE);
			ILaunchConfigurationWorkingCopy launchConfiguration = launchType.newInstance(null, "testProcessTerminationNotificationWithInputFromFile");
			launchConfiguration.setAttribute(IDebugUIConstants.ATTR_CAPTURE_STDIN_FILE, inFile.getAbsolutePath());
			TestUtil.log(IStatus.INFO, getName(), "Process terminates after Console is initialized.");
			processTerminationTest(launchConfiguration, false);
			TestUtil.log(IStatus.INFO, getName(), "Process terminates before Console is initialized.");
			processTerminationTest(launchConfiguration, true);
		} finally {
			inFile.delete();
		}
	}

	/**
	 * The shared code to test console finished notification.
	 *
	 * @param launchConfig <code>null</code> or configured with stdin file.
	 * @param terminateBeforeConsoleInitialization if <code>true</code> the
	 *            tested process is terminated before the ProcessConsole can
	 *            perform its initialization. If <code>false</code> the process
	 *            is guaranteed to run until the ProcessConsole was initialized.
	 */
	public void processTerminationTest(ILaunchConfiguration launchConfig, boolean terminateBeforeConsoleInitialization) throws Exception {
		final AtomicBoolean terminationSignaled = new AtomicBoolean(false);
		final Process mockProcess = new MockProcess(null, null, terminateBeforeConsoleInitialization ? 0 : -1);
		final IProcess process = DebugPlugin.newProcess(new Launch(launchConfig, ILaunchManager.RUN_MODE, null), mockProcess, "testProcessTerminationNotification");
		@SuppressWarnings("restriction")
		final org.eclipse.debug.internal.ui.views.console.ProcessConsole console = new org.eclipse.debug.internal.ui.views.console.ProcessConsole(process, new ConsoleColorProvider());
		console.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getSource() == console && IConsoleConstants.P_CONSOLE_OUTPUT_COMPLETE.equals(event.getProperty())) {
					terminationSignaled.set(true);
				}
			}
		});
		final IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		try {
			consoleManager.addConsoles(new IConsole[] { console });
			if (mockProcess.isAlive()) {
				mockProcess.destroy();
			}
			TestUtil.waitForJobs(getName(), 50, 10000);
			assertTrue("No console complete notification received.", terminationSignaled.get());
		} finally {
			consoleManager.removeConsoles(new IConsole[] { console });
			TestUtil.waitForJobs(getName(), 0, 10000);
		}
	}
}

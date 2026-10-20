/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.stress;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test opening and closing of items.
 */
public class OpenCloseTest {
	private static final String ORG_ECLIPSE_RESOURCE_PERSPECTIVE = "org.eclipse.ui.resourcePerspective";

	// Low iteration count keeps the automated build fast; raise it locally for real stress testing.
	private static final int numIterations = 4;

	// Per-method deadline; on expiry the watchdog dumps all threads and aborts.
	private static final long TEST_TIMEOUT_MS = 200_000;

	private static final long WATCHDOG_GRACE_MS = 20_000;

	private IWorkbenchWindow workbenchWindow;
	private IWorkbench workbench;
	private IWorkbenchPage page;

	private Thread watchdog;
	private volatile boolean deadlineExceeded;

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Before
	public void setup() {
		armWatchdog();
		workbench = PlatformUI.getWorkbench();
		workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IIntroPart intro = PlatformUI.getWorkbench().getIntroManager().getIntro();
		if (intro != null) {
			PlatformUI.getWorkbench().getIntroManager().closeIntro(intro);
		}

		try {
			PlatformUI.getWorkbench().showPerspective(ORG_ECLIPSE_RESOURCE_PERSPECTIVE, workbenchWindow);
		} catch (WorkbenchException e) {
			e.printStackTrace();
		}

		page = workbenchWindow.getActivePage();
		assertNotNull(page);
	}

	@After
	public void disarmWatchdog() {
		if (watchdog != null) {
			watchdog.interrupt();
			watchdog = null;
		}
		// Fail any method that exceeded the deadline, even if it recovered after the interrupt, so a
		// late run is never silently reported as passing.
		if (deadlineExceeded) {
			fail("Test exceeded " + TEST_TIMEOUT_MS + " ms; see the thread dump above.");
		}
	}

	private void armWatchdog() {
		deadlineExceeded = false;
		Thread testThread = Thread.currentThread();
		watchdog = new Thread(() -> {
			try {
				Thread.sleep(TEST_TIMEOUT_MS);
			} catch (InterruptedException e) {
				return; // test finished in time and disarmed the watchdog
			}
			deadlineExceeded = true;
			dumpAllThreads();
			testThread.interrupt(); // best effort: unblock an interruptible wait
			try {
				Thread.sleep(WATCHDOG_GRACE_MS);
			} catch (InterruptedException e) {
				return; // test recovered after the interrupt and disarmed the watchdog
			}
			// Still stuck: halt rather than wait for the bundle timeout. halt() avoids shutdown
			// hooks deadlocking again on the wedged UI thread.
			System.err.println("OpenCloseTest did not recover after interrupt; aborting JVM.");
			Runtime.getRuntime().halt(143);
		}, "OpenCloseTest-watchdog");
		watchdog.setDaemon(true);
		watchdog.start();
	}

	private static void dumpAllThreads() {
		System.err.println("OpenCloseTest exceeded " + TEST_TIMEOUT_MS + " ms; dumping all thread stacks:");
		for (var entry : Thread.getAllStackTraces().entrySet()) {
			System.err.println(entry.getKey());
			for (StackTraceElement element : entry.getValue()) {
				System.err.println("\tat " + element);
			}
		}
	}


	/**
	 * Test the opening and closing of a file.
	 */
	@Test
	public void testOpenCloseFile() throws CoreException {
		IWorkbenchPage page = workbenchWindow.getActivePage();
		FileUtil.createProject("TestProject");
		IProject testProject = ResourcesPlugin.getWorkspace().getRoot().getProject("TestProject"); //$NON-NLS-1$
		FileUtil.createFile("tempFile.txt", testProject);
		testProject.open(null);
		IEditorInput editorInput = new FileEditorInput(testProject.getFile("tempFile.txt"));
		IEditorPart editorPart = null;
		for (int index = 0; index < numIterations; index++) {
			editorPart = page.openEditor(editorInput, "org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
			page.closeEditor(editorPart, false);
		}
		FileUtil.deleteProject(testProject);
	}

	/**
	 * Test opening and closing of workbench window.
	 */
	@Test
	public void testOpenCloseWorkbenchWindow() throws WorkbenchException {
		IWorkbenchWindow secondWorkbenchWindow;
		for (int index = 0; index < numIterations; index++) {
			secondWorkbenchWindow = PlatformUI.getWorkbench()
					.openWorkbenchWindow(ResourcesPlugin.getWorkspace().getRoot());
			secondWorkbenchWindow.close();
		}
	}

	/**
	 * Test open and close of perspective.
	 */
	@Test
	public void testOpenClosePerspective() {
		// Reopening the perspective will create a new popup shell that would be
		// detected as leak
		closeTestWindows.disableLeakChecks();
		ICommandService commandService = workbench.getService(ICommandService.class);
		Command command = commandService.getCommand("org.eclipse.ui.window.closePerspective");

		HashMap<String, String> parameters = new HashMap<>();
		parameters.put(IWorkbenchCommandConstants.WINDOW_CLOSE_PERSPECTIVE_PARM_ID,
				ORG_ECLIPSE_RESOURCE_PERSPECTIVE);

		ParameterizedCommand pCommand = ParameterizedCommand.generateCommand(command, parameters);

		IHandlerService handlerService = workbenchWindow.getService(IHandlerService.class);

		for (int index = 0; index < numIterations; index++) {
			try {
				try {
					handlerService.executeCommand(pCommand, null);
				} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e1) {
				}
				PlatformUI.getWorkbench().showPerspective(ORG_ECLIPSE_RESOURCE_PERSPECTIVE, workbenchWindow);
			} catch (WorkbenchException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Test open and close of view.
	 */
	@Test
	public void testOpenCloseView() throws WorkbenchException {
		IViewPart consoleView;
		for (int index = 0; index < numIterations; index++) {
			consoleView = page.showView(IPageLayout.ID_MINIMAP_VIEW);
			page.hideView(consoleView);
		}
	}

	/**
	 * Test open and close intro.
	 */
	@Test
	public void testOpenCloseIntro() {
		IIntroPart introPart;
		for (int index = 0; index < numIterations; index++) {
			introPart = PlatformUI.getWorkbench().getIntroManager().showIntro(workbenchWindow, false);
			PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
		}
	}
}

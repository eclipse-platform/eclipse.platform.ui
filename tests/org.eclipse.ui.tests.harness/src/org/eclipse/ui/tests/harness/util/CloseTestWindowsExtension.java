/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
 *     Rolf Theunissen <rolf.theunissen@gmail.com> - Bug 553836 (extracted from UITestCase)
 *******************************************************************************/

package org.eclipse.ui.tests.harness.util;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 Extension for UI tests to clean up windows/shells:
 * <ul>
 * <li>prints the test name to the log before and after each test case
 * <li>closes windows opened during the test case
 * <li>checks for shells unintentionally leaked from the test case
 * </ul>
 */
public class CloseTestWindowsExtension implements BeforeEachCallback, AfterEachCallback {

	private static final String TEST_NAME_KEY = "testName";
	private static final String TEST_WINDOWS_KEY = "testWindows";
	private static final String WINDOW_LISTENER_KEY = "windowListener";
	private static final String INITIAL_SHELLS_KEY = "initialShells";
	private static final String LEAK_CHECKS_DISABLED_KEY = "leakChecksDisabled";

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		String testName = context.getDisplayName();
		context.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
				.put(TEST_NAME_KEY, testName);

		List<IWorkbenchWindow> testWindows = new ArrayList<>(3);
		context.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
				.put(TEST_WINDOWS_KEY, testWindows);

		TestWindowListener windowListener = new TestWindowListener(testWindows);
		context.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
				.put(WINDOW_LISTENER_KEY, windowListener);

		addWindowListener(windowListener);
		storeInitialShells(context);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		TestWindowListener windowListener = getWindowListener(context);
		removeWindowListener(windowListener);

		processEvents();
		closeAllTestWindows(context);
		processEvents();
		checkForLeakedShells(context);
	}


	/**
	 * Adds a window listener to the workbench to keep track of opened test windows.
	 */
	private void addWindowListener(TestWindowListener windowListener) {
		PlatformUI.getWorkbench().addWindowListener(windowListener);
	}

	/**
	 * Removes the listener.
	 */
	private void removeWindowListener(TestWindowListener windowListener) {
		if (windowListener != null) {
			PlatformUI.getWorkbench().removeWindowListener(windowListener);
		}
	}

	/**
	 * Close all test windows.
	 */
	private void closeAllTestWindows(ExtensionContext context) {
		@SuppressWarnings("unchecked")
		List<IWorkbenchWindow> testWindows = (List<IWorkbenchWindow>) context
				.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
				.get(TEST_WINDOWS_KEY);

		if (testWindows != null) {
			List<IWorkbenchWindow> testWindowsCopy = new ArrayList<>(testWindows);
			for (IWorkbenchWindow testWindow : testWindowsCopy) {
				testWindow.close();
			}
			testWindows.clear();
		}
	}

	private static class TestWindowListener implements IWindowListener {
		private final List<IWorkbenchWindow> testWindows;

		public TestWindowListener(List<IWorkbenchWindow> testWindows) {
			this.testWindows = testWindows;
		}

		@Override
		public void windowActivated(IWorkbenchWindow window) {
			// do nothing
		}

		@Override
		public void windowDeactivated(IWorkbenchWindow window) {
			// do nothing
		}

		@Override
		public void windowClosed(IWorkbenchWindow window) {
			testWindows.remove(window);
		}

		@Override
		public void windowOpened(IWorkbenchWindow window) {
			testWindows.add(window);
		}
	}

	private void storeInitialShells(ExtensionContext context) {
		Set<Shell> initialShells = Set.of(PlatformUI.getWorkbench().getDisplay().getShells());
		context.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
				.put(INITIAL_SHELLS_KEY, initialShells);
	}

	private void checkForLeakedShells(ExtensionContext context) {
		@SuppressWarnings("unchecked")
		Set<Shell> initialShells = (Set<Shell>) context
				.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
				.get(INITIAL_SHELLS_KEY);

		Boolean leakChecksDisabled = (Boolean) context
				.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
				.get(LEAK_CHECKS_DISABLED_KEY);

		if (initialShells == null) {
			return;
		}

		List<String> leakedModalShellTitles = new ArrayList<>();
		Shell[] shells = PlatformUI.getWorkbench().getDisplay().getShells();
		for (Shell shell : shells) {
			if (!shell.isDisposed() && !initialShells.contains(shell)) {
				leakedModalShellTitles.add(shell.getText());
				shell.close();
			}
		}

		if (leakChecksDisabled == null || !leakChecksDisabled) {
			assertEquals(0, leakedModalShellTitles.size(),
					"Test leaked modal shell: [" + String.join(", ", leakedModalShellTitles) + "]");
		}
	}

	/**
	 * Disable leak checks for the current test.
	 * This method should be called from test methods when needed.
	 */
	public void disableLeakChecks(ExtensionContext context) {
		context.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
				.put(LEAK_CHECKS_DISABLED_KEY, Boolean.TRUE);
	}


	private TestWindowListener getWindowListener(ExtensionContext context) {
		return (TestWindowListener) context
				.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
				.get(WINDOW_LISTENER_KEY);
	}
}

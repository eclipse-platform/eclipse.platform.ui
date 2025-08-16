/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.rules.ExternalResource;

/**
 * Rule that close windows opened during the test case and checks for shells
 * unintentionally leaked from the test case.
 */
public class CloseTestWindowsRule extends ExternalResource {

	private final List<IWorkbenchWindow> testWindows;

	private TestWindowListener windowListener;

	private Set<Shell> initialShells;

	private boolean leakChecksDisabled;

	public CloseTestWindowsRule() {
		testWindows = new ArrayList<>(3);
	}

	@Override
	protected void before() throws Exception {
		addWindowListener();
		storeInitialShells();
	}

	@Override
	protected void after() {
		removeWindowListener();
		processEvents();
		closeAllTestWindows();
		processEvents();
		checkForLeakedShells();
	}

	/**
	 * Adds a window listener to the workbench to keep track of opened test windows.
	 */
	private void addWindowListener() {
		windowListener = new TestWindowListener();
		PlatformUI.getWorkbench().addWindowListener(windowListener);
	}

	/**
	 * Removes the listener added by <code>addWindowListener</code>.
	 */
	private void removeWindowListener() {
		if (windowListener != null) {
			PlatformUI.getWorkbench().removeWindowListener(windowListener);
		}
	}

	/**
	 * Close all test windows.
	 */
	private void closeAllTestWindows() {
		List<IWorkbenchWindow> testWindowsCopy = new ArrayList<>(testWindows);
		for (IWorkbenchWindow testWindow : testWindowsCopy) {
			testWindow.close();
		}
		testWindows.clear();
	}

	private class TestWindowListener implements IWindowListener {
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

	private void storeInitialShells() {
		this.initialShells = Set.of(PlatformUI.getWorkbench().getDisplay().getShells());
	}

	private void checkForLeakedShells() {
		List<String> leakedModalShellTitles = new ArrayList<>();
		Shell[] shells = PlatformUI.getWorkbench().getDisplay().getShells();
		for (Shell shell : shells) {
			if (!shell.isDisposed() && !initialShells.contains(shell)) {
				leakedModalShellTitles.add(shell.getText());
				shell.close();
			}
		}
		if (!leakChecksDisabled) {
			assertEquals("Test leaked modal shell: [" + String.join(", ", leakedModalShellTitles) + "]", 0,
					leakedModalShellTitles.size());
		}
	}

	public void disableLeakChecks() {
		this.leakChecksDisabled = true;
	}

}

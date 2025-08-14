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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.rules.ExternalResource;

public class CloseTestWindowsRule extends ExternalResource {

	private boolean enabled = true;

	private final List<IWorkbenchWindow> testWindows;

	private TestWindowListener windowListener;

	public CloseTestWindowsRule() {
		testWindows = new ArrayList<>(3);
	}

	@Override
	protected void before() throws Exception {
		addWindowListener();
	}

	@Override
	protected void after() {
		removeWindowListener();
		processEvents();
		closeAllTestWindows();
		processEvents();
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
	public void closeAllTestWindows() {
		List<IWorkbenchWindow> testWindowsCopy = new ArrayList<>(testWindows);
		for (IWorkbenchWindow testWindow : testWindowsCopy) {
			testWindow.close();
		}
		testWindows.clear();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	class TestWindowListener implements IWindowListener {
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
			if (enabled) {
				testWindows.remove(window);
			}
		}

		@Override
		public void windowOpened(IWorkbenchWindow window) {
			if (enabled) {
				testWindows.add(window);
			}
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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

package org.eclipse.ui.tests.keys;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.AutomationUtil;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests that pressing delete in a styled text widget does not cause a double
 * delete situation.
 *
 * @since 3.0
 */
@Ignore("disabled as it fails on the Mac.")
// Ctrl+S doesn't save the editor, and posting MOD1+S also doesn't seem to work.
public class Bug53489Test {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	/**
	 * Tests that pressing delete in a styled text widget (in a running Eclipse)
	 * does not cause a double delete.
	 */
	@Test
	public void testDoubleDelete() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject testProject = workspace.getRoot().getProject("DoubleDeleteestProject"); //$NON-NLS-1$
		testProject.create(null);
		testProject.open(null);
		IFile textFile = testProject.getFile("A.txt"); //$NON-NLS-1$
		String originalContents = "A blurb"; //$NON-NLS-1$
		ByteArrayInputStream inputStream = new ByteArrayInputStream(originalContents.getBytes());
		textFile.create(inputStream, true, null);
		IDE.openEditor(window.getActivePage(), textFile, true);

		// Allow the editor to finish opening.
		Display display = Display.getCurrent();
		while (display.readAndDispatch()) {
		}

		AutomationUtil.performKeyCodeEvent(display, SWT.KeyDown, SWT.DEL);
		AutomationUtil.performKeyCodeEvent(display, SWT.KeyUp, SWT.DEL);
		AutomationUtil.performKeyCodeEvent(display, SWT.KeyDown, SWT.CTRL);
		AutomationUtil.performCharacterEvent(display, SWT.KeyDown, 'S');
		AutomationUtil.performCharacterEvent(display, SWT.KeyUp, 'S');
		AutomationUtil.performKeyCodeEvent(display, SWT.KeyUp, SWT.CTRL);

		// Spin the event loop.
		while (display.readAndDispatch()) {
		}

		// Test the text is only one character different.
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(textFile.getContents()));
		String currentContents = reader.readLine();
		assertTrue("'DEL' deleted more than one key.", (originalContents //$NON-NLS-1$
				.length() == (currentContents.length() + 1)));
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.keys;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.ParseException;
import org.eclipse.ui.tests.util.UITestCase;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Tests Bug 43321
 * 
 * @since 3.0
 */
public class Bug43321Test extends UITestCase {

	/**
	 * Constructor for Bug43321Test.
	 * 
	 * @param name
	 *           The name of the test
	 */
	public Bug43321Test(String name) {
		super(name);
	}

	/**
	 * Tests that non-check box items on the menu are not checked when
	 * activated from the keyboard.
	 * 
	 * @throws ParseException
	 *            If "CTRL+C" isn't a valid key stroke.
	 */
	public void testNoCheckOnNonCheckbox() throws CoreException, ParseException {
		IWorkbenchWindow window = openTestWindow();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject testProject = workspace.getRoot().getProject("TestProject"); //$NON-NLS-1$
		testProject.create(null);
		testProject.open(null);
		AbstractTextEditor editor = (AbstractTextEditor) window.getActivePage().openEditor(testProject.getFile(".project")); //$NON-NLS-1$
		editor.selectAndReveal(0, 1);

		// Update the display.
		Shell shell = window.getShell();
		Display display = shell.getDisplay();
		while (display.readAndDispatch());

		// Press "Ctrl+C" to perform a copy.
		KeyStroke[] keyStrokes = { KeyStroke.getInstance("CTRL+C")}; //$NON-NLS-1$
		Event event = new Event();
		((Workbench) window.getWorkbench()).press(keyStrokes, event);

		// Get the menu item we've just selected.
		IAction action = editor.getEditorSite().getActionBars().getGlobalActionHandler(IWorkbenchActionConstants.COPY);
		assertTrue("Non-checkbox menu item is checked.", !action.isChecked()); //$NON-NLS-1$
	}
}

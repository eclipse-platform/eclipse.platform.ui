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

import org.eclipse.ui.tests.util.UITestCase;

/**
 * Tests Bug 40023
 * 
 * @since 3.0
 */
public class Bug40023Test extends UITestCase {

	/**
	 * Constructor for Bug40023Test.
	 * 
	 * @param name
	 *           The name of the test
	 */
	public Bug40023Test(String name) {
		super(name);
	}

	/**
	 * Tests that check box items on the menu are checked when activated from 
	 * the keyboard.
	 */
	public void testCheckOnCheckbox()  {
		// TODO Need a way to set a key binding.
//		IWorkbenchWindow window = openTestWindow();
//		IWorkspace workspace = ResourcesPlugin.getWorkspace();
//		IProject testProject = workspace.getRoot().getProject("TestProject"); //$NON-NLS-1$
//		testProject.create(null);
//		testProject.open(null);
//		AbstractTextEditor editor = (AbstractTextEditor) window.getActivePage().openEditor(testProject.getFile(".project")); //$NON-NLS-1$
//		editor.selectAndReveal(0, 1);
//
//		// Update the display.
//		Shell shell = window.getShell();
//		Display display = shell.getDisplay();
//		while (display.readAndDispatch());
//
//		// Press "Ctrl+C" to perform a copy.
//		KeyStroke[] keyStrokes = { KeyStroke.getInstance("CTRL+C")}; //$NON-NLS-1$
//		Event event = new Event();
//		((Workbench) window.getWorkbench()).press(keyStrokes, event);
//
//		// Get the menu item we've just selected.
//		IAction action = editor.getEditorSite().getActionBars().getGlobalActionHandler(IWorkbenchActionConstants.COPY);
//		assertTrue("Non-checkbox menu item is checked.", !action.isChecked()); //$NON-NLS-1$
	}

//	public static MenuItem getMenuItem(MenuItem[] menuItems, String text) {
//		for (int i = 0; i < menuItems.length; i++) {
//			if (menuItems[i].getText().equals(text)) {
//				return menuItems[i];
//			}
//		}
//
//		return null;
//	}
}

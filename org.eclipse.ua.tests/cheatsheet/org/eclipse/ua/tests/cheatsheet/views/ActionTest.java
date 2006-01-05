/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.cheatsheet.views;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.swt.SWT;
import org.eclipse.ua.tests.cheatsheet.util.IncrementVariableAction;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.Keyboard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;

/*
 * Tests the cheat sheet actions.
 */
public class ActionTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(ActionTest.class);
	}
	
	/*
	 * Open the cheat sheets dialog.
	 */
	protected void setUp() throws Exception {
		IWorkbenchPage page = UserAssistanceTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.hideView(page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView"));
	}

	/*
	 * Run through the TestActionsWithSkip.xml actions.
	 */
	public void testSimpleActions() {
		/*
		 * Open the cheat sheet
		 */
		OpenCheatSheetAction action = new OpenCheatSheetAction("org.eclipse.ua.tests.cheatsheet.actions.TestActionsWithSkip");
		action.run();
		
		/*
		 * Initialize variable. Two of the actions will increment this.
		 */
		IncrementVariableAction.variable = 0;
		
		/*
		 * Complete intro item
		 */
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r');

		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 0);
		
		/*			
		 * Complete all other actions
		 */
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r');

		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 0);

		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r');

		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 1);

		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r');

		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 1);
		
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r');
		
		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 2);
	}
	
	/*
	 * Run through the TestActionsWithSkip.xml actions, skipping some of them and
	 * verifying that they don't get executed.
	 */
	public void testSkipActions() {
		/*
		 * Open the cheat sheet
		 */
		OpenCheatSheetAction action = new OpenCheatSheetAction("org.eclipse.ua.tests.cheatsheet.actions.TestActionsWithSkip");
		action.run();
		
		/*
		 * Initialize variable. Two of the actions will increment this.
		 */
		IncrementVariableAction.variable = 0;
		
		/*
		 * Complete intro item
		 */
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r');

		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 0);
		
		/*			
		 * Complete all other actions, skipping some of them
		 */
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r');

		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 0);

		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB); // skip (don't increment)
		Keyboard.press('\r');

		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 0);

		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB); // skip
		Keyboard.press('\r');

		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 0);
		
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r');
		
		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 1);
	}
}

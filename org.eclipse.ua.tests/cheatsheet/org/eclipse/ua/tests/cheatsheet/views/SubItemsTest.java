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
 * Tests cheat sheet sub-items.
 */
public class SubItemsTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(SubItemsTest.class);
	}
	
	/*
	 * Make sure the cheat sheet view is closed before starting.
	 */
	protected void setUp() throws Exception {
		IWorkbenchPage page = UserAssistanceTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.hideView(page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView"));
	}

	/*
	 * Close the cheat sheet view.
	 */
	protected void tearDown() throws Exception {
		IWorkbenchPage page = UserAssistanceTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.hideView(page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView"));
	}
	
	/*
	 * Run through the TestSubItems.xml actions.
	 */
	public void testSubItems() {
		/*
		 * Open the cheat sheet
		 */
		OpenCheatSheetAction action = new OpenCheatSheetAction("org.eclipse.ua.tests.cheatsheet.subitems");
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
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r');

		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 0);
		
		/*			
		 * Complete all other actions
		 */
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r'); // sub1 : complete

		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 0);

		Keyboard.press(SWT.TAB);
		Keyboard.press('\r'); // sub2: perform action

		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 1);

		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r'); // sub3: complete

		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 1);
		
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r'); // sub4: perform action 
		
		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 2);

		Keyboard.press(SWT.TAB);
		Keyboard.press('\r'); // sub5: complete
		
		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 2);

		Keyboard.press(SWT.TAB);
		Keyboard.press('\r'); // sub6: perform action
		
		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 3);

		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r'); // sub7: complete
		
		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 3);

		Keyboard.press(SWT.TAB);
		Keyboard.press('\r'); // sub8: perform action
		
		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 4);

		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r'); // sub9: skip
		
		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 4);

		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r'); // sub10: skip
		
		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 4);

		Keyboard.press(SWT.TAB);
		Keyboard.press('\r'); // sub11: perform action (no-op)
		
		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 4);

		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r'); // sub12: complete
		
		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 4);

		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r'); // All Done!: complete
		
		Assert.assertEquals("IncrementVariableAction.variable was not the expected value", IncrementVariableAction.variable, 4);
	}
}

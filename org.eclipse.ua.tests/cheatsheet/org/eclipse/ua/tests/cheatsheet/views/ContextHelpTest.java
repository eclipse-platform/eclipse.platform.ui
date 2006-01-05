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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.Keyboard;
import org.eclipse.ua.tests.util.WidgetFinder;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;

/*
 * Tests the cheat sheets view content.
 */
public class ContextHelpTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(ContextHelpTest.class);
	}
	
	/*
	 * Make sure cheat sheet and help view are not open.
	 */
	protected void setUp() throws Exception {
		IWorkbenchPage page = UserAssistanceTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.hideView(page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView"));
		page.hideView(page.findView("org.eclipse.help.ui.HelpView"));
	}
	
	/*
	 * Close all the views when finished.
	 */
	protected void tearDown() throws Exception {
		IWorkbenchPage page = UserAssistanceTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.hideView(page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView"));
		page.hideView(page.findView("org.eclipse.help.ui.HelpView"));
	}
	
	/*
	 * Tests the context help for a cheat sheet.
	 */
	public void testContextHelp() {
		/*
		 * Open the cheat sheet
		 */
		OpenCheatSheetAction action = new OpenCheatSheetAction("org.eclipse.ua.tests.infopop.help");
		action.run();
		
		IWorkbenchWindow window = CheatSheetPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow();
		Shell workbenchShell = window.getShell();
		IWorkbenchPage page = window.getActivePage();
		Assert.assertFalse("Unexpectedly found the infopop text before starting the test. It should not be present yet; halting test.", WidgetFinder.containsText("Infopop test successful>! The \"contextId\" was specified correctly in the cheat sheets content file.", workbenchShell));
		
		/*
		 * Complete intro item
		 */
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r');

		/*			
		 * Complete all other actions
		 */
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r');

		Keyboard.press(SWT.TAB);
		Keyboard.press(SWT.TAB);
		Keyboard.press('\r');

		Keyboard.press(SWT.TAB);
		Keyboard.press('\r');
		
		IViewPart helpView = page.findView("org.eclipse.help.ui.HelpView");
		Assert.assertNotNull("Opened context help, but the help view did not appear", helpView);
		Assert.assertTrue("The help view was not visible after opening context help (it was in the background)", page.isPartVisible(helpView));
		Assert.assertTrue("Could not find the infopop text that was supposed to appear", WidgetFinder.containsText("Infopop test successful! The \"contextId\" was specified correctly in the cheat sheets content file.", workbenchShell));
		Assert.assertTrue("Could not find the infopop link that was supposed to appear", WidgetFinder.containsText("abcdefg", workbenchShell));
	}
}

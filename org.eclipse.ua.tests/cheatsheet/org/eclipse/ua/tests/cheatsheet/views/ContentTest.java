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

import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.DisplayUtil;
import org.eclipse.ua.tests.util.Keyboard;
import org.eclipse.ua.tests.util.WidgetFinder;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheet;
import org.eclipse.ui.internal.cheatsheets.data.Item;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetViewer;

/*
 * Tests the cheat sheets view content.
 */
public class ContentTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(ContentTest.class);
	}
	
	/*
	 * Make sure the view is not open.
	 */
	protected void setUp() throws Exception {
		IWorkbenchPage page = UserAssistanceTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.hideView(page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView"));
	}
	
	/*
	 * Open all cheat sheets, then close them.
	 */
	public void testOpenCheatSheets() {
		IExtensionPoint extPt = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.ui.cheatsheets.cheatSheetContent");
		IExtension[] extensions = extPt.getExtensions();
		for (int i=0;i<extensions.length;++i) {
			IConfigurationElement[] elem = extensions[i].getConfigurationElements();
			for (int j=0;j<elem.length;++j) {
				if (elem[j].getName().equals("cheatsheet")) {
					OpenCheatSheetAction action = new OpenCheatSheetAction(elem[j].getAttribute("id"));
					action.run();
					DisplayUtil.flush();
					IWorkbenchPage page = UserAssistanceTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IViewPart view = page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView");
					Assert.assertNotNull("The cheat sheet view could not be opened.", view);
					page.hideView(view);
				}
			}
		}
	}

	/*
	 * Open the view with no cheat sheet, and make sure it displays the appropriate message.
	 */
	public void testOpenEmptyView() {
		IWorkbenchPage page = UserAssistanceTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.hideView(page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView"));
		try {
			page.showView("org.eclipse.ui.cheatsheets.views.CheatSheetView");
			CheatSheetView view = (CheatSheetView)page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView");
			Assert.assertNotNull("Unable to open cheat sheet view with no cheat sheet", view);
			Assert.assertTrue("Did not find the appropriate error string in the cheat sheet view upon opening with no cheat sheet", WidgetFinder.containsText(Messages.INITIAL_VIEW_DIRECTIONS, view.getCheatSheetViewer().getControl()));
		}
		catch(Exception e) {
			Assert.fail("An exception was thrown while trying to open an empty cheat sheet view: " + e);
		}
	}

	/*
	 * Try to open a non-existant cheat sheet.
	 */
	public void testOpenNonExistantSheet() {
		/*
		 * First make sure the view is closed.
		 */
		IWorkbenchPage page = UserAssistanceTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CheatSheetView view = (CheatSheetView)page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView");
		if (view != null) {
			page.hideView(view);
		}

		/*
		 * Try to open an invalid cheat sheet.
		 */
		OpenCheatSheetAction action = new OpenCheatSheetAction("this.id.is.not.valid");
		action.run();

		/*
		 * Confirm the view shows and displays the right error message.
		 */
		view = (CheatSheetView)page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView");
		Assert.assertNotNull("Tried opening an invalid cheat sheet, but the view did not show (it should show with an error message)", view);

		Control viewControl = view.getCheatSheetViewer().getControl();
		Assert.assertTrue("Did not find the correct error message for the cheat sheet view title when opening non-existent cheat sheet", WidgetFinder.containsText(Messages.ERROR_LOADING_CHEATSHEET_CONTENT, viewControl));
		Assert.assertTrue("Did not find the correct error message for the cheat sheet view body when opening non-existent cheat sheet", WidgetFinder.containsText(Messages.ERROR_CHEATSHEET_DOESNOT_EXIST, viewControl));
	}

	/*
	 * Run through the cheat sheets.
	 */
	public void testRunThroughCheatSheets() {
		IExtensionPoint extPt = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.ui.cheatsheets.cheatSheetContent");
		IExtension[] extensions = extPt.getExtensions();
		for (int i=0;i<extensions.length;++i) {
			IConfigurationElement[] elem = extensions[i].getConfigurationElements();
			for (int j=0;j<elem.length;++j) {
				if (elem[j].getName().equals("cheatsheet")) {
					/*
					 * Open the cheat sheet
					 */
					OpenCheatSheetAction action = new OpenCheatSheetAction(elem[j].getAttribute("id"));
					action.run();
					
					/*
					 * Find the view
					 */
					IWorkbenchPage page = UserAssistanceTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
					CheatSheetView view = (CheatSheetView)page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView");
					
					/*
					 * Verify the title
					 */
					try {
						String cheatSheetTitle = view.getCheatSheetViewer().getCheatSheet().getTitle();
						Assert.assertTrue("The cheat sheet's title did not appear anywhere in the cheat sheets view.", WidgetFinder.containsText(cheatSheetTitle, view.getCheatSheetViewer().getControl()));
					}
					catch (Exception e) {
						/*
						 * Something went wrong. Ensure that the error page is shown.
						 */
						Assert.assertTrue("An error occured, but the error title was not displayed in the cheat sheet view", WidgetFinder.containsText(Messages.ERROR_LOADING_CHEATSHEET_CONTENT, view.getCheatSheetViewer().getControl()));
						Assert.assertTrue("An error occured, but the error message was not displayed in the cheat sheet view", WidgetFinder.containsText(Messages.ERROR_PAGE_MESSAGE, view.getCheatSheetViewer().getControl()));
						
						/*
						 * Don't step through this one.
						 */
						continue;
					}
					
					/*
					 * Step through the cheat sheet
					 */
					CheatSheetViewer viewer = view.getCheatSheetViewer();
					CheatSheet sheet = viewer.getCheatSheet();
					
					/*
					 * Verify the initial item, it is open by default.
					 */
					Item intro = sheet.getIntroItem();
					Assert.assertTrue("Could not find the intro cheat sheet item title in the cheat sheet view: \"" + intro.getTitle() + "\"", WidgetFinder.containsText(intro.getTitle(), viewer.getControl()));
					
					// no way to get text from a FormText
					//Assert.assertTrue("Could not find the intro cheat sheet item descriptions in the cheat sheet view: \"" + intro.getDescription() + "\"", WidgetFinder.containsText(intro.getDescription(), viewer.getControl()));
					
					Keyboard.press(SWT.ARROW_LEFT);
					Keyboard.press(SWT.ARROW_DOWN);
					
					/*
					 * Verify all other items
					 */
					Iterator iter = sheet.getItems().iterator();
					while (iter.hasNext()) {
						Item item = (Item)iter.next();
						Keyboard.press(SWT.ARROW_RIGHT);
						Assert.assertTrue("Could not find one of the cheat sheet item titles in the cheat sheet view: \"" + item.getTitle() + "\"", WidgetFinder.containsText(item.getTitle(), viewer.getControl()));

						// no way to get text from a FormText
						//Assert.assertTrue("Could not find one of the cheat sheet item descriptions in the cheat sheet view: \"" + item.getDescription() + "\"", WidgetFinder.containsText(item.getDescription(), viewer.getControl()));
						
						Keyboard.press(SWT.ARROW_LEFT);
						Keyboard.press(SWT.ARROW_DOWN);
					}
				}
			}
		}
	}
}

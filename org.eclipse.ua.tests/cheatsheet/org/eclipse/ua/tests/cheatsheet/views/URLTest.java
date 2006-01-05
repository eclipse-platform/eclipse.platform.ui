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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Path;
import org.eclipse.help.internal.appserver.WebappManager;
import org.eclipse.swt.SWT;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.Keyboard;
import org.eclipse.ua.tests.util.ResourceFinder;
import org.eclipse.ua.tests.util.WidgetFinder;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.cheatsheets.AbstractItemExtensionElement;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.data.Action;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheet;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheetParser;
import org.eclipse.ui.internal.cheatsheets.data.Item;
import org.eclipse.ui.internal.cheatsheets.data.PerformWhen;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetViewer;

/*
 * Tests the cheat sheets view content.
 */
public class URLTest extends TestCase {

	private static final String WEB_APP_NAME = "cheatsheets.test";
	
	private String fHostName;
	private int fPort;
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(URLTest.class);
	}
	
	/*
	 * Hide the view and turn on the web server that will serve the cheat sheets.
	 */
	protected void setUp() throws Exception {
		/*
		 * Make sure the view is closed.
		 */
		IWorkbenchPage page = UserAssistanceTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.hideView(page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView"));

		WebappManager.start(WEB_APP_NAME, "org.eclipse.ua.tests", new Path("data/cheatsheet/valid"));
		fHostName = WebappManager.getHost();
		fPort = WebappManager.getPort();
	}
	
	protected void tearDown() throws Exception {
		/*
		 * Make sure the view is closed.
		 */
		IWorkbenchPage page = UserAssistanceTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.hideView(page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView"));
		
		/*
		 * Stop the app server when done.
		 */
		WebappManager.stop(WEB_APP_NAME);
	}
	
	/*
	 * Tests trying to open an invalid URL.
	 */
	public void testInvalidURLParsing() {
		try {
			CheatSheetParser parser = new CheatSheetParser();
			CheatSheet sheet = parser.parse(new URL("http://ladsfkjaafkewryugakewf:12345/asdfasdf/asfdasfa"));
			Assert.assertNull("Tried parsing a cheat sheet from an invalid URL, but got a non-null CheatSheet!", sheet);
		}
		catch (MalformedURLException e) {
			Assert.fail("Got a MalformedURLException in URL test");
		}
	}
	
	/*
	 * Check to make sure that the parsed cheat sheet via URL is the same as via file.
	 */
	public void testURLParsing() {
		final String PATH = "data/cheatsheet/valid";
		URL[] urls = ResourceFinder.findFiles(UserAssistanceTestPlugin.getDefault(), PATH, ".xml", true);
		Assert.assertTrue("Unable to find sample cheat sheets to test parser", urls.length > 0);
		
		for (int i=0;i<urls.length;++i) {
			String path = urls[i].getPath();
			String relative = path.substring(path.indexOf(PATH) + PATH.length() + 1);
			String urlString = "http://" + fHostName + ":" + fPort + "/" + WEB_APP_NAME + "/" + relative;
			URL url = null;
			try {
				url = new URL(urlString);
			}
			catch (MalformedURLException e) {
				Assert.fail("Got a MalformedURLException in URL test: " + urlString);
			}

			CheatSheetParser parser = new CheatSheetParser();
			CheatSheet local = parser.parse(urls[i]);
			CheatSheet remote = parser.parse(url);
			
			verifyEquals("Cheat sheets parsed via URL and directly did not match", local, remote);
		}
	}
	
	/*
	 * Test the cheat sheet view content with URL (TestOpeningURL.xml).
	 */
	public void testViewWithURL() {
		/*
		 * Open the cheat sheet
		 */
		try {
			OpenCheatSheetAction action = new OpenCheatSheetAction("url.cheatsheet.id", "My URL Cheat Sheet", new URL("http://" + fHostName + ":" + fPort + "/" + WEB_APP_NAME + "/TestOpeningURL.xml"));
			action.run();
		}
		catch(Exception e) {
			Assert.fail("An exception was thrown while opening the cheat sheet with a URL: " + e);
		}
		
		/*
		 * Find the view
		 */
		IWorkbenchPage page = UserAssistanceTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CheatSheetView view = (CheatSheetView)page.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView");
		
		/*
		 * Verify the title
		 */
		String cheatSheetTitle = view.getCheatSheetViewer().getCheatSheet().getTitle();
		Assert.assertTrue("The URL-loaded cheat sheet's title did not appear anywhere in the cheat sheets view.", WidgetFinder.containsText(cheatSheetTitle, view.getCheatSheetViewer().getControl()));
		
		/*
		 * Step through the cheat sheet
		 */
		CheatSheetViewer viewer = view.getCheatSheetViewer();
		CheatSheet sheet = viewer.getCheatSheet();
		
		/*
		 * Verify the initial item, it is open by default.
		 */
		Item intro = sheet.getIntroItem();
		Assert.assertTrue("Could not find the intro URL-loaded cheat sheet item title in the cheat sheet view: \"" + intro.getTitle() + "\"", WidgetFinder.containsText(intro.getTitle(), viewer.getControl()));
		
		// no way to get text from a FormText
		//Assert.assertTrue("Could not find the intro URL-loaded cheat sheet item descriptions in the cheat sheet view: \"" + intro.getDescription() + "\"", WidgetFinder.containsText(intro.getDescription(), viewer.getControl()));
		
		Keyboard.press(SWT.ARROW_LEFT);
		Keyboard.press(SWT.ARROW_DOWN);
		
		/*
		 * Verify all other items
		 */
		Iterator iter = sheet.getItems().iterator();
		while (iter.hasNext()) {
			Item item = (Item)iter.next();
			Keyboard.press(SWT.ARROW_RIGHT);
			Assert.assertTrue("Could not find one of the URL-loaded cheat sheet item titles in the cheat sheet view: \"" + item.getTitle() + "\"", WidgetFinder.containsText(item.getTitle(), viewer.getControl()));

			// no way to get text from a FormText
			//Assert.assertTrue("Could not find one of the URL-loaded cheat sheet item descriptions in the cheat sheet view: \"" + item.getDescription() + "\"", WidgetFinder.containsText(item.getDescription(), viewer.getControl()));
			
			Keyboard.press(SWT.ARROW_LEFT);
			Keyboard.press(SWT.ARROW_DOWN);
		}

	}
	
	/*
	 * Verifies that the two cheat sheets are identical in their content.
	 */
	private static void verifyEquals(String msg, CheatSheet a, CheatSheet b) {
		// to make it more readable
		if (!msg.endsWith(": ")) {
			msg = msg + ": ";
		}

		if (a == b) {
			return;
		}
		else if (a == null || b == null) {
			Assert.fail(msg + "One of the cheat sheets was null");
		}
		else {
			// both non-null
			Assert.assertEquals(msg + "Titles didn't match", a.getTitle(), b.getTitle());
			verifyEquals(msg, a.getIntroItem(), b.getIntroItem());
			
			List la = a.getItems();
			List lb = b.getItems();
			Assert.assertEquals("The number of items was different", la.size(), lb.size());
			
			for (int i=0;i<la.size();++i) {
				verifyEquals(msg, (Item)la.get(i), (Item)lb.get(i));
			}
		}
	}
	
	/*
	 * Verifies that two items are identical in their content.
	 */
	private static void verifyEquals(String msg, Item a, Item b) {
		// to make it more readable
		if (!msg.endsWith(": ")) {
			msg = msg + ": ";
		}
		
		if (a == b) {
			return;
		}
		else if (a == null | b == null) {
			Assert.fail(msg + "One of the items was null while its corresponding item in the other cheat sheet was not");
		}
		else {
			// both non-null
			Assert.assertEquals("Item titles didn't match", a.getTitle(), b.getTitle());
			Assert.assertEquals("Item descriptions didn't match", a.getDescription(), b.getDescription());
			Assert.assertEquals("Item context IDs didn't match", a.getContextId(), b.getContextId());
			Assert.assertEquals("Item help links didn't match", a.getHref(), b.getHref());
			Assert.assertEquals("Item dynamic flags didn't match", a.isDynamic(), b.isDynamic());
			Assert.assertEquals("Item skip flags didn't match", a.isSkip(), b.isSkip());
			
			verifyEquals(msg, a.getAction(), b.getAction());
			
			List la = a.getItemExtensions();
			List lb = b.getItemExtensions();
			
			// if one is null, make sure both are null
			if (la == null || lb == null) {
				Assert.assertTrue("One of the item extension lists was null, while the other was not", la == lb);
			}
			else {
				Assert.assertEquals("The number of item extensions was different", la.size(), lb.size());
	
				Iterator ia = la.iterator();
				Iterator ib = lb.iterator();
				while (ia.hasNext()) {
					AbstractItemExtensionElement[] aa = (AbstractItemExtensionElement[])ia.next();
					AbstractItemExtensionElement[] ab = (AbstractItemExtensionElement[])ib.next();
					
					// if one is null, make sure both are null
					if (la == null || lb == null) {
						Assert.assertTrue("One of the item extension lists was null, while the other was not", la == lb);
					}
					else {
						Assert.assertEquals("The number of elements in one of the item extensions was different", aa.length, ab.length);
						for (int i=0;i<aa.length;++i) {
							verifyEquals(msg, aa[i], ab[i]);
						}
					}
				}
			}
			verifyEquals(msg, a.getPerformWhen(), b.getPerformWhen());
		}
	}
	
	/*
	 * Verifies that two actions are identical.
	 */
	private static void verifyEquals(String msg, Action a, Action b) {
		// to make it more readable
		if (!msg.endsWith(": ")) {
			msg = msg + ": ";
		}
		
		if (a == b) {
			return;
		}
		else if (a == null | b == null) {
			Assert.fail(msg + "One of the actions was null while its peer in the other cheat sheet was not");
		}
		else {
			// both non-null
			Assert.assertEquals("Action confirm flags didn't match", a.isConfirm(), b.isConfirm());
			Assert.assertEquals("Action classes didn't match", a.getActionClass(), b.getActionClass());
			Assert.assertEquals("Action plugin IDs didn't match", a.getPluginID(), b.getPluginID());
			Assert.assertEquals("Action When expressions didn't match", a.getWhen(), b.getWhen());

			String[] pa = a.getParams();
			String[] pb = b.getParams();
			
			if (pa == null || pb == null) {
				Assert.assertTrue("One of the action params arrays was null, the other wasn't", pa == pb);
			}
			else {
				Assert.assertEquals("Number of action params was different", pa.length, pb.length);
				for (int i=0;i<pa.length;++i) {
					Assert.assertEquals("One of the action params was different", pa[i], pb[i]);
				}
			}
		}
	}

	/*
	 * Verifies that two item extensions are identical.
	 */
	private static void verifyEquals(String msg, AbstractItemExtensionElement a, AbstractItemExtensionElement b) {
		// to make it more readable
		if (!msg.endsWith(": ")) {
			msg = msg + ": ";
		}
		
		if (a == b) {
			return;
		}
		else if (a == null | b == null) {
			Assert.fail(msg + "One of the item extensions was null while its peer in the other cheat sheet was not");
		}
		else {
			// both non-null
			Assert.assertEquals("Item extension attribute names didn't match", a.getAttributeName(), b.getAttributeName());
		}
	}
	
	/*
	 * Verifies that two PerformWhens are identical.
	 */
	private static void verifyEquals(String msg, PerformWhen a, PerformWhen b) {
		// to make it more readable
		if (!msg.endsWith(": ")) {
			msg = msg + ": ";
		}
		
		if (a == b) {
			return;
		}
		else if (a == null | b == null) {
			Assert.fail(msg + "One of the PerformWhens was null while its peer in the other cheat sheet was not");
		}
		else {
			// both non-null
			Assert.assertEquals("PerformWhen conditions did not match", a.getCondition(), b.getCondition());
			
			verifyEquals(msg, a.getAction(), b.getAction());
			verifyEquals(msg, a.getSelectedAction(), b.getSelectedAction());
			
			List la = a.getActions();
			List lb = b.getActions();
			
			// if one is null, make sure both are null
			if (la == null || lb == null) {
				Assert.assertTrue("One of the PerformWhen's actions lists was null, while the other was not", la == lb);
			}
			Assert.assertEquals("The number of PerformWhen's actions was different", la.size(), lb.size());

			Iterator ia = la.iterator();
			Iterator ib = lb.iterator();
			while (ia.hasNext()) {
				verifyEquals(msg, (Action)ia.next(), (Action)ib.next());
			}
		}
	}
}

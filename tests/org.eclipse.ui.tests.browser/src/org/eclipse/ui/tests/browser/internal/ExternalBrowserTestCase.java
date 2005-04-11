/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.browser.internal;

import java.net.URL;

import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserPreference;

import junit.framework.TestCase;

public class ExternalBrowserTestCase extends TestCase {
	public void testBrowser() throws Exception {
		WebBrowserPreference.setBrowserChoice(WebBrowserPreference.EXTERNAL);
		IWorkbenchBrowserSupport wbs = WebBrowserTestsPlugin.getInstance().getWorkbench().getBrowserSupport();
		IWebBrowser wb = wbs.createBrowser("test2");
		
		wb.openURL(new URL("http://www.ibm.com"));
		
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// ignore
		}
		
		wb.openURL(new URL("http://www.eclipse.org"));
		
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// ignore
		}
		
		wb.close();
		
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// ignore
		}
	}
}
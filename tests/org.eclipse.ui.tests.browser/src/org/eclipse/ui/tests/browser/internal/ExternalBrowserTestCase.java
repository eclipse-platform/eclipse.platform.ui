/*******************************************************************************
 * Copyright (c) 2004, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.browser.internal;

import java.net.URI;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.junit.jupiter.api.Test;

public class ExternalBrowserTestCase {
	@Test
	public void testBrowser() throws Exception {
		WebBrowserPreference.setBrowserChoice(WebBrowserPreference.EXTERNAL);
		IWorkbenchBrowserSupport wbs = PlatformUI.getWorkbench().getBrowserSupport();
		IWebBrowser wb = wbs.createBrowser("test2");

		wb.openURL(new URI("http://www.ibm.com").toURL());

		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// ignore
		}

		wb.openURL(new URI("http://www.eclipse.org").toURL());

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
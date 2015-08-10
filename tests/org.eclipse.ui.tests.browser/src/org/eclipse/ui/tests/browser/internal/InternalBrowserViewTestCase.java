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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserPreference;

import junit.framework.TestCase;

public class InternalBrowserViewTestCase extends TestCase {
	protected Shell shell;

	public void testBrowser() throws Exception {
		shell = WebBrowserTestsPlugin.getInstance().getWorkbench().getActiveWorkbenchWindow().getShell();
		WebBrowserPreference.setBrowserChoice(WebBrowserPreference.INTERNAL);
		IWorkbenchBrowserSupport wbs = WebBrowserTestsPlugin.getInstance().getWorkbench().getBrowserSupport();
		IWebBrowser wb = wbs.createBrowser(IWorkbenchBrowserSupport.AS_VIEW, "test3", "MyBrowser", "A tooltip");

		wb.openURL(new URL("http://www.ibm.com"));
		runLoopTimer(2);

		wb.openURL(new URL("http://www.eclipse.org"));
		runLoopTimer(2);

		wb.close();
		runLoopTimer(2);
	}

	void runLoopTimer(final int seconds) {
		final boolean[] exit = {false};
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(seconds * 1000);
				} catch (Exception e) {
					// ignore
				}
				exit[0] = true;
				// wake up the event loop
				Display display = Display.getDefault();
				if (!display.isDisposed()) {
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							if (!shell.isDisposed()) shell.redraw();
						}
					});
				}
			}
		}.start();
		shell.open();
		Display display = Display.getCurrent();
		while (!exit[0] && !shell.isDisposed()) if (!display.readAndDispatch()) display.sleep();
	}
}
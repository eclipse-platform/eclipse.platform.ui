/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.junit.Test;

public class InternalBrowserViewTestCase {
	protected Shell shell;

	@Test
	public void testBrowser() throws Exception {
		shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WebBrowserPreference.setBrowserChoice(WebBrowserPreference.INTERNAL);
		IWorkbenchBrowserSupport wbs = PlatformUI.getWorkbench().getBrowserSupport();
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
					display.asyncExec(() -> {
						if (!shell.isDisposed())
							shell.redraw();
					});
				}
			}
		}.start();
		shell.open();
		Display display = Display.getCurrent();
		while (!exit[0] && !shell.isDisposed()) if (!display.readAndDispatch()) display.sleep();
	}

	@Test
	public void testDefaultBrowserStyle() {
		class TestBrowserViewer extends BrowserViewer {
			public TestBrowserViewer(Composite parent, int style) {
				super(parent, style);
			}

			@Override
			protected int getBrowserStyle() {
				return super.getBrowserStyle();
			}
		}

		TestBrowserViewer browserViewer = new TestBrowserViewer(shell, SWT.NONE);
		int browserStyle = browserViewer.getBrowserStyle();
		// Assert: Verify the returned style is SWT.NONE
		assertEquals("The default browser style should be SWT.NONE", SWT.NONE, browserStyle);
	}

	@Test
	public void testCustomBrowserStyle() {
		class CustomBrowserViewer extends BrowserViewer {
			public CustomBrowserViewer(Composite parent, int style) {
				super(parent, style);
			}

			@Override
			protected int getBrowserStyle() {
				return SWT.EDGE; // Custom style
			}
		}

		CustomBrowserViewer customBrowserViewer = new CustomBrowserViewer(shell, SWT.NONE);
		int browserStyle = customBrowserViewer.getBrowserStyle();
		// Assert: Verify the returned style is SWT.EDGE
		assertEquals("The custom browser style should be SWT.EDGE", SWT.EDGE, browserStyle);
	}
}
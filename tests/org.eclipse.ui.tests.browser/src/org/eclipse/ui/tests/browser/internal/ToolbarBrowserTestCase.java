/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.browser.internal;

import junit.framework.Test;
import junit.framework.TestCase;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.browser.BrowserViewer;

public class ToolbarBrowserTestCase extends TestCase {
	protected static Dialog dialog;
	protected static Shell shell;
	protected static BrowserViewer browser;

	class TestToolbarBrowser extends BrowserViewer {

		public TestToolbarBrowser(Composite parent, int style) {
			super(parent, style);
		}

		public void testProtectedMethods() {
			super.addToHistory("www.eclispe.org");
			super.updateBackNextBusy();
			super.updateHistory();
			super.updateLocation();
		}
	}

	public static Test suite() {
		return new OrderedTestSuite(ToolbarBrowserTestCase.class, "ToolbarBrowserTestCase");
	}

	public void test00Open() throws Exception {
		shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		dialog = new Dialog(shell) {
			protected Control createDialogArea(Composite parent) {
				Composite composite = (Composite) super.createDialogArea(parent);

				browser = new BrowserViewer(composite, BrowserViewer.LOCATION_BAR | BrowserViewer.BUTTON_BAR);
				GridData data = new GridData(GridData.FILL_BOTH);
				data.widthHint = 400;
				data.heightHint = 400;
				browser.setLayoutData(data);

				return composite;
			}
		};
		dialog.setBlockOnOpen(false);
		dialog.open();

		boolean b = Display.getCurrent().readAndDispatch();
		while (b)
			b = Display.getCurrent().readAndDispatch();
	}

	public void test01SetURL() throws Exception {
		runLoopTimer(5);
		browser.setURL("http://www.eclipse.org");
		runLoopTimer(10);
	}

	public void test02Home() throws Exception {
		browser.home();
		runLoopTimer(2);
	}

	public void test03SetURL() throws Exception {
		browser.setURL("http://www.eclipse.org/webtools/index.html");
		runLoopTimer(10);
	}

	public void test04IsBackEnabled() throws Exception {
		assertTrue(browser.isBackEnabled());
	}

	public void test05Back() throws Exception {
		assertTrue(browser.back());
		runLoopTimer(5);
	}

	public void test06IsForwardEnabled() throws Exception {
		assertTrue(browser.isForwardEnabled());
	}

	public void test07Forward() throws Exception {
		assertTrue(browser.forward());
		runLoopTimer(5);
	}

	public void test08Refresh() throws Exception {
		browser.refresh();
	}

	public void test09GetBrowser() throws Exception {
		assertNotNull(browser.getBrowser());
	}

	public void test10Stop() throws Exception {
		browser.stop();
	}

	public void test11GetURL() throws Exception {
		assertNotNull(browser.getURL());
	}

	public void test12SetFocus() throws Exception {
		browser.setFocus();
	}

	public void test13Close() throws Exception {
		dialog.close();
	}

	TestToolbarBrowser ttb = null;

	public void test14ProtectedMethods() {
		shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		dialog = new Dialog(shell) {
			protected Control createDialogArea(Composite parent) {
				Composite composite = (Composite) super.createDialogArea(parent);

				ttb = new TestToolbarBrowser(composite, BrowserViewer.LOCATION_BAR | BrowserViewer.BUTTON_BAR);
				GridData data = new GridData(GridData.FILL_BOTH);
				data.widthHint = 400;
				data.heightHint = 400;
				ttb.setLayoutData(data);

				return composite;
			}
		};
		dialog.setBlockOnOpen(false);
		dialog.open();

		ttb.testProtectedMethods();
		dialog.close();
	}

	public void test15Listeners() {
		BrowserViewer.IBackNextListener listener = new BrowserViewer.IBackNextListener() {
			public void updateBackNextBusy() {
				// ignore
			}
		};

		listener.updateBackNextBusy();
	}

	public void test16Listeners() {
		BrowserViewer.ILocationListener listener = new BrowserViewer.ILocationListener() {
			public void locationChanged(String url) {
				// ignore
			}

			public void historyChanged(String[] history2) {
				// ignore
			}
		};

		listener.locationChanged(null);
		listener.historyChanged(null);
	}

	void runLoopTimer(final int seconds) {
		final boolean[] exit = {false};
		new Thread() {
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
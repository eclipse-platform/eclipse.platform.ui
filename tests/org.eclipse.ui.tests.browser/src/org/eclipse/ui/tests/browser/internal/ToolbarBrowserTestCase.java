/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ToolbarBrowserTestCase {
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

	@Test
	public void test00Open() throws Exception {
		shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		dialog = new Dialog(shell) {
			@Override
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

	@Test
	public void test01SetURL() throws Exception {
		runLoopTimer(5);
		browser.setURL("http://www.eclipse.org");
		runLoopTimer(10);
	}

	@Test
	public void test02Home() throws Exception {
		browser.home();
		runLoopTimer(2);
	}

	@Test
	public void test03SetURL() throws Exception {
		browser.setURL("http://www.eclipse.org/webtools/index.html");
		runLoopTimer(10);
	}

	@Test
	public void test04IsBackEnabled() throws Exception {
		assertTrue(browser.isBackEnabled());
	}

	@Test
	public void test05Back() throws Exception {
		assertTrue(browser.back());
		runLoopTimer(5);
	}

	@Test
	public void test06IsForwardEnabled() throws Exception {
		assertTrue(browser.isForwardEnabled());
	}

	@Test
	public void test07Forward() throws Exception {
		assertTrue(browser.forward());
		runLoopTimer(5);
	}

	@Test
	public void test08Refresh() throws Exception {
		browser.refresh();
	}

	@Test
	public void test09GetBrowser() throws Exception {
		assertNotNull(browser.getBrowser());
	}

	@Test
	public void test10Stop() throws Exception {
		browser.stop();
	}

	@Test
	public void test11GetURL() throws Exception {
		assertNotNull(browser.getURL());
	}

	@Test
	public void test12SetFocus() throws Exception {
		browser.setFocus();
	}

	@Test
	public void test13Close() throws Exception {
		dialog.close();
	}

	TestToolbarBrowser ttb = null;

	@Test
	public void test14ProtectedMethods() {
		shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		dialog = new Dialog(shell) {
			@Override
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

	@Test
	public void test15Listeners() {
		BrowserViewer.IBackNextListener listener = () -> {
			// ignore
		};

		listener.updateBackNextBusy();
	}

	@Test
	public void test16Listeners() {
		BrowserViewer.ILocationListener listener = new BrowserViewer.ILocationListener() {
			@Override
			public void locationChanged(String url) {
				// ignore
			}

			@Override
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
}
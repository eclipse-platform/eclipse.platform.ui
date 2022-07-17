/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
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
import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.junit.Test;

public class InternalBrowserEditorTestCase {
	protected Shell shell;

	@Test
	public void testBrowser() throws Exception {
		shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WebBrowserPreference.setBrowserChoice(WebBrowserPreference.INTERNAL);
		IWorkbenchBrowserSupport wbs = PlatformUI.getWorkbench().getBrowserSupport();
		IWebBrowser wb = wbs.createBrowser(IWorkbenchBrowserSupport.AS_EDITOR, "test", "MyBrowser", "A tooltip");

		wb.openURL(new URL("http://www.ibm.com"));
		runLoopTimer(2);

		wb.openURL(new URL("http://www.eclipse.org"));
		runLoopTimer(2);

		wb.close();
		runLoopTimer(2);
	}

	@Test
	public void testBrowserID() throws Exception {
		shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WebBrowserPreference.setBrowserChoice(WebBrowserPreference.INTERNAL);
		IWorkbenchBrowserSupport wbs = PlatformUI.getWorkbench().getBrowserSupport();
		IWebBrowser wb = wbs.createBrowser(IWorkbenchBrowserSupport.AS_EDITOR, "test", "MyBrowser", "A tooltip");

		wb.openURL(new URL("http://www.ibm.com"));
		runLoopTimer(2);

		IEditorReference[] editorRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences();
		IEditorReference editor = editorRefs[0];
		editor.getEditor(true);

		WebBrowserEditorInput editorInput = (WebBrowserEditorInput) editor.getEditorInput();
		assertNotNull(editorInput.getBrowserId());
		assertEquals(wb.getId(), editorInput.getBrowserId());

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
}
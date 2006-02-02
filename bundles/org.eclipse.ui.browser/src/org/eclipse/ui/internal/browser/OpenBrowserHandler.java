/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class OpenBrowserHandler extends AbstractHandler {

	private static final String PARAM_ID_URL = "url"; //$NON-NLS-1$

	private static final String PARAM_ID_BROWSER_ID = "browserId"; //$NON-NLS-1$

	private static final String PARAM_ID_NAME = "name"; //$NON-NLS-1$

	private static final String PARAM_ID_TOOLTIP = "tooltip"; //$NON-NLS-1$

	public Object execute(ExecutionEvent event) throws ExecutionException {

		String urlText = event.getParameter(PARAM_ID_URL);
		URL url;
		if (urlText == null) {
			url = null;
		} else {
			try {
				url = new URL(urlText);
			} catch (MalformedURLException ex) {
				throw new ExecutionException("malformed URL:" + urlText, ex); //$NON-NLS-1$
			}
		}

		String browserId = event.getParameter(PARAM_ID_BROWSER_ID);
		String name = event.getParameter(PARAM_ID_NAME);
		String tooltip = event.getParameter(PARAM_ID_TOOLTIP);

		try {
			IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench()
					.getBrowserSupport();
			IWebBrowser browser = browserSupport.createBrowser(
					IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR,
					browserId, name, tooltip);
			browser.openURL(url);
		} catch (PartInitException ex) {
			throw new ExecutionException("error opening browser", ex); //$NON-NLS-1$
		}

		return null;
	}
}

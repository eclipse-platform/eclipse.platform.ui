/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.text.IRegion;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;


/**
 * URL hyperlink.
 *
 * @since 3.1
 */
final class URLHyperlink extends org.eclipse.jface.text.hyperlink.URLHyperlink {


	/**
	 * Creates a new URL hyperlink.
	 *
	 * @param region the region
	 * @param urlString the URL string
	 */
	public URLHyperlink(IRegion region, String urlString) {
		super(region, urlString);
	}

	@Override
	public void open() {
		// Create the browser
		IWorkbenchBrowserSupport support= PlatformUI.getWorkbench().getBrowserSupport();
		IWebBrowser browser;
		try {
			browser= support.createBrowser(null);
		} catch (PartInitException e) {
			EditorsPlugin.logErrorStatus("Could not create Web browser for URLHyperlink", e.getStatus()); //$NON-NLS-1$
			super.open();
			return;
		}

		try {
			browser.openURL(new URL(getURLString()));
		} catch (PartInitException e) {
			super.open();
		} catch (MalformedURLException e) {
			super.open();
		}
	}
}

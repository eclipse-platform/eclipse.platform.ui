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
package org.eclipse.ui.browser.internal;

import java.util.HashMap;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.AbstractWorkbenchBrowserSupport;
import org.eclipse.ui.browser.IWebBrowser;
/**
 * Implementation of the workbench browser support.
 */
public class DefaultBrowserSupport extends AbstractWorkbenchBrowserSupport {
	protected HashMap browserIdMap = new HashMap();

	public DefaultBrowserSupport() {
		// do nothing
	}

	protected IWebBrowser getExistingWebBrowser(String browserId) {
		try {
			IWebBrowser browser = (IWebBrowser) browserIdMap.get(browserId);
			if (browser != null)
				return browser;
		} catch (Exception e) {
			// ignore
		}
		return null;
	}

	public IWebBrowser createBrowser(int style, String browserId, String name, String tooltip) throws PartInitException {
		IWebBrowser browser = getExistingWebBrowser(browserId);
		if (browser != null)
			return browser;
		
		IWebBrowser webBrowser = null;
		if (WebBrowserPreference.isUseInternalBrowser()) {
			webBrowser = new InternalBrowserInstance(browserId, style, name, tooltip);
		} else {
			IBrowserDescriptor ewb = BrowserManager.getInstance().getCurrentWebBrowser();
			webBrowser = new ExternalBrowserInstance(browserId, ewb);
		}
		browserIdMap.put(browserId, webBrowser);
		return webBrowser;
	}

	public IWebBrowser createBrowser(String browserId) throws PartInitException {
		return createBrowser(0, browserId, null, null);
	}
}
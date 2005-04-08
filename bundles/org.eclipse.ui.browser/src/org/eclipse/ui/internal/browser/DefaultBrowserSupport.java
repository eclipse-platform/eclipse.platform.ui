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
package org.eclipse.ui.internal.browser;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.AbstractWorkbenchBrowserSupport;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
/**
 * Implementation of the workbench browser support.
 */
public class DefaultBrowserSupport extends AbstractWorkbenchBrowserSupport {
	private static final String SHARED_ID = "org.eclipse.ui.browser";
	protected HashMap browserIdMap = new HashMap();
	protected static DefaultBrowserSupport instance;

	public DefaultBrowserSupport() {
		// do nothing
		instance = this;
		BrowserManager.getInstance().addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				//TODO I am not sure what we should do here
				//The preferences have changed so maybe we should
				//close the opened browser in addition to clearing
				//the table
				browserIdMap.clear();
			}
		});
	}
	
	protected static DefaultBrowserSupport getInstance() {
		return instance;
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
		if (browserId==null) browserId = SHARED_ID;
		IWebBrowser browser = getExistingWebBrowser(browserId);
		if (browser != null)
			return browser;
		
		IWebBrowser webBrowser = null;

		// AS_EXTERNAL will force the external browser regardless of the user preference
		if ((style & AS_EXTERNAL) != 0 || !WebBrowserPreference.isUseInternalBrowser()) {
			IBrowserDescriptor ewb = BrowserManager.getInstance().getCurrentWebBrowser();
			IBrowserExt ext = WebBrowserUIPlugin.findBrowsers(ewb.getLocation());
			if (ext != null)
				webBrowser = ext.createBrowser(browserId,  ewb.getLocation(), ewb.getParameters());
			if (webBrowser == null)
				webBrowser = new ExternalBrowserInstance(browserId, ewb);
		} else {
			if ((style & IWorkbenchBrowserSupport.AS_VIEW) != 0)
				webBrowser = new InternalBrowserViewInstance(browserId, style, name, tooltip);
			else
				webBrowser = new InternalBrowserEditorInstance(browserId, style, name, tooltip);
		}
		
		browserIdMap.put(browserId, webBrowser);
		return webBrowser;
	}

	public IWebBrowser createBrowser(String browserId) throws PartInitException {
		return createBrowser(0, browserId, null, null);
	}
	
	protected void removeBrowser(String id) {
		browserIdMap.remove(id);
	}
}
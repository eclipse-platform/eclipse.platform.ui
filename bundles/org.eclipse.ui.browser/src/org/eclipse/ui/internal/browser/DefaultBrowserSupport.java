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

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.AbstractWorkbenchBrowserSupport;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * Implementation of the workbench browser support.
 */
public class DefaultBrowserSupport extends AbstractWorkbenchBrowserSupport {
	static final String SHARED_ID = "org.eclipse.ui.browser"; //$NON-NLS-1$

	protected HashMap browserIdMap = new HashMap();

	protected static DefaultBrowserSupport instance;

	public DefaultBrowserSupport() {
		// do nothing
		instance = this;
		BrowserManager.getInstance().addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				// TODO I am not sure what we should do here
				// The preferences have changed so maybe we should
				// close the opened browser in addition to clearing
				// the table
				browserIdMap.clear();
			}
		});
	}

	protected static DefaultBrowserSupport getInstance() {
		return instance;
	}

	protected IWebBrowser getExistingWebBrowser(String browserId) {
		try {
			Object obj = browserIdMap.get(browserId);
			IWebBrowser browser = null;
			if (obj instanceof IWebBrowser)
				browser = (IWebBrowser) obj;
			else if (obj instanceof HashMap) {
				HashMap wmap = (HashMap) obj;
				IWorkbenchWindow window = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				if (window != null) {
					browser = (IWebBrowser) wmap.get(getWindowKey(window));
				}
			}
			if (browser != null)
				return browser;
		} catch (Exception e) {
			// ignore
		}
		return null;
	}

	private Integer getWindowKey(IWorkbenchWindow window) {
		return new Integer(window.hashCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.browser.IWorkbenchBrowserSupport#createBrowser(int,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public IWebBrowser createBrowser(int style, String browserId, String name,
			String tooltip) throws PartInitException {
		if (browserId == null)
			browserId = SHARED_ID;
		IWebBrowser browser = getExistingWebBrowser(browserId);
		if (browser != null) {
			if (browser instanceof InternalBrowserInstance) {
				InternalBrowserInstance instance2 = (InternalBrowserInstance) browser;
				instance2.setName(name);
				instance2.setTooltip(tooltip);
			}
			return browser;
		}

		IWebBrowser webBrowser = null;

		// AS_EXTERNAL will force the external browser regardless of the user
		// preference
		if ((style & AS_EXTERNAL) != 0
				|| WebBrowserPreference.getBrowserChoice() != WebBrowserPreference.INTERNAL) {
			IBrowserDescriptor ewb = BrowserManager.getInstance()
					.getCurrentWebBrowser();
			if (ewb == null)
				throw new PartInitException(Messages.errorNoBrowser);
			
			if (ewb instanceof SystemBrowserDescriptor)
				webBrowser = new SystemBrowserInstance(browserId);
			else {
				IBrowserExt ext = null;
				if (ewb != null)
					ext = WebBrowserUIPlugin.findBrowsers(ewb.getLocation());
				if (ext != null)
					webBrowser = ext.createBrowser(browserId,
							ewb.getLocation(), ewb.getParameters());
				if (webBrowser == null)
					webBrowser = new ExternalBrowserInstance(browserId, ewb);
			}
		} else {
			if ((style & IWorkbenchBrowserSupport.AS_VIEW) != 0)
				webBrowser = new InternalBrowserViewInstance(browserId, style,
						name, tooltip);
			else
				webBrowser = new InternalBrowserEditorInstance(browserId,
						style, name, tooltip);
		}

		if (webBrowser instanceof InternalBrowserInstance) {
			// we should only share internal browsers within one
			// workbench window. Each workbench window can have
			// a shared browser with the same id.
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			Integer key = getWindowKey(window);
			HashMap wmap = (HashMap) browserIdMap.get(browserId);
			if (wmap == null) {
				wmap = new HashMap();
				browserIdMap.put(browserId, wmap);
			}
			wmap.put(key, webBrowser);
		} else {
			// External and system browsers are shared
			// for the entire workbench
			browserIdMap.put(browserId, webBrowser);
		}
		return webBrowser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.browser.IWorkbenchBrowserSupport#createBrowser(java.lang.String)
	 */
	public IWebBrowser createBrowser(String browserId) throws PartInitException {
		return createBrowser(0, browserId, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.browser.IWorkbenchBrowserSupport#isInternalWebBrowserAvailable()
	 */
	public boolean isInternalWebBrowserAvailable() {
		return WebBrowserUtil.canUseInternalWebBrowser();
	}

	protected void removeBrowser(IWebBrowser browser) {
		String baseId = WebBrowserUtil.decodeId(browser.getId());
		if (browser instanceof InternalBrowserInstance) {
			// Remove it from the window map and
			// also remove the window map itself if it is empty.
			Integer key = ((InternalBrowserInstance) browser).getWindowKey();
			HashMap wmap = (HashMap) browserIdMap.get(baseId);
			if (wmap != null) {
				wmap.remove(key);
				if (wmap.isEmpty())
					browserIdMap.remove(baseId);
			}
		} else
			browserIdMap.remove(baseId);
	}
}
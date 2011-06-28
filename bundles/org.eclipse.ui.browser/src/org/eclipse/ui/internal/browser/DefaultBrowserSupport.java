/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
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
	static final String DEFAULT_ID_BASE = "org.eclipse.ui.defaultBrowser"; //$NON-NLS-1$
	private static final String HELP_BROWSER_ID = "org.eclipse.help.ui"; //$NON-NLS-1$

	protected HashMap<String, Object> browserIdMap = new HashMap<String, Object>();

	protected static DefaultBrowserSupport instance;

	public DefaultBrowserSupport() {
		// do nothing
		instance = this;
		BrowserManager.getInstance().addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				// TODO I am not sure what we should do here
				// The preferences have changed so maybe we should
				// close the opened browsers in addition to clearing
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
				@SuppressWarnings("rawtypes")
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
			browserId = getDefaultId();
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
		// The help editor will open in an editor regardless of the user preferences
		boolean isHelpEditor = ((style & AS_EDITOR) != 0) && HELP_BROWSER_ID.equals(browserId);
		if ((style & AS_EXTERNAL) != 0 || 
		    (WebBrowserPreference.getBrowserChoice() != WebBrowserPreference.INTERNAL && !isHelpEditor)
		    || !WebBrowserUtil.canUseInternalWebBrowser()) {
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
			// a shared browser with the same id
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			Integer key = getWindowKey(window);
			@SuppressWarnings("unchecked")
			HashMap<Integer, IWebBrowser> wmap = (HashMap<Integer, IWebBrowser>) browserIdMap.get(browserId);
			if (wmap == null) {
				wmap = new HashMap<Integer, IWebBrowser>();
				browserIdMap.put(browserId, wmap);
			}
			wmap.put(key, webBrowser);
		} else {
			// external and system browsers are shared
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
			Object entry = browserIdMap.get(baseId);
			if (entry != null && entry instanceof HashMap) {
				@SuppressWarnings("rawtypes")
				HashMap wmap = (HashMap) entry;
				wmap.remove(key);
				if (wmap.isEmpty())
					browserIdMap.remove(baseId);
			}
		} else
			browserIdMap.remove(baseId);
	}
	
	private String getDefaultId() {
		String id = null;
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			id = DEFAULT_ID_BASE + i;
			if (browserIdMap.get(id) == null)
				break;
		}
		return id;
	}
}
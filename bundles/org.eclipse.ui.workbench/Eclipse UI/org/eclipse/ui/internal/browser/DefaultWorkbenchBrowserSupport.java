/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.browser;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.ui.browser.AbstractWorkbenchBrowserSupport;
import org.eclipse.ui.browser.IWebBrowser;

/**
 * Extends the abstract browser support class by providing minimal support for
 * external browsers.
 * <p>
 * This class is used when no alternative implementation is plugged in via the
 * 'org.eclipse.ui.browserSupport' extension point.
 * </p>
 *
 * @since 3.1
 */
public class DefaultWorkbenchBrowserSupport extends AbstractWorkbenchBrowserSupport {
	private Map<String, IWebBrowser> browsers;
	private static final String DEFAULT_BROWSER_ID_BASE = "org.eclipse.ui.defaultBrowser"; //$NON-NLS-1$

	/**
	 * The default constructor.
	 */
	public DefaultWorkbenchBrowserSupport() {
		browsers = new HashMap<>();
	}

	void registerBrowser(IWebBrowser browser) {
		browsers.put(browser.getId(), browser);
	}

	void unregisterBrowser(IWebBrowser browser) {
		browsers.remove(browser.getId());
	}

	IWebBrowser findBrowser(String id) {
		return browsers.get(id);
	}

	protected IWebBrowser doCreateBrowser(int style, String browserId, String name, String tooltip)
	{
		return new DefaultWebBrowser(this, browserId);
	}

	@Override
	public IWebBrowser createBrowser(int style, String browserId, String name, String tooltip)
	{
		IWebBrowser browser = findBrowser(browserId == null ? getDefaultId() : browserId);
		if (browser != null) {
			return browser;
		}
		browser = doCreateBrowser(style, browserId, name, tooltip);
		registerBrowser(browser);
		return browser;
	}

	@Override
	public IWebBrowser createBrowser(String browserId) {
		return createBrowser(AS_EXTERNAL, browserId, null, null);
	}

	private String getDefaultId() {
		String id = null;
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			id = DEFAULT_BROWSER_ID_BASE + i;
			if (browsers.get(id) == null)
				break;
		}
		return id;
	}
}

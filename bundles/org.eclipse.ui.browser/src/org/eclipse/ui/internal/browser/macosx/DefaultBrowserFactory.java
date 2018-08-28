/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.browser.macosx;

import org.eclipse.ui.browser.BrowserFactory;
import org.eclipse.ui.browser.IWebBrowser;

public class DefaultBrowserFactory extends BrowserFactory {
	/*
	 * @see BrowserFactory#createBrowser()
	 */
	@Override
	public IWebBrowser createBrowser(String id, String location, String parameters) {
		return new DefaultBrowser(id);
	}
}
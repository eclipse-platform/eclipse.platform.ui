/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.browser;

import org.eclipse.ui.browser.IWebBrowser;
/**
 * Implementators of <code>org.eclipse.ui.browser.browsers</code> extension
 * points must provide an implementation of this abstract class.
 * 
 * @since 3.1
 */
public abstract class BrowserFactory {
	/**
	 * Checks whether the factory can work on the user system.
	 * 
	 * @return <code>false</code> if the factory can work on this system; for
	 *    example the required native browser required by browser adapters that
	 *    it creates is not installed, or <code>true</code> otherwise
	 */
	public boolean isAvailable() {
		return true;
	}

	/**
	 * Obtains a new instance of a web browser.
	 * 
	 * @param id the browser id
	 * @param location the browser location
	 * @param parameters the browser parameters
	 * @return an instance of IWebBrowser
	 */
	public abstract IWebBrowser createBrowser(String id, String location, String parameters);
}
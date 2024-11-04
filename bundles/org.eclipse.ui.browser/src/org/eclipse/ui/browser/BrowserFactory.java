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
package org.eclipse.ui.browser;
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
	public Boolean isAvailable() {
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
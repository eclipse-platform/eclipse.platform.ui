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
package org.eclipse.ui.internal.browser.provisional;

import org.eclipse.ui.browser.IWebBrowser;
/**
 * Implementators of <code>org.eclipse.ui.browser.browsers</code> extension
 * points must provide implementation of this interface.
 * 
 * @since 1.0
 */
public interface IBrowserFactory {
	/**
	 * Checks whether the factory can work on the user system.
	 * 
	 * @return false if the factory cannot work on this system; for example the
	 *    required native browser required by browser adapters that it
	 *    creates is not installed
	 */
	public boolean isAvailable();

	/**
	 * Obtains a new instance of a web browser.
	 * 
	 * @return instance of IBrowser
	 */
	public IWebBrowser createBrowser(String id, String location, String parameters);
}

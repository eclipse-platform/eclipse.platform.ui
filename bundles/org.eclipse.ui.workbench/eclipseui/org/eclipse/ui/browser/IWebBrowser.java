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

import java.net.URL;
import org.eclipse.ui.PartInitException;

/**
 * An opened Web browser instance (either internal or external).
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @since 3.1
 * @see IWorkbenchBrowserSupport
 * @noimplement This interface is not intended to be implemented by clients.
 */

public interface IWebBrowser {
	/**
	 * Returns the unique identifier of this browser. If an id has been supplied to
	 * the browser support when the instance was created, it will be used.
	 * Otherwise, a generated id will be provided to the browser that is guaranteed
	 * to be unique.
	 *
	 * @return a unique identifier of this browser instance
	 */
	String getId();

	/**
	 * Opens a URL on this Web browser instance.
	 * <p>
	 * <b>NOTE</b> This method must be called from the current UI thread
	 * </p>
	 *
	 * @param url the URL to display
	 * @exception PartInitException if the browser fails to navigate to the provided
	 *                              url for any reason
	 */
	void openURL(URL url) throws PartInitException;

	/**
	 * Closes this browser instance.
	 * <p>
	 * <b>NOTE</b> This method must be called from the current UI thread
	 * </p>
	 *
	 * @return <code>true</code> if the browser was closed or <code>false</code> if
	 *         the operation failed or is not supported.
	 */
	boolean close();

}

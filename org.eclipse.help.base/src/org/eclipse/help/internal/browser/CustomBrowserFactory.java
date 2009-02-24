/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.browser;

import org.eclipse.help.browser.*;

/**
 * Produces Custom Browser
 */
public class CustomBrowserFactory implements IBrowserFactory {

	/**
	 * @see org.eclipse.help.browser.IBrowserFactory#isAvailable()
	 */
	public boolean isAvailable() {
		return true;
	}

	/**
	 * @see org.eclipse.help.browser.IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		return new CustomBrowser();
	}

}

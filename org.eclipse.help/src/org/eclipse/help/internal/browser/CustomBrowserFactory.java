/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
	 * @see org.eclipse.help.ui.browser.IBrowserFactory#isAvailable()
	 */
	public boolean isAvailable() {
		return true;
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		return new CustomBrowser();
	}

}

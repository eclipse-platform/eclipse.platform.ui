/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser;

import org.eclipse.help.ui.browser.*;

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

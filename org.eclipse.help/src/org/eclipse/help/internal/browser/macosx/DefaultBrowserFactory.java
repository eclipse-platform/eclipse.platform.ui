/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 * 
 * Andre Weinand, OTI - Initial version
 */
package org.eclipse.help.internal.browser.macosx;

import org.eclipse.help.browser.*;


public class DefaultBrowserFactory implements IBrowserFactory {

	public DefaultBrowserFactory() {
		super();
	}
	
	/*
	 * @see IBrowserFactory#isAvailable()
	 */
	public boolean isAvailable() {
		return System.getProperty("os.name").equals("Mac OS X");
		/*
		 * we assume that every Mac OS X has an "/usr/bin/osascript"
		 * so we don't test any further
		 */
	}
	
	/*
	 * @see IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		return DefaultBrowserAdapter.getInstance();
	}
}

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.solaris;
import org.eclipse.help.ui.browser.*;
public class NetscapeFactory implements IBrowserFactory {
	private NetscapeBrowserAdapter browserInstance = null;
	/**
	 * Constructor.
	 */
	public NetscapeFactory() {
		super();
	}
	/*
	 * @see IBrowserFactory#isAvailable()
	 */
	public boolean isAvailable() {
		return System.getProperty("os.name").toLowerCase().startsWith(
			"SunOS".toLowerCase());
	}
	/*
	 * @see IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		// Create single browser for all clients
		if (browserInstance == null) {
			browserInstance = new NetscapeBrowserAdapter();
		}
		return browserInstance;
	}
}
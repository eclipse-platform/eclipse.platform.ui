/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.solaris;
import org.eclipse.help.ui.browser.*;
public class NetscapeFactory implements IBrowserFactory {
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
		return System.getProperty("os.name").startsWith("SunOS");
	}
	/*
	 * @see IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		return NetscapeBrowserAdapter.getInstance();
	}
}
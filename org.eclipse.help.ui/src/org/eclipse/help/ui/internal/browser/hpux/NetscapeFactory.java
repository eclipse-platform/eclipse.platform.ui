/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.hpux;
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
		if (!System.getProperty("os.name").toLowerCase().startsWith("hp")) {
			return false;
		}
		return true;
	}
	/*
	 * @see IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		return NetscapeBrowserAdapter.getInstance();
	}
}
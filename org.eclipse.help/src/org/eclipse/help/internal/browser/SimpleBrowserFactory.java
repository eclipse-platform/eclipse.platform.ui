/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.browser;
import org.eclipse.core.runtime.*;
import org.eclipse.help.browser.*;
public class SimpleBrowserFactory implements IBrowserFactory {
	/**
	 * Constructor.
	 */
	public SimpleBrowserFactory() {
		super();
	}
	/*
	 * @see IBrowserFactory#isAvailable()
	 */
	public boolean isAvailable() {
		// this browser is only available on Windows when the system browser is not.
		return System.getProperty("os.name").startsWith("Win") &&
				Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.help.ui") == null;
	}
	/*
	 * @see IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		return new SimpleBrowserAdapter();
	}
}
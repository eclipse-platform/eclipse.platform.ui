/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.win32;
import org.eclipse.help.ui.browser.*;
public class IEFactory implements IBrowserFactory {
	/**
	 * Constructor.
	 */
	public IEFactory() {
		super();
	}
	/*
	 * @see IBrowserFactory#isAvailable()
	 */
	public boolean isAvailable() {
		return System.getProperty("os.name").startsWith("Win");
	}
	/*
	 * @see IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		return new IEBrowserAdapter();
	}
}
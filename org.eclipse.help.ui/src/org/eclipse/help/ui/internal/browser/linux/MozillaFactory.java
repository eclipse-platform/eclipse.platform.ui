/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.linux;
import org.eclipse.help.ui.internal.browser.*;
import org.eclipse.help.ui.browser.*;
import org.eclipse.help.ui.browser.*;
public class MozillaFactory implements IBrowserFactory {
	/**
	 * Constructor.
	 */
	public MozillaFactory() {
		super();
	}
	/*
	 * @see IBrowserFactory#isAvailable()
	 */
	public boolean isAvailable() {
		return System.getProperty("os.name").startsWith("Linux");
	}
	/*
	 * @see IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		return MozillaBrowserAdapter.getInstance();
	}
}
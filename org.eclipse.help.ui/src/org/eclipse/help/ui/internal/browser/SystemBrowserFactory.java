/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser;
import org.eclipse.help.ui.browser.*;
import org.eclipse.swt.program.Program;
public class SystemBrowserFactory implements IBrowserFactory {
	/**
	 * Constructor.
	 */
	public SystemBrowserFactory() {
		super();
	}
	/*
	 * @see IBrowserFactory#isAvailable()
	 */
	public boolean isAvailable() {
		return System.getProperty("os.name").startsWith("Win")
			|| Program.findProgram("html") != null;
	}
	/*
	 * @see IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		return new SystemBrowserAdapter();
	}
}
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.hpux;
import java.io.IOException;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.help.internal.ui.util.StreamConsumer;
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
		try {
			Process pr = Runtime.getRuntime().exec("which netscape");
			(new StreamConsumer(pr.getInputStream())).start();
			(new StreamConsumer(pr.getErrorStream())).start();
			pr.waitFor();
			int ret = pr.exitValue();
			if (ret == 0) {
				return true;
			}
		} catch (InterruptedException e) {
		} catch (IOException e) {
		}
		return false;
	}
	/*
	 * @see IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		return NetscapeBrowserAdapter.getInstance();
	}
}
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.linux;
import java.io.IOException;

import org.eclipse.help.internal.ui.util.StreamConsumer;
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
		if (!System.getProperty("os.name").startsWith("Linux")) {
			return false;
		}
		try {
			Process pr = Runtime.getRuntime().exec("which mozilla");
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
		return MozillaBrowserAdapter.getInstance();
	}
}
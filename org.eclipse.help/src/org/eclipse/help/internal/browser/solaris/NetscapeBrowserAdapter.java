/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.browser.solaris;
import java.io.*;

import org.eclipse.help.browser.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.browser.*;
import org.eclipse.help.internal.util.*;

public class NetscapeBrowserAdapter implements IBrowser {
	// delay that it takes the browser to start responding
	// to remote command after browser command has been called
	private static final int DELAY = 5000;
	private static long browserFullyOpenedAt = 0;
	private static boolean opened = false;
	private static Thread uiThread;
	/**
	 * Constructor
	 */
	NetscapeBrowserAdapter() {
		uiThread = Thread.currentThread();
	}
	/*
	 * @see IBrowser#close()
	 */
	public void close() {
	}
	/*
	 * @see IBrowser#displayURL(String)
	 */
	public synchronized void displayURL(String url) {
		new BrowserThread(url).start();
	}
	/*
	 * @see IBrowser#isCloseSupported()
	 */
	public boolean isCloseSupported() {
		return false;
	}
	/*
	 * @see IBrowser#isSetLocationSupported()
	 */
	public boolean isSetLocationSupported() {
		return false;
	}
	/*
	 * @see IBrowser#isSetSizeSupported()
	 */
	public boolean isSetSizeSupported() {
		return false;
	}
	/*
	 * @see IBrowser#setLocation(int, int)
	 */
	public void setLocation(int width, int height) {
	}
	/*
	 * @see IBrowser#setSize(int, int)
	 */
	public void setSize(int x, int y) {
	}
	private class BrowserThread extends Thread {
		private String url;
		public BrowserThread(String urlName) {
			this.url = urlName;
		}
		private void reuseBrowser(String browserCmd) {
			try {
				// If browser is opening, wait until it fully opens,
				while (System.currentTimeMillis() < browserFullyOpenedAt)
					try {
						Thread.sleep(100);
					} catch (InterruptedException ie) {
					}
				Process pr = Runtime.getRuntime().exec(browserCmd);
				(new StreamConsumer(pr.getInputStream())).start();
				(new StreamConsumer(pr.getErrorStream())).start();
				pr.waitFor();
			} catch (InterruptedException e) {
			} catch (IOException e) {
			}
		}
		public void run() {
			if (!opened) {
				openBrowser("netscape " + url);
			} else {
				reuseBrowser("netscape -remote openURL(" + url + ")");
			}
		}
	}
	public static synchronized void openBrowser(String browserCmd) {
		opened = true;
		browserFullyOpenedAt = System.currentTimeMillis() + DELAY;
		try {
			Process pr = Runtime.getRuntime().exec(browserCmd);
			(new StreamConsumer(pr.getInputStream())).start();
			(new StreamConsumer(pr.getErrorStream())).start();
			pr.waitFor();
		} catch (InterruptedException e) {
		} catch (IOException e) {
			String msg =
				Resources.getString("NetscapeBrowserAdapter.executeFailed");
			HelpPlugin.logError(msg, e);
			HelpSystem.getDefaultErrorUtil().displayError(msg, uiThread);
		} finally {
			opened = false;
		}
	}
}

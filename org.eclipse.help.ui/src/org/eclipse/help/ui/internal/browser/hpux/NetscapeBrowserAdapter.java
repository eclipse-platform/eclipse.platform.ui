/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.hpux;
import java.io.IOException;
import org.eclipse.help.internal.ui.util.StreamConsumer;
import org.eclipse.help.ui.browser.IBrowser;
public class NetscapeBrowserAdapter implements IBrowser {
	// delay that it takes browser to start responding
	// to remote command after the browser has been called
	private static int DELAY = 5000;
	private static long browserFullyOpenedAt = 0;
	private static BrowserThread lastBrowserThread = null;
	private static NetscapeBrowserAdapter instance;
	/**
	 */
	private NetscapeBrowserAdapter() {
	}
	public static NetscapeBrowserAdapter getInstance() {
		if (instance == null)
			instance = new NetscapeBrowserAdapter();
		return instance;
	}
	/*
	 * @see IBrowser#close()
	 */
	public void close() {
	}
	/*
	 * @see IBrowser#displayURL(String)
	 */
	public void displayURL(String url) {
		if (lastBrowserThread != null)
			lastBrowserThread.exitRequested = true;
		lastBrowserThread = new BrowserThread(url);
		lastBrowserThread.start();
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
		public boolean exitRequested = false;
		private String url;
		public BrowserThread(String urlName) {
			this.url = urlName;
		}
		private int openBrowser(String browserCmd) {
			try {
				Process pr = Runtime.getRuntime().exec(browserCmd);
				(new StreamConsumer(pr.getInputStream())).start();
				(new StreamConsumer(pr.getErrorStream())).start();
				pr.waitFor();
				return pr.exitValue();
			} catch (InterruptedException e) {
			} catch (IOException e) {
			}
			return -1;
		}
		public void run() {
			// If browser is opening, wait until it fully opens,
			waitForBrowser();
			if (exitRequested)
				return;
			if (openBrowser("netscape -remote openURL(" + url + ")") == 0)
				return;
			if (exitRequested)
				return;
			browserFullyOpenedAt = System.currentTimeMillis() + DELAY;
			openBrowser("netscape " + url);
		}
		private void waitForBrowser() {
			while (System.currentTimeMillis() < browserFullyOpenedAt)
				try {
					if (exitRequested)
						return;
					Thread.currentThread().sleep(100);
				} catch (InterruptedException ie) {
				}
		}
	}
}
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.solaris;
import java.io.IOException;
import org.eclipse.help.ui.internal.util.StreamConsumer;
import org.eclipse.help.ui.browser.IBrowser;
public class NetscapeBrowserAdapter implements IBrowser {
	// delay that it takes the browser to start responding
	// to remote command after browser command has been called
	private static int DELAY = 5000;
	private static long browserFullyOpenedAt = 0;
	private static boolean opened = false;
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
						Thread.currentThread().sleep(100);
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
		} finally {
			opened = false;
		}
	}
}
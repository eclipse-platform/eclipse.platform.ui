/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.linux;
import java.io.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.help.ui.browser.IBrowser;
import org.eclipse.help.ui.internal.WorkbenchHelpPlugin;
import org.eclipse.help.ui.internal.util.StreamConsumer;
public class NetscapeBrowserAdapter implements IBrowser {
	// delay that it takes netscape to start responding
	// to remote command after netscape has been called
	private static int DELAY = 5000;
	private static long browserFullyOpenedAt = 0;
	private static BrowserThread lastBrowserThread = null;
	private static NetscapeBrowserAdapter instance;
	private static int x, y;
	private static int width, height;
	private static boolean setLocationPending;
	private static boolean setSizePending;
	/**
	 * Constructor
	 */
	private NetscapeBrowserAdapter() {
	}
	public static NetscapeBrowserAdapter getInstance() {
		setLocationPending = false;
		setSizePending = false;
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
		if (setLocationPending || setSizePending) {
			url = createPositioningURL(url);
		}
		lastBrowserThread = new BrowserThread(url);
		lastBrowserThread.start();
		setLocationPending = false;
		setSizePending = false;
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
		return true;
	}
	/*
	 * @see IBrowser#isSetSizeSupported()
	 */
	public boolean isSetSizeSupported() {
		return true;
	}
	/*
	 * @see IBrowser#setLocation(int, int)
	 */
	public void setLocation(int x, int y) {
		NetscapeBrowserAdapter.x = x;
		NetscapeBrowserAdapter.y = y;
		setLocationPending = true;
	}
	/*
	 * @see IBrowser#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		NetscapeBrowserAdapter.width = width;
		NetscapeBrowserAdapter.height = height;
		setSizePending = true;
	}
	private synchronized String createPositioningURL(String url) {
		IPath pluginPath = WorkbenchHelpPlugin.getDefault().getStateLocation();
		File outFile =
			pluginPath
				.append("netscapePositon")
				.append("position.html")
				.toFile();
		try {
			outFile.getParentFile().mkdirs();
			PrintWriter writer =
				new PrintWriter(
					new BufferedWriter(
						new OutputStreamWriter(
							new FileOutputStream(outFile),
							"UTF8")),
					false);
			writer.println(
				"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
			writer.println("<html><head>");
			writer.println(
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
			writer.print("<title></title><script language=\"JavaScript\">");
			if (setSizePending)
				writer.print("window.resizeTo(" + width + "," + height + ");");
			if (setLocationPending)
				writer.print("window.moveTo(" + x + "," + y + ");");
			writer.print("location.replace(\"" + url + "\");");
			writer.print("</script></head><body>");
			writer.print("<a href=\"" + url + "\">--&gt;</a>");
			writer.print("</body></html>");
			writer.close();
			return "file://" + outFile.getAbsolutePath();
		} catch (IOException ioe) {
			// return the original url
			return url;
		}
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
					Thread.sleep(100);
				} catch (InterruptedException ie) {
				}
		}
	}
}
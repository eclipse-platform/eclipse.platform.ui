package org.eclipse.help.internal.ui.motif;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.IOException;

import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.contributions.Topic;
import org.eclipse.help.internal.ui.IBrowser;
import org.eclipse.help.internal.ui.util.StreamConsumer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
/**
 * Netscape based browser.
 */
class WebBrowser implements IBrowser {
	private static long browserFullyOpenedAt = 0;
	private static String browserPath;
	private static BrowserThread lastBrowserThread = null;
	Composite controlFrame;
	/**
	 */
	public WebBrowser(Composite parent) {
		controlFrame = new Composite(parent, SWT.NONE);
		controlFrame.setLayoutData(
			new GridData(
				GridData.GRAB_HORIZONTAL
					| GridData.GRAB_VERTICAL
					| GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_FILL));
	}
	private class BrowserThread extends Thread {
		public boolean exitRequested = false;
		private String url;
		public BrowserThread(String urlName) {
			this.url = urlName;
		}
		private void openNewBrowser(String url) {
			try {
				Process pr = Runtime.getRuntime().exec(browserPath + " " + url);
				(new StreamConsumer(pr.getInputStream())).start();
				(new StreamConsumer(pr.getErrorStream())).start();
				pr.waitFor();
			} catch (InterruptedException e) {
			} catch (IOException e) {
			}
		}
		private int reuseBrowser(String url) {
			try {
				Process pr =
					Runtime.getRuntime().exec(browserPath + " -remote openURL(" + url + ")");
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
			if (reuseBrowser(url) == 0) {
				return;
			}
			if (exitRequested)
				return;
			browserFullyOpenedAt = System.currentTimeMillis() + 5000;
			openNewBrowser(url);
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
	public int back() {
		return 0;
	}
	public int copy() {
		return 0;
	}
	public int forward() {
		return 0;
	}
	public Control getControl() {
		return controlFrame;
	}
	public String getLocationURL() {
		return null;
	}
	public int home() {
		return 0;
	}
	/**
	 * Causes browser to navigate to the given url
	 */
	public synchronized int navigate(String url) {
		browserPath = HelpSystem.getBrowserPath();
		if (browserPath == null || "".equals(browserPath))
			browserPath = "netscape";
		if(lastBrowserThread!=null)
			lastBrowserThread.exitRequested = true;
		lastBrowserThread = new BrowserThread(url);
		lastBrowserThread.start();
		return 0;
	}
	public int print() {
		// This feature is temporarily not supported on Linux.
		return 0;
	}
	/**
	 * Print a Topic and all it's children.
	 */
	public void printFullTopic(Topic rootTopic) {
		// This feature is temporarily not supported on Linux.      
	}
}
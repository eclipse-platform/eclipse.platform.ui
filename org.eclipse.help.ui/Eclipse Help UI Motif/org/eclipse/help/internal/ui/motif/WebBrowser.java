package org.eclipse.help.internal.ui.motif;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.IOException;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.jface.action.*;
import org.eclipse.help.internal.ui.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.ui.util.StreamConsumer;
import org.eclipse.help.internal.contributions.Topic;

/**
 * Netscape based browser. It opens an external window.
 */
class WebBrowser implements IBrowser {
	Composite controlFrame;
	private static boolean opened = false;
	// time when browser will be fully opened
	private static long browserFullyOpenedAt;

	private static String browserPath;

	class BrowserThread extends Thread {
		String url;

		public BrowserThread(String urlName) {
			this.url = urlName;
		}

		public synchronized void run() {
			if (!opened) {
				openBrowser(url);
			} else {
				reuseBrowser(url);
			}
		}

		public void reuseBrowser(String url) {
			try {
				// If browser has been  recently opened,
				// wait until anticipated time that it fully opens,
				while (System.currentTimeMillis() < browserFullyOpenedAt)
					try {
						Thread.currentThread().sleep(500);
					} catch (InterruptedException ie) {
					}
				Process pr =
					Runtime.getRuntime().exec(browserPath + " -remote openURL(" + url + ")");
				(new StreamConsumer(pr.getInputStream())).start();
				(new StreamConsumer(pr.getErrorStream())).start();
				pr.waitFor();
			} catch (InterruptedException e) {
			} catch (IOException e) {
			}
		}
	}

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
	 */
	public int navigate(String url) {
		browserPath = HelpSystem.getBrowserPath();
		if (browserPath == null || "".equals(browserPath))
			browserPath = "netscape";
		new BrowserThread(url).start();
		return 0;
	}
	private static synchronized void openBrowser(String url) {
		opened = true;
		browserFullyOpenedAt = System.currentTimeMillis() + 4000;
		try {
			Process pr = Runtime.getRuntime()
				//.exec("netscape -geometry =570x410+270+155 " + url);
	.exec(browserPath + " " + url);
			(new StreamConsumer(pr.getInputStream())).start();
			(new StreamConsumer(pr.getErrorStream())).start();
			pr.waitFor();
		} catch (InterruptedException e) {
		} catch (IOException e) {
		} finally {
			opened = false;
		}
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

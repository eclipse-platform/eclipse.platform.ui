/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.browser.embedded;
import org.eclipse.help.browser.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
/**
 * Web browser.
 */
public class EmbeddedBrowserAdapter implements IBrowser {
	private Display display;
	private EmbeddedBrowser browser;
	/**
	 * Adapter constructor.
	 */
	public EmbeddedBrowserAdapter() {
	}
	/*
	 * @see IBrowser#displayURL(String)
	 */
	public synchronized void displayURL(String url) {
		EmbeddedBrowser b = getBrowser();
		if (b != null)
			exec(new DisplayURL(b, url));
	}
	private class DisplayURL implements Runnable {
		EmbeddedBrowser b;
		String url;
		public DisplayURL(EmbeddedBrowser brows, String url) {
			this.b = brows;
			this.url = url;
		}
		public void run() {
			if (!b.isDisposed())
				b.displayUrl(url);
		}
	}
	/*
	 * @see IBrowser#close()
	 */
	public void close() {
		EmbeddedBrowser b = browser;
		if (b != null)
			exec(new Close(b));
	}
	private class Close implements Runnable {
		EmbeddedBrowser b;
		String url;
		public Close(EmbeddedBrowser brows) {
			this.b = brows;
		}
		public void run() {
			if (!b.isDisposed())
				b.close();
		}
	}
	/**
	 *  
	 */
	private EmbeddedBrowser getBrowser() {
		if (display != null && display.isDisposed()) {
			// disposed, reinitialize
			display = null;
			browser = null;
		}
		if (display == null) {
			Thread t = new Thread(new Runnable() {
				public void run() {
					display = new Display();
					try {
						browser = new EmbeddedBrowser();
						while (!browser.isDisposed()) {
							if (!display.readAndDispatch()) {
								display.sleep();
							}
						}
					} finally {
						display.dispose();
					}
				}
			});
			t.start();
			// wait until browser is created
			while (browser == null // not yet opened
					&& !(display != null && display.isDisposed())) { // already
																	 // closed
				try {
					Thread.sleep(50);
				} catch (InterruptedException ie) {
				}
			}
		}
		return browser;
	}
	private void exec(Runnable r){
		try {
			display.syncExec(r);
		} catch (SWTException swte) {
			// display can be disposed any time - ignore
			if(swte.code!= SWT.ERROR_DEVICE_DISPOSED)
				throw swte;
		}
	}
	/*
	 * @see IBrowser#isCloseSupported()
	 */
	public boolean isCloseSupported() {
		return true;
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
		// TODO browser.setLocation(x, y);
	}
	/*
	 * @see IBrowser#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		// TODO browser.setSize(width, height);
	}
	//	private String getWindowTitle() {
	//		if ("true"
	//			.equalsIgnoreCase(
	//				HelpBasePlugin.getDefault().getPluginPreferences().getString(
	//					"windowTitlePrefix"))) {
	//			return HelpUIResources.getString(
	//				"browserTitle",
	//				BaseHelpSystem.getProductName());
	//		} else {
	//			return BaseHelpSystem.getProductName();
	//		}
	//	}
}

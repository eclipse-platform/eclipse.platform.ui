/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.browser.embedded;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.browser.IBrowser;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpApplication;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
/**
 * Web browser.
 */
public class EmbeddedBrowserAdapter implements IBrowser, IBrowserCloseListener{
	private EmbeddedBrowser browser;
	// Thread to use in workbench mode on Windows
	private UIThread2 secondThread;
	class UIThread2 extends Thread {
		
		Display d;
		
		boolean runEventLoop = true;

		public UIThread2() {
			super();
			setDaemon(true);
			setName("Help Browser UI"); //$NON-NLS-1$
		}

		public void run() {
			d = new Display();
			while (runEventLoop) {
				if (!d.readAndDispatch()) {
					d.sleep();
				}
			}
			d.dispose();
		}
		public Display getDisplay() {
			while (d == null && isAlive()) {
				try {
					sleep(40);
				} catch (InterruptedException ie) {
				}
			}
			return d;
		}
		public void dispose() {
			runEventLoop = false;
		}
	}
	/**
	 * Adapter constructor.
	 */
	public EmbeddedBrowserAdapter() {
	}
	public Display getBrowserDisplay() {
		boolean useUIThread2 = BaseHelpSystem.getMode() == BaseHelpSystem.MODE_WORKBENCH
				&& Constants.OS_WIN32.equalsIgnoreCase(Platform.getOS())
		        && !Constants.WS_WPF.equalsIgnoreCase(SWT.getPlatform()) ;
		if (useUIThread2) {
			if (secondThread == null) {
				secondThread = new UIThread2();
				secondThread.start();
			}
			return secondThread.getDisplay();
		}
		return Display.getDefault();
	}

	public void browserClosed() {
		browser=null;
		if(secondThread!=null){
			secondThread.dispose();
			secondThread = null;
		}
	}
	/*
	 * @see IBrowser#displayURL(String)
	 */
	public synchronized void displayURL(final String url) {
		if (!HelpApplication.isShutdownOnClose()) {
			close();
		}
		if (getBrowserDisplay() == Display.getCurrent()) {
			uiDisplayURL(url);
		} else {
			getBrowserDisplay().asyncExec(new Runnable() {
				public void run() {
					uiDisplayURL(url);
				}
			});
		}
	}
	/**
	 * Must be run on UI thread
	 * 
	 * @param url
	 */
	private void uiDisplayURL(final String url) {
		getBrowser().displayUrl(url);
	}

	/*
	 * @see IBrowser#close()
	 */
	public synchronized void close() {
		if (getBrowserDisplay() == Display.getCurrent()) {
			uiClose();
		} else {
			getBrowserDisplay().syncExec(new Runnable() {
				public void run() {
					uiClose();
				}
			});
		}
	}
	/*
	 * Must be run on UI thread
	 */
	private void uiClose() {
		if (browser != null && !browser.isDisposed()){
			browser.close();
		}
		if(secondThread!=null){
			secondThread.dispose();
			secondThread=null;
		}
	}
	/**
	 *  
	 */
	private EmbeddedBrowser getBrowser() {
		if (browser == null || browser.isDisposed()) {
			browser = new EmbeddedBrowser();
			browser.addCloseListener(this);
		}
		return browser;
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
	public synchronized void setLocation(final int x, final int y) {
		if (getBrowserDisplay() == Display.getCurrent()) {
			uiSetLocation(x, y);
		} else {
			getBrowserDisplay().asyncExec(new Runnable() {
				public void run() {
					uiSetLocation(x, y);
				}
			});
		}
	}
	/*
	 * Must be run on UI thread
	 */
	private void uiSetLocation(int x, int y) {
		getBrowser().setLocation(x, y);
	}
	/*
	 * @see IBrowser#setSize(int, int)
	 */
	public synchronized void setSize(final int width, final int height) {
		if (getBrowserDisplay() == Display.getCurrent()) {
			uiSetSize(width, height);
		} else {
			getBrowserDisplay().asyncExec(new Runnable() {
				public void run() {
					uiSetSize(width, height);
				}
			});
		}
	}
	/*
	 * Must be run on UI thread
	 */
	private void uiSetSize(int width, int height) {
		getBrowser().setSize(width, height);
	}
}

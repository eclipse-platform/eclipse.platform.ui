/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Shawn Minto, patch for Bug 247731
 *******************************************************************************/
package org.eclipse.ui.internal.browser.browsers;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.browser.AbstractWebBrowser;
import org.eclipse.ui.internal.browser.WebBrowserUIPlugin;
import org.eclipse.ui.internal.browser.WebBrowserUtil;
/**
 * Browser adapter for browsers supporting -remote openURL command line option
 * i.e. Mozilla and Netscape.
 */
public class MozillaBrowser extends AbstractWebBrowser {
	// delay that it takes mozilla to start responding
	// to remote command after mozilla has been called
	protected static final int DELAY = 5000;

	protected long browserFullyOpenedAt = 0;

	private BrowserThread lastBrowserThread = null;

	protected String executable;
	
	protected boolean firstLaunch = true;

	private String parameters;

	/**
	 * Constructor
	 * 
	 * @executable executable filename to launch
	 * @executableName name of the program to display when error occurs
	 */
	public MozillaBrowser(String id, String executable, String parameters) {
		super(id);
		this.executable = executable;
		if (parameters == null) {
			this.parameters = ""; //$NON-NLS-1$
		} else {
		    this.parameters = parameters;
		}
	}
	
	String getExecutable() {
		return executable;
	}

	/*
	 * @see IBrowser#displayURL(String)
	 */
	public void openURL(URL url2) {
		String url = null;
		if (url2 != null) {
			url = url2.toExternalForm();
		}
		else {
			url = ""; //$NON-NLS-1$
		}
		if (lastBrowserThread != null)
			lastBrowserThread.exitRequested = true;

		lastBrowserThread = new BrowserThread(url);
		lastBrowserThread.setDaemon(true);
		lastBrowserThread.start();
	}

	private class BrowserThread extends Thread {
		public boolean exitRequested = false;

		private String url;

		public BrowserThread(String urlName) {
			this.url = urlName;
		}

		/**
		 * @param browserCmd
		 * @return int 0 if success
		 */
		private int openBrowser(String browserCmd) {
			try {
				Process pr = Runtime.getRuntime().exec(browserCmd);
				StreamConsumer outputs = new StreamConsumer(pr.getInputStream());
				(outputs).start();
				StreamConsumer errors = new StreamConsumer(pr.getErrorStream());
				(errors).start();
				pr.waitFor();
				int ret = pr.exitValue();

				if (ret == 0 && errorsInOutput(outputs, errors)) {
					return -1;
				}
				return ret;
			} catch (InterruptedException e) {
				// ignore
			} catch (IOException e) {
				WebBrowserUIPlugin.logError("Launching " + executable //$NON-NLS-1$
					+ " has failed.", e); //$NON-NLS-1$
				// TODO: log error
				/*String msg = HelpBaseResources.getString(
						"MozillaBrowser.executeFailed", //$NON-NLS-1$
						executableName);
				BaseHelpSystem.getDefaultErrorUtil()
						.displayError(msg, uiThread);*/
				// return success, so second command does not execute
				return 0;
			}
			return -1;
		}

		/**
		 * On some OSes 0 is always returned by netscape -remote. It is
		 * necessary to examine ouput to find out failure
		 * 
		 * @param outputs
		 * @param errors
		 * @return @throws
		 *         InterruptedException
		 */
		private boolean errorsInOutput(StreamConsumer outputs,
				StreamConsumer errors) {
			try {
				outputs.join(1000);
				if (outputs.getLastLine() != null
						&& (outputs.getLastLine().indexOf(
								"No running window found") //$NON-NLS-1$
						>= 0 || outputs.getLastLine().indexOf(
								"not running on display") //$NON-NLS-1$
						>= 0)) {
					return true;
				}
				errors.join(1000);
				if (errors.getLastLine() != null
						&& (errors.getLastLine().indexOf(
								"No running window found") //$NON-NLS-1$
						>= 0 || errors.getLastLine().indexOf(
								"not running on display") //$NON-NLS-1$
						>= 0)) {
					return true;
				}
			} catch (InterruptedException ie) {
				// ignore
			}
			return false;
		}

		public void run() {
			// if browser is opening, wait until it fully opens
			waitForBrowser();
			if (exitRequested)
				return;
			if (firstLaunch && Platform.OS_WIN32.equals(Platform.getOS())) {
				if (openBrowser(executable + " " + WebBrowserUtil.createParameterString(parameters, url)) == 0) //$NON-NLS-1$
					return;
				browserFullyOpenedAt = System.currentTimeMillis() + DELAY;
				return;
			}
			if (openBrowser(executable + ' ' + parameters + " -remote openURL(" + url + ")") //$NON-NLS-1$ //$NON-NLS-2$
					== 0)
				return;
			
			if (exitRequested)
				return;
			browserFullyOpenedAt = System.currentTimeMillis() + DELAY;
			openBrowser(executable + " " + WebBrowserUtil.createParameterString(parameters, url)); //$NON-NLS-1$
		}

		private void waitForBrowser() {
			while (System.currentTimeMillis() < browserFullyOpenedAt)
				try {
					if (exitRequested)
						return;
					Thread.sleep(100);
				} catch (InterruptedException ie) {
					// ignore
				}
		}
	}
}
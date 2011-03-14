/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.net.URL;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.AbstractWebBrowser;

/**
 * An instance of a running Web browser. rundll32.exe
 * url.dll,FileProtocolHandler www.ibm.com
 */
public class ExternalBrowserInstance extends AbstractWebBrowser {
	protected IBrowserDescriptor browser;

	protected Process process;

	public ExternalBrowserInstance(String id, IBrowserDescriptor browser) {
		super(id);
		this.browser = browser;
	}

	public void openURL(URL url) throws PartInitException {
		String urlText = url.toExternalForm();

		String location = browser.getLocation();
		String parameters = browser.getParameters();
		Trace
				.trace(
						Trace.FINEST,
						"Launching external Web browser: " + location + " - " + parameters + " - " + urlText); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		String params = WebBrowserUtil.createParameterString(parameters, urlText);

		try {
			Trace.trace(Trace.FINEST, "Launching " + location + " " + params); //$NON-NLS-1$//$NON-NLS-2$
			if (params == null || params.length() == 0)
				process = Runtime.getRuntime().exec(location);
			else
				process = Runtime.getRuntime().exec(location + " " + params); //$NON-NLS-1$
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Could not launch external browser", e); //$NON-NLS-1$
			WebBrowserUtil.openError(NLS.bind(
					Messages.errorCouldNotLaunchWebBrowser, urlText));
		}
		Thread thread = new Thread() {
			public void run() {
				try {
					process.waitFor();
					DefaultBrowserSupport.getInstance().removeBrowser(
							ExternalBrowserInstance.this);
				} catch (Exception e) {
					// ignore
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	public boolean close() {
		try {
			process.destroy();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
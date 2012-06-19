/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     Wind River - Fix the issue that failing to launch browser with space in its path
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.net.URL;
import java.util.ArrayList;

import org.eclipse.jface.util.Util;
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

		ArrayList<String> cmdOptions = new ArrayList<String>();
		String location = browser.getLocation();
		cmdOptions.add(location);
		String parameters = browser.getParameters();		
		Trace
		.trace(
				Trace.FINEST,
				"Launching external Web browser: " + location + " - " + parameters + " - " + urlText); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		String params = WebBrowserUtil.createParameterString(parameters, urlText);

		try {
			if ( Util.isMac()) {
				cmdOptions.add(0, "-a"); //$NON-NLS-1$
				cmdOptions.add(0, "open"); //$NON-NLS-1$
			}

			if (!(params == null || params.length() == 0))
				cmdOptions.add(params);

			String[] cmd = cmdOptions.toArray(new String[cmdOptions.size()]);
			Trace.trace(Trace.FINEST, "Launching " + join(" ", cmd)); //$NON-NLS-1$//$NON-NLS-2$

			process = Runtime.getRuntime().exec(cmd);
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

	private String join (String delim, String ... data) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			sb.append(data[i]);
			if (i >= data.length-1) {break;}
			sb.append(delim);
		}
		return sb.toString();
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
/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser.macosx;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.ui.internal.browser.WebBrowserUIPlugin;
import org.eclipse.ui.internal.browser.browsers.DefaultBrowser;

public class SafariBrowser extends DefaultBrowser {
	
	public SafariBrowser(String id, String location, String parameters) {
		super(id, location, parameters);
		this.location = location;
		this.parameters = parameters;
	}

	/**
	 * @see org.eclipse.help.browser.IBrowser#displayURL(String)
	 */
	public void openURL(URL url2) {
		String url = url2.toExternalForm();
		/*
		 * Code from Marc-Antoine Parent
		 */
		try {
			Runtime.getRuntime().exec(new String[] { "/usr/bin/osascript", //$NON-NLS-1$
					"-e", //$NON-NLS-1$
					"open location \"" + url + "\"" }); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException ioe) {
			WebBrowserUIPlugin.logError("Launching \"osascript\" has failed.", ioe); //$NON-NLS-1$
		}
	}

	/**
	 * Creates the final command to launch.
	 * 
	 * @param path
	 * @param url
	 * @return String[]
	 */
	protected String[] prepareCommand(String path, String url) {
		if (url != null && url.startsWith("file:")) { //$NON-NLS-1$
			url = url.substring(5);
		}
		
		ArrayList tokenList = new ArrayList();
		//Divide along quotation marks
		StringTokenizer qTokenizer = new StringTokenizer(path.trim(),
			"\"", true); //$NON-NLS-1$
		boolean withinQuotation = false;
		String quotedString = ""; //$NON-NLS-1$
		while (qTokenizer.hasMoreTokens()) {
			String curToken = qTokenizer.nextToken();
			if (curToken.equals("\"")) { //$NON-NLS-1$
				if (withinQuotation) {
					if (Constants.OS_WIN32.equalsIgnoreCase(Platform.getOS())) {
						// need to quote URLs on Windows
						tokenList.add("\"" + quotedString + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						// quotes prevent launching on Unix 35673
						tokenList.add(quotedString);
					}
				} else {
					quotedString = ""; //$NON-NLS-1$
				}
				withinQuotation = !withinQuotation;
				continue;
			} else if (withinQuotation) {
				quotedString = curToken;
				continue;
			} else {
				//divide unquoted strings along white space
				StringTokenizer parser = new StringTokenizer(curToken.trim());
				while (parser.hasMoreTokens()) {
					tokenList.add(parser.nextToken());
				}
			}
		}
		// substitute %1 by url
		boolean substituted = false;
		for (int i = 0; i < tokenList.size(); i++) {
			String token = (String) tokenList.get(i);
			String newToken = doSubstitutions(token, url);
			if (newToken != null) {
				tokenList.set(i, newToken);
				substituted = true;
			}
		}
		// add the url if not substituted already
		if (!substituted)
			tokenList.add(url);

		String[] command = new String[tokenList.size()];
		tokenList.toArray(command);
		return command;
	}
}
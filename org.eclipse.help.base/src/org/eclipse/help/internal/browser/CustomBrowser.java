/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.browser;

import java.util.*;

import org.eclipse.core.boot.*;
import org.eclipse.help.browser.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;

/**
 * 
 */
public class CustomBrowser implements IBrowser {
	public static final String CUSTOM_BROWSER_PATH_KEY = "custom_browser_path";

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#close()
	 */
	public void close() {
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#isCloseSupported()
	 */
	public boolean isCloseSupported() {
		return false;
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#displayURL(java.lang.String)
	 */
	public void displayURL(String url) throws Exception {
		String path =
			HelpBasePlugin.getDefault().getPluginPreferences().getString(
				CustomBrowser.CUSTOM_BROWSER_PATH_KEY);

		String[] command = prepareCommand(path, url);

		try {
			Process pr = Runtime.getRuntime().exec(command);
			Thread outConsumer = new StreamConsumer(pr.getInputStream());
			outConsumer.setName("Custom browser adapter output reader");
			outConsumer.start();
			Thread errConsumer = new StreamConsumer(pr.getErrorStream());
			errConsumer.setName("Custom browser adapter error reader");
			errConsumer.start();
		} catch (Exception e) {
			HelpBasePlugin.logError(
				Resources.getString("CustomBrowser.errorLaunching", url, path),
				e);
			throw new Exception(
				Resources.getString("CustomBrowser.errorLaunching", url, path));
		}
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#isSetLocationSupported()
	 */
	public boolean isSetLocationSupported() {
		return false;
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#isSetSizeSupported()
	 */
	public boolean isSetSizeSupported() {
		return false;
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#setLocation(int, int)
	 */
	public void setLocation(int x, int y) {
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#setSize(int, int)
	 */
	public void setSize(int width, int height) {
	}

	/**
	 * Creates the final command to launch.
	 * @param path
	 * @param url
	 * @return String[]
	 */
	private String[] prepareCommand(String path, String url) {
		ArrayList tokenList = new ArrayList();
		//Divide along quotation marks
		StringTokenizer qTokenizer =
			new StringTokenizer(path.trim(), "\"", true);
		boolean withinQuotation = false;
		String quotedString = "";
		while (qTokenizer.hasMoreTokens()) {
			String curToken = qTokenizer.nextToken();
			if (curToken.equals("\"")) {
				if (withinQuotation) {
					if (BootLoader
						.OS_WIN32
						.equalsIgnoreCase(BootLoader.getOS())) {
						// need to quote URLs on Windows
						tokenList.add("\"" + quotedString + "\"");
					} else {
						// qotes prevent launching on Unix 35673
						tokenList.add(quotedString);
					}
				} else {
					quotedString = "";
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
			if ("%1".equals(token)) {
				tokenList.set(i, url);
				substituted = true;
			} else if ("\"%1\"".equals(token)) {
				tokenList.set(i, "\"" + url + "\"");
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

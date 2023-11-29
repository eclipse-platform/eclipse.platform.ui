/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser.browsers;

import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.AbstractWebBrowser;
import org.eclipse.ui.internal.browser.Messages;
import org.eclipse.ui.internal.browser.Trace;
import org.eclipse.ui.internal.browser.WebBrowserUIPlugin;
public class DefaultBrowser extends AbstractWebBrowser {
	protected String location;
	protected String parameters;

	public DefaultBrowser(String id, String location, String parameters) {
		super(id);
		this.location = location;
		this.parameters = parameters;
	}

	@Override
	public void openURL(URL url2) throws PartInitException {
		String url = url2.toExternalForm();
		String path = location;

		String[] command = prepareCommand(path, url);
		Trace.trace(Trace.FINER, "Command: " + command); //$NON-NLS-1$

		try {
			Process pr = Runtime.getRuntime().exec(command);
			Thread outConsumer = new StreamConsumer(pr.getInputStream());
			outConsumer.setName("Custom browser adapter output reader"); //$NON-NLS-1$
			outConsumer.start();
			Thread errConsumer = new StreamConsumer(pr.getErrorStream());
			errConsumer.setName("Custom browser adapter error reader"); //$NON-NLS-1$
			errConsumer.start();
		} catch (Exception e) {
			WebBrowserUIPlugin.logError(
				"Launching URL \"" //$NON-NLS-1$
					+ url
					+ "\" using browser program \"" //$NON-NLS-1$
					+ path
					+ "\" has failed.  Specify another browser in help preferences.", //$NON-NLS-1$
					e);
			throw new PartInitException(NLS.bind(Messages.errorCouldNotLaunchExternalWebBrowser, path));
		}
	}

	/**
	 * Creates the final command to launch.
	 *
	 * @return String[]
	 */
	protected String[] prepareCommand(String path, String url) {
		ArrayList<String> tokenList = new ArrayList<>();
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
			String token = tokenList.get(i);
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

	/**
	 * Replaces any occurrences of <code>"%1"</code> or <code>%1</code> with
	 * the URL.
	 *
	 * @param token
	 *            The token in which the substitutions should be made; must not
	 *            be <code>null</code>.
	 * @return The substituted string, if a substitution is made;
	 *         <code>null</code> if no substitution is made.
	 */
	protected String doSubstitutions(String token, String url) {
		boolean substituted = false;
		StringBuilder newToken = new StringBuilder(token);
		String substitutionMarker = "%1"; //$NON-NLS-1$
		int index = newToken.indexOf(substitutionMarker);
		while (index != -1) {
			newToken.replace(index, index + substitutionMarker.length(), url);
			index = newToken.indexOf(substitutionMarker, index + url.length());
			substituted = true;
		}

		if (substituted) {
			return newToken.toString();
		}

		return null;
	}
}

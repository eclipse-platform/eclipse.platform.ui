/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser.macosx;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.ui.internal.browser.browsers.DefaultBrowser;

public class SafariBrowser extends DefaultBrowser {
	
	public SafariBrowser(String id, String location, String parameters) {
		super(id, location, parameters);
		this.location = location;
		this.parameters = parameters;
	}

	/**
	 * Creates the final command to launch.
	 * 
	 * @param path
	 * @param url
	 * @return String[]
	 */
	protected String[] prepareCommand(String path, String url) {
		if (url != null && url.toLowerCase().startsWith("file:")) { //$NON-NLS-1$
			url = url.substring(5);
		}
		
		ArrayList<String> tokenList = new ArrayList<String>();
		//Divide along quotation marks
		StringTokenizer qTokenizer = new StringTokenizer(path.trim(),
			"\"", true); //$NON-NLS-1$
		boolean withinQuotation = false;
		String quotedString = ""; //$NON-NLS-1$
		while (qTokenizer.hasMoreTokens()) {
			String curToken = qTokenizer.nextToken();
			if (curToken.equals("\"")) { //$NON-NLS-1$
				if (withinQuotation) {
					// quotes prevent launching on Unix 35673
					tokenList.add(quotedString);
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
}
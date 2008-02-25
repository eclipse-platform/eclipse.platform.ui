/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 
 package org.eclipse.help.ui.internal.preferences;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class TestConnectionUtility {

	//This class provides a utility for testing if a connection
	//can be made to a given URL
	
	public static boolean testConnection(String thisHost, String thisPort,
			String thisPath) {

		URL testURL;
		boolean validConnection = true;
		String urlConnection = "http://"; //$NON-NLS-1$

		// Build connection string
		if (thisPort.equals("80")) //$NON-NLS-1$
			urlConnection = urlConnection + thisHost + thisPath;
		else
			urlConnection = urlConnection + thisHost + ":" + thisPort //$NON-NLS-1$
					+ thisPath;

		// Test Connection. If exception thrown, invalid connection
		try {
			testURL = new URL(urlConnection);

			URLConnection myConnect = testURL.openConnection();
			//TODO: Add some time out for the connection test
			myConnect.connect();

		} catch (MalformedURLException e) {
			validConnection = false;
		} catch (IOException e) {
			validConnection = false;
		}
		return validConnection;
	}

}

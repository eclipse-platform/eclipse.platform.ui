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
package org.eclipse.help.internal.standalone;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This program is used to start or stop Eclipse Infocenter application. It
 * should be launched from command line.
 */
public class EclipseConnection {
	// help server host
	private String host;
	// help server port
	private String port;

	public EclipseConnection() {
	}

	public String getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	public void reset() {
		host = null;
		port = null;
	}

	public boolean isValid() {
		return (host != null && port != null);
	}

	public void connect(URL url) throws InterruptedException, Exception {
		try {
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			if (Options.isDebug()) {
				System.out.println("Connection  to control servlet created."); //$NON-NLS-1$
			}
			connection.connect();
			if (Options.isDebug()) {
				System.out.println("Connection  to control servlet connected."); //$NON-NLS-1$
			}
			int code = connection.getResponseCode();
			if (Options.isDebug()) {
				System.out
						.println("Response code from control servlet=" + code); //$NON-NLS-1$
			}
			connection.disconnect();
			return;
		} catch (IOException ioe) {
			if (Options.isDebug()) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * Obtains host and port from the file. Retries several times if file does
	 * not exists, and help might be starting up.
	 */
	public void renew() throws Exception {
		Properties p = new Properties();
		FileInputStream is = null;
		try {
			is = new FileInputStream(Options.getConnectionFile());
			p.load(is);
			is.close();
		} catch (IOException ioe) {
			// it is ok, eclipse might have just exited
			throw ioe;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ioe2) {
				}
			}
		}
		host = (String) p.get("host"); //$NON-NLS-1$
		port = (String) p.get("port"); //$NON-NLS-1$
		if (Options.isDebug()) {
			System.out.println("Help server host=" + host); //$NON-NLS-1$
		}
		if (Options.isDebug()) {
			System.out.println("Help server port=" + port); //$NON-NLS-1$
		}
	}

}

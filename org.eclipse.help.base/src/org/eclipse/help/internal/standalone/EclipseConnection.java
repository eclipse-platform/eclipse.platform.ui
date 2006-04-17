/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.standalone;

import java.io.*;
import java.net.*;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

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
			if (connection instanceof HttpsURLConnection) {
				HttpsURLConnection secureConnection = (HttpsURLConnection) connection;
				// The following allows the connection to
				// continue even if the default rules for
				// URL hostname verification fail.
				secureConnection.setHostnameVerifier(new HostnameVerifier() {
					public boolean verify(String urlHostName, javax.net.ssl.SSLSession session) {
						if (Options.isDebug()) {
							System.out.println("Warning: URL Host: " //$NON-NLS-1$
									+ urlHostName + " vs. " //$NON-NLS-1$
									+ session.getPeerHost());							
						}
			            return true;
					}
				});
			}
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
			if (code == HttpURLConnection.HTTP_MOVED_TEMP) {
				// Redirect from server.
				String redirectLocation = connection.getHeaderField("location"); //$NON-NLS-1$
				URL redirectURL = new URL(redirectLocation);
				if (url.equals(redirectURL)) {
					if (Options.isDebug()) {
						System.out.println("Redirecting to the same URL! " //$NON-NLS-1$
								+ redirectLocation);
					}
					return;
				}
				if (Options.isDebug()) {
					System.out.println("Follows redirect to " + redirectLocation); //$NON_NLS-1$ //$NON-NLS-1$
				}
				connect(redirectURL);
			}
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

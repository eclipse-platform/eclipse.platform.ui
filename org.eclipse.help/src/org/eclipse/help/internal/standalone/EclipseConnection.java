/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.standalone;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.help.internal.standalone.*;

/**
 * This program is used to start or stop Eclipse
 * Infocenter application.
 * It should be launched from command line.
 */
public class EclipseConnection {
	// timout for .hostport file to apper since starting eclipse [ms]
	// 0 if no waiting for file should occur
	int startupTimeout;
	// number of retries to connectect to webapp
	int connectionRetries;
	// time between retries to connectect to webapp [ms]
	int connectionRetryInterval;
	// help server host
	private String host;
	// help server port
	private String port;

	EclipseConnection() {
		this(0,0,5*1000);
	}

	EclipseConnection(
		int startupTimeout,
		int connectionRetries,
		int connectionRetryInterval) {
		
		this.startupTimeout = startupTimeout;
		this.connectionRetries = connectionRetries;
		this.connectionRetryInterval = connectionRetryInterval;
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
	

	public void renew() {
		obtainHostPort();
	}

	public boolean connect(URL url) {
		if (url == null) 
			return false;
		
		for (int i = 0; i <= connectionRetries; i++) {
			try {
				HttpURLConnection connection =
					(HttpURLConnection) url.openConnection();
				if (Options.isDebug()) {
					System.out.println(
						"Connection  to control servlet created.");
				}
				connection.connect();
				if (Options.isDebug()) {
					System.out.println(
						"Connection  to control servlet connected.");
				}
				int code = connection.getResponseCode();
				if (Options.isDebug()) {
					System.out.println(
						"Response code from control servlet=" + code);
				}
				connection.disconnect();
				return true;
			} catch (IOException ioe) {
				if (Options.isDebug()) {
					ioe.printStackTrace();
				}
			}
			try {
				Thread.currentThread().sleep(connectionRetryInterval);
			} catch (InterruptedException ie) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Obtains host and port from the file.
	 * Retries several times if file does not exists,
	 * and help might be starting up.
	 */
	private void obtainHostPort() {
		long time1 = System.currentTimeMillis();
		while (!Options.getConnectionFile().exists()) {
			// wait for .hostport file to appear
			if (Options.isDebug()) {
				System.out.println(
					"File " + Options.getConnectionFile() + " does not exist, at the moment.");
			}
			// timeout
			if (System.currentTimeMillis() - time1 >= startupTimeout) {
				if (Options.isDebug()) {
					System.out.println(
						"Timeout waiting for file " + Options.getConnectionFile());
				}
				return;
			}
			// wait more
			try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException ie) {
				return;
			}
		}
		Properties p = new Properties();
		FileInputStream is = null;
		try {
			is = new FileInputStream(Options.getConnectionFile());
			p.load(is);
			is.close();
		} catch (IOException ioe) {
			// it ok, eclipse might have just exited
			ioe.printStackTrace();
			return;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ioe2) {
				}
			}
		}
		host = (String) p.get("host");
		port = (String) p.get("port");
		if (Options.isDebug()) {
			System.out.println("Help server host=" + host);
		}
		if (Options.isDebug()) {
			System.out.println("Help server port=" + port);
		}
	}

}

/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.tests.ftp;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.extensions.TestSetup;
import junit.framework.Test;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.internal.ftp.FTPException;
import org.eclipse.team.internal.ftp.FTPServerLocation;
import org.eclipse.team.internal.ftp.client.FTPClient;
import org.eclipse.team.internal.ftp.client.IFTPClientListener;

/**
 * Provides the FTP tests with a host to ftp to.
 */
public class FTPTestSetup extends TestSetup {

	public static final String FTP_URL;
	public static final boolean SCRUB_URL;
	
	private static final IProgressMonitor DEFAULT_PROGRESS_MONITOR = new NullProgressMonitor();
	
	public static URL ftpURL;
	
	// Static initializer for constants
	static {
		loadProperties();
		FTP_URL = System.getProperty("eclipse.ftp.url");
		SCRUB_URL = Boolean.valueOf(System.getProperty("eclipse.ftp.init", "false")).booleanValue();
	}
	
	public static void loadProperties() {
		String propertiesFile = System.getProperty("eclipse.ftp.properties");
		if (propertiesFile == null) return;
		File file = new File(propertiesFile);
		if (file.isDirectory()) file = new File(file, "ftp.properties");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			try {
				for (String line; (line = reader.readLine()) != null; ) {						
					int sep = line.indexOf("=");
					String property = line.substring(0, sep).trim();
					String value = line.substring(sep + 1).trim();
					System.setProperty("eclipse.ftp." + property, value);
				}
			} finally {
				reader.close();
			}
		} catch (Exception e) {
			System.err.println("Could not read ftp properties file: " + file.getAbsolutePath());
		}
	}
	
	/**
	 * Constructor for FTPTestSetup.
	 * @param test
	 */
	public FTPTestSetup(Test test) {
		super(test);
	}

	public void setUp()  throws MalformedURLException, FTPException {
		if (ftpURL == null)
			ftpURL = setupURL(FTP_URL);
	}

	protected void scrubURL(URL url) {
	}
	
	protected URL setupURL(String urlString) throws MalformedURLException, FTPException {

		// Give some info about which repository the tests are running against
		System.out.println("Connecting to: " + urlString);
		
		// Validate that we can connect, also creates and caches the repository location. This
		// is important for the UI tests.
		URL url = new URL(urlString);
		FTPServerLocation location = FTPServerLocation.fromURL(url, false);
		FTPClient client = openFTPConnection(url);
		client.close(DEFAULT_PROGRESS_MONITOR);
		
		// Initialize the repo if requested (requires rsh access)
		if( SCRUB_URL ) {
			scrubURL(url);
		}
		
		return url;
	}
	
	public void tearDown() {
		// Nothing to do here
	}
	
	public static FTPClient openFTPConnection(URL url) throws FTPException {
		FTPServerLocation location = FTPServerLocation.fromURL(url, false);
		FTPClient client = new FTPClient(location, null, getListener());
		client.open(DEFAULT_PROGRESS_MONITOR);
		return client;
	}
	
	public static IFTPClientListener getListener() {
		return new IFTPClientListener() {
			public void responseReceived(int responseCode, String responseText) {
				System.out.println(responseText);
			}
			public void requestSent(String command, String argument) {
				if (argument != null) {
					System.out.println(command + " " + argument);
				} else {
					System.out.println(command);
				}
			}
		};
	}
	
}

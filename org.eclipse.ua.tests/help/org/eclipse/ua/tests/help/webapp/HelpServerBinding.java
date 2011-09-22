/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webapp;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.server.WebappManager;

/**
 * Test to see if the help server binds to host 127.0.0.1 in Workbench mode
 */

public class HelpServerBinding extends TestCase {

	private int previousMode;
	// Tests to access the server using it's IP need to be disabled
	// for testing in the build because a firewall can block this access
	// To enable these tests for local testing set testUsingIP to true.
	private final boolean testUsingIP = false;

	protected void setUp() throws Exception {
		previousMode = BaseHelpSystem.getMode();
	}
	
	protected void tearDown() throws Exception {
		BaseHelpSystem.setMode(previousMode);
	}
	
	private String getHostIP() throws UnknownHostException {	
	    InetAddress host = InetAddress.getLocalHost();
	    byte[] ipAddr = host.getAddress(); 
        String result = "" + ipAddr[0];
        for (int i = 1; i < ipAddr.length; i++) {
        	result += '.';
        	result += ipAddr[i];
        }
        return result;
	}

	public void testInfocenterBinding() throws Exception {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		WebappManager.stop("help");
		WebappManager.start("help");
		assertTrue(canAccessServer("127.0.0.1"));
		if (testUsingIP) {
			assertTrue(canAccessServer(getHostIP()));
		}
	}	

	public void testWorkbenchBinding() throws Exception {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_WORKBENCH);
		WebappManager.stop("help");
		WebappManager.start("help");
		assertTrue(canAccessServer("127.0.0.1"));
		if (testUsingIP) {
		    assertFalse(canAccessServer(getHostIP()));
		}
	}	
	
	public void testStandaloneBinding() throws Exception {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_STANDALONE);
		WebappManager.stop("help");
		WebappManager.start("help");
		assertTrue(canAccessServer("127.0.0.1"));
		if (testUsingIP) {
		    assertTrue(canAccessServer(getHostIP()));
		}
	}	
	
	private boolean canAccessServer(String host) throws Exception {
		InputStream input;
		try {
			int port = WebappManager.getPort();
			URL url = new URL("http", host, port, "/help/index.jsp");	
			URLConnection connection = url.openConnection();
			setTimeout(connection, 5000);
			input = connection.getInputStream();
			int firstbyte = input.read();
			input.close();
			return firstbyte > 0;
		} catch (Exception e) {
			return false;            
		}
	}
	
	private static void setTimeout(URLConnection conn, int milliseconds) {
		conn.setConnectTimeout(milliseconds);
		conn.setReadTimeout(milliseconds);
	}
		
}

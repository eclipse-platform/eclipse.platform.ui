/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.performance;

import java.io.InputStream;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.help.internal.server.WebappManager;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCase;

/**
 * Test the performance of the help server without launching the Help UI
 */

public class HelpServerTest extends PerformanceTestCase {
	
	static long uniqueParam = System.currentTimeMillis();
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(HelpServerTest.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		stopServer();
	}

	private void startServer() throws Exception {
		WebappManager.start("help");
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/index.jsp");
		InputStream input = url.openStream();
		int firstbyte = input.read();
		assertTrue(firstbyte > 0);
		input.close();
	}
	
	private void stopServer() throws CoreException {
		WebappManager.stop("help");
	}
	
	private void readLoadServlet() throws Exception {
		int port = WebappManager.getPort();
		// Use a unique parameter to defeat caching
		++uniqueParam;
		URL url = new URL("http", "localhost", port, "/help/loadtest?value=" + uniqueParam);
		InputStream input = url.openStream();
		int nextChar;
		long value = 0;
		// The loadtest servlet  returns the uniqueParam in an opening comment such as <!--1234-->
		// Read this to verify that we are not getting a cached page
		boolean inFirstComment = true;
        do {
		    nextChar = input.read();
		    if (inFirstComment) {
		    	if (nextChar == '>') {
		    		inFirstComment = false;
		    	} else if (Character.isDigit((char) nextChar)) {
		    		value = value * 10 + (nextChar - '0');
		    	}
		    }
        } while (nextChar != '$');
        assertEquals(uniqueParam, value);
        input.close();
	}
		
	public void testServletRead() throws Exception {
		tagAsSummary("Servlet Read", Dimension.ELAPSED_PROCESS);
		startServer();
		// run the tests
		for (int i=0; i < 100; ++i) {
			boolean warmup = i < 2;
			if (!warmup) {
			    startMeasuring();
			} 
			
			for (int j = 0; j <= 10; j++) {
                 readLoadServlet();
			}

			if (!warmup) {
			    stopMeasuring();
		    }
		}
		
		commitMeasurements();
		assertPerformance();
	}

	public void testStartServer() throws Exception {
		tagAsSummary("Start Server", Dimension.ELAPSED_PROCESS);
		
		// run the tests
		for (int i=0; i < 25; ++i) {
			boolean warmup = i < 2;
			stopServer();
			if (!warmup) {
			    startMeasuring();
			}
	
			startServer();
			
			if (!warmup) {
			    stopMeasuring();
		    }
		}
		
		commitMeasurements();
		assertPerformance();
	}
		
}

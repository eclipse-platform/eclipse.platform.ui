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

import org.eclipse.help.internal.server.WebappManager;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCase;

public class StartServer extends PerformanceTestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(StartServer.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		WebappManager.stop("help");
	}
	
	public void startServer() throws Exception {
		WebappManager.start("help");
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/index.jsp");
		InputStream input = url.openStream();
		int firstbyte = input.read();
		assertTrue(firstbyte > 0);
	}



	public void testStartServer() throws Exception {
		tagAsSummary("Start Server", Dimension.ELAPSED_PROCESS);
		
		// run the tests
		for (int i=0; i < 50; ++i) {
			boolean warmup = i < 2;
			WebappManager.stop("help");
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

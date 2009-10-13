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

package org.eclipse.ua.tests.help.remote;

import org.eclipse.core.runtime.CoreException;

public class TestServerManager {
	
	private static JettyTestServer server;
	private static boolean serverRunning = false;
	
	private static JettyTestServer getHelpServer() {
		if (server == null) {
			server = new JettyTestServer();
		}
		return server;
	}
	
	public static void start(String webappName) throws Exception {
		if (!serverRunning) {
			getHelpServer().start(webappName);
			serverRunning = true;
		}
	}

	public static void stop(String webappName) throws CoreException {
		if (serverRunning) {
		    getHelpServer().stop(webappName);
		    serverRunning = false;
		}
	}
	
	public static int getPort() {
		return getHelpServer().getPort();
	}

	public static String getHost() {
        return getHelpServer().getHost();
	}

}

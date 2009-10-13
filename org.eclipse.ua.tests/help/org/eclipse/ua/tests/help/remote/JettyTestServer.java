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

import org.eclipse.help.internal.server.JettyHelpServer;

public class JettyTestServer extends JettyHelpServer {
	
	protected String getOtherInfo() {
		return //super.getOtherInfo();
		    "org.eclipse.ua.tests";
	}
	
	protected int getPortParameter() {
		return AUTO_SELECT_JETTY_PORT;
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.appserver;

import org.eclipse.osgi.util.NLS;

public final class AppserverResources extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.help.internal.appserver.AppserverResources";//$NON-NLS-1$

	private AppserverResources() {
		// Do not instantiate
	}

	public static String LocalConnectionTest_cannotGetLocalhostName;
	public static String Appserver_cannotFindPlugin;
	public static String Appserver_cannotFindPath;
	public static String Appserver_cannotResolvePath;
	public static String Appserver_engineRemove;
	public static String Appserver_embeddedStop;
	public static String Appserver_addingWebapp;
	public static String Appserver_start;

	static {
		NLS.initializeMessages(BUNDLE_NAME, AppserverResources.class);
	}
}
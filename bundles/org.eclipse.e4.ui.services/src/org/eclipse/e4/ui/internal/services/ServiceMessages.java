/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.e4.ui.internal.services;

import org.eclipse.osgi.util.NLS;

public class ServiceMessages extends NLS {
	
	private static final String BUNDLE_NAME = "org.eclipse.e4.ui.internal.services.serviceMessages"; //$NON-NLS-1$

	// Event broker
	public static String NO_EVENT_ADMIN;
	public static String NO_BUNDLE_CONTEXT;
	
	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, ServiceMessages.class);
	}
}
/**********************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.runtime.auth;

import org.eclipse.osgi.util.NLS;

// Runtime plugin message catalog
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.runtime.auth.messages"; //$NON-NLS-1$

	public static String meta_authFormatChanged;
	public static String meta_unableToReadAuthorization;
	public static String meta_unableToWriteAuthorization;

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.osgi.util.NLS;

public class ExtensionEventHandlerMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.ExtensionEventHandler";//$NON-NLS-1$
	//
	// Copyright (c) 2003, 2004 IBM Corporation and others.
	// All rights reserved. This program and the accompanying materials
	// are made available under the terms of the Eclipse Public License v1.0
	// which accompanies this distribution, and is available at
	// http://www.eclipse.org/legal/epl-v10.html
	//
	// Contributors:
	//     IBM Corporation - initial API and implementation
	//
	public static String ExtensionEventHandler_new_action_set;
	public static String ExtensionEventHandler_following_changes;
	public static String ExtensionEventHandler_change_format;
	public static String ExtensionEventHandler_need_to_reset;
	public static String ExtensionEventHandler_reset_perspective;


	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ExtensionEventHandlerMessages.class);
	}
}
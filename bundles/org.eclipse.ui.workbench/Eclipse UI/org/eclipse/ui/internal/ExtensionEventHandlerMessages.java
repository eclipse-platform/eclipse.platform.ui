/**********************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.osgi.util.NLS;

public class ExtensionEventHandlerMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.ExtensionEventHandler";//$NON-NLS-1$

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
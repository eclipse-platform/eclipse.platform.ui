/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.keys;

import org.eclipse.osgi.util.NLS;

/**
 * The KeyAssistMessages class is the class that manages the messages used in
 * the KeyAssistDialog.
 *
 */
public class KeyAssistMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.keys.KeyAssistDialog";//$NON-NLS-1$

	public static String NoMatches_Message;
	public static String openPreferencePage;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, KeyAssistMessages.class);
	}
}

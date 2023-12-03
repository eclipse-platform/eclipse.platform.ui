/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.contentoutline;

import org.eclipse.osgi.util.NLS;

/**
 * ContentOutlineMessages is the message class for the messages used in the content outline.
 */
public class ContentOutlineMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.views.contentoutline.messages";//$NON-NLS-1$

	// ==============================================================================
	// Outline View
	// ==============================================================================
	/** */
	public static String ContentOutline_noOutline;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ContentOutlineMessages.class);
	}
}
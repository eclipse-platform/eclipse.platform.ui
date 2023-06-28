/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.ui.contentassist;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 *
 * @since 3.0
 */
final class ContentAssistMessages extends NLS {

	private static final String BUNDLE_NAME= ContentAssistMessages.class.getName();

	private ContentAssistMessages() {
		// Do not instantiate
	}

	public static String ContentAssistHandler_contentAssistAvailable;
	public static String ContentAssistHandler_contentAssistAvailableWithKeyBinding;

	static {
		NLS.initializeMessages(BUNDLE_NAME, ContentAssistMessages.class);
	}
}
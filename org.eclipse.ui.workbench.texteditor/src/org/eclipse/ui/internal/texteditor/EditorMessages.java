/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
final class EditorMessages extends NLS {

	private static final String BUNDLE_NAME= EditorMessages.class.getName();

	private EditorMessages() {
		// Do not instantiate
	}

	public static String Editor_error_gotoLastEditPosition_title;
	public static String Editor_error_gotoLastEditPosition_message;

	static {
		NLS.initializeMessages(BUNDLE_NAME, EditorMessages.class);
	}
}

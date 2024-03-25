/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
package org.eclipse.ui.internal.texteditor;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
final class RulerMessages extends NLS {

	private static final String BUNDLE_NAME= RulerMessages.class.getName();

	private RulerMessages() {
		// Do not instantiate
	}

	public static String AbstractDecoratedTextEditor_revision_quickdiff_switch_title;
	public static String AbstractDecoratedTextEditor_revision_quickdiff_switch_message;
	public static String AbstractDecoratedTextEditor_revision_quickdiff_switch_rememberquestion;

	static {
		NLS.initializeMessages(BUNDLE_NAME, RulerMessages.class);
	}

}

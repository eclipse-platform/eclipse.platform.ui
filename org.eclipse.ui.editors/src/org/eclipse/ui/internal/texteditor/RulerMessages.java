/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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

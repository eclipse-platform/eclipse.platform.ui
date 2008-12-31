/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.spelling;

import org.eclipse.osgi.util.NLS;

/**
 * Spelling messages. Helper class to get NLSed messages.
 *
 * @since 3.1
 */
final class SpellingMessages extends NLS {

	private static final String BUNDLE_NAME= SpellingMessages.class.getName();

	private SpellingMessages() {
		// Do not instantiate
	}


	public static String EmptySpellingPreferenceBlock_emptyCaption;
	public static String NoCompletionsProposal_displayString;


	static {
		NLS.initializeMessages(BUNDLE_NAME, SpellingMessages.class);
	}
}

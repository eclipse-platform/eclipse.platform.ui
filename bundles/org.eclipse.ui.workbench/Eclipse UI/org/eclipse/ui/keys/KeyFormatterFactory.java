/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.keys;

import org.eclipse.ui.internal.keys.CompactKeyFormatter;
import org.eclipse.ui.internal.keys.EmacsKeyFormatter;
import org.eclipse.ui.internal.keys.FormalKeyFormatter;

/**
 * A cache for formatters. It keeps a bunch of formatters around for use within
 * the application.
 * 
 * @since 3.0
 */
public final class KeyFormatterFactory {
	private static final IKeyFormatter COMPACT_KEY_FORMATTER =
		new CompactKeyFormatter();
	private static final IKeyFormatter FORMAL_KEY_FORMATTER =
		new FormalKeyFormatter();
	private static final IKeyFormatter EMACS_KEY_FORMATTER =
		new EmacsKeyFormatter();
	private static IKeyFormatter defaultKeyFormatter = FORMAL_KEY_FORMATTER;

	/**
	 * Provides an instance of <code>CompactKeyFormatter</code>.
	 * 
	 * @return The compact formatter; never <code>null</code>.
	 */
	public static final IKeyFormatter getCompactKeyFormatter() {
		return COMPACT_KEY_FORMATTER;
	}

	/**
	 * An accessor for the current default key formatter.
	 * 
	 * @return The default formatter; never <code>null</code>.
	 */
	public static IKeyFormatter getDefault() {
		return defaultKeyFormatter;
	}

	/**
	 * Provides an instance of <code>XemacsKeyFormatter</code>.
	 * 
	 * @return The Xemacs formatter; never <code>null</code>.
	 */
	public static IKeyFormatter getEmacsKeyFormatter() {
		return EMACS_KEY_FORMATTER;
	}

	/**
	 * Provides an instance of <code>FormalKeyFormatter</code>.
	 * 
	 * @return The formal formatter; never <code>null</code>.
	 */
	public static IKeyFormatter getFormalKeyFormatter() {
		return FORMAL_KEY_FORMATTER;
	}

	/**
	 * Sets the default key formatter.
	 * 
	 * @param formatter
	 *            the default key formatter. Must not be <code>null</code>.
	 */
	public static void setDefault(IKeyFormatter defaultKeyFormatter) {
		if (defaultKeyFormatter == null)
			throw new NullPointerException();

		KeyFormatterFactory.defaultKeyFormatter = defaultKeyFormatter;
	}

	private KeyFormatterFactory() {
	}
}

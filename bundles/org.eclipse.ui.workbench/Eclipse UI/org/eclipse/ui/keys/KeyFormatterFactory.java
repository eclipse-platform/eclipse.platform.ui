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

	/**
	 * A compact formatter instance.
	 */
	private static final IKeyFormatter COMPACT_FORMATTER =
		new CompactKeyFormatter();

	/**
	 * A formal formatter instance.
	 */
	private static final IKeyFormatter FORMAL_FORMATTER =
		new FormalKeyFormatter();

	/**
	 * An Emacs natural formatter instance.
	 */
	private static final IKeyFormatter EMACS_FORMATTER =
		new EmacsKeyFormatter();

	/**
	 * The default formatter instance.
	 */
	private static IKeyFormatter defaultFormatter = FORMAL_FORMATTER;

	/**
	 * Provides an instance of <code>CompactKeyFormatter</code>.
	 * 
	 * @return The compact formatter; never <code>null</code>.
	 */
	public static final IKeyFormatter getCompactKeyFormatter() {
		return COMPACT_FORMATTER;
	}

	/**
	 * An accessor for the current default key formatter.
	 * 
	 * @return The default formatter; never <code>null</code>.
	 */
	public static IKeyFormatter getDefault() {
		return defaultFormatter;
	}

	/**
	 * Provides an instance of <code>XemacsKeyFormatter</code>.
	 * 
	 * @return The Xemacs formatter; never <code>null</code>.
	 */
	public static IKeyFormatter getEmacsKeyFormatter() {
		return EMACS_FORMATTER;
	}

	/**
	 * Provides an instance of <code>FormalKeyFormatter</code>.
	 * 
	 * @return The formal formatter; never <code>null</code>.
	 */
	public static IKeyFormatter getFormalKeyFormatter() {
		return FORMAL_FORMATTER;
	}

	/**
	 * Sets the default key formatter. If the <code>formatter</code> provided
	 * is <code>null</code>, then this changes the default to the formal
	 * formatter.
	 * 
	 * @param formatter
	 *            The formatter to use; <code>null</code> means use the
	 *            formal formatter.
	 */
	public static void setDefault(IKeyFormatter formatter) {
		if (formatter == null) {
			defaultFormatter = FORMAL_FORMATTER;
		} else {
			defaultFormatter = formatter;
		}
	}

	private KeyFormatterFactory() {
	}
}

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
import org.eclipse.ui.internal.keys.NativeKeyFormatter;

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
	 * A native formatter instance.
	 */
	private static final IKeyFormatter NATIVE_FORMATTER =
		new NativeKeyFormatter();

	/**
	 * An Emacs natural formatter instance.
	 */
	private static final IKeyFormatter EMACS_FORMATTER =
		new EmacsKeyFormatter();

	/**
	 * The default formatter instance.
	 */
	private static IKeyFormatter defaultFormatter = NATIVE_FORMATTER;

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
	 * Provides an instance of <code>NativeKeyFormatter</code>.
	 * 
	 * @return The native formatter; never <code>null</code>.
	 */
	public static IKeyFormatter getNativeKeyFormatter() {
		return NATIVE_FORMATTER;
	}

	/**
	 * Sets the default key formatter. If the <code>formatter</code> provided
	 * is <code>null</code>, then this changes the default to the native
	 * formatter.
	 * 
	 * @param formatter
	 *            The formatter to use; <code>null</code> means use the
	 *            native formatter.
	 */
	public static void setDefault(IKeyFormatter formatter) {
		if (formatter == null) {
			defaultFormatter = NATIVE_FORMATTER;
		} else {
			defaultFormatter = formatter;
		}
	}

	/**
	 * This class should never be instantied.
	 */
	private KeyFormatterFactory() {
		// This class should not be instantiated.
	}
}

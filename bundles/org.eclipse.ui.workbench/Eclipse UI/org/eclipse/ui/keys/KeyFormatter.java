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

/**
 * Any formatter capable of taking key sequence or a key stroke and converting
 * it into a string. These formatters are used to produce the strings that the
 * user sees in the keys preference page and the menus, as well as the strings
 * that are used for persistent storage.
 * 
 * @since 3.0
 */
public interface KeyFormatter {

	/**
	 * The delimiter between multiple keys in a single key strokes -- expressed
	 * in the formal key stroke grammar. This is not to be displayed to the
	 * user. It is only intended as an internal representation.
	 */
	public final static String KEY_DELIMITER = Character.toString('\u002B');
	/**
	 * The key for the delimiter between keys. This is used in the
	 * internationalization bundles.
	 */
	final static String KEY_DELIMITER_KEY = "KEY_DELIMITER"; //$NON-NLS-1$

	/**
	 * The delimiter between multiple key strokes in a single key sequence --
	 * expressed in the formal key stroke grammar. This is not to be displayed
	 * to the user. It is only intended as an internal representation.
	 */
	public final static String KEY_STROKE_DELIMITER = Character.toString('\u0020'); //$NON-NLS-1$

	/**
	 * The key for the delimiter between key strokes. This is used in the
	 * internationalization bundles.
	 */
	final static String KEY_STROKE_DELIMITER_KEY = "KEY_STROKE_DELIMITER"; //$NON-NLS-1$

	/**
	 * Format the given key sequence into a string. The manner of the
	 * conversion is dependent on the formatter. It is required that unequal
	 * key seqeunces return unequal strings.
	 * 
	 * @param keySequence
	 *            The key sequence to convert; must not be <code>null</code>.
	 * @return A string representation of the key sequence; must not be <code>null</code>.
	 */
	public String format(KeySequence keySequence);

	/**
	 * Format the given key strokes into a string. The manner of the conversion
	 * is dependent on the formatter. It is required that unequal key strokes
	 * return unequal strings.
	 * 
	 * @param keyStroke
	 *            The key stroke to convert; must not be <Code>null</code>.
	 * @return A string representation of the key stroke; must not be <code>
	 *         null</code>
	 */
	public String formatKeyStroke(KeyStroke keyStroke);
}

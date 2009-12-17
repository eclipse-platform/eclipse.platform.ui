/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.keys;

/**
 * Any formatter capable of taking key sequence or a key stroke and converting
 * it into a string. These formatters are used to produce the strings that the
 * user sees in the keys preference page and the menus, as well as the strings
 * that are used for persistent storage.
 * 
 * @deprecated Please use org.eclipse.jface.bindings.keys.IKeyFormatter
 * @since 3.0
 */
public interface IKeyFormatter {

    /**
     * Formats an individual key into a human readable format. This uses an
     * internationalization resource bundle to look up the key. This does not
     * do any platform-specific formatting (e.g., Carbon's command character).
     * 
     * @param key
     *            The key to format; must not be <code>null</code>.
     * @return The key formatted as a string; should not be <code>null</code>.
     */
    String format(Key key);

    /**
     * Format the given key sequence into a string. The manner of the
     * conversion is dependent on the formatter. It is required that unequal
     * key seqeunces return unequal strings.
     * 
     * @param keySequence
     *            The key sequence to convert; must not be <code>null</code>.
     * @return A string representation of the key sequence; must not be <code>null</code>.
     */
    String format(KeySequence keySequence);

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
    String format(KeyStroke keyStroke);
}

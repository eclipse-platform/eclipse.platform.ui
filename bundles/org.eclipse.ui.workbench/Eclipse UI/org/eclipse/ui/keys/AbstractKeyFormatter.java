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

import java.util.Comparator;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.ui.internal.util.Util;

/**
 * An abstract implementation of a key formatter that provides a lot of common
 * key formatting functionality. It is recommended that those people
 * implementing their own key formatters subclass from here, rather than
 * implementing <code>KeyFormatter</code> directly.
 * 
 * @since 3.0
 */
public abstract class AbstractKeyFormatter implements KeyFormatter {

	/**
	 * The bundle in which to look up the internationalized text for all of the
	 * individual keys in the system. This is the platform-agnostic version of
	 * the internationalized strings. Some platforms (namely Carbon) provide
	 * special Unicode characters and glyphs for some keys.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(AbstractKeyFormatter.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.KeyFormatter#format(org.eclipse.ui.keys.KeySequence)
	 */
	public String format(KeySequence keySequence) {
		StringBuffer stringBuffer = new StringBuffer();

		Iterator keyStrokeItr = keySequence.getKeyStrokes().iterator();
		while (keyStrokeItr.hasNext()) {
			stringBuffer.append(formatKeyStroke((KeyStroke) keyStrokeItr.next()));

			if (keyStrokeItr.hasNext()) {
				stringBuffer.append(getKeyStrokeDelimiter());
			}
		}

		return stringBuffer.toString();
	}

	/**
	 * Formats an individual key into a human readable format. This uses an
	 * internationalization resource bundle to look up the key. This does not
	 * do any platform-specific formatting (e.g., Carbon's command character).
	 * 
	 * @param key
	 *            The key to format; must not be <code>null</code>.
	 * @return The key formatted as a string; should not be <code>null</code>.
	 */
	protected String formatKey(Key key) {
		String name = key.name;
		return Util.translateString(RESOURCE_BUNDLE, name, name, false, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.KeyFormatter#formatKeyStroke(org.eclipse.ui.keys.KeyStroke)
	 */
	public String formatKeyStroke(KeyStroke keyStroke) {
		String keyDelimiter = getKeyDelimiter();

		// Format the modifier keys, in sorted order.
		SortedSet modifierKeys = new TreeSet(getModifierKeyComparator());
		modifierKeys.addAll(keyStroke.getModifierKeys());
		StringBuffer stringBuffer = new StringBuffer();
		Iterator modifierKeyItr = modifierKeys.iterator();
		while (modifierKeyItr.hasNext()) {
			stringBuffer.append(formatKey((ModifierKey) modifierKeyItr.next()));
			stringBuffer.append(keyDelimiter);
		}

		// Format the natural key, if any.
		NaturalKey naturalKey = keyStroke.getNaturalKey();
		if (naturalKey != null) {
			stringBuffer.append(formatKey(naturalKey));
		}

		return stringBuffer.toString();

	}

	/**
	 * An accessor for the delimiter you wish to use between keys. This is used
	 * by the default format implementations to determine the key delimiter.
	 * 
	 * @return The delimiter to use between keys; should not be <code>null</code>.
	 */
	protected abstract String getKeyDelimiter();

	/**
	 * An accessor for the delimiter you wish to use between key strokes. This
	 * used by the default format implementations to determine the key stroke
	 * delimiter.
	 * 
	 * @return The delimiter to use between key strokes; should not be <code>null</code>.
	 */
	protected abstract String getKeyStrokeDelimiter();

	/**
	 * An accessor for the comparator to use for sorting modifier keys. This is
	 * used by the default format implementations to sort the modifier keys
	 * before formatting them into a string.
	 * 
	 * @return The comparator to use to sort modifier keys; must not be <code>null</code>.
	 */
	protected abstract Comparator getModifierKeyComparator();

}

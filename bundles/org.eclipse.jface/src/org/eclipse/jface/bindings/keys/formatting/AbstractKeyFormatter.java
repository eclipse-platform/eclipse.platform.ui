/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.bindings.keys.formatting;

import java.util.ResourceBundle;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * An abstract implementation of a key formatter that provides a lot of common
 * key formatting functionality. It is recommended that those people
 * implementing their own key formatters subclass from here, rather than
 * implementing <code>KeyFormatter</code> directly.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 */
public abstract class AbstractKeyFormatter implements IKeyFormatter {

	/**
	 * The key for the delimiter between keys. This is used in the
	 * internationalization bundles.
	 */
	protected static final String KEY_DELIMITER_KEY = "KEY_DELIMITER"; //$NON-NLS-1$

	/**
	 * The key for the delimiter between key strokes. This is used in the
	 * internationalization bundles.
	 */
	protected static final String KEY_STROKE_DELIMITER_KEY = "KEY_STROKE_DELIMITER"; //$NON-NLS-1$

	/**
	 * An empty integer array that can be used in
	 * <code>sortModifierKeys(int)</code>.
	 */
	protected static final int[] NO_MODIFIER_KEYS = new int[0];

	/**
	 * The bundle in which to look up the internationalized text for all of the
	 * individual keys in the system. This is the platform-agnostic version of
	 * the internationalized strings. Some platforms (namely Carbon) provide
	 * special Unicode characters and glyphs for some keys.
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(AbstractKeyFormatter.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.KeyFormatter#format(org.eclipse.ui.keys.KeySequence)
	 */
	public String format(final int key) {
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		final String name = lookup.formalNameLookup(key);
		return Util.translateString(RESOURCE_BUNDLE, name, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.KeyFormatter#format(org.eclipse.ui.keys.KeySequence)
	 */
	public String format(KeySequence keySequence) {
		StringBuffer stringBuffer = new StringBuffer();

		final KeyStroke[] keyStrokes = keySequence.getKeyStrokes();
		final int keyStrokesLength = keyStrokes.length;
		for (int i = 0; i < keyStrokesLength; i++) {
			stringBuffer.append(format(keyStrokes[i]));

			if (i + 1 < keyStrokesLength) {
				stringBuffer.append(getKeyStrokeDelimiter());
			}
		}

		return stringBuffer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.KeyFormatter#formatKeyStroke(org.eclipse.ui.keys.KeyStroke)
	 */
	public String format(final KeyStroke keyStroke) {
		final String keyDelimiter = getKeyDelimiter();

		// Format the modifier keys, in sorted order.
		final int modifierKeys = keyStroke.getModifierKeys();
		final int[] sortedModifierKeys = sortModifierKeys(modifierKeys);
		final StringBuffer stringBuffer = new StringBuffer();
		if (sortedModifierKeys != null) {
			for (int i = 0; i < sortedModifierKeys.length; i++) {
				final int modifierKey = sortedModifierKeys[i];
				if (modifierKey != KeyStroke.NO_KEY) {
					stringBuffer.append(format(modifierKey));
					stringBuffer.append(keyDelimiter);
				}
			}
		}

		// Format the natural key, if any.
		final int naturalKey = keyStroke.getNaturalKey();
		if (naturalKey != 0) {
			stringBuffer.append(format(naturalKey));
		}

		return stringBuffer.toString();

	}

	/**
	 * An accessor for the delimiter you wish to use between keys. This is used
	 * by the default format implementations to determine the key delimiter.
	 * 
	 * @return The delimiter to use between keys; should not be
	 *         <code>null</code>.
	 */
	protected abstract String getKeyDelimiter();

	/**
	 * An accessor for the delimiter you wish to use between key strokes. This
	 * used by the default format implementations to determine the key stroke
	 * delimiter.
	 * 
	 * @return The delimiter to use between key strokes; should not be
	 *         <code>null</code>.
	 */
	protected abstract String getKeyStrokeDelimiter();

	/**
	 * Separates the modifier keys from each other, and then places them in an
	 * array in some sorted order. The sort order is dependent on the type of
	 * formatter.
	 * 
	 * @param modifierKeys
	 *            The modifier keys from the key stroke.
	 * @return An array of modifier key values -- separated and sorted in some
	 *         order. Any values in this array that are
	 *         <code>KeyStroke.NO_KEY</code> should be ignored.
	 */
	protected abstract int[] sortModifierKeys(final int modifierKeys);
}

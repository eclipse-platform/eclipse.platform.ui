/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.keys;

import java.util.Comparator;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.ui.keys.Key;

/**
 * Formats the keys in the internal key sequence grammar. This is used for
 * persistence, and is not really intended for display to the user.
 *
 * @since 3.0
 */
public class FormalKeyFormatter extends AbstractKeyFormatter {

	/**
	 * A comparator that guarantees that modifier keys will be sorted the same
	 * across different platforms.
	 */
	private static final Comparator FORMAL_MODIFIER_KEY_COMPARATOR = new AlphabeticModifierKeyComparator();

	@SuppressWarnings("removal")
	@Override
	public String format(Key key) {
		return key.toString();
	}


	@Override
	protected String getKeyDelimiter() {
		return KeyStroke.KEY_DELIMITER;
	}


	@Override
	protected String getKeyStrokeDelimiter() {
		return KeySequence.KEY_STROKE_DELIMITER;
	}

	@Override
	protected Comparator getModifierKeyComparator() {
		return FORMAL_MODIFIER_KEY_COMPARATOR;
	}

}

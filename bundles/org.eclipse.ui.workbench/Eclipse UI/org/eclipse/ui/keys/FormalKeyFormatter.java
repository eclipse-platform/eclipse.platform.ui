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

/**
 * TODO javadoc
 */
public class FormalKeyFormatter extends AbstractKeyFormatter {

	/**
	 * A comparator that guarantees that modifier keys will be sorted the same 
	 * across different platforms.
	 */
	private static final Comparator FORMAL_MODIFIER_KEY_COMPARATOR = new Comparator() {
		public int compare(Object left, Object right) {
			ModifierKey modifierKeyLeft = (ModifierKey) left;
			ModifierKey modifierKeyRight = (ModifierKey) right;
			return modifierKeyLeft.name.compareTo(modifierKeyRight.name);
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.AbstractKeyFormatter#getKeyDelimiter()
	 */
	protected String getKeyDelimiter() {
		return KeyFormatter.KEY_DELIMITER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.AbstractKeyFormatter#getKeyStrokeDelimiter()
	 */
	protected String getKeyStrokeDelimiter() {
		return KeyFormatter.KEY_STROKE_DELIMITER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.AbstractKeyFormatter#getModifierKeyComparator()
	 */
	protected Comparator getModifierKeyComparator() {
		return FORMAL_MODIFIER_KEY_COMPARATOR;
	}

}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.ModifierKey;
import org.eclipse.ui.keys.NaturalKey;

/**
 * A key formatter providing a special compact format for displaying key
 * bindings.
 *
 * @since 3.0
 */
public class CompactKeyFormatter extends NativeKeyFormatter {

	@Override
	public String format(KeySequence keySequence) {
		StringBuilder stringBuffer = new StringBuilder();

		List<KeyStroke> keyStrokes = keySequence.getKeyStrokes();
		KeyStroke[] keyStrokeArray = keyStrokes.toArray(new KeyStroke[keyStrokes.size()]);
		Set<ModifierKey> previousModifierKeys = Collections.emptySet();
		List<NaturalKey> naturalKeys = new ArrayList<>();
		for (int i = 0; i < keyStrokeArray.length; i++) {
			KeyStroke keyStroke = keyStrokeArray[i];
			Set<ModifierKey> currentModifierKeys = keyStroke.getModifierKeys();

			if (!previousModifierKeys.equals(currentModifierKeys)) {
				// End the old sequence fragment.
				if (i > 0) {
					stringBuffer.append(formatKeyStrokes(previousModifierKeys, naturalKeys));
					stringBuffer.append(getKeyStrokeDelimiter());
				}

				// Start a new one.
				previousModifierKeys = currentModifierKeys;
				naturalKeys.clear();

			}

			naturalKeys.add(keyStroke.getNaturalKey());
		}

		stringBuffer.append(formatKeyStrokes(previousModifierKeys, naturalKeys));

		return stringBuffer.toString();
	}

	public String formatKeyStrokes(Set<ModifierKey> modifierKeys, List<?> naturalKeys) {
		StringBuilder stringBuffer = new StringBuilder();
		String keyDelimiter = getKeyDelimiter();

		// Format the modifier keys, in sorted order.
		SortedSet<ModifierKey> sortedModifierKeys = new TreeSet<>(getModifierKeyComparator());
		sortedModifierKeys.addAll(modifierKeys);
		Iterator<ModifierKey> sortedModifierKeyItr = sortedModifierKeys.iterator();
		while (sortedModifierKeyItr.hasNext()) {
			stringBuffer.append(format(sortedModifierKeyItr.next()));
			stringBuffer.append(keyDelimiter);
		}

		// Format the natural key, if any.
		Iterator<?> naturalKeyItr = naturalKeys.iterator();
		while (naturalKeyItr.hasNext()) {
			Object naturalKey = naturalKeyItr.next();
			if (naturalKey instanceof NaturalKey) {
				stringBuffer.append(format((NaturalKey) naturalKey));
				if (naturalKeyItr.hasNext()) {
					stringBuffer.append(keyDelimiter);
				}
			}
		}

		return stringBuffer.toString();

	}
}

/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.bindings.keys.formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ModifierKey;
import org.eclipse.jface.bindings.keys.NaturalKey;


/**
 * A key formatter providing a special compact format for displaying key
 * bindings.
 * 
 * @since 3.1
 */
public class CompactKeyFormatter extends NativeKeyFormatter {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.keys.KeyFormatter#format(org.eclipse.ui.keys.KeySequence)
     */
    public String format(KeySequence keySequence) {
        StringBuffer stringBuffer = new StringBuffer();

        List keyStrokes = keySequence.getKeyStrokes();
        KeyStroke[] keyStrokeArray = (KeyStroke[]) keyStrokes
                .toArray(new KeyStroke[keyStrokes.size()]);
        Set previousModifierKeys = Collections.EMPTY_SET;
        List naturalKeys = new ArrayList();
        for (int i = 0; i < keyStrokeArray.length; i++) {
            KeyStroke keyStroke = keyStrokeArray[i];
            Set currentModifierKeys = keyStroke.getModifierKeys();

            if (!previousModifierKeys.equals(currentModifierKeys)) {
                // End the old sequence fragment.
                if (i > 0) {
                    stringBuffer.append(formatKeyStrokes(previousModifierKeys,
                            naturalKeys));
                    stringBuffer.append(getKeyStrokeDelimiter());
                }

                // Start a new one.
                previousModifierKeys = currentModifierKeys;
                naturalKeys.clear();

            }

            naturalKeys.add(keyStroke.getNaturalKey());
        }

        stringBuffer
                .append(formatKeyStrokes(previousModifierKeys, naturalKeys));

        return stringBuffer.toString();
    }

    /**
     * Formats the given modifier keys and natural keys into a nice, compact
     * representation.
     * 
     * @param modifierKeys
     *            The modifier keys that apply to all of the natural keys; must
     *            not be <code>null</code>.
     * @param naturalKeys
     *            The natural keys which shared the same modifier keys; must not
     *            be <code>null</code>.
     * @return A human-readable, compact representation of the arguments; never
     *         <code>null</code>.
     */
    public String formatKeyStrokes(Set modifierKeys, List naturalKeys) {
        StringBuffer stringBuffer = new StringBuffer();
        String keyDelimiter = getKeyDelimiter();

        // Format the modifier keys, in sorted order.
        SortedSet sortedModifierKeys = new TreeSet(getModifierKeyComparator());
        sortedModifierKeys.addAll(modifierKeys);
        Iterator sortedModifierKeyItr = sortedModifierKeys.iterator();
        while (sortedModifierKeyItr.hasNext()) {
            stringBuffer.append(format((ModifierKey) sortedModifierKeyItr
                    .next()));
            stringBuffer.append(keyDelimiter);
        }

        // Format the natural key, if any.
        Iterator naturalKeyItr = naturalKeys.iterator();
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

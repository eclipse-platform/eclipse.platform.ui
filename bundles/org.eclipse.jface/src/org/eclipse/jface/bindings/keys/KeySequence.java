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

package org.eclipse.jface.bindings.keys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.formatting.KeyFormatterFactory;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * A <code>KeySequence</code> is defined as a list of zero or more
 * <code>KeyStrokes</code>, with the stipulation that all
 * <code>KeyStroke</code> objects must be complete, save for the last one,
 * whose completeness is optional. A <code>KeySequence</code> is said to be
 * complete if all of its <code>KeyStroke</code> objects are complete.
 * </p>
 * <p>
 * All <code>KeySequence</code> objects have a formal string representation
 * available via the <code>toString()</code> method. There are a number of
 * methods to get instances of <code>KeySequence</code> objects, including one
 * which can parse this formal string representation.
 * </p>
 * <p>
 * All <code>KeySequence</code> objects, via the <code>format()</code>
 * method, provide a version of their formal string representation translated by
 * platform and locale, suitable for display to a user.
 * </p>
 * <p>
 * <code>KeySequence</code> objects are immutable. Clients are not permitted
 * to extend this class.
 * </p>
 * 
 * @since 3.1
 */
public class KeySequence extends TriggerSequence implements Comparable {

    /**
     * The delimiter between multiple key strokes in a single key sequence --
     * expressed in the formal key stroke grammar. This is not to be displayed
     * to the user. It is only intended as an internal representation.
     */
    public final static String KEY_STROKE_DELIMITER = Character
            .toString('\u0020'); //$NON-NLS-1$

    /**
     * An empty key sequence instance for use by everyone.
     */
    private final static KeySequence EMPTY_KEY_SEQUENCE = new KeySequence(
            Collections.EMPTY_LIST);

    /**
     * The set of delimiters for <code>KeyStroke</code> objects allowed during
     * parsing of the formal string representation.
     */
    public final static String KEY_STROKE_DELIMITERS = KEY_STROKE_DELIMITER
            + "\b\r\u007F\u001B\f\n\0\t\u000B"; //$NON-NLS-1$

    /**
     * Gets an instance of <code>KeySequence</code>.
     * 
     * @return a key sequence. This key sequence will have no key strokes.
     *         Guaranteed not to be <code>null</code>.
     */
    public static final KeySequence getInstance() {
        return EMPTY_KEY_SEQUENCE;
    }

    /**
     * Gets an instance of <code>KeySequence</code> given a key sequence and a
     * key stroke.
     * 
     * @param keySequence
     *            a key sequence. Must not be <code>null</code>.
     * @param keyStroke
     *            a key stroke. Must not be <code>null</code>.
     * @return a key sequence that is equal to the given key sequence with the
     *         given key stroke appended to the end. Guaranteed not to be
     *         <code>null</code>.
     */
    public static final KeySequence getInstance(final KeySequence keySequence,
            final KeyStroke keyStroke) {
        if (keySequence == null || keyStroke == null)
            throw new NullPointerException();

        final List keyStrokes = new ArrayList(keySequence.getKeyStrokes());
        keyStrokes.add(keyStroke);
        return new KeySequence(keyStrokes);
    }

    /**
     * Gets an instance of <code>KeySequence</code> given a single key stroke.
     * 
     * @param keyStroke
     *            a single key stroke. Must not be <code>null</code>.
     * @return a key sequence. Guaranteed not to be <code>null</code>.
     */
    public static final KeySequence getInstance(final KeyStroke keyStroke) {
        return new KeySequence(Collections.singletonList(keyStroke));
    }

    /**
     * Gets an instance of <code>KeySequence</code> given an array of key
     * strokes.
     * 
     * @param keyStrokes
     *            the array of key strokes. This array may be empty, but it must
     *            not be <code>null</code>. This array must not contain
     *            <code>null</code> elements.
     * @return a key sequence. Guaranteed not to be <code>null</code>.
     */
    public static final KeySequence getInstance(final KeyStroke[] keyStrokes) {
        return new KeySequence(Arrays.asList(keyStrokes));
    }

    /**
     * Gets an instance of <code>KeySequence</code> given a list of key
     * strokes.
     * 
     * @param keyStrokes
     *            the list of key strokes. This list may be empty, but it must
     *            not be <code>null</code>. If this list is not empty, it
     *            must only contain instances of <code>KeyStroke</code>.
     * @return a key sequence. Guaranteed not to be <code>null</code>.
     */
    public static final KeySequence getInstance(final List keyStrokes) {
        return new KeySequence(keyStrokes);
    }

    /**
     * Gets an instance of <code>KeySequence</code> by parsing a given a
     * formal string representation.
     * 
     * @param string
     *            the formal string representation to parse.
     * @return a key sequence. Guaranteed not to be <code>null</code>.
     * @throws ParseException
     *             if the given formal string representation could not be parsed
     *             to a valid key sequence.
     */
    public static final KeySequence getInstance(final String string)
            throws ParseException {
        if (string == null)
            throw new NullPointerException();

        final List keyStrokes = new ArrayList();
        final StringTokenizer stringTokenizer = new StringTokenizer(string,
                KEY_STROKE_DELIMITERS);

        while (stringTokenizer.hasMoreTokens())
            keyStrokes.add(KeyStroke.getInstance(stringTokenizer.nextToken()));

        try {
            return new KeySequence(keyStrokes);
        } catch (Throwable t) {
            throw new ParseException(
                    "Could not construct key sequence with these key strokes: " //$NON-NLS-1$
                            + keyStrokes);
        }
    }

    /**
     * Constructs an instance of <code>KeySequence</code> given a list of key
     * strokes.
     * 
     * @param keyStrokes
     *            the list of key strokes. This list may be empty, but it must
     *            not be <code>null</code>. If this list is not empty, it
     *            must only contain instances of <code>KeyStroke</code>.
     */
    protected KeySequence(List keyStrokes) {
        super(keyStrokes);

        for (int i = 0; i < triggers.size() - 1; i++) {
            KeyStroke keyStroke = (KeyStroke) triggers.get(i);

            if (!keyStroke.isComplete())
                throw new IllegalArgumentException();
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public final int compareTo(final Object object) {
        final KeySequence castedObject = (KeySequence) object;
        return Util.compare(triggers, castedObject.triggers);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public final boolean equals(final Object object) {
        if (!(object instanceof KeySequence))
            return false;

        final KeySequence castedObject = (KeySequence) object;
        return triggers.equals(castedObject.triggers);
    }

    /**
     * Formats this key sequence into the current default look.
     * 
     * @return A string representation for this key sequence using the default
     *         look; never <code>null</code>.
     */
    public final String format() {
        return KeyFormatterFactory.getDefault().format(this);
    }

    /**
     * Returns the list of key strokes for this key sequence.
     * 
     * @return the list of key strokes keys. This list may be empty, but is
     *         guaranteed not to be <code>null</code>. If this list is not
     *         empty, it is guaranteed to only contain instances of
     *         <code>KeyStroke</code>.
     */
    public final List getKeyStrokes() {
        return getTriggers();
    }

    /**
     * Returns whether or not this key sequence is complete. Key sequences are
     * complete iff all of their key strokes are complete.
     * 
     * @return <code>true</code>, iff the key sequence is complete.
     */
    public final boolean isComplete() {
        return triggers.isEmpty()
                || ((KeyStroke) triggers.get(triggers.size() - 1))
                        .isComplete();
    }

    /**
     * Returns the formal string representation for this key sequence.
     * 
     * @return The formal string representation for this key sequence.
     *         Guaranteed not to be <code>null</code>.
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        return KeyFormatterFactory.getFormalKeyFormatter().format(this);
    }
}

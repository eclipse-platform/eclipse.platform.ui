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
package org.eclipse.jface.bindings;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.Util;

/**
 * A sequence of one or more triggers. None of these triggers may be
 * <code>null</code>.
 * 
 * @since 3.1
 */
public class TriggerSequence {

    /**
     * A factor for computing the hash code for all trigger sequences.
     */
    protected final static int HASH_FACTOR = 89;

    /**
     * An internal constant used only in this object's hash code algorithm.
     */
    private final static int HASH_INITIAL = TriggerSequence.class.getName()
            .hashCode();

    /**
     * The hash code for this object. This value is computed lazily, and marked
     * as invalid when one of the values on which it is based changes.
     */
    protected transient int hashCode;

    /**
     * Whether <code>hashCode</code> still contains a valid value.
     */
    protected transient boolean hashCodeComputed = false;

    /**
     * The list of trigger in this sequence. This value is never
     * <code>null</code>, and never contains <code>null</code> elements.
     */
    protected final List triggers;

    /**
     * Constructs a new instance of <code>TriggerSequence</code>.
     * 
     * @param triggers
     *            The triggers contained within this sequence; must not be
     *            <code>null</code> or contain <code>null</code> elements.
     *            May be empty.
     */
    public TriggerSequence(final List triggers) {
        if (triggers == null) {
            throw new NullPointerException("The triggers cannot be null"); //$NON-NLS-1$
        }

        final Iterator triggerItr = triggers.iterator();
        while (triggerItr.hasNext()) {
            if (!(triggerItr.next() instanceof Trigger)) {
                throw new IllegalArgumentException(
                        "All triggers in a trigger sequence must be an instance of Trigger"); //$NON-NLS-1$
            }
        }

        this.triggers = triggers;

    }

    /**
     * Returns whether or not this key sequence ends with the given key
     * sequence.
     * 
     * @param triggerSequence
     *            a trigger sequence. Must not be <code>null</code>.
     * @param equals
     *            whether or not an identical trigger sequence should be
     *            considered as a possible match.
     * @return <code>true</code>, iff the given trigger sequence ends with
     *         this trigger sequence.
     */
    public final boolean endsWith(final TriggerSequence triggerSequence,
            final boolean equals) {
        if (triggerSequence == null) {
            throw new NullPointerException(
                    "Cannot end with a null trigger sequence"); //$NON-NLS-1$
        }

        return Util.endsWith(triggers, triggerSequence.triggers, equals);
    }

    /**
     * Returns the list of triggers.
     * 
     * @return The triggers; never <code>null</code> and guaranteed to only
     *         contain instances of <code>Trigger</code>.
     */
    public final List getTriggers() {
        return triggers;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public final int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + triggers.hashCode();
            hashCodeComputed = true;
        }

        return hashCode;
    }

    /**
     * Returns whether or not this trigger sequence is empty.
     * 
     * @return <code>true</code>, iff the trigger sequence is empty.
     */
    public final boolean isEmpty() {
        return triggers.isEmpty();
    }

    /**
     * Returns whether or not this trigger sequence starts with the given
     * trigger sequence.
     * 
     * @param triggerSequence
     *            a trigger sequence. Must not be <code>null</code>.
     * @param equals
     *            whether or not an identical trigger sequence should be
     *            considered as a possible match.
     * @return <code>true</code>, iff the given trigger sequence starts with
     *         this key sequence.
     */
    public final boolean startsWith(final TriggerSequence triggerSequence,
            final boolean equals) {
        if (triggerSequence == null)
            throw new NullPointerException(
                    "A trigger sequence cannot start with null"); //$NON-NLS-1$

        return Util.startsWith(triggers, triggerSequence.triggers, equals);
    }
}

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

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.util.Util;

/**
 * A keyboard shortcut. This is a binding between some keyboard input and the
 * triggering of a command. This object is immutable.
 * 
 * @since 3.1
 */
public final class KeyBinding extends Binding {

    /**
     * A factor for computing the hash code for all key bindings.
     */
    private final static int HASH_FACTOR = 89;

    /**
     * The seed for the hash code for all key bindings.
     */
    private final static int HASH_INITIAL = KeyBinding.class.getName()
            .hashCode();

    /**
     * The hash code for this key binding. This value is computed lazily, and
     * marked as invalid when one of the values on which it is based changes.
     */
    private transient int hashCode;

    /**
     * Whether <code>hashCode</code> still contains a valid value.
     */
    private transient boolean hashCodeComputed = false;

    /**
     * The key sequence which triggers this binding. This sequence is never
     * <code>null</code>.
     */
    private final KeySequence keySequence;

    /**
     * Constructs a new instance of <code>KeyBinding</code>.
     * 
     * @param keySequence
     *            The key sequence which should trigger this binding. This value
     *            must not be <code>null</code>. It also must be a complete,
     *            non-empty key sequence.
     * @param commandId
     *            The command to which this binding applies; this value may be
     *            <code>null</code> if the binding is meant to "unbind" (no
     *            op).
     * @param schemeId
     *            The scheme to which this binding belongs; this value must not
     *            be <code>null</code>.
     * @param contextId
     *            The context to which this binding applies; this value must not
     *            be <code>null</code>.
     * @param locale
     *            The locale to which this binding applies; this value may be
     *            <code>null</code> if it applies to all locales.
     * @param platform
     *            The platform to which this binding applies; this value may be
     *            <code>null</code> if it applies to all platforms.
     * @param windowManager
     *            The window manager to which this binding applies; this value
     *            may be <code>null</code> if it applies to all window
     *            managers. This value is currently ignored.
     * @param type
     *            The type of binding. This should be either <code>SYSTEM</code>
     *            or <code>USER</code>.
     */
    public KeyBinding(final KeySequence keySequence, final String commandId,
            final String schemeId, final String contextId, final String locale,
            final String platform, final String windowManager, final int type) {
        super(commandId, schemeId, contextId, locale, platform, windowManager,
                type);

        if (keySequence == null) {
            throw new NullPointerException("The key sequence cannot be null"); //$NON-NLS-1$
        }

        if (!keySequence.isComplete()) {
            throw new IllegalArgumentException(
                    "Cannot bind to an incomplete key sequence"); //$NON-NLS-1$
        }

        if (keySequence.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot bind to an empty key sequence"); //$NON-NLS-1$
        }

        this.keySequence = keySequence;
    }

    /**
     * Tests whether this key binding is equal to another object. Key bindings
     * are only equal to other key bindings with equivalent values.
     * 
     * @param object
     *            The object with which to compare; may be <code>null</code>.
     * @return <code>true</code> if the object is the key binding with
     *         equivalent values for all of its properties; <code>false</code>
     *         otherwise.
     */
    public final boolean equals(final Object object) {
        if (!(object instanceof KeyBinding)) {
            return false;
        }

        final KeyBinding binding = (KeyBinding) object;
        boolean equals = true;
        equals &= Util.equals(keySequence, binding.keySequence);
        equals &= Util.equals(getCommandId(), binding.getCommandId());
        equals &= Util.equals(getContextId(), binding.getContextId());
        equals &= Util.equals(getKeySequence(), binding.getKeySequence());
        equals &= Util.equals(getLocale(), binding.getLocale());
        equals &= Util.equals(getPlatform(), binding.getPlatform());
        equals &= Util.equals(getSchemeId(), binding.getSchemeId());

        return equals;
    }

    /**
     * Returns the key sequence which triggers this binding. The key sequence
     * will not be <code>null</code>, empty or incomplete.
     * 
     * @return The key sequence; never <code>null</code>.
     */
    public final KeySequence getKeySequence() {
        return keySequence;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.bindings.Binding#getTrigger()
     */
    public TriggerSequence getTriggerSequence() {
        return getKeySequence();
    }

    /**
     * Computes the hash code for this key binding based on all of its
     * attributes.
     * 
     * @return The hash code for this key binding.
     */
    public final int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(keySequence);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(getCommandId());
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(getContextId());
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(getLocale());
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(getPlatform());
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(getSchemeId());
            hashCodeComputed = true;
        }

        return hashCode;
    }

    /**
     * The string representation of this binding -- for debugging purposes only.
     * This string should not be shown to an end user. This should be overridden
     * by subclasses that add properties.
     * 
     * @return The string representation; never <code>null</code>.
     */
    public final String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("KeyBinding("); //$NON-NLS-1$
            stringBuffer.append(keySequence);
            stringBuffer.append(',');
            stringBuffer.append(getCommandId());
            stringBuffer.append(',');
            stringBuffer.append(getContextId());
            stringBuffer.append(',');
            stringBuffer.append(getSchemeId());
            stringBuffer.append(',');
            stringBuffer.append(getLocale());
            stringBuffer.append(',');
            stringBuffer.append(getPlatform());
            stringBuffer.append(')');
            string = stringBuffer.toString();
        }

        return string;
    }
}

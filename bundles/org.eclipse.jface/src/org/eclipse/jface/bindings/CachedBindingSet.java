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

import java.util.Map;

import org.eclipse.jface.util.Util;

/**
 * A resolution of bindings for a given state. To see if we already have a
 * cached binding set, just create one of these binding sets and then look it up
 * in a map. If it is not already there, then add it and set the cached binding
 * resolution.
 * 
 * @since 3.1
 */
public final class CachedBindingSet {

    /**
     * A factor for computing the hash code for all cached binding sets.
     */
    private final static int HASH_FACTOR = 89;

    /**
     * The seed for the hash code for all cached binding sets.
     */
    private final static int HASH_INITIAL = CachedBindingSet.class.getName()
            .hashCode();

    /**
     * <p>
     * A representation of the tree of active contexts at the time this cached
     * binding set was computed. It is a map of context id (<code>String</code>)
     * to context id (<code>String</code>). Each key represents one of the
     * active contexts or one of its ancestors, while each value represents its
     * parent. This is a way of perserving information about what the hierarchy
     * looked like.
     * </p>
     * <p>
     * This value will never be <code>null</code>. However, it might be
     * empty. All of the keys are guaranteed to be non- <code>null</code>,
     * but the values can be <code>null</code> (i.e., no parent).
     * </p>
     */
    private final Map activeContextTree;

    /**
     * The map representing the resolved state of the bindings. This is a map of
     * a trigger (<code>Trigger</code>) to command id (<code>String</code>).
     * This value may be <code>null</code> if it has not yet been initialized.
     */
    private Map commandIdsByTrigger = null;

    /**
     * The hash code for this object. This value is computed lazily, and marked
     * as invalid when one of the values on which it is based changes.
     */
    private transient int hashCode;

    /**
     * Whether <code>hashCode</code> still contains a valid value.
     */
    private transient boolean hashCodeComputed = false;

    /**
     * <p>
     * The list of locales that were active at the time this binding set was
     * computed. This list starts with the most specific representation of the
     * locale, and moves to more general representations. For example, this
     * array might look like ["en_US", "en", "", null].
     * </p>
     * <p>
     * This value will never be <code>null</code>, and it will never be
     * empty. It must contain at least one element, but its elements can be
     * <code>null</code>.
     * </p>
     */
    private final String[] locales;

    /**
     * <p>
     * The list of platforms that were active at the time this binding set was
     * computed. This list starts with the most specific representation of the
     * platform, and moves to more general representations. For example, this
     * array might look like ["gtk", "", null].
     * </p>
     * <p>
     * This value will never be <code>null</code>, and it will never be
     * empty. It must contain at least one element, but its elements can be
     * <code>null</code>.
     * </p>
     */
    private final String[] platforms;

    /**
     * <p>
     * The list of schemes that were active at the time this binding set was
     * computed. This list starts with the active scheme, and then continues
     * with all of its ancestors -- in order. For example, this might look like
     * ["emacs", "default"].
     * </p>
     * <p>
     * This value will never be <code>null</code>, and it will never be
     * empty. It must contain at least one element. Its elements cannot be
     * <code>null</code>.
     * </p>
     */
    private final String[] schemeIds;

    /**
     * Constructs a new instance of <code>CachedBindingSet</code>.
     * 
     * @param activeContextTree
     *            The set of context identifiers that were active when this
     *            binding set was calculated; must not be <code>null</code>,
     *            but may be empty. This is a map of context id (
     *            <code>String</code>) to parent context id (
     *            <code>String</code>). This is a way of caching the look of
     *            the context tree at the time the binding set was computed.
     * @param locales
     *            The locales that were active when this binding set was
     *            calculated. The first element is the currently active locale,
     *            and it is followed by increasingly more general locales. This
     *            must not be <code>null</code> and must contain at least one
     *            element. The elements can be <code>null</code>, though.
     * @param platforms
     *            The platform that were active when this binding set was
     *            calculated. The first element is the currently active
     *            platform, and it is followed by increasingly more general
     *            platforms. This must not be <code>null</code> and must
     *            contain at least one element. The elements can be
     *            <code>null</code>, though.
     * @param schemeIds
     *            The scheme that was active when this binding set was
     *            calculated, followed by its ancestors. This must not be
     *            <code>null</code> and must contain at least one element. The
     *            elements cannot be <code>null</code>.
     */
    CachedBindingSet(final Map activeContextTree, final String[] locales,
            final String[] platforms, final String[] schemeIds) {
        if (activeContextTree == null) {
            throw new NullPointerException("The context tree cannot be null."); //$NON-NLS-1$
        }

        if (locales == null) {
            throw new NullPointerException("The locales cannot be null."); //$NON-NLS-1$
        }

        if (locales.length == 0) {
            throw new NullPointerException("The locales cannot be empty."); //$NON-NLS-1$
        }

        if (platforms == null) {
            throw new NullPointerException("The platforms cannot be null."); //$NON-NLS-1$
        }

        if (platforms.length == 0) {
            throw new NullPointerException("The platforms cannot be empty."); //$NON-NLS-1$
        }

        if (schemeIds == null) {
            throw new NullPointerException("The schemes cannot be null."); //$NON-NLS-1$
        }

        if (schemeIds.length == 0) {
            throw new NullPointerException("The schemes cannot be empty."); //$NON-NLS-1$
        }

        this.activeContextTree = activeContextTree;
        this.locales = locales;
        this.platforms = platforms;
        this.schemeIds = schemeIds;
    }

    /**
     * Compares this binding set with another object. The objects will be equal
     * if they are both instance of <code>CachedBindingSet</code> and have
     * equivalent values for all of their properties.
     * 
     * @param object
     *            The object with which to compare; may be <code>null</code>.
     * @return <code>true</code> if they are both instances of
     *         <code>CachedBindingSet</code> and have the same values for all
     *         of their properties; <code>false</code> otherwise.
     */
    public final boolean equals(final Object object) {
        if (!(object instanceof CachedBindingSet)) {
            return false;
        }

        final CachedBindingSet other = (CachedBindingSet) object;
        boolean equals = true;
        equals &= Util.equals(activeContextTree, other.activeContextTree);
        equals &= Util.equals(locales, other.locales);
        equals &= Util.equals(platforms, other.platforms);
        equals &= Util.equals(schemeIds, other.schemeIds);

        return equals;
    }

    /**
     * Returns the map of command identifiers indexed by trigger.
     * 
     * @return A map of triggers (<code>Trigger</code>) to command
     *         identifiers (<code>String</code>). This value may be
     *         <code>null</code> if this was not yet initialized.
     */
    public final Map getCommandIdsByTrigger() {
        return commandIdsByTrigger;
    }

    /**
     * Computes the hash code for this cached binding set. The hash code is
     * based only on the immutable values. This allows the set to be created and
     * checked for in a hashed collection <em>before</em> doing any
     * computation.
     * 
     * @return The hash code for this cached binding set.
     */
    public final int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(activeContextTree);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(locales);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(platforms);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(schemeIds);
            hashCodeComputed = true;
        }

        return hashCode;
    }

    /**
     * Sets the map of command identifiers indexed by trigger.
     * 
     * @param commandIdsByTrigger
     *            The map to set; must not be <code>null</code>. This is a
     *            map of triggers (<code>Trigger</code>) to command
     *            identifiers (<code>String</code>).
     */
    final void setCommandIdsByTrigger(Map commandIdsByTrigger) {
        if (commandIdsByTrigger == null) {
            throw new NullPointerException(
                    "Cannot set a null binding resolution"); //$NON-NLS-1$
        }

        this.commandIdsByTrigger = commandIdsByTrigger;
    }
}

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

/**
 * An instance of this class describes changes to an instance of
 * <code>IScheme</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.1
 * @see ISchemeListener#schemeChanged(SchemeEvent)
 */
public class SchemeEvent {
    /**
     * Whether the scheme has become defined or undefined.
     */
    private final boolean definedChanged;

    /**
     * Whether the description of the scheme has changed.
     */
    private final boolean descriptionChanged;

    /**
     * Whether the name of the scheme has changed.
     */
    private final boolean nameChanged;

    /**
     * Whether the parent identifier has changed.
     */
    private final boolean parentIdChanged;

    /**
     * The scheme that has changed; this value is never <code>null</code>.
     */
    private final Scheme scheme;

    /**
     * Creates a new instance of this class.
     * 
     * @param scheme
     *            the instance of the interface that changed; must not be
     *            <code>null</code>.
     * @param definedChanged
     *            true, iff the defined property changed.
     * @param nameChanged
     *            true, iff the name property changed.
     * @param descriptionChanged
     *            <code>true</code> if the description property changed;
     *            <code>false</code> otherwise.
     * @param parentIdChanged
     *            true, iff the parentId property changed.
     */
    public SchemeEvent(Scheme scheme, boolean definedChanged,
            boolean nameChanged, boolean descriptionChanged,
            boolean parentIdChanged) {
        if (scheme == null)
            throw new NullPointerException();

        this.scheme = scheme;
        this.definedChanged = definedChanged;
        this.descriptionChanged = descriptionChanged;
        this.nameChanged = nameChanged;
        this.parentIdChanged = parentIdChanged;
    }

    /**
     * Returns the instance of the interface that changed.
     * 
     * @return the instance of the interface that changed. Guaranteed not to be
     *         <code>null</code>.
     */
    public final Scheme getScheme() {
        return scheme;
    }

    /**
     * Returns whether or not the defined property changed.
     * 
     * @return true, iff the defined property changed.
     */
    public final boolean hasDefinedChanged() {
        return definedChanged;
    }

    /**
     * Returns whether or not the description property has changed.
     * 
     * @return <code>true</code> if the description property changed;
     *         <code>false</code> otherwise.
     */
    public final boolean hasDescriptionChanged() {
        return descriptionChanged;
    }

    /**
     * Returns whether or not the name property changed.
     * 
     * @return true, iff the name property changed.
     */
    public final boolean hasNameChanged() {
        return nameChanged;
    }

    /**
     * Returns whether or not the parentId property changed.
     * 
     * @return true, iff the parentId property changed.
     */
    public final boolean hasParentIdChanged() {
        return parentIdChanged;
    }
}

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
 * <code>BindingManager</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.1
 * @see IBindingManagerListener#bindingManagerChanged(BindingManagerEvent)
 */
public final class BindingManagerEvent {

    /**
     * Whether the active scheme has changed.
     */
    private final boolean activeSchemeChanged;

    /**
     * The binding manager that has changed; this value is never
     * <code>null</code>.
     */
    private final BindingManager manager;

    /**
     * The identifier of the scheme that became defined or undefined. This value
     * may be <code>null</code> if no scheme changed its defined state.
     */
    private final String schemeId;

    /**
     * Whether the given scheme id became defined.
     */
    private final boolean schemeIdDefined;

    /**
     * Whether the given scheme id became undefined.
     */
    private final boolean schemeIdUndefined;

    /**
     * Creates a new instance of this class.
     * 
     * @param manager
     *            the instance of manager that changed; must not be
     *            <code>null</code>.
     * @param activeSchemeChanged
     *            true, iff the active scheme changed.
     * @param schemeId
     *            The scheme identifier that became defined or undefined;
     *            <code>null</code> if no scheme changed state.
     * @param schemeIdDefined
     *            <code>true</code> if the given scheme became defined;
     *            <code>false</code> otherwise.
     * @param schemeIdUndefined
     *            <code>true</code> if the given scheme became undefined;
     *            <code>false</code> otherwise.
     */
    public BindingManagerEvent(final BindingManager manager,
            final boolean activeSchemeChanged, final String schemeId,
            final boolean schemeIdDefined, final boolean schemeIdUndefined) {
        if (manager == null)
            throw new NullPointerException(
                    "A binding manager event needs a binding manager"); //$NON-NLS-1$

        if (schemeIdDefined || schemeIdUndefined) {
            if (schemeId == null) {
                throw new NullPointerException(
                        "If a scheme changed defined state, then there should be a scheme identifier"); //$NON-NLS-1$
            }
        } else if (schemeId != null) {
            throw new IllegalArgumentException(
                    "The scheme has not changed defined state"); //$NON-NLS-1$
        }

        this.manager = manager;
        this.activeSchemeChanged = activeSchemeChanged;
        this.schemeId = schemeId;
        this.schemeIdDefined = schemeIdDefined;
        this.schemeIdUndefined = schemeIdUndefined;
    }

    /**
     * Returns the instance of the manager that changed.
     * 
     * @return the instance of the manager that changed. Guaranteed not to be
     *         <code>null</code>.
     */
    public final BindingManager getManager() {
        return manager;
    }

    /**
     * Returns the scheme identifier for the scheme that changed.
     * 
     * @return The identifier for the changed scheme
     */
    public final String getSchemeId() {
        return schemeId;
    }

    /**
     * Returns whether or not the active scheme changed.
     * 
     * @return true, iff the active scheme property changed.
     */
    public final boolean hasActiveSchemeChanged() {
        return activeSchemeChanged;
    }

    /**
     * Returns whether or not the scheme identifier became defined
     * 
     * @return <code>true</code> if the scheme identifier became defined.
     */
    public final boolean isSchemeIdDefined() {
        return schemeIdDefined;
    }

    /**
     * Returns whether or not the scheme identifier became undefined
     * 
     * @return <code>true</code> if the scheme identifier became undefined.
     */
    public final boolean isSchemeIdUndefined() {
        return schemeIdUndefined;
    }
}

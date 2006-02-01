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

package org.eclipse.commands.contexts;

/**
 * An instance of this class describes changes to an instance of
 * <code>IContext</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.1
 * @see IContextListener#contextChanged(ContextEvent)
 */
public final class ContextEvent {

    /**
     * The context that has changed. This value is never <code>null</code>.
     */
    private final Context context;

    /**
     * Whether the context has become defined or undefined.
     */
    private final boolean definedChanged;

    /**
     * Whether the context has become enabled or disabled.
     */
    private final boolean enabledChanged;

    /**
     * Whether the name of the context has changed.
     */
    private final boolean nameChanged;

    /**
     * Whether the parent identifier has changed.
     */
    private final boolean parentIdChanged;

    /**
     * Creates a new instance of this class.
     * 
     * @param context
     *            the instance of the interface that changed.
     * @param definedChanged
     *            true, iff the defined property changed.
     * @param enabledChanged
     *            true, iff the enabled property changed.
     * @param nameChanged
     *            true, iff the name property changed.
     * @param parentIdChanged
     *            true, iff the parentId property changed.
     */
    public ContextEvent(final Context context, final boolean definedChanged,
            final boolean enabledChanged, final boolean nameChanged,
            final boolean parentIdChanged) {
        if (context == null)
            throw new NullPointerException();

        this.context = context;
        this.definedChanged = definedChanged;
        this.enabledChanged = enabledChanged;
        this.nameChanged = nameChanged;
        this.parentIdChanged = parentIdChanged;
    }

    /**
     * Returns the instance of the interface that changed.
     * 
     * @return the instance of the interface that changed. Guaranteed not to be
     *         <code>null</code>.
     */
    public final Context getContext() {
        return context;
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
     * Returns whether or not the enabled property changed.
     * 
     * @return true, iff the enabled property changed.
     */
    public final boolean hasEnabledChanged() {
        return enabledChanged;
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
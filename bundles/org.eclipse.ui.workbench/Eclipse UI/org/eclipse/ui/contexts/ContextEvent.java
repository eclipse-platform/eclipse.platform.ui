/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.contexts;

/**
 * An instance of this class describes changes to an instance of
 * <code>IContext</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see IContextListener#contextChanged(ContextEvent)
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 * @see org.eclipse.core.commands.contexts.ContextEvent
 */
public final class ContextEvent {

    /**
     * The context that has changed. This value is never <code>null</code>.
     */
    private final IContext context;

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
    public ContextEvent(IContext context, boolean definedChanged,
            boolean enabledChanged, boolean nameChanged, boolean parentIdChanged) {
        if (context == null) {
			throw new NullPointerException();
		}

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
    public IContext getContext() {
        return context;
    }

    /**
     * Returns whether or not the defined property changed.
     * 
     * @return true, iff the defined property changed.
     */
    public boolean hasDefinedChanged() {
        return definedChanged;
    }

    /**
     * Returns whether or not the enabled property changed.
     * 
     * @return true, iff the enabled property changed.
     */
    public boolean hasEnabledChanged() {
        return enabledChanged;
    }

    /**
     * Returns whether or not the name property changed.
     * 
     * @return true, iff the name property changed.
     */
    public boolean hasNameChanged() {
        return nameChanged;
    }

    /**
     * Returns whether or not the parentId property changed.
     * 
     * @return true, iff the parentId property changed.
     */
    public boolean hasParentIdChanged() {
        return parentIdChanged;
    }
}

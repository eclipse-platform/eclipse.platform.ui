/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
 * @see IContextListener#contextChanged
 */
public final class ContextEvent {

    private IContext context;

    private boolean contextContextBindingsChanged;

    private boolean definedChanged;

    private boolean enabledChanged;

    private boolean nameChanged;

    private boolean parentIdChanged;

    /**
     * Creates a new instance of this class.
     * 
     * @param context
     *            the instance of the interface that changed.
     * @param contextContextBindingsChanged
     *            true, iff the contextContextBindings property changed.
     * @param definedChanged
     *            true, iff the defined property changed.
     * @param enabledChanged
     *            true, iff the enabled property changed.
     * @param nameChanged
     *            true, iff the name property changed.
     * @param parentIdChanged
     *            true, iff the parentId property changed.
     */
    public ContextEvent(IContext context,
            boolean contextContextBindingsChanged, boolean definedChanged,
            boolean enabledChanged, boolean nameChanged, boolean parentIdChanged) {
        if (context == null) throw new NullPointerException();

        this.context = context;
        this.contextContextBindingsChanged = contextContextBindingsChanged;
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

    /**
     * Returns whether or not the contextContextBindings property changed.
     * 
     * @return true, iff the contextContextBindings property changed.
     */
    public boolean haveContextContextBindingsChanged() {
        return contextContextBindingsChanged;
    }
}
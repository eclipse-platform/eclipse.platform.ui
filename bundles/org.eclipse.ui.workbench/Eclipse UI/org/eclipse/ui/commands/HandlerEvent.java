/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.commands;

import java.util.Set;

import org.eclipse.ui.internal.util.Util;

/**
 * An instance of this class describes changes to an instance of <code>IHandler</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see IHandlerListener#handlerChanged
 */
public final class HandlerEvent {

    private IHandler handler;

    private Set attributeNames;

    private boolean definedAttributeNamesChanged;

    /**
     * Creates a new instance of this class.
     * 
     * @param handler
     *            the instance of the interface that changed.
     * @param attributeNames
     *            the set of names of attributes whose values have changed.
     *            This set may be empty, but it must not be <code>null</code>.
     *            If this set is not empty, it must only contain instances of
     *            <code>String</code>.
     * @param definedAttributeNamesChanged
     *            true, iff the definedAttributeNames property changed.
     */
    public HandlerEvent(IHandler handler, Set attributeNames,
            boolean definedAttributeNamesChanged) {
        if (handler == null) throw new NullPointerException();

        this.handler = handler;
        this.attributeNames = Util.safeCopy(attributeNames, String.class);
        this.definedAttributeNamesChanged = definedAttributeNamesChanged;
    }

    /**
     * Returns the instance of the interface that changed.
     * 
     * @return the instance of the interface that changed. Guaranteed not to be
     *         <code>null</code>.
     */
    public IHandler getHandler() {
        return handler;
    }

    /**
     * Returns the set of names of attributes whose values have changed.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the set of names of attributes whose values have changed. This
     *         set may be empty, but is guaranteed not to be <code>null</code>.
     *         If this set is not empty, it is guaranteed to only contain
     *         instances of <code>String</code>.
     */
    public Set getAttributeNames() {
        return attributeNames;
    }

    /**
     * Returns whether or not the definedAttributeNames property changed.
     * 
     * @return true, iff the definedAttributeNames property changed.
     */
    public boolean haveDefinedAttributeNamesChanged() {
        return definedAttributeNamesChanged;
    }
}

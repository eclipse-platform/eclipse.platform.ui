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
 * An instance of this class describes changes to an instance of
 * <code>IHandler</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see IHandlerListener#handlerChanged
 */
public final class HandlerEvent {

    private boolean definedAttributeNamesChanged;

    private IHandler handler;

    private Set previouslyDefinedAttributeNames;

    /**
     * Creates a new instance of this class.
     * 
     * @param handler
     *            the instance of the interface that changed.
     * @param definedAttributeNamesChanged
     *            true, iff the definedAttributeNames property changed.
     * @param previouslyAttributeNames
     *            the set of previously defined attribute names. This set may be
     *            empty. If this set is not empty, it must only contain
     *            instances of <code>String</code>. This set must be
     *            <code>null</code> if definedAttributeNamesChanged is
     *            <code>false</code> and must not be null if
     *            definedAttributeNamesChanged is <code>true</code>.
     */
    public HandlerEvent(IHandler handler, boolean definedAttributeNamesChanged,
            Set previouslyDefinedAttributeNames) {
        if (handler == null) throw new NullPointerException();

        if (!definedAttributeNamesChanged
                && previouslyDefinedAttributeNames != null)
                throw new IllegalArgumentException();

        if (definedAttributeNamesChanged)
                this.previouslyDefinedAttributeNames = Util.safeCopy(
                        previouslyDefinedAttributeNames, String.class);

        this.handler = handler;
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
     * Returns the set of previously defined attribute names.
     * 
     * @return the set of previously defined attribute names. This set may be
     *         empty. If this set is not empty, it is guaranteed to only contain
     *         instances of <code>String</code>. This set is guaranteed to be
     *         <code>null</code> if haveDefinedAttributeNamesChanged() is
     *         <code>false</code> and is guaranteed to not be null if
     *         haveDefinedAttributeNamesChanged() is <code>true</code>.
     */
    public Set getPreviouslyDefinedAttributeNames() {
        return previouslyDefinedAttributeNames;
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
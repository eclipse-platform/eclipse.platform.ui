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

import java.util.Map;

import org.eclipse.ui.internal.util.Util;

/**
 * An instance of this class describes changes to an instance of
 * <code>IHandler</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see IHandlerListener#handlerChanged(HandlerEvent)
 */
public final class HandlerEvent {

    /**
     * Whether the attributes of the handler changed.
     */
    private final boolean attributeValuesByNameChanged;

    /**
     * The handler that changed; this value is never <code>null</code>.
     */
    private final IHandler handler;

    /**
     * The map of previous attributes, if they changed.  If they did not change,
     * then this value is <code>null</code>.  The map's keys are the attribute
     * names (strings), and its value are any object.
     */
    private final Map previousAttributeValuesByName;

    /**
     * Creates a new instance of this class.
     * 
     * @param handler
     *            the instance of the interface that changed.
     * @param attributeValuesByNameChanged
     *            true, iff the attributeValuesByName property changed.
     * @param previousAttributeValuesByName
     *            the map of previous attribute values by name. This map may be
     *            empty. If this map is not empty, it's collection of keys must
     *            only contain instances of <code>String</code>. This map
     *            must be <code>null</code> if attributeValuesByNameChanged is
     *            <code>false</code> and must not be null if
     *            attributeValuesByNameChanged is <code>true</code>.
     */
    public HandlerEvent(IHandler handler, boolean attributeValuesByNameChanged,
            Map previousAttributeValuesByName) {
        if (handler == null)
            throw new NullPointerException();

        if (!attributeValuesByNameChanged
                && previousAttributeValuesByName != null)
            throw new IllegalArgumentException();

        if (attributeValuesByNameChanged) {
            this.previousAttributeValuesByName = Util.safeCopy(
                    previousAttributeValuesByName, String.class, Object.class,
                    false, true);
        } else {
            this.previousAttributeValuesByName = null;
        }

        this.handler = handler;
        this.attributeValuesByNameChanged = attributeValuesByNameChanged;
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
     * Returns the map of previous attribute values by name.
     * 
     * @return the map of previous attribute values by name. This map may be
     *         empty. If this map is not empty, it's collection of keys is
     *         guaranteed to only contain instances of <code>String</code>.
     *         This map is guaranteed to be <code>null</code> if
     *         haveAttributeValuesByNameChanged() is <code>false</code> and is
     *         guaranteed to not be null if haveAttributeValuesByNameChanged()
     *         is <code>true</code>.
     */
    public Map getPreviousAttributeValuesByName() {
        return previousAttributeValuesByName;
    }

    /**
     * Returns whether or not the attributeValuesByName property changed.
     * 
     * @return true, iff the attributeValuesByName property changed.
     */
    public boolean haveAttributeValuesByNameChanged() {
        return attributeValuesByNameChanged;
    }
}
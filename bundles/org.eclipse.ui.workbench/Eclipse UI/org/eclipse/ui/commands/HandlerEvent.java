/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
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
     * This is the cached result of getPreviousAttributeValuesByName. It is
     * computed the first time getPreviousAttributeValuesByName is called.
     */
    private Map previousAttributeValuesByName;

    /**
     * The map of previous attributes, if they changed.  If they did not change,
     * then this value is <code>null</code>.  The map's keys are the attribute
     * names (strings), and its value are any object.
     * 
     * This is the original map passed into the constructor. This object always
     * returns a copy of this map, not the original. However the constructor of
     * this object is called very frequently and the map is rarely requested,
     * so we only copy the map the first time it is requested. 
     * 
     * @since 3.1
     */
    private final Map originalPreviousAttributeValuesByName;
    
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
        if (handler == null) {
			throw new NullPointerException();
		}

        if (!attributeValuesByNameChanged
                && previousAttributeValuesByName != null) {
			throw new IllegalArgumentException();
		}

        if (attributeValuesByNameChanged) {
            this.originalPreviousAttributeValuesByName = previousAttributeValuesByName;
        } else {
            this.originalPreviousAttributeValuesByName = null;
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
        if (originalPreviousAttributeValuesByName == null) {
            return null;
        }
        
        if (previousAttributeValuesByName == null) {
            previousAttributeValuesByName = Util.safeCopy(
                    originalPreviousAttributeValuesByName, String.class, Object.class,
                    false, true);
        }
        
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

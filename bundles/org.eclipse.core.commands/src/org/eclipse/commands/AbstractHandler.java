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
package org.eclipse.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class is a partial implementation of <code>IHandler</code>. This
 * abstract implementation provides support for handler listeners. You should
 * subclass from this method unless you want to implement your own listener
 * support. Subclasses should call
 * {@link AbstractHandler#fireHandlerChanged(HandlerEvent)}when the handler
 * changes. Subclasses should also override
 * {@link AbstractHandler#getAttributeValuesByName()}if they have any
 * attributes.
 * 
 * @since 3.1
 */
public abstract class AbstractHandler implements IHandler {

    /**
     * Those interested in hearing about changes to this instance of
     * <code>IHandler</code>. This member is null iff there are no listeners
     * attached to this handler. (Most handlers don't have any listeners, and
     * this optimization saves some memory.)
     */
    private List handlerListeners;

    /**
     * @see IHandler#addHandlerListener(IHandlerListener)
     */
    public void addHandlerListener(final IHandlerListener handlerListener) {
        if (handlerListener == null)
            throw new NullPointerException();
        if (handlerListeners == null)
            handlerListeners = new ArrayList();
        if (!handlerListeners.contains(handlerListener))
            handlerListeners.add(handlerListener);
    }

    /**
     * The default implementation does nothing. Subclasses who attach listeners
     * to other objects are encouraged to detach them in this method.
     * 
     * @see org.eclipse.commands.IHandler#dispose()
     */
    public void dispose() {
        // Do nothing.
    }

    /**
     * Fires an event to all registered listeners describing changes to this
     * instance.
     * 
     * @param handlerEvent
     *            the event describing changes to this instance. Must not be
     *            <code>null</code>.
     */
    protected void fireHandlerChanged(final HandlerEvent handlerEvent) {
        if (handlerEvent == null)
            throw new NullPointerException();
        if (handlerListeners != null)
            for (int i = 0; i < handlerListeners.size(); i++)
                ((IHandlerListener) handlerListeners.get(i))
                        .handlerChanged(handlerEvent);
    }

    /**
     * This simply return an empty map. The default implementation has no
     * attributes.
     * 
     * @see IHandler#getAttributeValuesByName()
     */
    public Map getAttributeValuesByName() {
        return Collections.EMPTY_MAP;
    }

    /**
     * Returns true iff there is one or more IHandlerListeners attached to this
     * AbstractHandler.
     * 
     * @return true iff there is one or more IHandlerListeners attached to this
     *         AbstractHandler
     * @since 3.1
     */
    protected final boolean hasListeners() {
        return handlerListeners != null;
    }

    /**
     * @see IHandler#removeHandlerListener(IHandlerListener)
     */
    public void removeHandlerListener(final IHandlerListener handlerListener) {
        if (handlerListener == null)
            throw new NullPointerException();
        if (handlerListeners == null) {
            return;
        }

        if (handlerListeners != null)
            handlerListeners.remove(handlerListener);
        if (handlerListeners.isEmpty()) {
            handlerListeners = null;
        }
    }
}

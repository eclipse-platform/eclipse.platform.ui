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
package org.eclipse.ui.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * This class is a partial implementation of <code>IHandler</code>.
 * 
 * @since 3.0
 */
public abstract class AbstractHandler implements IHandler {

    private List handlerListeners;

    public void addHandlerListener(IHandlerListener handlerListener) {
        if (handlerListener == null) throw new NullPointerException();

        if (handlerListeners == null) handlerListeners = new ArrayList();

        if (!handlerListeners.contains(handlerListener))
                handlerListeners.add(handlerListener);
    }

    /**
     * Fires an event to all registered listeners describing changes to this
     * instance.
     * 
     * @param handlerEvent
     *            the event describing changes to this instance. Must not be
     *            <code>null</code>.
     */
    protected void fireHandlerChanged(HandlerEvent handlerEvent) {
        if (handlerEvent == null) throw new NullPointerException();

        if (handlerListeners != null)
                for (int i = 0; i < handlerListeners.size(); i++)
                    ((IHandlerListener) handlerListeners.get(i))
                            .handlerChanged(handlerEvent);
    }

    public Object getAttributeValue(String attributeName)
            throws NoSuchAttributeException {
        throw new NoSuchAttributeException();
    }

    public Set getDefinedAttributeNames() {
        return Collections.EMPTY_SET;
    }

    public void removeHandlerListener(IHandlerListener handlerListener) {
        if (handlerListener == null) throw new NullPointerException();

        if (handlerListeners != null) handlerListeners.remove(handlerListener);
    }
}
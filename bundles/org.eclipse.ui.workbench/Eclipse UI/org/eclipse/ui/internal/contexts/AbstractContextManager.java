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

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.contexts.ContextManagerEvent;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextManagerListener;

public abstract class AbstractContextManager implements IContextManager {
    private List contextManagerListeners;

    protected AbstractContextManager() {
    }

    public void addContextManagerListener(
            IContextManagerListener contextManagerListener) {
        if (contextManagerListener == null)
            throw new NullPointerException();

        if (contextManagerListeners == null)
            contextManagerListeners = new ArrayList();

        if (!contextManagerListeners.contains(contextManagerListener))
            contextManagerListeners.add(contextManagerListener);
    }

    protected void fireContextManagerChanged(
            ContextManagerEvent contextManagerEvent) {
        if (contextManagerEvent == null)
            throw new NullPointerException();

        if (contextManagerListeners != null)
            for (int i = 0; i < contextManagerListeners.size(); i++)
                ((IContextManagerListener) contextManagerListeners.get(i))
                        .contextManagerChanged(contextManagerEvent);
    }

    public void removeContextManagerListener(
            IContextManagerListener contextManagerListener) {
        if (contextManagerListener == null)
            throw new NullPointerException();

        if (contextManagerListeners != null)
            contextManagerListeners.remove(contextManagerListener);
    }
}
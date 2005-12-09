/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

public class DebugTargetProxy extends EventHandlerModelProxy {

    private IDebugTarget fDebugTarget;

    public DebugTargetProxy(IDebugTarget target) {
        fDebugTarget = target;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();
		fDebugTarget = null;
	}

	protected boolean containsEvent(DebugEvent event) {
        Object source = event.getSource();
        if (source instanceof IDebugElement) {
            IDebugTarget debugTarget = ((IDebugElement) source).getDebugTarget();
            // an expression can return null for debug target
            if (debugTarget != null) {
            	return debugTarget.equals(fDebugTarget);
            }
        }
        return false;
    }

    protected DebugEventHandler[] createEventHandlers() {
        return new DebugEventHandler[] { new DebugTargetEventHandler(this), new ThreadEventHandler(this) };
    }

	public void setInitialState() {
	}

}

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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.ModelDelta;

/**
 * Default model proxy for a debug target.
 * 
 * @since 3.2
 */
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#containsEvent(org.eclipse.debug.core.DebugEvent)
	 */
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

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#createEventHandlers()
     */
    protected DebugEventHandler[] createEventHandlers() {
        return new DebugEventHandler[] { new DebugTargetEventHandler(this), new ThreadEventHandler(this) };
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AbstractModelProxy#installed()
	 */
	public void installed() {
		// select any thread that is already suspended after installation
		IDebugTarget target = fDebugTarget;
		if (target != null) {
			try {
				IThread[] threads = target.getThreads();
				for (int i = 0; i < threads.length; i++) {
					IThread thread = threads[i];
					if (thread.isSuspended()) {
						IStackFrame frame = thread.getTopStackFrame();
						if (frame != null) {
							ModelDelta delta = new ModelDelta(DebugPlugin.getDefault().getLaunchManager(), IModelDelta.NO_CHANGE);
							ModelDelta node = delta.addNode(target.getLaunch(), IModelDelta.NO_CHANGE);
							node = node.addNode(target, IModelDelta.NO_CHANGE | IModelDelta.EXPAND);
							node = node.addNode(thread, IModelDelta.NO_CHANGE | IModelDelta.EXPAND);
							node = node.addNode(frame, IModelDelta.NO_CHANGE | IModelDelta.SELECT);
							fireModelChanged(delta);
							return;
						}
					}
				}
			} catch (DebugException e) {
			}
		}
	}
    
    

}

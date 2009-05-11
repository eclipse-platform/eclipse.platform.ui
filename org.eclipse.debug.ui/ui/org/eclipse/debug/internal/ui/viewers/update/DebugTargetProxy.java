/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.Viewer;

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
        ThreadEventHandler threadEventHandler = new ThreadEventHandler(this);
		return new DebugEventHandler[] { new DebugTargetEventHandler(this), threadEventHandler,
				new StackFrameEventHandler(this, threadEventHandler) };
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelProxy#installed(org.eclipse.jface.viewers.Viewer)
	 */
	public void installed(Viewer viewer) {
		// select any thread that is already suspended after installation
		IDebugTarget target = fDebugTarget;
		if (target != null) {
			ModelDelta delta = getNextSuspendedThreadDelta(null, false);
			if (delta == null) {
                try {
                    ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
                    ILaunch launch = target.getLaunch();
                    int launchIndex = indexOf(manager.getLaunches(), target.getLaunch());
                    int targetIndex = indexOf(target.getLaunch().getChildren(), target);
                    delta = new ModelDelta(manager, IModelDelta.NO_CHANGE);
                    ModelDelta node = delta.addNode(launch, launchIndex, IModelDelta.NO_CHANGE, target.getLaunch().getChildren().length);
                    node = node.addNode(target, targetIndex, IModelDelta.EXPAND | IModelDelta.SELECT, target.getThreads().length);
                } catch (DebugException e) {
                    // In case of exception do not fire delta
                    return;
                } 
			}
			// expand the target if no suspended thread
			fireModelChanged(delta);
		}
	}
    
    protected ModelDelta getNextSuspendedThreadDelta(IThread currentThread, boolean reverse) {
        IDebugTarget target = fDebugTarget;
        if (target != null) {
            try {
                IThread[] threads = target.getThreads();
                ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
                ILaunch launch = target.getLaunch();
                int launchIndex = indexOf(manager.getLaunches(), target.getLaunch());
                int targetIndex = indexOf(target.getLaunch().getChildren(), target);
                IThread chosen = null;
                int threadIndex = -1;
                // select the first thread with a breakpoint, or the first suspended thread
                // if none have breakpoints
                boolean takeNext = currentThread == null;
                int startIdx = reverse ? threads.length - 1 : 0;
                int endIdx = reverse ? -1 : threads.length;
                int increment = reverse ? -1 : 1;
                for (int i = startIdx; i != endIdx; i = i + increment) {
                    IThread thread = threads[i];
                    if (takeNext && thread.isSuspended()) {
                        IBreakpoint[] bps = thread.getBreakpoints();
                        if (bps != null && bps.length > 0) {
                            chosen = thread;
                            threadIndex = i;
                            break;
                        } else {
                            if (chosen == null) {
                                chosen = thread;
                                threadIndex = i;
                            }
                        }
                    }
                    takeNext = takeNext || thread.equals(currentThread);
                }
                if (chosen != null) {
                    IStackFrame frame = chosen.getTopStackFrame();
                    if (frame != null) {
                        ModelDelta delta = new ModelDelta(manager, IModelDelta.NO_CHANGE);
                        ModelDelta node = delta.addNode(launch, launchIndex, IModelDelta.NO_CHANGE, target.getLaunch().getChildren().length);
                        node = node.addNode(target, targetIndex, IModelDelta.NO_CHANGE, threads.length);
                        node = node.addNode(chosen, threadIndex, IModelDelta.NO_CHANGE | IModelDelta.EXPAND, chosen.getStackFrames().length);
                        node = node.addNode(frame, 0, IModelDelta.NO_CHANGE | IModelDelta.SELECT, 0);
                        return delta;
                    }
                }
            } catch (DebugException e) {
            }
        }
        return null;
    }

}

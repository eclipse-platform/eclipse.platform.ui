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
package org.eclipse.debug.internal.ui.contexts;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.contexts.ISuspendTrigger;
import org.eclipse.debug.ui.contexts.ISuspendTriggerListener;

/**
 * @since 3.2
 */
public class LaunchSuspendTrigger implements ISuspendTrigger, IDebugEventSetListener {

	private ListenerList fListeners = new ListenerList();
	
	public LaunchSuspendTrigger() {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	protected void dispose() {
		fListeners = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.ISuspendTrigger#addSuspendTriggerListener(org.eclipse.debug.ui.contexts.ISuspendTriggerListener)
	 */
	public void addSuspendTriggerListener(ISuspendTriggerListener listener) {
        if (fListeners != null) {
            fListeners.add(listener);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.ISuspendTrigger#removeSuspendTriggerListener(org.eclipse.debug.ui.contexts.ISuspendTriggerListener)
	 */
	public void removeSuspendTriggerListener(ISuspendTriggerListener listener) { 
        if (fListeners != null) {
            fListeners.remove(listener);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		// open the debugger if this is a suspend event and the debug view is not yet open
		// and the preferences are set to switch
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.SUSPEND && !event.isEvaluation() && event.getDetail() != DebugEvent.STEP_END) {
			    // Don't switch perspective for evaluations or stepping
				notifySuspend(event);
			}
		}
	}

	/**
	 * @param event
	 */
	private void notifySuspend(DebugEvent event) {
		Object source = event.getSource();
		if (source instanceof IDebugElement) {
			final ILaunch launch = ((IDebugElement)source).getLaunch();
			Object context = null;
			if (source instanceof IThread) {
				try {
					context = ((IThread)source).getTopStackFrame();
				} catch (DebugException e) {
				}
			} else if (source instanceof IDebugTarget) {
				context = source;
			}
			final Object temp = context;
            ListenerList list = fListeners;
            if (list != null) {
                Object[] listeners = list.getListeners();
        		for (int i = 0; i < listeners.length; i++) {
        			final ISuspendTriggerListener listener = (ISuspendTriggerListener) listeners[i];
        			Platform.run(new ISafeRunnable() {
        				public void run() throws Exception {
        					listener.suspended(launch, temp);
        				}
        			
        				public void handleException(Throwable exception) {
        					DebugUIPlugin.log(exception);
        				}
        			
        			}); 			
        		}
            }

		}
		
	}

}

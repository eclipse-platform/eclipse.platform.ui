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
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.IModelDeltaNode;
import org.eclipse.debug.internal.ui.viewers.IModelProxy;

/**
 * @since 3.2
 */
public class ThreadEventHandler extends DebugEventHandler {
	/**
	 * Constructs and event handler for a threads in the given viewer.
	 * 
	 * @param viewer
	 */
	public ThreadEventHandler(IModelProxy proxy) {
		super(proxy);
	}

	public void dispose() {
		super.dispose();
	}

	protected void handleSuspend(DebugEvent event) {
		fireDelta((IThread) event.getSource(), IModelDelta.CHANGED | IModelDelta.CONTENT | IModelDelta.EXPAND);
	}
	
	protected void handleResumeExpectingSuspend(DebugEvent event) {
		fireDelta((IThread) event.getSource(), IModelDelta.CHANGED | IModelDelta.STATE);
	}

	protected void handleResume(DebugEvent event) {
		fireDelta((IThread) event.getSource(), IModelDelta.CHANGED | IModelDelta.STATE);
	}

	protected void handleCreate(DebugEvent event) {
		fireDelta((IThread) event.getSource(), IModelDelta.ADDED | IModelDelta.STATE);
	}

	protected void handleTerminate(DebugEvent event) {
		fireDelta((IThread) event.getSource(), IModelDelta.REMOVED);
	}

	protected void handleChange(DebugEvent event) {
		fireDelta((IThread) event.getSource(), IModelDelta.CHANGED | IModelDelta.STATE);
	}

	protected void handleLateSuspend(DebugEvent suspend, DebugEvent resume) {
		fireDelta((IThread) suspend.getSource(), IModelDelta.CHANGED | IModelDelta.CONTENT | IModelDelta.EXPAND);
	}

	protected void handleSuspendTimeout(DebugEvent event) {
		fireDelta((IThread) event.getSource(), IModelDelta.CHANGED | IModelDelta.CONTENT);
	}

	private void fireDelta(IThread thread, int flags) {
		ModelDelta delta = new ModelDelta();
		IModelDeltaNode node = delta.addNode(DebugPlugin.getDefault().getLaunchManager(), IModelDelta.NOCHANGE);
		node = node.addNode(thread.getLaunch(), IModelDelta.NOCHANGE);
		node = node.addNode(thread.getDebugTarget(), IModelDelta.NOCHANGE);
		node = node.addNode(thread, flags);
		if ((flags & IModelDelta.EXPAND) != 0) {
			Object topStackFrame = null;
			try {
				 topStackFrame = thread.getTopStackFrame();
			} catch (DebugException e) {
				topStackFrame = new Object();
			}
			node.addNode(topStackFrame, IModelDelta.CHANGED | IModelDelta.SELECT);
		}
		getModelProxy().fireModelChanged(delta);
	}

	protected boolean handlesEvent(DebugEvent event) {
		return event.getSource() instanceof IThread;
	}

}

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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.viewers.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.IModelProxy;


/**
 * @since 3.2
 */
public class DebugTargetEventHandler extends DebugEventHandler {

	/**
	 * Constructs an event handler for a debug target on the given viewer.
	 * 
	 * @param viewer
	 */
	public DebugTargetEventHandler(IModelProxy proxy) {
		super(proxy);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler#handlesEvent(org.eclipse.debug.core.DebugEvent)
	 */
	protected boolean handlesEvent(DebugEvent event) {
		return event.getSource() instanceof IDebugTarget;
	}

	protected void handleChange(DebugEvent event) {
		fireDelta((IDebugTarget) event.getSource(), IModelDelta.CHANGED | IModelDelta.STATE);
	}

	protected void handleCreate(DebugEvent event) {
		// do nothing - launch change notification handles this
	}

	protected void handleResume(DebugEvent event) {
		fireDelta((IDebugTarget) event.getSource(), IModelDelta.CHANGED | IModelDelta.CONTENT | IModelDelta.STATE);
	}

	protected void handleSuspend(DebugEvent event) {
		fireDelta((IDebugTarget) event.getSource(), IModelDelta.CHANGED | IModelDelta.CONTENT);
	}

	protected void handleTerminate(DebugEvent event) {
		fireDelta((IDebugTarget) event.getSource(), IModelDelta.CHANGED | IModelDelta.STATE);
	}

	private void fireDelta(IDebugTarget target, int flags) {
		IModelDelta root = new ModelDeltaNode(DebugPlugin.getDefault().getLaunchManager(), IModelDelta.NOCHANGE);
		IModelDelta delta = root.addNode(target.getLaunch(), IModelDelta.NOCHANGE);
		delta.addNode(target, flags);
		fireDelta(root);
	}
	
}

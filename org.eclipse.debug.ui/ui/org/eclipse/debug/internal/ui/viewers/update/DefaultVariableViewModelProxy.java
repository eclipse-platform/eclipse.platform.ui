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
import org.eclipse.debug.core.model.IStackFrame;

/**
 * Default update for variables view.
 * 
 * @since 3.2
 */
public class DefaultVariableViewModelProxy extends EventHandlerModelProxy {

	/**
	 * Root model element for this update policy
	 */
	private IStackFrame fFrame;

	/**
	 * Constructs an update policy on the given target.
	 * 
	 * @param target
	 */
	public DefaultVariableViewModelProxy(IStackFrame frame) {
		super();
		fFrame = frame;
	}

	public void dispose() {
		fFrame = null;
	}

	protected DebugEventHandler[] createEventHandlers() {
		return new DebugEventHandler[] { new VariablesViewEventHandler(this, fFrame) };
	}

	protected boolean containsEvent(DebugEvent event) {
		Object source = event.getSource();
		if (source instanceof IDebugElement) {
			return fFrame.getDebugTarget().equals(((IDebugElement) source).getDebugTarget());
		}
		return false;
	}
}

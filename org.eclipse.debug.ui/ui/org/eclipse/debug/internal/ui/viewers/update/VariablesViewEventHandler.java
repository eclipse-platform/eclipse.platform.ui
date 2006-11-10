/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;

/**
 *
 * @since 3.2
 */
public class VariablesViewEventHandler extends DebugEventHandler {

	private IStackFrame fFrame;

	public VariablesViewEventHandler(AbstractModelProxy proxy, IStackFrame frame) {
		super(proxy);
		fFrame = frame;
	}

	protected boolean handlesEvent(DebugEvent event) {
		return true;
	}

	protected void refreshRoot(DebugEvent event) {
		if (event.getDetail() != DebugEvent.EVALUATION_IMPLICIT) {
			// Don't refresh everytime an implicit evaluation finishes
			if (event.getSource() instanceof ISuspendResume) {
				if (!((ISuspendResume)event.getSource()).isSuspended()) {
					// no longer suspended
					return;
				}
			}
			
			ModelDelta delta = new ModelDelta(fFrame, IModelDelta.CONTENT);
			fireDelta(delta);
		}
	}

}

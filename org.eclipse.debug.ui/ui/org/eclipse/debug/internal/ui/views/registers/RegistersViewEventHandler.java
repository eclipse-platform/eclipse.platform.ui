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

package org.eclipse.debug.internal.ui.views.registers;

import java.util.ArrayList;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewEventHandler;
import org.eclipse.debug.ui.AbstractDebugView;

public class RegistersViewEventHandler extends VariablesViewEventHandler {

	public RegistersViewEventHandler(AbstractDebugView view) {
		super(view);
	}

	protected DebugEvent[] filterEvents(DebugEvent[] events) {
		ArrayList filtered = null;
		for (int i=0; i<events.length; i++) {
			// filter out change events 
			if (events[i].getKind() == DebugEvent.CHANGE) {
				// IRegister is a subclass to IVariable, no need to check for that
				Object source = events[i].getSource();
				if (!(source instanceof IStackFrame ||
					  source instanceof IVariable ||
					  source instanceof IRegisterGroup ||
					  source instanceof IDebugTarget)) {
					if (events.length == 1) {
						return EMPTY_EVENT_SET;
					}
					if (filtered == null) {
						filtered = new ArrayList();
					}
					filtered.add(events[i]);
				}
			}
		}
		if (filtered == null) {
			return events;
		}
		if (filtered.size() == events.length) {
			return EMPTY_EVENT_SET;
		}
		ArrayList all = new ArrayList(events.length);
		for (int i = 0; i < events.length; i++) {
			all.add(events[i]);
		}
		all.removeAll(filtered);
		return (DebugEvent[]) all.toArray(new DebugEvent[all.size()]);		
	}

}

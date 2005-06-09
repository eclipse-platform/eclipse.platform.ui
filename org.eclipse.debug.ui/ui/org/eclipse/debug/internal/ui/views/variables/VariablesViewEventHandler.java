/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;


import java.util.ArrayList;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.internal.ui.views.RemoteTreeContentManager;
import org.eclipse.debug.ui.AbstractDebugView;

/**
 * Updates the variables view
 */
public class VariablesViewEventHandler extends AbstractDebugEventHandler {	
	
	private RemoteTreeContentManager fContentManager = null;
	
	/**
	 * Constructs a new event handler on the given view
	 * 
	 * @param view variables view
	 */
	public VariablesViewEventHandler(AbstractDebugView view) {
		super(view);
	}
	
	/**
	 * @see AbstractDebugEventHandler#handleDebugEvents(DebugEvent[])
	 */
	protected void doHandleDebugEvents(DebugEvent[] events, Object data) {
		for (int i = 0; i < events.length; i++) {	
			DebugEvent event = events[i];
			switch (event.getKind()) {
				case DebugEvent.SUSPEND:
						doHandleSuspendEvent(event);
					break;
				case DebugEvent.CHANGE:
						doHandleChangeEvent(event);
					break;
				case DebugEvent.RESUME:
						doHandleResumeEvent(event);
					break;
			}
		}
	}
	
	/**
	 * @see AbstractDebugEventHandler#updateForDebugEvents(DebugEvent[])
	 */
	protected void updateForDebugEvents(DebugEvent[] events, Object data) {
		for (int i = 0; i < events.length; i++) {	
			DebugEvent event = events[i];
			switch (event.getKind()) {
				case DebugEvent.TERMINATE:
					doHandleTerminateEvent(event);
					break;
			}
		}
	}	

	/**
	 * Clear cached variable expansion state
	 */
	protected void doHandleResumeEvent(DebugEvent event) {
		if (!event.isStepStart() && !event.isEvaluation()) {
			// clear variable expansion state
			getVariablesView().clearExpandedVariables(event.getSource());
		}
		if (!event.isEvaluation()) {
			Object input = getVariablesView().getVariablesViewer().getInput();
			if (input instanceof IStackFrame) {
				IStackFrame frame = (IStackFrame)input;
				if (event.getSource().equals(frame.getThread())) {
					fContentManager.cancel();
				}
			}
		}
	}

	/**
	 * Clear any cached variable expansion state for the
	 * terminated thread/target. Also, remove the part listener if there are
	 * no more active debug targets.
	 */
	protected void doHandleTerminateEvent(DebugEvent event) {
		getVariablesView().clearExpandedVariables(event.getSource());
	}
	
	/**
	 * Process a SUSPEND event
	 */
	protected void doHandleSuspendEvent(DebugEvent event) {
		if (event.getDetail() != DebugEvent.EVALUATION_IMPLICIT) {
			// Don't refresh everytime an implicit evaluation finishes
			if (event.getSource() instanceof ISuspendResume) {
				if (!((ISuspendResume)event.getSource()).isSuspended()) {
					// no longer suspended
					return;
				}
			}
			refresh();
			getVariablesView().populateDetailPane();
		}		
	}
	
	/**
	 * Process a CHANGE event
	 */
	protected void doHandleChangeEvent(DebugEvent event) {
		if (event.getDetail() == DebugEvent.STATE) {
			// only process variable state changes
			if (event.getSource() instanceof IVariable) {
				refresh(event.getSource());
				getVariablesView().populateDetailPane();
			}
		} else {
			if (!(event.getSource() instanceof IExpression)) {
				refresh();
				getVariablesView().populateDetailPane();
			}
		}	
	}	

	/**
	 * Returns the view that event handler updates.
	 */
	protected VariablesView getVariablesView() {
		return (VariablesView)getView();
	}
	
	/**
	 * Also update the details area.
	 * 
	 * @see org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler#viewBecomesVisible()
	 */
	protected void viewBecomesVisible() {
		super.viewBecomesVisible();
		getVariablesView().populateDetailPane();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler#filterEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	protected DebugEvent[] filterEvents(DebugEvent[] events) {
		ArrayList filtered = null;
		for (int i=0; i<events.length; i++) {
			if (isFiltered(events[i])) {
				if (events.length == 1) {
					return EMPTY_EVENT_SET;
				}
				if (filtered == null) {
					filtered = new ArrayList();
				}
				filtered.add(events[i]);
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
	
	public void setContentManager(RemoteTreeContentManager manager) { 
		fContentManager = manager;
	}

	protected boolean isFiltered(DebugEvent event) {
		if (event.getKind() == DebugEvent.CHANGE) {
			Object source = event.getSource();
			switch (event.getDetail()) {
				case DebugEvent.CONTENT:
					if (source instanceof IVariable ||
						source instanceof IStackFrame ||
						source instanceof IThread ||
						source instanceof IDebugTarget) {
						return false;
					}
					return true;
				case DebugEvent.STATE:
					if (source instanceof IVariable) {
						return false;
					}
					return true;
				default: // UNSPECIFIED
					return true;
			}
		}
		return false;
	}
}


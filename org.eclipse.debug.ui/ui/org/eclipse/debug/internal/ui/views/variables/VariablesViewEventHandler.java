/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;


import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.ui.AbstractDebugView;

/**
 * Updates the variables view
 */
public class VariablesViewEventHandler extends AbstractDebugEventHandler {	
	
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
			}
		} else {
			if (!(event.getSource() instanceof IExpression)) {
				refresh();
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

}


package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IVariable;
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
	protected void doHandleDebugEvents(DebugEvent[] events) {	
		for (int i = 0; i < events.length; i++) {	
			DebugEvent event = events[i];
			switch (event.getKind()) {
				case DebugEvent.SUSPEND:
					if (event.getDetail() != DebugEvent.EVALUATION_IMPLICIT) {
						// Don't refresh everytime an implicit evaluation finishes
						refresh();
						if (event.getDetail() == DebugEvent.STEP_END) {
							getVariablesView().populateDetailPane();
						}
						// return since we've done a complete refresh
						return;
					}
					break;
				case DebugEvent.CHANGE:
					if (event.getDetail() == DebugEvent.STATE) {
						// only process variable state changes
						if (event.getSource() instanceof IVariable) {
							refresh(event.getSource());
						}
					} else {
						refresh();
						// return since we've done a complete refresh
						return;
					}
					break;
			}
		}
	}


	protected VariablesView getVariablesView() {
		return (VariablesView)getView();
	}
}


package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.Viewer;

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
	 * @see BasicContentProvider#doHandleDebug(Event)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {		
		switch (event.getKind()) {
			case DebugEvent.SUSPEND:
				if (event.getDetail() != DebugEvent.EVALUATION_READ_ONLY) {
					refresh();
				}
				if (event.getDetail() == DebugEvent.STEP_END) {
					getVariablesView().populateDetailPane();
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
				}
				break;
		}
	}


	protected VariablesView getVariablesView() {
		return (VariablesView)getView();
	}
	
}


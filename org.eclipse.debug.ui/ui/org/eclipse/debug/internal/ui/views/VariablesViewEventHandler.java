package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.Viewer;

/**
 * Updates the variables view
 */
public class VariablesViewEventHandler extends AbstractDebugEventHandler {
	
	/**
	 * Constructs a new event handler on the given view and
	 * viewer
	 * 
	 * @param view variables view
	 * @param viewer tree viewer
	 */
	public VariablesViewEventHandler(AbstractDebugView view, Viewer viewer) {
		super(view, viewer);
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
				refresh();
				break;
		}
	}


	protected VariablesView getVariablesView() {
		return (VariablesView)getView();
	}
	
}


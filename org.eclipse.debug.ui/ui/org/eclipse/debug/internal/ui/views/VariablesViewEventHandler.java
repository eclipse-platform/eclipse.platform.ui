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
		// if the stack frame which is an input to this view
		// is no longer valid (i.e. its thread has resumed), do
		// not update (bug 6518)
		Object input = getViewer().getInput();
		if (input instanceof IStackFrame) {
			if (!((IStackFrame)input).isSuspended()) {
				return;
			}
		}
		
		switch (event.getKind()) {
			case DebugEvent.SUSPEND:
			case DebugEvent.CHANGE:
				refresh();

				// We have to be careful NOT to populate the detail pane in the
				// variables view on any CHANGE DebugEvent, since the very act of 
				// populating the detail pane does an evaluation, which queues up
				// a CHANGE DebugEvent, which would lead to an infinite loop.  It's
				// probably safer to add invidual event details here as needed,
				// rather than try to exclude the ones we think are problematic.
				if (event.getDetail() == DebugEvent.STEP_END) {
					getVariablesView().populateDetailPane();
				}
				break;
		}
	}


	protected VariablesView getVariablesView() {
		return (VariablesView)getView();
	}
	
}


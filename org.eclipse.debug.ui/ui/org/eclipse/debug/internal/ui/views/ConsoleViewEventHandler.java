package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.Viewer;

/**
 * Updates actions as the input to the view changes
 * state
 */
public class ConsoleViewEventHandler extends AbstractDebugEventHandler {

	/**
	 * Constructs an event handler for the given
	 * view and viewer.
	 * 
	 * @param view debug view
	 * @param viewer viewer
	 */
	public ConsoleViewEventHandler(AbstractDebugView view, Viewer viewer) {
		super(view, viewer);
	}
	
	/**
	 * @see AbstractDebugEventHandler#doHandleDebugEvent(DebugEvent)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		if (event.getSource().equals(getConsoleView().getProcess())) {
			getView().updateActions();
			((ConsoleView)getView()).updateTitle();
		}
	}
	
	/**
	 * Returns the console view
	 * 
	 * @return console view
	 */
	protected ConsoleView getConsoleView() {
		return (ConsoleView)getView();
	}

}

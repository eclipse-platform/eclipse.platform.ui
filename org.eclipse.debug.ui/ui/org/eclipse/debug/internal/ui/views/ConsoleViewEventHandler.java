package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.Viewer;

/**
 * Updates actions as the input to the view changes
 * state
 */
public class ConsoleViewEventHandler extends AbstractDebugEventHandler {

	/**
	 * Constructs an event handler for the given view.
	 * 
	 * @param view debug view
	 */
	public ConsoleViewEventHandler(AbstractDebugView view) {
		super(view);
	}
	
	/**
	 * @see AbstractDebugEventHandler#doHandleDebugEvents(DebugEvent[])
	 */
	protected void doHandleDebugEvents(DebugEvent[] events) {
		boolean update= false;
		
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			Object source= event.getSource();
			if (source == null) {
				continue;
			}
			IProcess process= getConsoleView().getProcess();
			if (source.equals(process)) {
				update= true;
				break;
			} else {
				if (process != null) {
					Object obj = process.getAdapter(IDebugTarget.class);
					if (source.equals(obj)) {
						update= true;	
						break;
					}
				}
			}
		}
		
		if (update) {
			getView().updateActions();
			getConsoleView().updateTitle();
		}
	}
	
	/**
	 * Returns the console view.
	 * 
	 * @return console view
	 */
	protected ConsoleView getConsoleView() {
		return (ConsoleView)getView();
	}
}

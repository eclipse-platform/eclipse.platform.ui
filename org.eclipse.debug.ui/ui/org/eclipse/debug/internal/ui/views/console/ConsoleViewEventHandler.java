package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.ui.AbstractDebugView;

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
			getView().updateObjects();
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

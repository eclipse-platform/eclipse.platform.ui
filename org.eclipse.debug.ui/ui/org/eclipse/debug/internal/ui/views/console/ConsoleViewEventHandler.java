/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;

 
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
			refresh();
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
	
	/**
	 * @see org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler#refresh()
	 */
	public void refresh() {
		if (isAvailable()) {
			getView().updateObjects();
			getConsoleView().updateTitle();
			IProcess process = getConsoleView().getProcess();
			((ConsoleViewer)getViewer()).setEditable(process != null && !process.isTerminated());			
		}
	}

}

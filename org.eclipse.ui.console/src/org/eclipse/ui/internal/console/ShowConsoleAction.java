/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleView;

/**
 * Shows a specific console in the console view
 */
public class ShowConsoleAction extends Action {
	
	private IConsole fConsole;
	private IConsoleView fView;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if (!fConsole.equals(fView.getConsole())) {
            boolean pinned = fView.isPinned();
            if (pinned) {
                fView.setPinned(false);
            }
		    fView.display(fConsole);
            if (pinned) {
               fView.setPinned(true); 
            }
		}
	}

	/**
	 * Constructs an action to display the given console.
	 * 
	 * @param view the console view in which the given console is contained
	 * @param console the console
	 */
	public ShowConsoleAction(IConsoleView view, IConsole console) {
		super(console.getName(), AS_RADIO_BUTTON);
		fConsole = console;
		fView = view;
		setImageDescriptor(console.getImageDescriptor());
	}
}

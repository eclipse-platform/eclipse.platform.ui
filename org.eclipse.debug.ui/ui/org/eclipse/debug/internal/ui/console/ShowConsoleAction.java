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
package org.eclipse.debug.internal.ui.console;

import org.eclipse.jface.action.Action;

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
		fView.display(fConsole);
	}

	/**
	 * Constructs an action to display the given console.
	 * 
	 * @param view the console view in which the given console is contained
	 * @param console the console
	 */
	public ShowConsoleAction(IConsoleView view, IConsole console) {
		super();
		fConsole = console;
		fView = view;
		setText(console.getName());
		setImageDescriptor(console.getImageDescriptor());
	}
	
	

}

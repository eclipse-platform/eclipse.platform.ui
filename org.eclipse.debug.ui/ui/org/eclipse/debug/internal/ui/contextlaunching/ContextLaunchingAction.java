/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contextlaunching;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;

/**
 * A universal context launching action for popup menus. 
 * This action gets its immediate context from what was right-clicked
 * on to present the action.
 * 
 * @see {@link ContextRunner}
 * @see {@link IActionDelegate2}
 * @see {@link RunContextLaunchingAction}
 * @see {@link DebugContextLaunchingAction}
 * @see {@link ProfileContextLaunchingAction}
 * 
 * @since 3.3
 * EXPERIMENTAL
 * CONTEXTLAUNCHING
 */
public class ContextLaunchingAction implements IActionDelegate2 {

	/**
	 * the mode the action is created on
	 */
	private String fMode = null;
	
	/**
	 * Constructor
	 * @param mode
	 */
	public ContextLaunchingAction(String mode) {
		fMode = mode;
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {}

	/**
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {}

	/**
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		ContextRunner.getDefault().launch(fMode);
	}
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		//not called
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {}
}

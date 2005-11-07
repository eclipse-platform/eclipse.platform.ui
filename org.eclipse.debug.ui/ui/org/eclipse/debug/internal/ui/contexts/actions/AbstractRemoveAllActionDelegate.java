/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts.actions;

 
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;

public abstract class AbstractRemoveAllActionDelegate extends AbstractDebugContextActionDelegate {

	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) {
		doAction();
		update();
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action){
		doAction(null);
	}
	
	protected abstract void doAction();
	
	protected void update(IAction action, ISelection s) {
		update();
	}
	
	protected abstract void update();
	
	/**
	 * Remove all actions do care nothing about the current selection
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection s) {
		if (!isInitialized()) {
			action.setEnabled(false);
			setAction(action);
			setInitialized(true);
		}
		update();
	}
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		setView(view);
		setWindow(view.getViewSite().getWorkbenchWindow());
	}
}

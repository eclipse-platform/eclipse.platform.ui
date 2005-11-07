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

import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public abstract class SelectAllAction extends AbstractDebugContextActionDelegate {


	protected boolean initialize(IAction action, ISelection selection) {
		if (!isInitialized()) {
			IDebugView debugView =
				(IDebugView) getView().getAdapter(IDebugView.class);
			if (debugView != null) {
				debugView.setAction(getActionId(), action);
			}
			return super.initialize(action, selection);
		}
		return false;
	}

	protected abstract String getActionId();
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action){
		doAction(null);
	}

	protected void doAction(Object element) {
		if (!(getView() instanceof IDebugView)) {
			return;
		}
		Viewer viewer = ((IDebugView) getView()).getViewer();
		if (!(viewer instanceof TreeViewer)) {
			return;
		}
		((TreeViewer) viewer).getTree().selectAll();
		//ensure that the selection change callback is fired
		viewer.setSelection(viewer.getSelection());
	}
	
	protected abstract void update();
	
	protected void update(IAction action, ISelection selection) {
		update();
	}
}

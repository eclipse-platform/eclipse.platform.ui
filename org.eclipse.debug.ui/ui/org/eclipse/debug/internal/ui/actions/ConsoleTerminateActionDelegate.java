package org.eclipse.debug.internal.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.views.console.ConsoleView;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.texteditor.IUpdate;
 
/**
 * Terminate action for the console. Terminates the process
 * currently being displayed in the console.
 */
public class ConsoleTerminateActionDelegate extends TerminateActionDelegate implements IUpdate {

	/**
	 * Returns a selection with the console view's
	 * current process, or an empty selection.
	 * 
	 * @return structured selection
	 */	
	protected IStructuredSelection getSelection() {
		IViewPart view = getView();
		if (view instanceof ConsoleView) {
			IProcess process = ((ConsoleView)view).getProcess();
			if (process != null) {
				return new StructuredSelection(process);
			}
		}
		return StructuredSelection.EMPTY;
	}
	
	/**
	 * @see AbstractDebugActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		super.init(view);
		IDebugView debugView= (IDebugView)view.getAdapter(IDebugView.class);
		if (debugView != null) {
			debugView.add(this);
		}
	}
	
	/**
	 * @see AbstractDebugActionDelegate#dispose()
	 */
	public void dispose() {
		IViewPart view= getView();
		IDebugView debugView= (IDebugView)view.getAdapter(IDebugView.class);
		if (debugView != null) {
			debugView.remove(this);
		}
		super.dispose();
	}
	
	/**
	 * @see IUpdate#update()
	 */
	public void update() {
		if (getAction() != null) {
			update(getAction(), null);
		}
	}
	
	/**
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#update(IAction, ISelection)
	 */
	protected void update(IAction action, ISelection s) {
		//only update on the current process associated with the
		//console view
		s= getSelection();
		super.update(action, s);
	}
}

package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
 
public class DebugHistoryMenuAction extends DebugDropDownAction implements IMenuCreator {
	public DebugHistoryMenuAction() {
		super(null);
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (action instanceof Action) {
			((Action)action).setMenuCreator(this);
		} else {
			action.setEnabled(false);
		}
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		//do nothing as the action strictly generates the history sub menu
	}
	
	/**
	 * @see IActionDelegateWithEvent#runWithEvent(IAction, Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		//do nothing as the action strictly generates the history sub menu
	}
}

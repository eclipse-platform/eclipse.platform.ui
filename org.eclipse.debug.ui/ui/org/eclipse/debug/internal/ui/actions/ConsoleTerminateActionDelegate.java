package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.views.ConsoleView;
import org.eclipse.jface.action.IAction;
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
	 * The presentation for this action delegate
	 */
	private IAction fPresentationAction;

	/**
	 * Returns a selection with the console view's
	 * current process, or <code>null</code>
	 * 
	 * @return structured selection, or <code>null</code>
	 */	
	protected IStructuredSelection getSelection() {
		IViewPart view = getView();
		if (view instanceof ConsoleView) {
			IProcess process = ((ConsoleView)view).getProcess();
			if (process != null) {
				return new StructuredSelection(process);
			}
		}
		return null;
	}
	
	/**
	 * @see IUpdate#update()
	 */
	public void update() {
		if (getPresentationAction() != null) {
			update(getPresentationAction(), getSelection());
		}
	}

	/**
	 * @see ControlActionDelegate#initialize(IAction)
	 */
	protected void initialize(IAction action) {
		super.initialize(action);
		fPresentationAction = action;
	}
	
	/**
	 * @see ControlActionDelegate#initializeForOwner(ControlAction)
	 */
	public void initializeForOwner(ControlAction action) {
		super.initializeForOwner(action);
		fPresentationAction = action;
	}	
	
	/**
	 * Returns the action that handles the presentation
	 * for this delegate
	 * 
	 * @return action
	 */
	protected IAction getPresentationAction() {
		return fPresentationAction;
	}
}

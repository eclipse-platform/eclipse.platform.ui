package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.views.ConsoleView;
import org.eclipse.debug.internal.ui.views.DebugSelectionManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
 
/**
 * Terminate action for the console. Terminates the process
 * currently being displayed in the console.
 */
public class ConsoleTerminateActionDelegate extends TerminateActionDelegate {

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
	
	public void init(IViewPart view) {
		super.init(view);
		//listen to selections in the launch view
		DebugSelectionManager.getDefault().addSelectionChangedListener(this, getWindow(), IDebugUIConstants.ID_DEBUG_VIEW);
	}
	
	public void dispose() {
		super.dispose();
		DebugSelectionManager.getDefault().removeSelectionChangedListener(this, getWindow(), IDebugUIConstants.ID_DEBUG_VIEW);	
	}
	
	protected void update(IAction action, ISelection s) {
		super.update(action, getSelection());
	}
}

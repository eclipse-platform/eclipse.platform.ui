package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.views.ConsoleView;
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
	 * @see IUpdate#update()
	 */
	public void update() {
		if (getAction() != null) {
			update(getAction(), getSelection());
		}
	}
}

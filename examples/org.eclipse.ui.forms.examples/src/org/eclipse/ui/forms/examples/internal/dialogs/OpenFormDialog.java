package org.eclipse.ui.forms.examples.internal.dialogs;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class OpenFormDialog implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		SampleFormDialog dialog = new SampleFormDialog(window.getShell());
		dialog.create();
		dialog.getShell().setText("Sample Form Dialog");
		dialog.getShell().setSize(500,500);
		dialog.getShell().setLocation(100, 300);
		dialog.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
}

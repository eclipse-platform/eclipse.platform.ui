package org.eclipse.help.ui.internal.search;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class SearchIndexAction implements IObjectActionDelegate {
	private ISelection selection;

	public SearchIndexAction() {
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		if (obj==null) return;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
}
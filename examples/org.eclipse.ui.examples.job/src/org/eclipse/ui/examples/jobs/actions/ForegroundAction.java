package org.eclipse.ui.examples.jobs.actions;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ForegroundAction implements IWorkbenchWindowActionDelegate {
	public void run(IAction action) {
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					//no-op
				}
			}, null);
		} catch (OperationCanceledException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	public void selectionChanged(IAction action, ISelection selection) {
		//do nothing
	}
	public void dispose() {
		//do nothing
	}
	public void init(IWorkbenchWindow window) {
		//do nothing
	}
}
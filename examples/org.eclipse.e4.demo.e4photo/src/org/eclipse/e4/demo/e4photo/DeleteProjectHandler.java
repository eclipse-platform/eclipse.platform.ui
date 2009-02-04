package org.eclipse.e4.demo.e4photo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.workbench.ui.IExceptionHandler;

public class DeleteProjectHandler {

//	public boolean canExecute(ISelectionService service) {
//		IProject project = (IProject) service.getSelection(IProject.class);
//		return project != null && project.exists();
//	}

	public void execute(IProject project, IProgressMonitor monitor,
			IExceptionHandler exceptionHandler) {
		try {
			if (project == null) {
				return;
			}
			project.delete(true, monitor);
		} catch (CoreException e) {
			exceptionHandler.handleException(e);
		}
	}
}

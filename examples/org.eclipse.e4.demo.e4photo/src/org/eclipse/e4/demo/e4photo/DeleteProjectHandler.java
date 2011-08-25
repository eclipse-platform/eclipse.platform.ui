/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4photo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.IExceptionHandler;

public class DeleteProjectHandler {

//	@CanExecute
//	public boolean canExecute(ISelectionService service) {
//		IProject project = (IProject) service.getSelection(IProject.class);
//		return project != null && project.exists();
//	}

	@Execute
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

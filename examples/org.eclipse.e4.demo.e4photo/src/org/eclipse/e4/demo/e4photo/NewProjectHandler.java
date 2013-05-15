/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
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
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Execute;

public class NewProjectHandler {

	private int counter;

	@Execute
	public void execute(IWorkspace workspace, IProgressMonitor monitor) {

		String projectName = "Album " + (++counter);
		final IProject project = workspace.getRoot().getProject(projectName);
		final IProjectDescription pd = workspace
				.newProjectDescription(projectName);
		try {
			workspace.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					if (!project.exists()) {
						project.create(pd, monitor);
					}
					if (!project.isOpen()) {
						project.open(monitor);
						System.out.println("Created at: " + project.getLocation());
					}
				}
			}, monitor);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}

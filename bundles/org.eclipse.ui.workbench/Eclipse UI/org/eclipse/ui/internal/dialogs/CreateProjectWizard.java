/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Internal workbench wizard to create a project
 * resource in the workspace. This project will have
 * no capabilities when created. This wizard is intended
 * to be used by the CreateProjectStep class only.
 */
public class CreateProjectWizard extends Wizard {
	private NewProjectWizard wizard;
	private WizardNewProjectNameAndLocationPage page;

	/**
	 * Creates an empty wizard for creating a new project
	 * in the workspace.
	 */
	/* package */ CreateProjectWizard(WizardNewProjectNameAndLocationPage page, NewProjectWizard wizard) {
		super();
		this.page = page;
		this.wizard = wizard;
	}

	/**
	 * Creates a new project resource with the entered name.
	 *
	 * @return the created project resource, or <code>null</code> if the project
	 *    was not created
	 */
	private IProject createNewProject() {
		// get a project handle
		final IProject newProjectHandle = page.getProjectHandle();
	
		// get a project descriptor
		IPath defaultPath = Platform.getLocation();
		IPath newPath = page.getLocationPath();
		if (defaultPath.equals(newPath))
			newPath = null;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
		description.setLocation(newPath);
	
		// define the operation to create a new project
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				createProject(description, newProjectHandle, monitor);
			}
		};
	
		// run the operation to create a new project
		try {
			getContainer().run(true, true, op);
		}
		catch (InterruptedException e) {
			return null;
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				if (((CoreException)t).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
					MessageDialog.openError(
						getShell(), 
						WorkbenchMessages.getString("CreateProjectWizard.errorTitle"),  //$NON-NLS-1$
						WorkbenchMessages.getString("CreateProjectWizard.caseVariantExistsError")  //$NON-NLS-1$,
						);	
				} else {
					ErrorDialog.openError(
						getShell(), 
						WorkbenchMessages.getString("CreateProjectWizard.errorTitle"),  //$NON-NLS-1$
						null, // no special message
				 		((CoreException) t).getStatus());
				}
			} else {
				// Unexpected runtime exceptions and errors may still occur.
				Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog().log(
					new Status(
						Status.ERROR, 
						PlatformUI.PLUGIN_ID, 
						0, 
						t.toString(),
						t));
				MessageDialog.openError(
					getShell(),
					WorkbenchMessages.getString("CreateProjectWizard.errorTitle"),  //$NON-NLS-1$
					WorkbenchMessages.format("CreateProjectWizard.internalError", new Object[] {t.getMessage()})); //$NON-NLS-1$
			}
			return null;
		}
	
		return newProjectHandle;
	}
	
	/**
	 * Creates a project resource given the project handle and description.
	 *
	 * @param description the project description to create a project resource for
	 * @param projectHandle the project handle to create a project resource for
	 * @param monitor the progress monitor to show visual progress with
	 *
	 * @exception CoreException if the operation fails
	 * @exception OperationCanceledException if the operation is canceled
	 */
	private void createProject(IProjectDescription description, IProject projectHandle, IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		try {
			monitor.beginTask("", 2000); //$NON-NLS-1$
	
			projectHandle.create(description, new SubProgressMonitor(monitor,1000));
	
			if (monitor.isCanceled())
				throw new OperationCanceledException();
	
			projectHandle.open(new SubProgressMonitor(monitor,1000));
	
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns the current project name.
	 *
	 * @return the project name or <code>null</code>
	 *   if no project name is known
	 */
	/* package */ String getProjectName() {
		return page.getProjectName();
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		if (wizard.getNewProject() != null)
			return true;
			
		IProject project = createNewProject();
		if (project != null) {
			wizard.setNewProject(project);
			return true;
		} else {
			return false;
		}
	}
}

package org.eclipse.ui.internal.dialogs;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Internal workbench wizard to remove capabilities
 * from a project. This wizard is intended to be used
 * by the RemoveCapabilitiesStep class only.
 */
public class RemoveCapabilitiesWizard extends Wizard {
	private IProject project;
	private String[] natureIds;

	/**
	 * Creates an empty wizard for removing capabilities
	 * from a project.
	 * 
	 * @param project the project to remove the capabilities from
	 * @param natureIds the list of nature ids to keep on the project
	 */
	/* package */ RemoveCapabilitiesWizard(IProject project, String[] natureIds) {
		super();
		this.natureIds = natureIds;
		this.project = project;
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		return updateNatures();
	}

	/**
	 * Update the project natures
	 */
	private boolean updateNatures() {
		// define the operation to update natures
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				try {
					IProjectDescription description = project.getDescription();
					description.setNatureIds(natureIds);
					project.setDescription(description, monitor);
				} finally {
					monitor.done();
				}
			}
		};
	
		// run the update nature operation
		try {
			getContainer().run(true, true, op);
		}
		catch (InterruptedException e) {
			return false;
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				ErrorDialog.openError(
					getShell(), 
					WorkbenchMessages.getString("RemoveCapabilitiesWizard.errorMessage"),  //$NON-NLS-1$
					null, // no special message
			 		((CoreException) t).getStatus());
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
					WorkbenchMessages.getString("RemoveCapabilitiesWizard.errorMessage"),  //$NON-NLS-1$
					WorkbenchMessages.format("RemoveCapabilitiesWizard.internalError", new Object[] {t.getMessage()})); //$NON-NLS-1$
			}
			return false;
		}
	
		return true;
	}
}

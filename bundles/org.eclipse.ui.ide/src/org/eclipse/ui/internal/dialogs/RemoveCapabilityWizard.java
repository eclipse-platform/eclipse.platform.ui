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
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.ICapabilityUninstallWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Internal workbench wizard to remove a capability
 * from a project. Also removes prerequisite natures
 * as specified in the <code>init</code> method.
 * <p>
 * This wizard is intended to be used by the
 * <code>RemoveCapabilityStep</code> class only.
 * </p>
 */
public class RemoveCapabilityWizard extends Wizard implements ICapabilityUninstallWizard {
	private IWorkbench workbench;
	private IProject project;
	private String[] natureIds;

	/**
	 * Creates an empty wizard for removing a capability
	 * from a project.
	 */
	/* package */ RemoveCapabilityWizard() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on ICapabilityUninstallWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection, IProject project, String[] natureIds) {
		this.workbench = workbench;
		this.project = project;
		this.natureIds = natureIds;
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
					String[] oldIds = description.getNatureIds();
					ArrayList newIds = new ArrayList(oldIds.length);
					for (int i = 0; i < oldIds.length; i++) {
						boolean keepNature = true;
						for (int j = 0; j < natureIds.length; j++) {
							if (natureIds[j].equals(oldIds[i])) {
								keepNature = false;
								break;
							}
						}
						if (keepNature)
							newIds.add(oldIds[i]);
					}
					String[] results = new String[newIds.size()];
					newIds.toArray(results);
					description.setNatureIds(results);
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
					WorkbenchMessages.getString("RemoveCapabilityWizard.errorMessage"),  //$NON-NLS-1$
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
					WorkbenchMessages.getString("RemoveCapabilityWizard.errorMessage"),  //$NON-NLS-1$
					WorkbenchMessages.format("RemoveCapabilityWizard.internalError", new Object[] {t.getMessage()})); //$NON-NLS-1$
			}
			return false;
		}
	
		return true;
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.xml.sax.SAXException;

public class ProjectSetImportWizard extends Wizard implements IImportWizard {
	ImportProjectSetMainPage mainPage;

	public ProjectSetImportWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(TeamUIMessages.ProjectSetImportWizard_Project_Set_1); 
	}
	
	public void addPages() {
		mainPage = new ImportProjectSetMainPage("projectSetMainPage", TeamUIMessages.ProjectSetImportWizard_Import_a_Project_Set_3, TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_PROJECTSET_IMPORT_BANNER)); //$NON-NLS-1$ 
		addPage(mainPage);
	}

	public boolean performFinish() {
		// check if the desired working set exists
		final String workingSetName = mainPage.getWorkingSetName();
		
		final boolean[] result = new boolean[] {false};
		try {
			getContainer().run(true, true, new WorkspaceModifyOperation(null) {
				public void execute(IProgressMonitor monitor) throws InvocationTargetException {
					String psfFile = mainPage.getFileName();
					PsfFilenameStore.remember(psfFile);
					IProject[] newProjects= ProjectSetImporter.importProjectSet(psfFile, getShell(), monitor);
					if (workingSetName != null)
						createWorkingSet(workingSetName, newProjects);
					result[0] = true;
				}
			});
		} catch (InterruptedException e) {
			return true;
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof TeamException) {
				ErrorDialog.openError(getShell(), null, null, ((TeamException)target).getStatus());
				return false;
			}
			if (target instanceof RuntimeException) {
				throw (RuntimeException)target;
			}
			if (target instanceof Error) {
				throw (Error)target;
			}
			if (target instanceof SAXException) {
			    ErrorDialog.openError(getShell(), null, null, new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, NLS.bind(TeamUIMessages.ProjectSetImportWizard_2, new String[] { target.getMessage() }), target)); 
			    return false;
			}
			ErrorDialog.openError(getShell(), null, null, new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, NLS.bind(TeamUIMessages.ProjectSetImportWizard_3, new String[] { target.getMessage() }), target)); 
		}
		return result[0];
	}
		
	/* private */ void createWorkingSet(String workingSetName, IProject[] projects) {
		IWorkingSetManager manager = TeamUIPlugin.getPlugin().getWorkbench().getWorkingSetManager();
		IWorkingSet oldSet = manager.getWorkingSet(workingSetName);
		if (oldSet == null) {
			IWorkingSet newSet = manager.createWorkingSet(workingSetName, projects);
			manager.addWorkingSet(newSet);
		}else {
			//don't overwrite the old elements
			IAdaptable[] tempElements = oldSet.getElements();
			IAdaptable[] finalElementList = new IAdaptable[tempElements.length + projects.length];
			System.arraycopy(tempElements, 0, finalElementList, 0, tempElements.length);
			IAdaptable[] adaptedProjects = oldSet.adaptElements(projects);
			System.arraycopy(adaptedProjects, 0,finalElementList, tempElements.length, adaptedProjects.length);
			oldSet.setElements(finalElementList);
		}	
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// The code that finds "selection" is broken (it is always empty), so we
		// must dig for the selection in the workbench.
		PsfFilenameStore.setDefaultFromSelection(workbench);
	}
}

/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * @since 3.3
 *
 */
public class OpenFolderAsProjectAction extends Action {

	private final IFolder folder;
	private final CommonViewer viewer;

	/**
	 * @param folder
	 * @param viewer
	 */
	public OpenFolderAsProjectAction(IFolder folder, CommonViewer viewer) {
		super(WorkbenchNavigatorMessages.OpenProjectAction_OpenExistingProject);
		this.folder = folder;
		this.viewer = viewer;
		setDescription(WorkbenchNavigatorMessages.OpenProjectAction_OpenExistingProject_desc);
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(SharedImages.IMG_OBJ_PROJECT));
	}

	@Override
	public void run() {
		Job job = Job.create(NLS.bind(WorkbenchNavigatorMessages.OpenProjectAction_opening, folder.getName()), monitor -> {
			IProject parentProject = folder.getProject();
			Set<IWorkingSet> parentWorkingSets = new HashSet<>();
			IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
			for (IWorkingSet workingSet : workingSetManager.getWorkingSets()) {
				for (IAdaptable element : workingSet.getElements()) {
					if (parentProject.equals(Adapters.adapt(element, IProject.class))) {
						parentWorkingSets.add(workingSet);
						break;
					}
				}
			}
			try {
				IProjectDescription desc = ResourcesPlugin.getWorkspace()
						.loadProjectDescription(folder.getLocation().append(IProjectDescription.DESCRIPTION_FILE_NAME));
				desc.setLocation(folder.getLocation());
				CreateProjectOperation operation = new CreateProjectOperation(desc, desc.getName());
				IStatus status = OperationHistoryFactory.getOperationHistory().execute(operation, null, null);
				if (status.isOK()) {
					IProject newProject = (IProject) operation.getAffectedObjects()[0];
					workingSetManager.addToWorkingSets(newProject,
							parentWorkingSets.toArray(new IWorkingSet[parentWorkingSets.size()]));
					viewer.getTree().getDisplay().asyncExec(() -> {
						viewer.refresh(folder.getParent());
						viewer.setSelection(new StructuredSelection(newProject));
					});
					return Status.OK_STATUS;
				}
				return status;
			} catch (ExecutionException ex) {
				return Status.error(ex.getMessage(), ex);
			} catch (CoreException ex) {
				return ex.getStatus();
			}
		});
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}
}

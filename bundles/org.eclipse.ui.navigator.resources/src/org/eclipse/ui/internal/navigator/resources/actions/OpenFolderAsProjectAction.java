/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.actions;

import java.io.IOException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.internal.resources.ProjectDescriptionReader;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * @since 3.3
 *
 */
public class OpenFolderAsProjectAction extends Action {

	private IFolder folder;
	private CommonViewer viewer;

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
		try {
			IProjectDescription desc = new ProjectDescriptionReader().read(folder.getLocation().append(IProjectDescription.DESCRIPTION_FILE_NAME));
			desc.setLocation(folder.getLocation());
			CreateProjectOperation operation = new CreateProjectOperation(desc, desc.getName());
			IStatus status = OperationHistoryFactory.getOperationHistory().execute(operation, null, null);
			if (status.isOK()) {
				viewer.setSelection(new StructuredSelection(operation.getAffectedObjects()));
			} else {
				WorkbenchNavigatorPlugin.getDefault().getLog().log(status);
			}
		} catch (IOException e) {
			WorkbenchNavigatorPlugin
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.getDefault().getBundle().getSymbolicName(),
							"Failed to import " + folder.getName(), e)); //$NON-NLS-1$
		} catch (ExecutionException e) {
			WorkbenchNavigatorPlugin
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.getDefault().getBundle().getSymbolicName(),
							"Failed to import " + folder.getName(), e)); //$NON-NLS-1$
		}
	}
}

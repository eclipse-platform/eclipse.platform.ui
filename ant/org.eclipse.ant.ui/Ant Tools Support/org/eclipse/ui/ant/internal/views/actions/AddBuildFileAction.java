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
package org.eclipse.ui.ant.internal.views.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ant.ui.internal.views.AntView;
import org.eclipse.ant.ui.internal.views.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.externaltools.internal.ui.FileSelectionDialog;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action that prompts the user for a build file and adds the selected file to
 * an <code>AntView</code>
 */
public class AddBuildFileAction extends Action {

	private AntView view;

	public AddBuildFileAction(AntView view) {
		super(AntViewActionMessages.getString("AddBuildFileAction.Add_Build_File_1"), ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_ADD)); //$NON-NLS-1$
		this.view= view;
		setToolTipText(AntViewActionMessages.getString("AddBuildFileAction.Add_Build_File_1")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IExternalToolsHelpContextIds.ADD_BUILDFILE_ACTION);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		FileSelectionDialog dialog = new FileSelectionDialog(Display.getCurrent().getActiveShell(), ResourcesPlugin.getWorkspace().getRoot(), AntViewActionMessages.getString("AddBuildFileAction.&Select")); //$NON-NLS-1$
		dialog.setFileFilter("*.xml", true); //$NON-NLS-1$
		dialog.open();
		IFile file= dialog.getResult();
		if (file == null) {
			return;
		}
		String buildFileName= file.getFullPath().toString();
		ProjectNode[] existingProjects= view.getProjects();
		for (int j = 0; j < existingProjects.length; j++) {
			ProjectNode existingProject = existingProjects[j];
			if (existingProject.getBuildFileName().equals(buildFileName)) {
				// Don't parse projects that have already been added.
				return;
			}
		}
		ProjectNode project= new ProjectNode(buildFileName);
		view.addProject(project);
	}

}

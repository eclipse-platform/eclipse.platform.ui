/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.view.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.*;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;

/**
 * Action that prompts the user for a build file and adds the selected file to
 * an <code>AntView</code>
 */
public class AddBuildFileAction extends Action {

	private AntView view;

	public AddBuildFileAction(AntView view) {
		super("Add Build File", ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_ADD));
		this.view= view;
		setToolTipText("Add build file");
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		FileSelectionDialog dialog = new FileSelectionDialog(Display.getCurrent().getActiveShell(), ResourcesPlugin.getWorkspace().getRoot(), "&Select a build file:");
		dialog.setFileFilter("*.xml", true); //$NON-NLS-1$
		dialog.open();
		IFile file= dialog.getResult();
		if (file == null) {
			return;
		}
		ProjectNode project= new ProjectNode(file.getLocation().toString());
		view.addProject(project);
	}

}

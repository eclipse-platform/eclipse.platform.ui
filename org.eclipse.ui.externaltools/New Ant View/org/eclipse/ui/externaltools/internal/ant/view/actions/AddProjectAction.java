/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.view.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;

/**
 * Action that prompts the user for a build file and adds the selected file to
 * an <code>AntView</code>
 */
public class AddProjectAction extends Action {

	private AntView view;

	public AddProjectAction(AntView view) {
		super("Add Build File", ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_ADD));
		this.view= view;
		setToolTipText("Add build file");
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		ResourceSelectionDialog dialog;
		dialog = new ResourceSelectionDialog(Display.getCurrent().getActiveShell(), ResourcesPlugin.getWorkspace().getRoot(), "Select a build file");
		dialog.open();
		Object[] results = dialog.getResult();
		if (results == null || results.length < 1) {
			return;
		}
		IResource resource = (IResource)results[0];
		if (resource.getType() != IResource.FILE) {
			return;
		}
		view.addBuildFile(((IFile)resource).getLocation().toString());

	}

}

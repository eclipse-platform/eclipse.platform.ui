/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.views.actions;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.ant.internal.ui.model.AntUIImages;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.ant.internal.ui.model.IAntUIHelpContextIds;
import org.eclipse.ant.internal.ui.views.AntView;
import org.eclipse.ant.internal.ui.views.elements.ProjectNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.ui.FileSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action that prompts the user for build files and adds the selected files to
 * an <code>AntView</code>
 */
public class AddBuildFilesAction extends Action {

	private AntView view;

	public AddBuildFilesAction(AntView view) {
		super(AntViewActionMessages.getString("AddBuildFilesAction.1"), AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ADD)); //$NON-NLS-1$
		this.view= view;
		setToolTipText(AntViewActionMessages.getString("AddBuildFilesAction.0")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IAntUIHelpContextIds.ADD_BUILDFILE_ACTION);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		FileSelectionDialog dialog = new FileSelectionDialog(Display.getCurrent().getActiveShell(), ResourcesPlugin.getWorkspace().getRoot(), AntViewActionMessages.getString("AddBuildFilesAction.2")); //$NON-NLS-1$
		dialog.setFileFilter("*.xml", true); //$NON-NLS-1$
		dialog.setAllowMultiselection(true);
		dialog.open();
		final IStructuredSelection result = dialog.getResult();
		if (result == null) {
			return;
		}

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					monitor.beginTask(AntViewActionMessages.getString("AddBuildFilesAction.3"), result.size()); //$NON-NLS-1$
					Object[] files= result.toArray();
					files: for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
						Object file = files[i];
						if (file instanceof IFile) {
							String buildFileName= ((IFile)file).getFullPath().toString();
							ProjectNode[] existingProjects= view.getProjects();
							for (int j = 0; j < existingProjects.length; j++) {
								ProjectNode existingProject = existingProjects[j];
								if (existingProject.getBuildFileName().equals(buildFileName)) {
									// Don't parse projects that have already been added.
									monitor.worked(1);
									continue files;
								}
							}
							final ProjectNode project= new ProjectNode(buildFileName);
							project.getName();
							monitor.worked(1);
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									view.addProject(project);
								}
							});
						}
					}
				}
			});
			
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			}
	}
}
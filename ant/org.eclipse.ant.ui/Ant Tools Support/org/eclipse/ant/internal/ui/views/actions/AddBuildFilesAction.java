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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.IAntUIHelpContextIds;
import org.eclipse.ant.internal.ui.model.AntProjectNode;
import org.eclipse.ant.internal.ui.model.AntProjectNodeProxy;
import org.eclipse.ant.internal.ui.preferences.FileSelectionDialog;
import org.eclipse.ant.internal.ui.views.AntView;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
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
		String title= AntViewActionMessages.getString("AddBuildFilesAction.2"); //$NON-NLS-1$
		String message= AntViewActionMessages.getString("AddBuildFilesAction.4"); //$NON-NLS-1$
		String filterExtension= "xml"; //$NON-NLS-1$
		String filterMessage= AntViewActionMessages.getString("AddBuildFilesAction.5"); //$NON-NLS-1$
		
		FileSelectionDialog dialog = new FileSelectionDialog(Display.getCurrent().getActiveShell(), getBuildFiles(), title, message, filterExtension, filterMessage);
		dialog.open();
		final Object[] result= dialog.getResult();
		if (result == null) {
			return;
		}

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					monitor.beginTask(AntViewActionMessages.getString("AddBuildFilesAction.3"), result.length); //$NON-NLS-1$
					for (int i = 0; i < result.length && !monitor.isCanceled(); i++) {
						Object file = result[i];
						if (file instanceof IFile) {
							String buildFileName= ((IFile)file).getFullPath().toString();
							final AntProjectNode project= new AntProjectNodeProxy(buildFileName);
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

	private List getBuildFiles() {
		AntProjectNode[] existingProjects= view.getProjects();
		List buildFiles= new ArrayList(existingProjects.length);
		for (int j = 0; j < existingProjects.length; j++) {
			AntProjectNode existingProject = existingProjects[j];
			buildFiles.add(AntUtil.getFile(existingProject.getBuildFileName()));
		}
		return buildFiles;
	}
}
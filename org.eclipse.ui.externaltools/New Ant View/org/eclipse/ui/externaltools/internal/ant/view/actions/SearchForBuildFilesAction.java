package org.eclipse.ui.externaltools.internal.ant.view.actions;
/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;

/**
 * This action opens a dialog to search for build files and adds the resulting
 * projects to the ant view.
 */
public class SearchForBuildFilesAction extends Action {
	private AntView view;
	
	public SearchForBuildFilesAction(AntView view) {
		super("Search", ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_SEARCH));
		setToolTipText("Add build files with search");
		this.view= view;
	}
	
	/**
	 * Opens the <code>SearchForBuildFilesDialog</code> and adds the results to
	 * the ant view.
	 */
	public void run() {
		SearchForBuildFilesDialog dialog= new SearchForBuildFilesDialog();
		if (dialog.open() != Dialog.CANCEL) {
			final IFile[] files= dialog.getResults();
			final boolean includeErrorNodes= dialog.getIncludeErrorResults();
			final ProgressMonitorDialog progressDialog= new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
			try {
				progressDialog.run(false, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Processing search results", files.length);
						for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
							String fileName= files[i].getLocation().toString();
							monitor.subTask(MessageFormat.format("Adding {0}", new String[] {fileName}));
							ProjectNode project= new ProjectNode(fileName);
							// Force the project to be parsed so the error state is set.
							project.getName();
							monitor.worked(1);
							if (includeErrorNodes || !(project.isErrorNode())) {
								view.addProject(project);
							}
						}
					}
				});
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			}
		}
	}

}

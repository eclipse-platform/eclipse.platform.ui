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
package org.eclipse.ui.externaltools.internal.ant.view.actions;
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
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This action opens a dialog to search for build files and adds the resulting
 * projects to the ant view.
 */
public class SearchForBuildFilesAction extends Action {
	private AntView view;
	
	public SearchForBuildFilesAction(AntView view) {
		super(AntViewActionMessages.getString("SearchForBuildFilesAction.Search_1"), ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_SEARCH)); //$NON-NLS-1$
		setToolTipText(AntViewActionMessages.getString("SearchForBuildFilesAction.Add_build_files_with_search_2")); //$NON-NLS-1$
		this.view= view;
		WorkbenchHelp.setHelp(this, IExternalToolsHelpContextIds.SEARCH_FOR_BUILDFILES_ACTION);
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
			final ProjectNode[] existingProjects= view.getProjects();
			try {
				progressDialog.run(true, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask(AntViewActionMessages.getString("SearchForBuildFilesAction.Processing_search_results_3"), files.length); //$NON-NLS-1$
						for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
							String buildFileName= files[i].getLocation().toString();
							monitor.subTask(MessageFormat.format(AntViewActionMessages.getString("SearchForBuildFilesAction.Adding_{0}_4"), new String[] {buildFileName})); //$NON-NLS-1$
							if (alreadyAdded(buildFileName)) {
								// Don't parse projects that have already been added.
								continue;
							}
							final ProjectNode project= new ProjectNode(buildFileName);
							// Force the project to be parsed so the error state is set.
							project.getName();
							monitor.worked(1);
							if (includeErrorNodes || !(project.isErrorNode())) {
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										view.addProject(project);
									}
								});
							}
						}
					}
					/**
					 * Returns whether or not the given build file already
					 * exists in the ant view.
					 */
					private boolean alreadyAdded(String buildFileName) {
						for (int j = 0; j < existingProjects.length; j++) {
							ProjectNode existingProject = existingProjects[j];
							if (existingProject.getBuildFileName().equals(buildFileName)) {
								return true;
							}
						}
						return false;
					}
				});
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			}
		}
	}

}

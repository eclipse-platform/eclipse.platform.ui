/*******************************************************************************
 * Copyright (c) 2014, 2015, 2023 Red Hat Inc. and others
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
 *     Nikifor Fedorov (ArSysOp) - Import more than one project at once (eclipse.platform#226)
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.actions;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * @since 3.3
 *
 */
public class SelectProjectForFolderAction extends Action {

	private final Collection<IProject> project;
	private final CommonViewer viewer;

	/**
	 * @param project
	 * @param viewer
	 */
	public SelectProjectForFolderAction(Collection<IProject> project, CommonViewer viewer) {
		super(WorkbenchNavigatorMessages.SelectProjectForFolderAction_SelectProjects);
		this.project = project;
		this.viewer = viewer;
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(SharedImages.IMG_OBJ_PROJECT));
	}

	public SelectProjectForFolderAction(IProject project, CommonViewer viewer) {
		super(NLS.bind(WorkbenchNavigatorMessages.SelectProjectForFolderAction_SelectProject, project.getName()));
		this.project = Collections.singleton(project);
		this.viewer = viewer;
		setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(SharedImages.IMG_OBJ_PROJECT));
	}

	@Override
	public void run() {
		viewer.setSelection(new StructuredSelection(project));
	}

}

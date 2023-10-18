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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;

public class FoldersAsProjectsActionProvider extends CommonActionProvider {

	private CommonViewer viewer;

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		viewer = (CommonViewer) aSite.getStructuredViewer();
	}

	@Override
	public void fillContextMenu(IMenuManager aMenu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		if (selection.isEmpty()) {
			return;
		}
		Map<IPath, IProject> projects = projectsMap();
		List<IProject> existing = new LinkedList<>();
		List<IFolder> nonexisting = new LinkedList<>();
		for (Object object : selection) {
			IFolder folder = Adapters.adapt(object, IFolder.class);
			if (folder == null) {
				return;
			}
			if (!folder.getFile(IProjectDescription.DESCRIPTION_FILE_NAME).exists()) {
				return;
			}
			if (projects.get(folder.getLocation()) != null) {
				existing.add(projects.get(folder.getLocation()));
			} else {
				nonexisting.add(folder);
			}
			if (existing.size() > 0 && nonexisting.size() > 0) {
				// In case of ambiguity drop everything and do not show any option
				return;
			}
		}
		if (existing.size() > 0) {
			SelectProjectForFolderAction action = new SelectProjectForFolderAction(existing, this.viewer);
			aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, action);
		} else {
			OpenFolderAsProjectAction action = new OpenFolderAsProjectAction(nonexisting, this.viewer);
			aMenu.prependToGroup(ICommonMenuConstants.GROUP_PORT, action);
		}
	}

	private Map<IPath, IProject> projectsMap() {
		return Stream.of(ResourcesPlugin.getWorkspace().getRoot().getProjects())
				.collect(Collectors.toMap(IProject::getLocation, self -> self));
	}

}

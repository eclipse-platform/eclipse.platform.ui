/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.misc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provides content for a tree viewer that shows only containers.
 */
public class ContainerContentProvider implements ITreeContentProvider {
	private boolean showClosedProjects = true;

	/**
	 * Creates a new ContainerContentProvider.
	 */
	public ContainerContentProvider() {
	}

	/**
	 * The visual part that is using this content provider is about
	 * to be disposed. Deallocate all allocated SWT resources.
	 */
	@Override
	public void dispose() {
	}

	@Override
	public Object[] getChildren(Object element) {
		if (element instanceof IWorkspace) {
			// check if closed projects should be shown
			IProject[] allProjects = ((IWorkspace) element).getRoot().getProjects();
			if (showClosedProjects) {
				return allProjects;
			}

			ArrayList<IProject> accessibleProjects = new ArrayList<>();
			for (IProject project : allProjects) {
				if (project.isOpen()) {
					accessibleProjects.add(project);
				}
			}
			return accessibleProjects.toArray();
		} else if (element instanceof IContainer container) {
			if (container.isAccessible()) {
				try {
					List<IResource> children = new ArrayList<>();
					for (IResource member : container.members()) {
						if (member.getType() != IResource.FILE) {
							children.add(member);
						}
					}
					return children.toArray();
				} catch (CoreException e) {
					// this should never happen because we call #isAccessible before invoking #members
				}
			}
		}
		return new Object[0];
	}

	@Override
	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IResource) {
			return ((IResource) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * Specify whether or not to show closed projects in the tree
	 * viewer.  Default is to show closed projects.
	 *
	 * @param show boolean if false, do not show closed projects in the tree
	 */
	public void showClosedProjects(boolean show) {
		showClosedProjects = show;
	}

}

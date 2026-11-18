/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.ui.tests.navigator;

import java.io.ByteArrayInputStream;
import java.util.Collections;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.navigator.resources.ResourceBookmarks;
import org.eclipse.ui.internal.navigator.resources.actions.ToggleBookmarkHandler;

/**
 * Shared helpers of the bookmark toggle tests.
 */
final class BookmarkTestSupport {

	private BookmarkTestSupport() {
	}

	static IProject createProject(String prefix) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(prefix + System.nanoTime());
		project.create(null);
		project.open(null);
		return project;
	}

	static IFolder createFolder(IProject project, String name) throws CoreException {
		IFolder folder = project.getFolder(name);
		folder.create(true, true, null);
		return folder;
	}

	static IFile createFile(IProject project, String name) throws CoreException {
		IFile file = project.getFile(name);
		file.create(new ByteArrayInputStream(new byte[0]), true, null);
		return file;
	}

	static void deleteProjects(IProject... projects) throws CoreException {
		for (IProject project : projects) {
			if (project != null && project.exists()) {
				project.delete(true, true, null);
			}
		}
	}

	/**
	 * Creates an event whose context holds the given selections. A
	 * <code>null</code> selection leaves the variable undefined.
	 */
	static ExecutionEvent event(ISelection menuSelection, ISelection currentSelection) {
		EvaluationContext context = new EvaluationContext(null, new Object());
		if (menuSelection != null) {
			context.addVariable(ISources.ACTIVE_MENU_SELECTION_NAME, menuSelection);
		}
		if (currentSelection != null) {
			context.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, currentSelection);
		}
		return new ExecutionEvent(null, Collections.emptyMap(), null, context);
	}

	/** Executes the handler on the given elements as the active menu selection. */
	static Object toggle(Object... elements) {
		return execute(event(new StructuredSelection(elements), null));
	}

	static Object execute(ExecutionEvent event) {
		return new ToggleBookmarkHandler().execute(event);
	}

	/** Number of bookmark markers of the toggle command on the resource. */
	static int bookmarkCount(IResource resource) throws CoreException {
		return resource.findMarkers(ResourceBookmarks.MARKER_TYPE, false, IResource.DEPTH_ZERO).length;
	}
}

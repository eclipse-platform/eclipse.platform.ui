/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
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
 ******************************************************************************/
package org.eclipse.ui.ide;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveableFilter;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;


/**
 * A saveable filter where the given savable must either match one of the given roots or be a direct
 * or indirect child of one of the roots.
 *
 * @since 3.9
 */
public class ResourceSaveableFilter implements ISaveableFilter {

	private final IResource[] roots;

	/**
	 * Creates a new filter.
	 *
	 * @param roots the save roots
	 */
	public ResourceSaveableFilter(IResource[] roots) {
		this.roots = roots;
	}

	@Override
	public boolean select(Saveable saveable, IWorkbenchPart[] containingParts) {
		if (isDescendantOfRoots(saveable)) {
			return true;
		}
		// For backwards compatibility, we need to check the parts
		for (IWorkbenchPart workbenchPart : containingParts) {
			if (workbenchPart instanceof IEditorPart) {
				IEditorPart editorPart = (IEditorPart) workbenchPart;
				if (isEditingDescendantOf(editorPart)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Return whether the given saveable contains any resources that are descendants of the root
	 * resources.
	 *
	 * @param saveable the saveable
	 * @return whether the given saveable contains any resources that are descendants of the root
	 *         resources
	 */
	private boolean isDescendantOfRoots(Saveable saveable) {
		// First, try and adapt the saveable to a resource mapping.
		ResourceMapping mapping = ResourceUtil.getResourceMapping(saveable);
		if (mapping != null) {
			try {
				ResourceTraversal[] traversals = mapping.getTraversals(
						ResourceMappingContext.LOCAL_CONTEXT, null);
				for (ResourceTraversal traversal : traversals) {
					IResource[] resources = traversal.getResources();
					for (IResource resource : resources) {
						if (isDescendantOfRoots(resource)) {
							return true;
						}
					}
				}
			} catch (CoreException e) {
				IDEWorkbenchPlugin
						.log(
								NLS
										.bind(
												"An internal error occurred while determining the resources for {0}", saveable.getName()), e); //$NON-NLS-1$
			}
		} else {
			// If there is no mapping, try to adapt to a resource or file directly
			IFile file = ResourceUtil.getFile(saveable);
			if (file != null) {
				return isDescendantOfRoots(file);
			}
		}
		return false;
	}

	/**
	 * Return whether the given resource is either equal to or a descendant of one of the given
	 * roots.
	 *
	 * @param resource the resource to be tested
	 * @return whether the given resource is either equal to or a descendant of one of the given
	 *         roots
	 */
	private boolean isDescendantOfRoots(IResource resource) {
		for (IResource root : roots) {
			if (root.getFullPath().isPrefixOf(resource.getFullPath())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return whether the given dirty editor part is editing resources that are
	 * descendants of the given roots.
	 *
	 * @param part the dirty editor part
	 * @return whether the given dirty editor part is editing resources that are
	 *         descendants of the given roots
	 */
	private boolean isEditingDescendantOf(IEditorPart part) {
		IFile file = ResourceUtil.getFile(part.getEditorInput());
		if (file != null) {
			return isDescendantOfRoots(file);
		}
		return false;
	}

}
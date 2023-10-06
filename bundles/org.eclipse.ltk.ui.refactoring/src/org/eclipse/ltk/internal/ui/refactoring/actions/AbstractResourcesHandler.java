/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
package org.eclipse.ltk.internal.ui.refactoring.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;

import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

public abstract class AbstractResourcesHandler extends AbstractHandler {

	protected IResource[] getSelectedResources(IStructuredSelection sel) {
		List<IResource> resources= new ArrayList<>(sel.size());
		for (Object next : sel) {
			if (next instanceof IResource) {
				resources.add((IResource) next);
				continue;
			} else if (next instanceof IAdaptable) {
				IResource resource= ((IAdaptable) next).getAdapter(IResource.class);
				if (resource != null) {
					resources.add(resource);
					continue;
				}
			} else {
				IAdapterManager adapterManager= Platform.getAdapterManager();
				ResourceMapping mapping= adapterManager.getAdapter(next, ResourceMapping.class);

				if (mapping != null) {

					ResourceTraversal[] traversals= null;
					try {
						traversals= mapping.getTraversals(ResourceMappingContext.LOCAL_CONTEXT, new NullProgressMonitor());
					} catch (CoreException exception) {
						RefactoringUIPlugin.log(exception.getStatus());
					}

					if (traversals != null) {
						for (ResourceTraversal traversal : traversals) {
							IResource[] traversalResources= traversal.getResources();
							if (traversalResources != null) {
								resources.addAll(Arrays.asList(traversalResources)); // for
							}// if
						}// for
					}// if
				}// if
			}
		}
		return resources.toArray(new IResource[resources.size()]);
	}

}

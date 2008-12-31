/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.actions;

import java.util.ArrayList;
import java.util.Iterator;
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
		List resources= new ArrayList(sel.size());
		for (Iterator e= sel.iterator(); e.hasNext();) {
			Object next= e.next();
			if (next instanceof IResource) {
				resources.add(next);
				continue;
			} else if (next instanceof IAdaptable) {
				Object resource= ((IAdaptable) next).getAdapter(IResource.class);
				if (resource != null) {
					resources.add(resource);
					continue;
				}
			} else {
				IAdapterManager adapterManager= Platform.getAdapterManager();
				ResourceMapping mapping= (ResourceMapping) adapterManager.getAdapter(next, ResourceMapping.class);

				if (mapping != null) {

					ResourceTraversal[] traversals= null;
					try {
						traversals= mapping.getTraversals(ResourceMappingContext.LOCAL_CONTEXT, new NullProgressMonitor());
					} catch (CoreException exception) {
						RefactoringUIPlugin.log(exception.getStatus());
					}

					if (traversals != null) {
						for (int i= 0; i < traversals.length; i++) {
							IResource[] traversalResources= traversals[i].getResources();
							if (traversalResources != null) {
								for (int j= 0; j < traversalResources.length; j++) {
									resources.add(traversalResources[j]);
								}// for
							}// if
						}// for
					}// if
				}// if
			}
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);
	}

}

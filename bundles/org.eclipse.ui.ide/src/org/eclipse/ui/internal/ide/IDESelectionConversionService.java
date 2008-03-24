/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.ide;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.ISelectionConversionService;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * The IDESelectionConversionService is the selection service that uses the
 * resource support available to the IDE.
 * 
 * @since 3.2
 */
public class IDESelectionConversionService implements
		ISelectionConversionService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.SelectionConversionService#convertToResources(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public IStructuredSelection convertToResources(
			IStructuredSelection originalSelection) {

		List result = new ArrayList();
		Iterator elements = originalSelection.iterator();

		while (elements.hasNext()) {
			Object currentElement = elements.next();

			IResource resource = ResourceUtil.getResource(currentElement);

			if (resource == null) {

				ResourceMapping mapping = ResourceUtil
						.getResourceMapping(currentElement);
				if (mapping == null)
					continue;

				ResourceTraversal[] traversals = null;
				try {
					traversals = mapping.getTraversals(
							ResourceMappingContext.LOCAL_CONTEXT,
							new NullProgressMonitor());
				} catch (CoreException e) {
					StatusManager.getManager().handle(e, IDEWorkbenchPlugin.IDE_WORKBENCH);
				}
				if (traversals != null) {
					ResourceTraversal traversal = null;
					IResource[] resources = null;
					for (int i = 0; i < traversals.length; i++) {
						traversal = traversals[i];
						resources = traversal.getResources();
						if (resources != null) {
							for (int j = 0; j < resources.length; j++) {
								result.add(resources[j]);
							}
						}
					}
				}

			} else
				result.add(resource);
		}

		// all that can be converted are done, answer new selection
		if (result.isEmpty()) {
			return StructuredSelection.EMPTY;
		}
		return new StructuredSelection(result.toArray());
	}
}

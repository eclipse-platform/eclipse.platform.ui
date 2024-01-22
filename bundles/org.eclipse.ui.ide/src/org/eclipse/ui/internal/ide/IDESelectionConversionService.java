/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.ide;

import java.util.ArrayList;
import java.util.Arrays;
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

	@Override
	public IStructuredSelection convertToResources(
			IStructuredSelection originalSelection) {

		List<Object> result = new ArrayList<>();

		for (Object currentElement : originalSelection) {

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
					IResource[] resources = null;
					for (ResourceTraversal traversal : traversals) {
						resources = traversal.getResources();
						if (resources != null) {
							result.addAll(Arrays.asList(resources));
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

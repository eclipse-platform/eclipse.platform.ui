/*******************************************************************************
 * Copyright (c) 2007, 2026 IBM Corporation and others.
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
 *     Felix Schmid - adapted for copy resource refactoring contribution
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.resource.CopyResourcesDescriptor;

/**
 * Refactoring contribution for the copy resources refactoring.
 *
 * @since 3.16
 */
public class CopyResourcesRefactoringContribution extends RefactoringContribution {

	/**
	 * Key used for the number of resource to be copied
	 */
	private static final String ATTRIBUTE_NUMBER_OF_RESOURCES= "resources"; //$NON-NLS-1$

	/**
	 * Key prefix used for the paths of the resources to be copied
	 * <p>
	 * The element arguments are simply distinguished by appending a number to the argument name,
	 * e.g. element1. The indices of this argument are one-based.
	 * </p>
	 */
	private static final String ATTRIBUTE_ELEMENT= "element"; //$NON-NLS-1$

	/**
	 * Key prefix used for the destination paths of the resources to be copied
	 * <p>
	 * The element arguments are simply distinguished by appending a number to the argument name,
	 * e.g. element1. The indices of this argument are one-based.
	 * </p>
	 */
	private static final String ATTRIBUTE_DESTINATION= "destination"; //$NON-NLS-1$

	@Override
	public Map<String, String> retrieveArgumentMap(RefactoringDescriptor descriptor) {
		if (descriptor instanceof CopyResourcesDescriptor copyDesc) {
			HashMap<String, String> map= new HashMap<>();
			String project= copyDesc.getProject();

			IPath[] resourcePaths= copyDesc.getResourcePaths();
			map.put(ATTRIBUTE_NUMBER_OF_RESOURCES, String.valueOf(resourcePaths.length));
			storePaths(ATTRIBUTE_ELEMENT, resourcePaths, map, project);
			storePaths(ATTRIBUTE_DESTINATION, copyDesc.getDestinationPaths(), map, project);
			return map;
		}
		return Collections.emptyMap();
	}

	@Override
	public RefactoringDescriptor createDescriptor() {
		return new CopyResourcesDescriptor();
	}

	@Override
	public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment,
			Map<String, String> arguments, int flags) throws IllegalArgumentException {
		try {
			int numResources= Integer.parseInt(arguments.get(ATTRIBUTE_NUMBER_OF_RESOURCES));
			if (numResources < 0 || numResources > 100000) {
				throw new IllegalArgumentException("Can not restore CopyResourceDescriptor from map, number of moved elements invalid"); //$NON-NLS-1$
			}

			IPath[] resourcePaths= loadPaths(ATTRIBUTE_ELEMENT, numResources, arguments, project);
			IPath[] destinationPaths= loadPaths(ATTRIBUTE_DESTINATION, numResources, arguments, project);

			if (resourcePaths.length > 0) {
				CopyResourcesDescriptor descriptor= new CopyResourcesDescriptor();
				descriptor.setProject(project);
				descriptor.setDescription(description);
				descriptor.setComment(comment);
				descriptor.setFlags(flags);
				descriptor.setResourcePaths(resourcePaths);
				descriptor.setDestinationPaths(destinationPaths);
				return descriptor;
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Can not restore CopyResourceDescriptor from map"); //$NON-NLS-1$
		}
		throw new IllegalArgumentException("Can not restore CopyResourceDescriptor from map"); //$NON-NLS-1$
	}

	private void storePaths(String keyPrefix, IPath[] paths, Map<String, String> arguments, String project) {
		for (int i= 0; i < paths.length; i++) {
			arguments.put(keyPrefix + (i + 1), ResourceProcessors.resourcePathToHandle(project, paths[i]));
		}
	}

	private IPath[] loadPaths(String keyPrefix, int pathCount, Map<String, String> arguments, String project) {
		IPath[] paths= new IPath[pathCount];
		for (int i= 0; i < pathCount; i++) {
			String path= arguments.get(keyPrefix + String.valueOf(i + 1));
			if (path == null) {
				throw new IllegalArgumentException("Can not restore CopyResourceDescriptor from map, path missing"); //$NON-NLS-1$
			}
			paths[i]= ResourceProcessors.handleToResourcePath(project, path);
		}
		return paths;
	}
}

/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.resource.CopyProjectDescriptor;

/**
 * @since 3.15
 */
public class CopyProjectRefactoringContribution extends RefactoringContribution {

	/**
	 * Key used for the new resource name
	 */
	private static final String ATTRIBUTE_NAME= "name"; //$NON-NLS-1$

	/**
	 * Key used for the new resource destination
	 */
	private static final String ATTRIBUTE_DESTINATION= "destination"; //$NON-NLS-1$

	/**
	 * Key prefix used for the path of the project to copy
	 */
	private static final String ATTRIBUTE_ELEMENT= "element"; //$NON-NLS-1$

	@Override
	public Map<String, String> retrieveArgumentMap(RefactoringDescriptor descriptor) {
		if (descriptor instanceof CopyProjectDescriptor copyDesc) {
			HashMap<String, String> map= new HashMap<>();
			IPath resources= copyDesc.getSourcePath();
			String project= copyDesc.getProject();
			map.put(ATTRIBUTE_ELEMENT, ResourceProcessors.resourcePathToHandle(project, resources));
			map.put(ATTRIBUTE_NAME, copyDesc.getNewName());
			IPath destinationPath= copyDesc.getNewLocation();
			map.put(ATTRIBUTE_DESTINATION, ResourceProcessors.resourcePathToHandle(descriptor.getProject(), destinationPath));

			return map;
		}
		return Collections.emptyMap();
	}

	@Override
	public RefactoringDescriptor createDescriptor() {
		return new CopyProjectDescriptor();
	}

	@Override
	public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map<String, String> arguments, int flags) throws IllegalArgumentException {
		String pathString= arguments.get(ATTRIBUTE_ELEMENT);
		String newName= arguments.get(ATTRIBUTE_NAME);

		String destination= arguments.get(ATTRIBUTE_DESTINATION);
		if (destination == null) {
			throw new IllegalArgumentException("Can not restore CopyProjectDescriptor from map, destination missing"); //$NON-NLS-1$
		}

		IPath resourcePath= ResourceProcessors.handleToResourcePath(project, pathString);
		IPath destPath= ResourceProcessors.handleToResourcePath(project, destination);

		if (resourcePath != null && newName != null) {
			CopyProjectDescriptor descriptor= new CopyProjectDescriptor();
			descriptor.setProject(project);
			descriptor.setDescription(description);
			descriptor.setComment(comment);
			descriptor.setFlags(flags);
			descriptor.setResourcePath(resourcePath);
			descriptor.setNewName(newName);
			descriptor.setNewLocation(destPath);
			descriptor.setResourcePath(resourcePath);

			return descriptor;
		}
		throw new IllegalArgumentException("Can not restore CopyProjectDescriptor from map"); //$NON-NLS-1$
	}
}

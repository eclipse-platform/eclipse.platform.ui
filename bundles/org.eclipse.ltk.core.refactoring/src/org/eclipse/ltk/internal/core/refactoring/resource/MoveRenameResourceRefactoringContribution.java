/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
 *     Red Hat Inc. - created based on RenameResourceRefactoringContribution
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.resource.MoveRenameResourceDescriptor;

/**
 * Refactoring contribution for the rename resource refactoring.
 *
 * @since 3.10
 */
public final class MoveRenameResourceRefactoringContribution extends RefactoringContribution {

	/**
	 * Key used for the path of the resource to be renamed
	 */
	private static final String ATTRIBUTE_INPUT= "input"; //$NON-NLS-1$

	/**
	 * Key used for the new resource name
	 */
	private static final String ATTRIBUTE_NAME= "name"; //$NON-NLS-1$

	/**
	 * Key used for the new resource destination
	 */
	private static final String ATTRIBUTE_DESTINATION= "destination"; //$NON-NLS-1$

	/**
	 * Key used for the 'update references' property
	 */
	private static final String ATTRIBUTE_UPDATE_REFERENCES= "updateReferences"; //$NON-NLS-1$


	@Override
	public Map<String, String> retrieveArgumentMap(final RefactoringDescriptor descriptor) {
		HashMap<String, String> map= new HashMap<>();

		if (descriptor instanceof MoveRenameResourceDescriptor) {
			MoveRenameResourceDescriptor resourceDescriptor= (MoveRenameResourceDescriptor) descriptor;
			map.put(ATTRIBUTE_INPUT, ResourceProcessors.resourcePathToHandle(descriptor.getProject(), resourceDescriptor.getResourcePath()));
			map.put(ATTRIBUTE_NAME, resourceDescriptor.getNewName());
			IPath destinationPath= resourceDescriptor.getDestinationPath();
			map.put(ATTRIBUTE_DESTINATION, ResourceProcessors.resourcePathToHandle(descriptor.getProject(), destinationPath));
			map.put(ATTRIBUTE_UPDATE_REFERENCES, resourceDescriptor.isUpdateReferences() ? "true" : "false"); //$NON-NLS-1$//$NON-NLS-2$
			return map;
		}
		return null;
	}

	@Override
	public RefactoringDescriptor createDescriptor() {
		return new MoveRenameResourceDescriptor();
	}

	@Override
	public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map<String, String> arguments, int flags) {
		String pathString= arguments.get(ATTRIBUTE_INPUT);
		String newName= arguments.get(ATTRIBUTE_NAME);

		String destination= arguments.get(ATTRIBUTE_DESTINATION);
		if (destination == null) {
			throw new IllegalArgumentException("Can not restore MoveRenameResourceDescriptor from map, destination missing"); //$NON-NLS-1$
		}
		IPath destPath= ResourceProcessors.handleToResourcePath(project, destination);

		boolean updateReferences= "true".equals(arguments.get(ATTRIBUTE_UPDATE_REFERENCES)); //$NON-NLS-1$

		if (pathString != null && newName != null) {
			IPath path= ResourceProcessors.handleToResourcePath(project, pathString);
			MoveRenameResourceDescriptor descriptor= new MoveRenameResourceDescriptor();
			descriptor.setProject(project);
			descriptor.setDescription(description);
			descriptor.setComment(comment);
			descriptor.setFlags(flags);
			descriptor.setNewName(newName);
			descriptor.setDestinationPath(destPath);
			descriptor.setResourcePath(path);
			descriptor.setUpdateReferences(updateReferences);
			return descriptor;
		}
		throw new IllegalArgumentException("Can not restore MoveRenameResourceDescriptor from map"); //$NON-NLS-1$
	}
}

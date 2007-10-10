/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceDescriptor;

/**
 * Refactoring contribution for the rename resource refactoring.
 * 
 * @since 3.4
 */
public final class RenameResourceRefactoringContribution extends RefactoringContribution {

	/**
	 * Key used for the path of the resource to be renamed
	 */
	private static final String ATTRIBUTE_INPUT= "input"; //$NON-NLS-1$

	/**
	 * Key used for the new resource name
	 */
	private static final String ATTRIBUTE_NAME= "name"; //$NON-NLS-1$

	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.RefactoringContribution#retrieveArgumentMap(org.eclipse.ltk.core.refactoring.RefactoringDescriptor)
	 */
	public Map retrieveArgumentMap(final RefactoringDescriptor descriptor) {
		HashMap map= new HashMap();
		
		if (descriptor instanceof RenameResourceDescriptor) {
			RenameResourceDescriptor resourceDescriptor= (RenameResourceDescriptor) descriptor;
			map.put(ATTRIBUTE_INPUT, resourcePathToHandle(descriptor.getProject(), resourceDescriptor.getResourcePath()));
			map.put(ATTRIBUTE_NAME, resourceDescriptor.getNewName());
			return map;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.RefactoringContribution#createDescriptor()
	 */
	public RefactoringDescriptor createDescriptor() {
		return new RenameResourceDescriptor();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.RefactoringContribution#createDescriptor(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Map, int)
	 */
	public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map arguments, int flags) {
		String pathString= (String) arguments.get(ATTRIBUTE_INPUT);
		String newName= (String) arguments.get(ATTRIBUTE_NAME);
		
		if (pathString != null && newName != null) {
			IPath path= handleToResourcePath(project, pathString);
			RenameResourceDescriptor descriptor= new RenameResourceDescriptor();
			descriptor.setProject(project);
			descriptor.setDescription(description);
			descriptor.setComment(comment);
			descriptor.setFlags(flags);
			descriptor.setNewName(newName);
			descriptor.setResourcePath(path);
			return descriptor;
		}
		throw new IllegalArgumentException("Can not restore RenameResourceDescriptor from map"); //$NON-NLS-1$
	}

	private static IPath handleToResourcePath(final String project, final String handle) {
		final IPath path= Path.fromPortableString(handle);
		if (project != null && project.length() > 0 && !path.isAbsolute())
			return new Path(project).append(path).makeAbsolute();
		return path;
	}

	private static String resourcePathToHandle(final String project, final IPath resourcePath) {
		if (project != null && project.length() > 0 && resourcePath.segmentCount() != 1)
			if (resourcePath.segment(0).equals(project)) {
				return resourcePath.removeFirstSegments(1).toPortableString();
			}
		return resourcePath.toPortableString();
	}
}

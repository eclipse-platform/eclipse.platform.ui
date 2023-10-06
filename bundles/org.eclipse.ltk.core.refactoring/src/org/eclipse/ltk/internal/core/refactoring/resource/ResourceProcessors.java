/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.core.refactoring.resource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class ResourceProcessors {

	public static String[] computeAffectedNatures(IResource resource) throws CoreException {
		IProject project= resource.getProject();
		Set<String> result= new HashSet<>();
		Set<IProject> visitedProjects= new HashSet<>();
		computeNatures(result, visitedProjects, project);
		return result.toArray(new String[result.size()]);
	}

	public static String[] computeAffectedNatures(IResource[] resources) throws CoreException {
		Set<String> result= new HashSet<>();
		Set<IProject> visitedProjects= new HashSet<>();
		for (IResource resource : resources) {
			computeNatures(result, visitedProjects, resource.getProject());
		}
		return result.toArray(new String[result.size()]);
	}

	private static void computeNatures(Set<String> result, Set<IProject> visitedProjects, IProject focus) throws CoreException {
		if (visitedProjects.contains(focus))
			return;
		String[] pns= focus.getDescription().getNatureIds();
		result.addAll(Arrays.asList(pns));
		visitedProjects.add(focus);
		IProject[] referencing= focus.getReferencingProjects();
		for (IProject r : referencing) {
			computeNatures(result, visitedProjects, r);
		}
	}

	public static IPath handleToResourcePath(final String project, final String handle) {
		final IPath path= Path.fromPortableString(handle);
		if (project != null && project.length() > 0 && !path.isAbsolute())
			return new Path(project).append(path).makeAbsolute();
		return path;
	}

	public static String resourcePathToHandle(final String project, final IPath resourcePath) {
		if (project != null && project.length() > 0 && resourcePath.segmentCount() != 1)
			if (resourcePath.segment(0).equals(project)) {
				return resourcePath.removeFirstSegments(1).toPortableString();
			}
		return resourcePath.toPortableString();
	}

	private ResourceProcessors() {
	}
}

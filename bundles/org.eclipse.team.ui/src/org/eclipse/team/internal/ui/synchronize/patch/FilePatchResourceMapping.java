/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.patch;

import org.eclipse.compare.internal.core.patch.DiffProject;
import org.eclipse.compare.internal.core.patch.FileDiffResult;
import org.eclipse.compare.internal.patch.WorkspaceFileDiffResult;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class FilePatchResourceMapping extends ResourceMapping {

	private final FileDiffResult object;

	public FilePatchResourceMapping(FileDiffResult fileDiffResult) {
		object = fileDiffResult;
	}

	public Object getModelObject() {
		return object;
	}

	public String getModelProviderId() {
		return PatchModelProvider.ID;
	}

	public IProject[] getProjects() {
		DiffProject dp = (DiffProject) object.getDiff().getProject();
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(
				dp.getName());
		return new IProject[] { p };
	}

	public ResourceTraversal[] getTraversals(ResourceMappingContext context,
			IProgressMonitor monitor) throws CoreException {
		return new ResourceTraversal[] { new ResourceTraversal(
				new IResource[] { getResource() }, IResource.DEPTH_INFINITE,
				IResource.NONE) };
	}

	private IResource getResource() {
		return ((WorkspaceFileDiffResult) object).getTargetFile();
	}
}

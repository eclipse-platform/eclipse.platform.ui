/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.undo;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * GroupDescription is a lightweight description that describes a group to be
 * created.
 * 
 * This class is not intended to be instantiated or used by clients.
 * 
 * @since 3.3
 * 
 */
public class GroupDescription extends ContainerDescription {

	/**
	 * Create a GroupDescription from the specified group handle.
	 * 
	 * @param group
	 *            the group to be described
	 */
	public GroupDescription(IFolder group) {
		super(group);
		this.name = group.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ide.undo.ContainerDescription#createResourceHandle()
	 */
	public IResource createResourceHandle() {
		IWorkspaceRoot workspaceRoot = getWorkspace().getRoot();
		IPath folderPath = parent.getFullPath().append(name);
		return workspaceRoot.getFolder(folderPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ide.undo.ResourceDescription#createExistentResourceFromHandle(org.eclipse.core.resources.IResource,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void createExistentResourceFromHandle(IResource resource,
			IProgressMonitor monitor) throws CoreException {

		Assert.isLegal(resource instanceof IFolder);
		if (resource.exists()) {
			return;
		}
		IFolder folderHandle = (IFolder) resource;
		try {
			monitor.beginTask("", 200); //$NON-NLS-1$
			monitor.setTaskName(UndoMessages.GroupDescription_NewGroupProgress);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			folderHandle.createGroup(IResource.ALLOW_MISSING_LOCAL, new SubProgressMonitor(
					monitor, 100));
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			createChildResources(folderHandle, monitor, 100);

		} finally {
			monitor.done();
		}
	}
}
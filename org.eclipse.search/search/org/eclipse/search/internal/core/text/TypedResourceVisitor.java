/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.core.text;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;

import org.eclipse.jface.util.Assert;

public class TypedResourceVisitor implements IResourceVisitor {

	private MultiStatus fStatus;

	TypedResourceVisitor(MultiStatus status) {
		Assert.isNotNull(status);
		fStatus= status;
	}
	
	public boolean visit(IResource resource) {
		try {
			switch(resource.getType()) {
				case IResource.FILE:
					return visitFile((IFile)resource);
				case IResource.FOLDER:
					return visitFolder((IFolder)resource);
				case IResource.PROJECT:
					return visitProject((IProject)resource);
				default:
					Assert.isTrue(false, "unknown resource type"); //$NON-NLS-1$
			}
			return false;
		} catch (CoreException ex) {
			addToStatus(ex);
			return false;
		}
	}

	protected boolean visitProject(IProject project) throws CoreException {
		return true;
	}
	
	protected boolean visitFolder(IFolder folder) throws CoreException {
		return true;
	}
	
	protected boolean visitFile(IFile file) throws CoreException {
		return true;
	}

	protected void addToStatus(CoreException ex) {
		fStatus.add(ex.getStatus());
	}
}
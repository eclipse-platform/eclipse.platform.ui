/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.core.text;

import org.eclipse.jface.util.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

public class TypedResourceVisitor implements IResourceVisitor {

	public boolean visit(IResource resource) throws CoreException {
		switch(resource.getType()) {
			case IResource.FILE:
				return visitFile((IFile)resource);
			case IResource.FOLDER:
				return visitFolder((IFolder)resource);
			case IResource.PROJECT:
				return visitProject((IProject)resource);
			default:
				Assert.isTrue(false, "Unknown resource type");
		}
		return false;
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
}
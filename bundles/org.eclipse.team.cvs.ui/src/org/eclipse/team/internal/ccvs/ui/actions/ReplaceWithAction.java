package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.ui.actions.TeamAction;

public abstract class ReplaceWithAction extends TeamAction {
	/**
	 * Copied from CVSDecorationRunnable
	 */
	protected boolean isDirty(IResource resource) {
		final CoreException DECORATOR_EXCEPTION = new CoreException(new Status(IStatus.OK, "id", 1, "", null));
		try {
			resource.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {

					// a project can't be dirty, continue with its children
					if (resource.getType() == IResource.PROJECT) {
						return true;
					}
					
					// if the resource does not exist in the workbench or on the file system, stop searching.
					if(!resource.exists()) {
						return false;
					}

					ICVSResource cvsResource;
					if (resource.getType() == IResource.FILE) {
						cvsResource = new LocalFile(resource.getLocation().toFile());
					} else {
						cvsResource = new LocalFolder(resource.getLocation().toFile());
					}

					try {
						if (!cvsResource.isManaged()) {
							if (cvsResource.isIgnored()) {
								return false;
							} else {
								// new resource, show as dirty
								throw DECORATOR_EXCEPTION;
							}
						}
						if (!cvsResource.isFolder()) {
							if (((ICVSFile) cvsResource).isModified()) {
								// file has changed, show as dirty
								throw DECORATOR_EXCEPTION;
							}
						}
					} catch (CVSException e) {
						return true;
					}
					// no change -- keep looking in children
					return true;
				}
			}, IResource.DEPTH_INFINITE, true);
		} catch (CoreException e) {
			//if our exception was caught, we know there's a dirty child
			return e == DECORATOR_EXCEPTION;
		}
		return false;
	}	
}

package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;

public class DirtyDeltaVisitor extends ResourceDeltaVisitor {

	/**
	 * @see ResourceDeltaVisitor#handleAdded(IProject, IResource)
	 */
	protected void handleAdded(IProject project, IResource resource) {
	}

	/**
	 * @see ResourceDeltaVisitor#handleRemoved(IProject, IResource)
	 */
	protected void handleRemoved(IProject project, IResource resource) {
		clear(resource);
	}

	/**
	 * @see ResourceDeltaVisitor#handleChanged(IProject, IResource)
	 */
	protected void handleChanged(IProject project, IResource resource) {
		clear(resource);
	}

	private void clear(IResource resource) {
		ICVSFile mFile;
		
		if (!(resource instanceof IFile)) {
			return;
		}
		
		try {
			mFile = Client.getManagedFile(resource.getLocation().toFile());
		} catch (CVSException e) {
			Assert.isTrue(false);
		}
		
		//System.out.println(resource.getName() + " cleared");
	}
}
package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Dumps every change in the resources to the consol
 */
public class DumpDeltaVisitor extends ResourceDeltaVisitor {

	/**
	 * @see ResourceDeltaVisitor#handleAdded(IProject, IResource)
	 */
	protected void handleAdded(IProject project, IResource resource) {
		System.out.println(resource.getName() + " added");
	}

	/**
	 * @see ResourceDeltaVisitor#handleRemoved(IProject, IResource)
	 */
	protected void handleRemoved(IProject project, IResource resource) {
		System.out.println(resource.getName() + " removed");
	}

	/**
	 * @see ResourceDeltaVisitor#handleChanged(IProject, IResource)
	 */
	protected void handleChanged(IProject project, IResource resource) {
		System.out.println(resource.getName() + " changed");
	}

}


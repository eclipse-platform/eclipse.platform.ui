 /*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.sourcelookup.containers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;

/**
 * A project in the workspace. Source is searched for in the root project
 * folder and all folders within the project recursively. Optionally,
 * referenced projects may be searched as well.
 * 
 * @since 3.0
 */
public class ProjectSourceContainer extends ContainerSourceContainer {

	boolean fReferencedProjects=false;
	
	/**
	 * Constructs a project source container.
	 * 
	 * @param project the project to search for source in
	 * @param referenced whether referenced projects should be considered
	 */
	public ProjectSourceContainer(IProject project, boolean referenced) {
		super(project, true);
		fReferencedProjects = referenced;
	}
	
	/**
	 * Returns whether referenced projects are considered.
	 * 
	 * @return whether referenced projects are considered
	 */
	public boolean isSearchReferencedProjects() {
		return fReferencedProjects;
	}
	
	/**
	 * Returns the project this source container references.
	 * 
	 * @return the project this source container references
	 */
	public IProject getProject() {
		return (IProject) getContainer();
	}

	/* (non-Javadoc)
	* @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	*/
	public ISourceContainerType getType() {
		return SourceLookupUtils.getSourceContainerType(ProjectSourceContainerType.TYPE_ID);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getSourceContainers()
	 */
	public ISourceContainer[] getSourceContainers() throws CoreException {
		// TODO: cache the result
		IProject project = getProject();
		IProject[] projects = project.getReferencedProjects();
		if (projects.length > 0) {
			ISourceContainer[] folders = super.getSourceContainers();
			ISourceContainer[] all = new ISourceContainer[folders.length + projects.length];
			for (int i = 0; i < projects.length; i++) {
				all[i] = new ProjectSourceContainer(projects[i], true);
			}
			System.arraycopy(folders, 0, all, projects.length, folders.length);
			for (int i = 0; i < all.length; i++) {
				ISourceContainer container = all[i];
				container.init(getDirector());
			}			
			return all;
		} else {
			return super.getSourceContainers();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#isComposite()
	 */
	public boolean isComposite() {
		return true;
	}

}

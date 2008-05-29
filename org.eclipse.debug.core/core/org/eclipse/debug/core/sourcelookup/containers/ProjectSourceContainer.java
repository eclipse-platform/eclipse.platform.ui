/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mikhail Khodjaiants, QNX - Bug 110227: Possible infinite loop in ProjectSourceContainer  
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup.containers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;

/**
 * A project in the workspace. Source is searched for in the root project
 * folder and all folders within the project recursively. Optionally,
 * referenced projects may be searched as well.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ProjectSourceContainer extends ContainerSourceContainer {

	boolean fReferencedProjects=false;
	/**
	 * Unique identifier for the project source container type
	 * (value <code>org.eclipse.debug.core.containerType.project</code>).
	 */	
	public static final String TYPE_ID = DebugPlugin.getUniqueIdentifier() + ".containerType.project"; //$NON-NLS-1$
	
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
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#isComposite()
	 */
	public boolean isComposite() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.containers.CompositeSourceContainer#createSourceContainers()
	 */
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		if (getProject().isOpen()) {
			if (isSearchReferencedProjects()) {
				IProject project = getProject();
				IProject[] projects = getAllReferencedProjects(project);
				ISourceContainer[] folders = super.createSourceContainers();
				List all = new ArrayList(folders.length + projects.length);
				for (int i = 0; i < folders.length; i++) {
					all.add(folders[i]);
				}
				for (int i = 0; i < projects.length; i++) {
					if (project.exists() && project.isOpen()) {
						ProjectSourceContainer container = new ProjectSourceContainer(projects[i], false);
						container.init(getDirector());
						all.add(container);
					}
				}
				return (ISourceContainer[]) all.toArray(new ISourceContainer[all.size()]);
			} 
			return super.createSourceContainers();
		}
		return new ISourceContainer[0];
	}

	private IProject[] getAllReferencedProjects(IProject project) throws CoreException {
		Set all = new HashSet();
		getAllReferencedProjects(all, project);
		return (IProject[]) all.toArray(new IProject[all.size()]);
	}

	private void getAllReferencedProjects(Set all, IProject project) throws CoreException {
		IProject[] refs = project.getReferencedProjects();
		for (int i = 0; i < refs.length; i++) {
			if (!all.contains(refs[i]) && refs[i].exists() && refs[i].isOpen()) {
				all.add(refs[i]);
				getAllReferencedProjects(all, refs[i]);
			}
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup.containers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupMessages;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;

/**
 * All projects in the workspace.
 * 
 * TODO: need workspace browser/workbench adapter
 * 
 * @since 3.0
 */
public class WorkspaceSourceContainer extends CompositeSourceContainer {
	
	/**
	 * Unique identifier for the workspace source container type
	 * (value <code>org.eclipse.debug.core.containerType.workspace</code>).
	 */
	public static final String TYPE_ID = DebugPlugin.getUniqueIdentifier() + ".containerType.workspace"; //$NON-NLS-1$

	public WorkspaceSourceContainer() {
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return SourceLookupMessages.getString("WorkspaceSourceContainer.0"); //$NON-NLS-1$
	}
		
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return obj instanceof WorkspaceSourceContainer;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */	
	public int hashCode() {
		return ResourcesPlugin.getWorkspace().hashCode();
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return SourceLookupUtils.getSourceContainerType(WorkspaceSourceContainer.TYPE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer#createSourceContainers()
	 */
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		ISourceContainer[] containers = new ISourceContainer[projects.length];
		for (int i = 0; i < projects.length; i++) {
			ISourceContainer container = new ProjectSourceContainer(projects[i], false);
			container.init(getDirector());
			containers[i] = container;
		}
		return containers;
	}

}

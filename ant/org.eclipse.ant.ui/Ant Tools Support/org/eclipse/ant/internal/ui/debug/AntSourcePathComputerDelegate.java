/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.debug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourcePathComputer;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;

/**
 * Computes the default source lookup path for an Ant launch configuration.
 * The default source lookup path contains the folder or project containing 
 * the Ant buildfile. If the folder or project cannot be determined, the workspace
 * is searched by default. The classpath entries for the Ant build are also added as 
 * containers for source lookup.
 */
public class AntSourcePathComputerDelegate extends JavaSourcePathComputer {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		String path = configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, (String)null);
		path= VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(path);
        List sourceContainers= new ArrayList();
		ISourceContainer folderContainer = null;
		IProject project= null;
		if (path != null) {
			IResource resource = AntUtil.getFileForLocation(path, null);
			if (resource != null) {
				IContainer container = resource.getParent();
				if (container.getType() == IResource.PROJECT) {
					project= (IProject)container;
				} else if (container.getType() == IResource.FOLDER) {
					folderContainer = new FolderSourceContainer(container, true);
					project= container.getProject();
				}
			}
		}
		
		if (folderContainer != null) {
			sourceContainers.add(folderContainer);
		}
		if (project != null) {
			sourceContainers.add(new ProjectSourceContainer(project, false));
		}
        sourceContainers.add(new WorkspaceSourceContainer());
        
        ISourceContainer[] classpathContainers= super.computeSourceContainers(configuration, monitor);
        sourceContainers.addAll(Arrays.asList(classpathContainers));
        
		return (ISourceContainer[]) sourceContainers.toArray(new ISourceContainer[sourceContainers.size()]);
	}
}
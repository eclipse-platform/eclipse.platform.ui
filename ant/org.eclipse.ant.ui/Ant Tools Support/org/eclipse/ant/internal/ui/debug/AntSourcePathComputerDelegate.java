/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourcePathComputer;

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
//		String path = configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, (String)null);
//		path= VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(path);
//        List sourceContainers= new ArrayList();
//		ISourceContainer sourceContainer = null;
//		IProject project= null;
//		if (path != null) {
//			IResource resource = AntUtil.getFileForLocation(path, null);
//			if (resource != null) {
//				IContainer container = resource.getParent();
//				if (container.getType() == IResource.PROJECT) {
//					project= (IProject)container;
//				} else if (container.getType() == IResource.FOLDER) {
//					sourceContainer = new FolderSourceContainer(container, true);
//					project= container.getProject();
//				}
//			} else { //external to the workspace
//			    File buildFile= new File(path);
//                if (buildFile.exists()) {
//                    sourceContainer= new DirectorySourceContainer(buildFile.getParentFile(), true);
//                }
//            }
//		}
//		
//		if (sourceContainer != null) {
//			sourceContainers.add(sourceContainer);
//		}
//		if (project != null) {
//			sourceContainers.add(new ProjectSourceContainer(project, false));
//		}
//        sourceContainers.add(new WorkspaceSourceContainer());
//        
//        ISourceContainer[] classpathContainers= super.computeSourceContainers(configuration, monitor);
//        sourceContainers.addAll(Arrays.asList(classpathContainers));
//        
//		return (ISourceContainer[]) sourceContainers.toArray(new ISourceContainer[sourceContainers.size()]);
		
		return new ISourceContainer[] {new AntSourceContainer()};
	}
}

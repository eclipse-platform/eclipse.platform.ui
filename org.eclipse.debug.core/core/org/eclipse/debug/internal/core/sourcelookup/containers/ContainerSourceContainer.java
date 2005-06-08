/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Bug 80857
 *******************************************************************************/
package org.eclipse.debug.internal.core.sourcelookup.containers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.CompositeSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;

/**
 * A container in the workspace. Source elements are searched
 * for within this container and optionally nested containers.
 * <p>
 * Names specified in <code>findSourceElements</code> method can
 * be simple or qualified. When a name is qualified, a file will
 * be searched for relative to this container, and optionally
 * nested containers.
 * </p>
 * 
 * @since 3.0
 */
public abstract class ContainerSourceContainer extends CompositeSourceContainer {

	private IContainer fContainer = null;
	private boolean fSubfolders = false;
	
	private IPath fRootPath = null;
	private String[] fRootSegments = null;
	private File fRootFile = null;
	private IWorkspaceRoot fRoot = null;

	/**
	 * Constructs a source container on the given workspace container. 
	 * 
	 * @param container the container to search for source in
	 * @param subfolders whether nested folders should be searched
	 *  for source elements
	 */
	public ContainerSourceContainer(IContainer container, boolean subfolders) {
		fContainer = container;
		fSubfolders = subfolders;
		fRootPath = fContainer.getLocation();
		if (fRootPath != null) {
			fRootSegments = fRootPath.segments();
			fRootFile = fRootPath.toFile();
			fRoot = ResourcesPlugin.getWorkspace().getRoot();
		}
	}
	
	/**
	 * Returns the workspace container this source container is
	 * rooted at.
	 *  
	 * @return the workspace container this source container is
	 * rooted at
	 */
	public IContainer getContainer() {
		return fContainer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	public Object[] findSourceElements(String name) throws CoreException {
		if (fRootPath == null) {
			return EMPTY;
		}
		ArrayList sources = new ArrayList();

		// An IllegalArgumentException is thrown from the "getFile" method 
		// if the path created by appending the file name to the container 
		// path doesn't conform with Eclipse resource restrictions.
		// To prevent the interruption of the search procedure we check 
		// if the path is valid before passing it to "getFile".		
		if ( validateFile(name) ) {
			File osFile = new File(fRootFile, name);
			if (osFile.exists()) {
				try {
					// See bug 82627 and bug 95679 - we have to append the container path in the case
					// that Eclipse thinks it is, with the file system case of the file in order to
					// be successful when finding the IFile for a location.
					// See bug 98090 - we need to handle relative path names
					Path canonicalPath = new Path(osFile.getCanonicalPath());
					String[] canonicalSegments = canonicalPath.segments();
					IPath workspacePath = new Path(""); //$NON-NLS-1$
					workspacePath = workspacePath.setDevice(canonicalPath.getDevice());
					for (int i = 0; i < canonicalSegments.length; i++) {
						String segment = canonicalSegments[i];
						if (i < fRootSegments.length) {
							if (fRootSegments[i].equalsIgnoreCase(segment)) {
								workspacePath = workspacePath.append(fRootSegments[i]);
							} else {
								workspacePath = workspacePath.append(segment);
							}
						} else {
							workspacePath = workspacePath.append(segment);
						}
					}
					IFile[] files = fRoot.findFilesForLocation(workspacePath);
					if (isFindDuplicates() && files.length > 1) {
						for (int i = 0; i < files.length; i++) {
							sources.add(files[i]);
						}
					} else if (files.length > 0) {
						sources.add(files[0]);
					}					
				} catch (IOException e) {
				}
			}
		}

		//check subfolders		
		if ((isFindDuplicates() && fSubfolders) || (sources.isEmpty() && fSubfolders)) {
			ISourceContainer[] containers = getSourceContainers();
			for (int i=0; i < containers.length; i++) {
				Object[] objects = containers[i].findSourceElements(name);
				if (objects == null || objects.length == 0) {
					continue;
				}
				if (isFindDuplicates()) {
					for(int j=0; j < objects.length; j++)
						sources.add(objects[j]);
				} else {
					sources.add(objects[0]);
					break;
				}
			}
		}			
		
		if(sources.isEmpty())
			return EMPTY;
		return sources.toArray();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {		
		return getContainer().getName(); 
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ContainerSourceContainer) {
			ContainerSourceContainer loc = (ContainerSourceContainer) obj;
			return loc.getContainer().equals(getContainer());
		}	
		return false;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getContainer().hashCode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#isComposite()
	 */
	public boolean isComposite() {	
		return fSubfolders;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer#createSourceContainers()
	 */
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		if(fSubfolders) {
			IResource[] resources = getContainer().members();
			List list = new ArrayList(resources.length);
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if (resource.getType() == IResource.FOLDER) {
					list.add(new FolderSourceContainer((IFolder)resource, fSubfolders));
				}
			}
			ISourceContainer[] containers = (ISourceContainer[]) list.toArray(new ISourceContainer[list.size()]);
			for (int i = 0; i < containers.length; i++) {
				ISourceContainer container = containers[i];
				container.init(getDirector());
			}			
			return containers;
		}
		return new ISourceContainer[0];
	}

	/**
	 * Validates the given string as a path for a file in this container. 
	 */
	private boolean validateFile(String name) {
		IContainer container = getContainer();
		IPath path = container.getFullPath().append(name);
		return ResourcesPlugin.getWorkspace().validatePath(path.toOSString(), IResource.FILE).isOK();
	}

}

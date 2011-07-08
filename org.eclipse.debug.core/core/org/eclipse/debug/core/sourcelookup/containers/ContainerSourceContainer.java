/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Bug 80857
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup.containers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
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

/**
 * A source container for a container in the workspace. Source elements are searched
 * for within this container and optionally nested containers.
 * <p>
 * Names specified in <code>findSourceElements</code> method can
 * be simple or qualified. When a name is qualified, a file will
 * be searched for relative to this container, and optionally
 * nested containers.
 * </p>
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class ContainerSourceContainer extends CompositeSourceContainer {

	private IContainer fContainer = null;
	private boolean fSubfolders = false;
	
	private URI fRootURI = null;
	private IFileStore fRootFile = null;
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
		fRootURI = fContainer.getLocationURI();
		if (fRootURI != null) {
			try {
				fRootFile = EFS.getStore(fRootURI);
			} catch (CoreException e) {
			}
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
		ArrayList sources = new ArrayList();

		// An IllegalArgumentException is thrown from the "getFile" method 
		// if the path created by appending the file name to the container 
		// path doesn't conform with Eclipse resource restrictions.
		// To prevent the interruption of the search procedure we check 
		// if the path is valid before passing it to "getFile".		
		if ( validateFile(name) ) {
			IFile file = fContainer.getFile(new Path(name));
			if (file.exists()) {
				sources.add(file);
			} else {
				// See bug 82627 - perform case insensitive source lookup
				if (fRootURI == null) {
					return EMPTY;
				}
				// bug 295828 root file may be null for an invalid linked resource
				if (fRootFile != null) {
	                // See bug 98090 - we need to handle relative path names
    				IFileStore target = fRootFile.getFileStore(new Path(name));
    				if (target.fetchInfo().exists()) {
    					// We no longer have to account for bug 95832, and URIs take care
    					// of canonical paths (fix to bug 95679 was removed).
    					IFile[] files = fRoot.findFilesForLocationURI(target.toURI());
    					if (isFindDuplicates() && files.length > 1) {
    						for (int i = 0; i < files.length; i++) {
    							sources.add(files[i]);
    						}
    					} else if (files.length > 0) {
    						sources.add(files[0]);
    					}					
    				}
				}
			}
		}

		//check sub-folders		
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
	 * 
	 * @param name path name
	 * @return <code>true</code> if the path is valid <code>false</code> otherwise
	 */
	private boolean validateFile(String name) {
		IContainer container = getContainer();
		IPath path = container.getFullPath().append(name);
		return ResourcesPlugin.getWorkspace().validatePath(path.toOSString(), IResource.FILE).isOK();
	}

}

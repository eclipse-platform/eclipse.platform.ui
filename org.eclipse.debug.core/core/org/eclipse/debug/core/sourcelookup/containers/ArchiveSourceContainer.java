/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup.containers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;

/**
 * Archive source container for an archive in the workspace. Returns instances
 * of <code>ZipEntryStorage</code> as source elements.
 * <p>
 * Clients may instantiate this class. 
 * </p>
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ArchiveSourceContainer extends AbstractSourceContainer {
	
	private IFile fFile;
	private boolean fDetectRoot; 
	private ExternalArchiveSourceContainer fDelegateContainer;
	
	/**
	 * Unique identifier for the archive source container type
	 * (value <code>org.eclipse.debug.core.containerType.archive</code>).
	 */
	public static final String TYPE_ID = DebugPlugin.getUniqueIdentifier() + ".containerType.archive";	 //$NON-NLS-1$
	
	/**
	 * Creates an archive source container on the given file. 
	 * 
	 * @param archive archive in the workspace
	 * @param detectRootPath whether a root path should be detected. When
	 *   <code>true</code>, searching is performed relative to a root path
	 *   within the archive based on fully qualified file names. The root
	 *   path is automatically determined when the first successful search
	 *   is performed. For example, when searching for a file named
	 *   <code>a/b/c.d</code>, and an entry in the archive named
	 *   <code>r/a/b/c.d</code> exists, the root path is set to <code>r</code>.
	 *   From that point on, searching is performed relative to <code>r</code>.
	 *   When <code>false</code>, searching is performed by
	 *   matching file names as suffixes to the entries in the archive. 
	 */
	public ArchiveSourceContainer(IFile archive, boolean detectRootPath) {
		fFile = archive;
		fDetectRoot = detectRootPath;
		if (archive.exists() && archive.getLocation() != null) {
		    fDelegateContainer = new ExternalArchiveSourceContainer(archive.getLocation().toOSString(), detectRootPath);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return fFile.getName();
	}
	
	/**
	 * Returns the associated file in the workspace.
	 *  
	 * @return associated file in the workspace
	 */
	public IFile getFile() {
		return fFile;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return obj instanceof ArchiveSourceContainer &&
			((ArchiveSourceContainer)obj).getName().equals(getName());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getName().hashCode();
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
     */
    public Object[] findSourceElements(String name) throws CoreException {
        ExternalArchiveSourceContainer container = getDelegateContainer();
        if (container != null) {
            return container.findSourceElements(name);
        }
        return EMPTY;
    }
    
    /**
     * Returns the underlying external archive source container.
     * 
     * @return underlying external archive source container
     * @since 3.0.1.1
     */
    private ExternalArchiveSourceContainer getDelegateContainer() {
        return fDelegateContainer;
    }
    /* (non-Javadoc)
     * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#init(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
     */
    public void init(ISourceLookupDirector director) {
        super.init(director);
        if (fDelegateContainer != null) {
            fDelegateContainer.init(director);
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#dispose()
     */
    public void dispose() {
        super.dispose();
        if (fDelegateContainer != null) {
            fDelegateContainer.dispose();
        }
    }
    
	/**
	 * Returns whether root paths are automatically detected in this
	 * archive source container.
	 *  
	 * @return whether root paths are automatically detected in this
	 * archive source container
	 * @since 3.0.1.1
	 */
	public boolean isDetectRoot() {
		return fDetectRoot;
	}    
}

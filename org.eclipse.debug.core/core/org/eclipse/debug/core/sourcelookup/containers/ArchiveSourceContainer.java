/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup.containers;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;

/**
 * Archive source container for an archive in the workspace. Returns instances
 * of <code>ZipEntryStorage</code> as source elemetns.
 * <p>
 * Clients may instantiate this class. This class is not intended to
 * be subclassed.
 * </p>
 * @since 3.0
 */
public class ArchiveSourceContainer extends ExternalArchiveSourceContainer {
	
	private IFile fFile;
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
		super(archive.getLocation().toOSString(), detectRootPath);
		fFile = archive;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
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
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
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
}

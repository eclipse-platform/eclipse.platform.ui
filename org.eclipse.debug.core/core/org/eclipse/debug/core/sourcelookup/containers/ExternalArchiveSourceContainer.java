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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupMessages;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;

/**
 * Archive source container. Returns instances of <code>ZipEntryStorage</code>
 * as source elemetns.
 * 
 * @since 3.0
 */
public class ExternalArchiveSourceContainer extends AbstractSourceContainer {
	
	private boolean fDetectRoot = false;
	private String fRoot = null;
	private String fArchivePath = null;
	/**
	 * Unique identifier for the external archive source container type
	 * (value <code>org.eclipse.debug.core.containerType.externalArchive</code>).
	 */
	public static final String TYPE_ID = DebugPlugin.getUniqueIdentifier() + ".containerType.externalArchive";	 //$NON-NLS-1$
	
	/**
	 * Creates an archive source container on the archive at the 
	 * specified location in the local file system. 
	 * 
	 * @param archivePath path to the archive in the local file system
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
	public ExternalArchiveSourceContainer(String archivePath, boolean detectRootPath) {
		fArchivePath = archivePath;
		fDetectRoot = detectRootPath;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	public Object[] findSourceElements(String name) throws CoreException {
		name = name.replace('\\', '/');
		ZipFile file = getArchive();
		if (fDetectRoot) {
			if (fRoot == null) {
				detectRoot(file, name);
			}
			if (fRoot != null) {
				if (fRoot.length() > 0) {
					name = fRoot + name;
				}
				ZipEntry entry = file.getEntry(name);
				if (entry != null) {
					return new Object[]{new ZipEntryStorage(file, entry)};
				}
			}
		} else {
			Enumeration entries = file.entries();
			// try exact match
			ZipEntry entry = file.getEntry(name);
			if (entry != null) {
				// can't be any dups if there is an exact match
				return new Object[]{new ZipEntryStorage(file, entry)};
			}
			// search
			List matches = null;
			while (entries.hasMoreElements()) {
				entry = (ZipEntry)entries.nextElement();
				if (entry.getName().endsWith(name)) {
					if (isFindDuplicates()) {
						if (matches == null) {
							matches = new ArrayList();
						}
						matches.add(new ZipEntryStorage(file, entry));
					} else {
						return new Object[]{new ZipEntryStorage(file, entry)};
					}
				}
			}
			if (matches != null) {
				return matches.toArray();
			}
		}
		return EMPTY;
	}
	
	/**
	 * Detects the root path in this archive by searching for an entry
	 * with the given name, as a suffix.
	 * 
	 * @param file zip file to search in
	 * @param name entry to search for
	 * @exception CoreException if an exception occurrs while detecting the root
	 */
	private void detectRoot(ZipFile file, String name) throws CoreException {
		synchronized (file) {
			Enumeration entries = file.entries();
			try {
				while (entries.hasMoreElements()) {
					ZipEntry entry = (ZipEntry)entries.nextElement();
					String entryName = entry.getName();
					if (entryName.endsWith(name)) {
						int rootLength = entryName.length() - name.length();
						if (rootLength > 0) {
							fRoot = entryName.substring(0, rootLength);
						} else {
							fRoot = ""; //$NON-NLS-1$
						}
						return;
					}
				}
			} catch (IllegalStateException e) {
				abort(MessageFormat.format(SourceLookupMessages.getString("ExternalArchiveSourceContainer.1"), new String[] {getName()}), e); //$NON-NLS-1$
			}
		}		
	}

	/**
	 * Returns the archive to search in.
	 * 
	 * @throws CoreException if unable to access the archive
	 */
	private ZipFile getArchive() throws CoreException {
		try {
			return SourceLookupUtils.getZipFile(fArchivePath);
		} catch (IOException e) {
			abort(MessageFormat.format(SourceLookupMessages.getString("ExternalArchiveSourceContainer.2"), new String[]{fArchivePath}), e); //$NON-NLS-1$
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return fArchivePath;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
	
	/**
	 * Returns whether the root path in this archive source container
	 * is to be automatically detected.
	 *  
	 * @return whether the root path in this archive source container
	 * is to be automatically detected
	 */
	public boolean isDetectRoot() {
		return fDetectRoot;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return obj instanceof ExternalArchiveSourceContainer &&
			((ExternalArchiveSourceContainer)obj).getName().equals(getName());
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getName().hashCode();
	}
}

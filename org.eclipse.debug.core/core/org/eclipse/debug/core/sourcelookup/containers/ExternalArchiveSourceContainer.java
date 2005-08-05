/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup.containers;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupMessages;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;

/**
 * An archive in the local file system. Returns instances
 * of <code>ZipEntryStorage</code> as source elemetns.
 * <p>
 * Clients may instantiate this class. This class is not intended to
 * be subclassed.
 * </p>
 * @since 3.0
 */
public class ExternalArchiveSourceContainer extends AbstractSourceContainer {
	
	private boolean fDetectRoots = false;
	private Map fRoots = new HashMap(5);
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
	 * @param detectRootPaths whether root container paths should be detected. When
	 *   <code>true</code>, searching is performed relative to a root path
	 *   within the archive based on fully qualified file names. A root
	 *   path is automatically determined for each file type when the first
	 *   successful search is performed. For example, when searching for a file
	 *   named <code>a/b/c.d</code>, and an entry in the archive named
	 *   <code>r/a/b/c.d</code> exists, the root path is set to <code>r</code>
	 *   for file type <code>d</code>.
	 *   From that point on, searching is performed relative to <code>r</code>
	 *   for files of type <code>d</code>.
	 *   When searching for an unqualified file name, root containers are not
	 *   considered.
	 *   When <code>false</code>, searching is performed by
	 *   matching file names as suffixes to the entries in the archive. 
	 */
	public ExternalArchiveSourceContainer(String archivePath, boolean detectRootPaths) {
		fArchivePath = archivePath;
		fDetectRoots = detectRootPaths;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	public Object[] findSourceElements(String name) throws CoreException {
		name = name.replace('\\', '/');
		ZipFile file = getArchive();
		synchronized (file) {
			boolean isQualfied = name.indexOf('/') > 0;
			if (fDetectRoots && isQualfied) {
				String root = getRoot(file, name);
				if (root != null) {
					if (root.length() > 0) {
						name = root + name;
					}
					ZipEntry entry = file.getEntry(name);
					if (entry != null) {
						return new Object[]{new ZipEntryStorage(file, entry)};
					}
				}
			} else {
				// try exact match
				ZipEntry entry = file.getEntry(name);
				if (entry != null) {
					// can't be any dups if there is an exact match
					return new Object[]{new ZipEntryStorage(file, entry)};
				}
				// search
				Enumeration entries = file.entries();
				List matches = null;
				while (entries.hasMoreElements()) {
					entry = (ZipEntry)entries.nextElement();
					String entryName = entry.getName();
					if (entryName.endsWith(name)) {
						if (isQualfied || entryName.length() == name.length() || entryName.charAt(entryName.length() - name.length() - 1) == '/') {
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
				}
				if (matches != null) {
					return matches.toArray();
				}
			}
		}
		return EMPTY;
	}
	
	/**
	 * Returns the root path in this archive for the given file name, based
	 * on its type, or <code>null</code> if none. Detects a root if a root has
	 * not yet been detected for the given file type.
	 * 
	 * @param file zip file to search in
	 * @param name file name
	 * @exception CoreException if an exception occurrs while detecting the root
	 */
	private String getRoot(ZipFile file, String name) throws CoreException {
		int index = name.lastIndexOf('.');
		String fileType = null;
		if (index >= 0) {
			fileType = name.substring(index);
		} else {
			// no filetype, use "" as key
			fileType = ""; //$NON-NLS-1$
		}
		String root = (String) fRoots.get(fileType);
		if (root == null) {
			root = detectRoot(file, name);
			if (root != null) {
				fRoots.put(fileType, root);
			}
		}
		return root;
	}
	
	/**
	 * Detects and returns the root path in this archive by searching for an entry
	 * with the given name, as a suffix.
	 * 
	 * @param file zip file to search in
	 * @param name entry to search for
	 * @return root
	 * @exception CoreException if an exception occurrs while detecting the root
	 */
	private String detectRoot(ZipFile file, String name) throws CoreException {
		synchronized (file) {
			Enumeration entries = file.entries();
			try {
				while (entries.hasMoreElements()) {
					ZipEntry entry = (ZipEntry)entries.nextElement();
					String entryName = entry.getName();
					if (entryName.endsWith(name)) {
						int rootLength = entryName.length() - name.length();
						if (rootLength > 0) {
							return entryName.substring(0, rootLength);
						} 
						return ""; //$NON-NLS-1$
					}
				}
			} catch (IllegalStateException e) {
				abort(MessageFormat.format(SourceLookupMessages.ExternalArchiveSourceContainer_1, new String[] {getName()}), e); 
			}
		}
		return null;
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
			abort(MessageFormat.format(SourceLookupMessages.ExternalArchiveSourceContainer_2, new String[]{fArchivePath}), e); 
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return fArchivePath;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
	
	/**
	 * Returns whether root paths are automatically detected in this
	 * archive source container.
	 *  
	 * @return whether root paths are automatically detected in this
	 * archive source container
	 */
	public boolean isDetectRoot() {
		return fDetectRoots;
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
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#dispose()
	 */
	public void dispose() {
		super.dispose();
		fRoots.clear();
	}	
}

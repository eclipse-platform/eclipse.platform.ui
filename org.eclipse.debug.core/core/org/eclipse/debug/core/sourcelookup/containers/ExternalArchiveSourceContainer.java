/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup.containers;

import java.io.File;
import java.io.IOException;
import com.ibm.icu.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupMessages;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;

/**
 * An archive in the local file system. Returns instances
 * of <code>ZipEntryStorage</code> as source elements.
 * <p>
 * Clients may instantiate this class. 
 * </p>
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ExternalArchiveSourceContainer extends AbstractSourceContainer {
	
	private boolean fDetectRoots = false;
	private Set fPotentialRoots = null;
	private List fRoots = new ArrayList();
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
	 *   path is automatically determined for when the first
	 *   successful search is performed. For example, when searching for a file
	 *   named <code>a/b/c.d</code>, and an entry in the archive named
	 *   <code>r/a/b/c.d</code> exists, a root path is set to <code>r</code>.
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
		// NOTE: archive can be closed between get (above) and synchronized block (below)
		synchronized (file) {
			boolean isQualfied = name.indexOf('/') > 0;
			if (fDetectRoots && isQualfied) {
				ZipEntry entry = searchRoots(file, name);
				if (entry != null) {
					return new Object[]{new ZipEntryStorage(file, entry)};
				}
			} else {
				// try exact match
				ZipEntry entry = null;
				try {
					entry = file.getEntry(name);
				} catch (IllegalStateException e) {
					// archive was closed between retrieving and locking
					throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), 
							e.getMessage(), e));
				}
				if (entry != null) {
					// can't be any duplicates if there is an exact match
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
	 * @return the {@link ZipEntry} with the given name or <code>null</code>
	 * @exception CoreException if an exception occurs while detecting the root
	 */
	private ZipEntry searchRoots(ZipFile file, String name) throws CoreException {
		if (fPotentialRoots == null) {
			fPotentialRoots = new HashSet();
			fPotentialRoots.add(""); //$NON-NLS-1$
			// all potential roots are the directories
			try {
				Enumeration entries = file.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = (ZipEntry) entries.nextElement();
					if (entry.isDirectory()) {
						fPotentialRoots.add(entry.getName());
					} else {
						String entryName = entry.getName();
						int index = entryName.lastIndexOf("/"); //$NON-NLS-1$
						while (index > 0) {
							if (fPotentialRoots.add(entryName.substring(0, index + 1))) {
								entryName = entryName.substring(0, index);
								index = entryName.lastIndexOf("/"); //$NON-NLS-1$
							} else {
								break;
							}
						}
					}
				}
			} catch (IllegalStateException e) {
				// archive was closed between retrieving and locking
				throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), 
					e.getMessage(), e));
			}
		}
		int i = 0;
		while (i < fRoots.size()) {
			String root = (String) fRoots.get(i);
			ZipEntry entry = file.getEntry(root+name);
			if (entry != null) {
				return entry;
			}
			i++;
		}
		if (!fPotentialRoots.isEmpty()) {
			Iterator roots = fPotentialRoots.iterator();
			String root = null;
			ZipEntry entry = null;
			while (roots.hasNext()) {
				root = (String) roots.next();
				entry = file.getEntry(root+name);
				if (entry != null) {
					break;
				}
			}
			if (entry != null) {
				if (root != null) {
					fRoots.add(root);
					fPotentialRoots.remove(root);
					// remove any roots that begin with the new root, as roots cannot be nested
					Iterator rs = fPotentialRoots.iterator();
					while (rs.hasNext()) {
						String r = (String) rs.next();
						if (r.startsWith(root)) {
							rs.remove();
						}
					}
				}				
				return entry;
			}
		}
		return null;
	}

	/**
	 * Returns the archive to search in.
	 * @return the {@link ZipFile} to search in
	 * 
	 * @throws CoreException if unable to access the archive
	 */
	private ZipFile getArchive() throws CoreException {
		try {
			return SourceLookupUtils.getZipFile(fArchivePath);
		} catch (IOException e) {
			File file = new File(fArchivePath);
			if (file.exists()) {
				abort(MessageFormat.format(SourceLookupMessages.ExternalArchiveSourceContainer_2, new String[]{fArchivePath}), e);
			} else {
				warn(MessageFormat.format(SourceLookupMessages.ExternalArchiveSourceContainer_1, new String[]{fArchivePath}), e);
			}
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
		if (fPotentialRoots != null) {
			fPotentialRoots.clear();
		}
		fRoots.clear();
	}	
}

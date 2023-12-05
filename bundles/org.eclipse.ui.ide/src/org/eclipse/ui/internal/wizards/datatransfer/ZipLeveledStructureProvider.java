/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat, Inc - Was ZipFileStructureProvider, performed changes from
 *     IImportStructureProvider to ILeveledImportStructureProvider
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * This class provides information regarding the context structure and content
 * of specified zip file entry objects.
 *
 * @since 3.1
 */
public class ZipLeveledStructureProvider implements
		ILeveledImportStructureProvider {
	private ZipFile zipFile;

	private ZipEntry root = new ZipEntry("/");//$NON-NLS-1$

	private Map<ZipEntry, List<ZipEntry>> children;

	private Map<IPath, ZipEntry> directoryEntryCache = new HashMap<>();

	private int stripLevel;

	private Set<String> invalidEntries = new HashSet<>();

	/**
	 * Creates a <code>ZipFileStructureProvider</code>, which will operate on
	 * the passed zip file.
	 *
	 * @param sourceFile
	 *            The source file to create the ZipLeveledStructureProvider
	 *            around
	 */
	public ZipLeveledStructureProvider(ZipFile sourceFile) {
		super();
		zipFile = sourceFile;
		stripLevel = 0;
	}

	/**
	 * Creates a new container zip entry with the specified name, iff it has
	 * not already been created. If the parent of the given element does not
	 * already exist it will be recursively created as well.
	 * @param pathname The path representing the container
	 * @return The element represented by this pathname (it may have already existed)
	 */
	protected ZipEntry createContainer(IPath pathname) {
		ZipEntry existingEntry = directoryEntryCache.get(pathname);
		if (existingEntry != null) {
			return existingEntry;
		}

		ZipEntry parent;
		if (pathname.segmentCount() == 0) {
			return null;
		} else if (pathname.segmentCount() == 1) {
			parent = root;
		} else {
			parent = createContainer(pathname.removeLastSegments(1));
		}
		ZipEntry newEntry = new ZipEntry(pathname.toString());
		directoryEntryCache.put(pathname, newEntry);
		List<ZipEntry> childList = new ArrayList<>();
		children.put(newEntry, childList);

		List<ZipEntry> parentChildList = children.get(parent);
		parentChildList.add(newEntry);
		return newEntry;
	}

	/**
	 * Creates a new file zip entry with the specified name.
	 */
	protected void createFile(ZipEntry entry) {
		IPath pathname = IPath.fromOSString(entry.getName());
		ZipEntry parent;
		if (pathname.segmentCount() == 1) {
			parent = root;
		} else {
			parent = directoryEntryCache.get(pathname
					.removeLastSegments(1));
		}

		List<ZipEntry> childList = children.get(parent);
		childList.add(entry);
	}

	@Override
	public List<?> getChildren(Object element) {
		if (children == null) {
			initialize();
		}

		return (children.get(element));
	}

	@Override
	public InputStream getContents(Object element) {
		try {
			if (invalidEntries.contains(((ZipEntry) element).getName())) {
				throw new IOException("Cannot get content of Entry as it is outside of the target dir: " //$NON-NLS-1$
						+ ((ZipEntry) element).getName());
			}
			return zipFile.getInputStream((ZipEntry) element);
		} catch (IOException e) {
			IDEWorkbenchPlugin.log(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/*
	 * Strip the leading directories from the path
	 */
	private String stripPath(String path) {
		String pathOrig = path;
		for (int i = 0; i < stripLevel; i++) {
			int firstSep = path.indexOf('/');
			// If the first character was a separator we must strip to the next
			// separator as well
			if (firstSep == 0) {
				path = path.substring(1);
				firstSep = path.indexOf('/');
			}
			// No separator was present so we're in a higher directory right
			// now
			if (firstSep == -1) {
				return pathOrig;
			}
			path = path.substring(firstSep);
		}
		return path;
	}

	@Override
	public String getFullPath(Object element) {
		String name = ((ZipEntry) element).getName();
		String base = "base"; //$NON-NLS-1$
		if (!java.nio.file.Path.of(base, name).normalize().startsWith(base)) {
			throw new RuntimeException("Bad zip entry in " + zipFile.getName() + ": " + name); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return stripPath(name);
	}

	@Override
	public String getLabel(Object element) {
		if (element.equals(root)) {
			return ((ZipEntry) element).getName();
		}

		return stripPath(IPath.fromOSString(((ZipEntry) element).getName()).lastSegment());
	}

	/**
	 * Returns the entry that this importer uses as the root sentinel.
	 *
	 * @return java.util.zip.ZipEntry
	 */
	@Override
	public Object getRoot() {
		return root;
	}

	/**
	 * Returns the zip file that this provider provides structure for.
	 *
	 * @return The zip file
	 */
	public ZipFile getZipFile() {
		return zipFile;
	}


	@Override
	public boolean closeArchive(){
		try {
			getZipFile().close();
		} catch (IOException e) {
			IDEWorkbenchPlugin.log(DataTransferMessages.ZipImport_couldNotClose
					+ zipFile.getName(), e);
			return false;
		}
		return true;
	}

	@Override
	public void close() throws Exception {
		closeArchive();
	}

	/**
	 * Initializes this object's children table based on the contents of the
	 * specified source file.
	 */
	protected void initialize() {
		children = new HashMap<>(1000);

		IPath zipFileDirPath = IPath.fromOSString(zipFile.getName()).removeLastSegments(1);
		String canonicalDestinationDirPath = zipFileDirPath.toString();
		File zipDestinationDir = new File(zipFileDirPath.toString());

		try {
			canonicalDestinationDirPath = zipDestinationDir.getCanonicalPath();
		} catch (IOException e) {
			return;
		}
		children.put(root, new ArrayList<>());
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		if (!canonicalDestinationDirPath.endsWith(File.separator)) {
			canonicalDestinationDirPath += File.separator;
		}

		while (entries.hasMoreElements()) {
			try {
				ZipEntry entry = entries.nextElement();
				File destinationfile = new File(zipDestinationDir, entry.getName());
				String canonicalDestinationFile = destinationfile.getCanonicalPath();
				if (!canonicalDestinationFile.startsWith(canonicalDestinationDirPath)) {
					invalidEntries.add(entry.getName());
					throw new IOException("Entry is outside of the target dir: " + entry.getName()); //$NON-NLS-1$
				}
				IPath path = IPath.fromOSString(entry.getName()).addTrailingSeparator();

				if (entry.isDirectory()) {
					createContainer(path);
				} else {
					// Ensure the container structure for all levels above this is initialized
					// Once we hit a higher-level container that's already added we need go no
					// further
					int pathSegmentCount = path.segmentCount();
					if (pathSegmentCount > 1) {
						createContainer(path.uptoSegment(pathSegmentCount - 1));
					}
					createFile(entry);
				}
			} catch (IOException e) {
				IDEWorkbenchPlugin.log(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public boolean isFolder(Object element) {
		return ((ZipEntry) element).isDirectory();
	}

	@Override
	public void setStrip(int level) {
		stripLevel = level;
	}

	@Override
	public int getStrip() {
		return stripLevel;
	}
}

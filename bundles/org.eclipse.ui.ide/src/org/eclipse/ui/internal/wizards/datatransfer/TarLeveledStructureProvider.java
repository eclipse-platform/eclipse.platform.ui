/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Red Hat, Inc - Was TarFileStructureProvider, performed changes from
 *     IImportStructureProvider to ILeveledImportStructureProvider
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * This class provides information regarding the context structure and content
 * of specified tar file entry objects.
 *
 * @since 3.1
 */
public class TarLeveledStructureProvider implements
		ILeveledImportStructureProvider {
	private TarFile tarFile;

	private TarEntry root = new TarEntry("/");//$NON-NLS-1$

	private Map children;

	private Map directoryEntryCache = new HashMap();

	private int stripLevel;

	/**
	 * Creates a <code>TarFileStructureProvider</code>, which will operate on
	 * the passed tar file.
	 *
	 * @param sourceFile
	 *            the source TarFile
	 */
	public TarLeveledStructureProvider(TarFile sourceFile) {
		super();
		tarFile = sourceFile;
		root.setFileType(TarEntry.DIRECTORY);
	}

	/**
	 * Creates a new container tar entry with the specified name, iff it has
	 * not already been created. If the parent of the given element does not
	 * already exist it will be recursively created as well.
	 * @param pathname The path representing the container
	 * @return The element represented by this pathname (it may have already existed)
	 */
	protected TarEntry createContainer(IPath pathname) {
		TarEntry existingEntry = (TarEntry) directoryEntryCache.get(pathname);
		if (existingEntry != null) {
			return existingEntry;
		}

		TarEntry parent;
		if (pathname.segmentCount() == 1) {
			parent = root;
		} else {
			parent = createContainer(pathname.removeLastSegments(1));
		}
		TarEntry newEntry = new TarEntry(pathname.toString());
		newEntry.setFileType(TarEntry.DIRECTORY);
		directoryEntryCache.put(pathname, newEntry);
		List childList = new ArrayList();
		children.put(newEntry, childList);

		List parentChildList = (List) children.get(parent);
		parentChildList.add(newEntry);
		return newEntry;
	}

	/**
	 * Creates a new tar file entry with the specified name.
	 */
	protected void createFile(TarEntry entry) {
		IPath pathname = IPath.fromOSString(entry.getName());
		TarEntry parent;
		if (pathname.segmentCount() == 1) {
			parent = root;
		} else {
			parent = (TarEntry) directoryEntryCache.get(pathname
					.removeLastSegments(1));
		}

		List childList = (List) children.get(parent);
		childList.add(entry);
	}

	@Override
	public List getChildren(Object element) {
		if (children == null) {
			initialize();
		}

		return ((List) children.get(element));
	}

	@Override
	public InputStream getContents(Object element) {
		try {
			return tarFile.getInputStream((TarEntry) element);
		} catch (TarException | IOException e) {
			IDEWorkbenchPlugin.log(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the resource attributes for this file.
	 *
	 * @return the attributes of the file
	 */
	public ResourceAttributes getResourceAttributes(Object element) {
		ResourceAttributes attributes = new ResourceAttributes();
		TarEntry entry = (TarEntry) element;
		attributes.setExecutable((entry.getMode() & 0100) != 0);
		attributes.setReadOnly((entry.getMode() & 0200) == 0);
		return attributes;
	}

	@Override
	public String getFullPath(Object element) {
		return stripPath(((TarEntry) element).getName());
	}

	@Override
	public String getLabel(Object element) {
		if (element.equals(root)) {
			return ((TarEntry) element).getName();
		}

		return stripPath(IPath.fromOSString(((TarEntry) element).getName()).lastSegment());
	}

	/**
	 * Returns the entry that this importer uses as the root sentinel.
	 *
	 * @return TarEntry entry
	 */
	@Override
	public Object getRoot() {
		return root;
	}

	/**
	 * Returns the tar file that this provider provides structure for.
	 *
	 * @return TarFile file
	 */
	public TarFile getTarFile() {
		return tarFile;
	}

	@Override
	public boolean closeArchive(){
		TarFile tf = getTarFile();
		try (tf) {
			// autoclose
		} catch (IOException e) {
			IDEWorkbenchPlugin.log(DataTransferMessages.ZipImport_couldNotClose
					+ tf.getName(), e);
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
		children = new HashMap(1000);

		children.put(root, new ArrayList());
		Enumeration entries = tarFile.entries();
		while (entries.hasMoreElements()) {
			TarEntry entry = (TarEntry) entries.nextElement();
			IPath path = IPath.fromOSString(entry.getName()).addTrailingSeparator();

			if (entry.getFileType() == TarEntry.DIRECTORY) {
				createContainer(path);
			} else
			{
				// Ensure the container structure for all levels above this is initialized
				// Once we hit a higher-level container that's already added we need go no further
				int pathSegmentCount = path.segmentCount();
				if (pathSegmentCount > 1) {
					createContainer(path.uptoSegment(pathSegmentCount - 1));
				}
				createFile(entry);
			}
		}
	}

	@Override
	public boolean isFolder(Object element) {
		return (((TarEntry) element).getFileType() == TarEntry.DIRECTORY);
	}

	/*
	 * Strip the leading directories from the path
	 */
	private String stripPath(String path) {
		String pathOrig = path;
		for (int i = 0; i < stripLevel; i++) {
			int firstSep = path.indexOf('/');
			// If the first character was a seperator we must strip to the next
			// seperator as well
			if (firstSep == 0) {
				path = path.substring(1);
				firstSep = path.indexOf('/');
			}
			// No seperator wasw present so we're in a higher directory right
			// now
			if (firstSep == -1) {
				return pathOrig;
			}
			path = path.substring(firstSep);
		}
		return path;
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

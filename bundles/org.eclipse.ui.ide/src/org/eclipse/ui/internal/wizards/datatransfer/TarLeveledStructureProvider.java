/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat, Inc - Was TarFileStructureProvider, performed changes from 
 *     IImportStructureProvider to ILeveledImportStructureProvider
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
import org.eclipse.core.runtime.Path;

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
	 * Adds the specified child to the internal collection of the parent's
	 * children.
	 */
	protected void addToChildren(TarEntry parent, TarEntry child) {
		List childList = (List) children.get(parent);
		if (childList == null) {
			childList = new ArrayList();
			children.put(parent, childList);
		}

		childList.add(child);
	}

	/**
	 * Creates a new container tar entry with the specified name, iff it has not
	 * already been created.
	 */
	protected void createContainer(IPath pathname) {
		if (directoryEntryCache.containsKey(pathname))
			return;

		TarEntry parent;
		if (pathname.segmentCount() == 1)
			parent = root;
		else
			parent = (TarEntry) directoryEntryCache.get(pathname
					.removeLastSegments(1));

		TarEntry newEntry = new TarEntry(pathname.toString());
		newEntry.setFileType(TarEntry.DIRECTORY);
		directoryEntryCache.put(pathname, newEntry);
		addToChildren(parent, newEntry);
	}

	/**
	 * Creates a new tar file entry with the specified name.
	 */
	protected void createFile(TarEntry entry) {
		IPath pathname = new Path(entry.getName());
		TarEntry parent;
		if (pathname.segmentCount() == 1)
			parent = root;
		else
			parent = (TarEntry) directoryEntryCache.get(pathname
					.removeLastSegments(1));

		addToChildren(parent, entry);
	}

	/*
	 * (non-Javadoc) Method declared on IImportStructureProvider
	 */
	public List getChildren(Object element) {
		if (children == null)
			initialize();

		return ((List) children.get(element));
	}

	/*
	 * (non-Javadoc) Method declared on IImportStructureProvider
	 */
	public InputStream getContents(Object element) {
		try {
			return tarFile.getInputStream((TarEntry) element);
		} catch (TarException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the resource attributes for this file.
	 * 
	 * @param element
	 * @return the attributes of the file
	 */
	public ResourceAttributes getResourceAttributes(Object element) {
		ResourceAttributes attributes = new ResourceAttributes();
		TarEntry entry = (TarEntry) element;
		attributes.setExecutable((entry.getMode() & 0100) != 0);
		attributes.setReadOnly((entry.getMode() & 0200) == 0);
		return attributes;
	}

	/*
	 * (non-Javadoc) Method declared on IImportStructureProvider
	 */
	public String getFullPath(Object element) {
		return stripPath(((TarEntry) element).getName());
	}

	/*
	 * (non-Javadoc) Method declared on IImportStructureProvider
	 */
	public String getLabel(Object element) {
		if (element.equals(root))
			return ((TarEntry) element).getName();

		return stripPath(new Path(((TarEntry) element).getName()).lastSegment());
	}

	/**
	 * Returns the entry that this importer uses as the root sentinel.
	 * 
	 * @return TarEntry entry
	 */
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

	/**
	 * Initializes this object's children table based on the contents of the
	 * specified source file.
	 */
	protected void initialize() {
		children = new HashMap(1000);

		Enumeration entries = tarFile.entries();
		while (entries.hasMoreElements()) {
			TarEntry entry = (TarEntry) entries.nextElement();
			if (entry.getFileType() == TarEntry.FILE) {
				IPath path = new Path(entry.getName()).addTrailingSeparator();
				int pathSegmentCount = path.segmentCount();

				for (int i = 1; i < pathSegmentCount; i++)
					createContainer(path.uptoSegment(i));
				createFile(entry);
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on IImportStructureProvider
	 */
	public boolean isFolder(Object element) {
		return (((TarEntry) element).getFileType() == TarEntry.DIRECTORY);
	}

	/*
	 * Strip the leading directories from the path
	 */
	private String stripPath(String path) {
		String pathOrig = new String(path);
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
			if (firstSep == -1)
				return pathOrig;
			path = path.substring(firstSep);
		}
		return path;
	}

	public void setStrip(int level) {
		stripLevel = level;
	}

	public int getStrip() {
		return stripLevel;
	}
}

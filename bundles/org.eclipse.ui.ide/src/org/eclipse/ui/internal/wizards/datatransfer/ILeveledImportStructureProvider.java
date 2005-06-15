/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat, Inc - setStrip(int), getStrip()
 *******************************************************************************/

package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.InputStream;
import java.util.List;

import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * Interface which can provide structure and content information for an element
 * (for example, a file system element). Used by the import wizards to abstract
 * the commonalities between importing from the file system and importing from
 * an archive.
 * 
 * @since 3.1
 */
interface ILeveledImportStructureProvider extends IImportStructureProvider {
	/**
	 * Returns a collection with the children of the specified structured
	 * element.
	 */
	public abstract List getChildren(Object element);

	/**
	 * Returns the contents of the specified structured element, or
	 * <code>null</code> if there is a problem determining the element's
	 * contents.
	 * 
	 * @param element
	 *            a structured element
	 * @return the contents of the structured element, or <code>null</code>
	 */
	public abstract InputStream getContents(Object element);

	/**
	 * Returns the full path of the specified structured element.
	 * 
	 * @param element
	 *            a structured element
	 * @return the display label of the structured element
	 */
	public abstract String getFullPath(Object element);

	/**
	 * Returns the display label of the specified structured element.
	 * 
	 * @param element
	 *            a structured element
	 * @return the display label of the structured element
	 */
	public abstract String getLabel(Object element);

	/**
	 * Returns a boolean indicating whether the passed structured element
	 * represents a container element (as opposed to a leaf element).
	 * 
	 * @return boolean
	 * @param element
	 *            java.lang.Object
	 */
	public abstract boolean isFolder(Object element);

	/**
	 * Returns the entry that this importer uses as the root sentinel.
	 * 
	 * @return TarEntry entry
	 */
	public abstract Object getRoot();

	/**
	 * Tells the provider to strip N number of directories from the path of any
	 * path or file name returned by the IImportStructureProvider (Default=0).
	 * 
	 * @param level
	 *            The number of directories to strip
	 */
	public abstract void setStrip(int level);

	/**
	 * Returns the number of directories that this IImportStructureProvider is
	 * stripping from the file name
	 * 
	 * @return int Number of entries
	 */
	public abstract int getStrip();
}

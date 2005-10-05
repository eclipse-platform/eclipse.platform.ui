/*******************************************************************************
 * Copyright (c) 2004 Red Hat Incorporated
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API 
 *     Red Hat Incorporated - initial implementation
 *******************************************************************************/

package org.eclipse.core.resources;

import org.eclipse.core.filesystem.FileSystemCore;
import org.eclipse.core.filesystem.IFileStore;

/**
 * This class represents platform specific attributes of files.
 * Any attributes can be added, but only the attributes that are 
 * supported by the platform will be used. These methods do not set the 
 * attributes in the file system.
 * <p>
 * This class is not intended to be subclassed. This class may be instantiated.
 * </p>
 * 
 * @author Red Hat Incorporated
 * @see IResource#getResourceAttributes()
 * @see IResource#setResourceAttributes(ResourceAttributes)
 * @since 3.1
 */
public class ResourceAttributes {
	private boolean executable = false;
	private boolean readOnly = false;
	private boolean archive = false;

	/**
	 * Creates a new resource attributes instance with attributes
	 * taken from the specified file in the file system.  If the specified
	 * file does not exist or is not accessible, this method has the
	 * same effect as calling the default constructor.
	 * 
	 * @param file The file to get attributes from
	 * @return A resource attributes object
	 */
	public static ResourceAttributes fromFile(java.io.File file) {
		IFileStore store = FileSystemCore.getLocalFileSystem().getStore(file.toURI());
		ResourceAttributes attributes = new ResourceAttributes();
		attributes.setReadOnly(store.fetchInfo().isReadOnly());
		return attributes;
	}

	/**
	 * Creates a new instance of <code>ResourceAttributes</code>.
	 */
	public ResourceAttributes() {
		super();
	}

	/**
	 * Returns whether this ResourceAttributes object is marked archive.
	 *
	 * @return <code>true</code> if this resource is marked archive, 
	 *		<code>false</code> otherwise
	 * @see #setArchive(boolean)
	 */
	public boolean isArchive() {
		return archive;
	}

	/**
	 * Returns whether this ResourceAttributes object is marked executable.
	 *
	 * @return <code>true</code> if this resource is marked executable, 
	 *		<code>false</code> otherwise
	 * @see #setExecutable(boolean)
	 */
	public boolean isExecutable() {
		return executable;
	}

	/**
	 * Returns whether this ResourceAttributes object is marked read only.
	 *
	 * @return <code>true</code> if this resource is marked as read only, 
	 *		<code>false</code> otherwise
	 * @see #setReadOnly(boolean)
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Sets or unsets whether this ResourceAttributes object is marked archive.
	 *
	 * @param archive <code>true</code> to set it to be archive, 
	 *		<code>false</code> to unset
	 * @see #isArchive()
	 */
	public void setArchive(boolean archive) {
		this.archive = archive;
	}

	/**
	 * Sets or unsets whether this ResourceAttributes object is marked executable.
	 *
	 * @param executable <code>true</code> to set it to be executable, 
	 *		<code>false</code> to unset
	 * @see #isExecutable()
	 */
	public void setExecutable(boolean executable) {
		this.executable = executable;
	}

	/**
	 * Sets or unsets whether this ResourceAttributes object is marked read only.
	 *
	 * @param readOnly <code>true</code> to set it to be marked read only, 
	 *		<code>false</code> to unset
	 * @see #isReadOnly()
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * Returns a string representation of the attributes, suitable 
	 * for debugging purposes only.
	 */
	public String toString() {
		return "ResourceAttributes(readOnly=" + readOnly + ",executable=" + executable + ",archive=" + archive + ')'; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
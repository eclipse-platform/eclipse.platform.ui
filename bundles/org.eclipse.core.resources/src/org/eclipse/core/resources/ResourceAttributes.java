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


/**
 * This class represents platform specific attributes of files.
 * Any attributes can be added, but only the attributes that are 
 * supported by the platform will be used. These methods do not set the 
 * attributes in the file system.
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
	/* (non-Javadoc)
	 * For debugging purposes only.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ResourceAttributes(readOnly=" + readOnly + ",executable=" + executable + ",archive=" + archive + ')'; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
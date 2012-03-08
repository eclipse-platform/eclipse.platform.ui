/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.model;

import org.eclipse.core.runtime.Assert;

/**
 * An object which has the general characteristics of all elements
 * in a plug-in manifest.
 * <p>
 * This class may be subclassed.
 * </p>
 * @deprecated In Eclipse 3.0 the runtime was refactored and all 
 * non-essential elements removed.  This class provides facilities primarily intended
 * for tooling.  As such it has been removed and no directly substitutable API provided.
 * This API will be deleted in a future release. See bug 370248 for details.
 */
public abstract class PluginModelObject {

	// DTD properties (included in plug-in manifest)
	private String name = null;

	// transient properties (not included in plug-in manifest)
	private int flags = 0;
	// the last bit is a read-only flag
	// IMPORTANT: One bit in the "flags" integer is used to store the 
	// read-only flag and the other bits are used to store an integer value
	// which can be from -1 to (2**31) - 1. To help with the bit masking, the integer
	// value stored in "flags" is (value + 1). This means that a "flags" value
	// of 0 will NOT be marked as read-only and will return -1 for the start line value.
	static final int M_READ_ONLY = 0x80000000;

	/**
	 * Checks that this model object is writeable.  A runtime exception
	 * is thrown if it is not.
	 */
	protected void assertIsWriteable() {
		Assert.isTrue(!isReadOnly(), "Model is read-only"); //$NON-NLS-1$
	}

	/**
	 * Returns the name of this element.
	 * 
	 * @return the name of this element or <code>null</code>
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the line number for the start tag for this plug-in object. This
	 * is the line number of the element declaration from the plug-in manifest file.
	 * 
	 * @return the line number of the start tag for this object
	 */
	public int getStartLine() {
		return (flags & ~M_READ_ONLY) - 1;
	}

	/**
	 * Returns whether or not this model object is read-only.
	 * 
	 * @return <code>true</code> if this model object is read-only,
	 *		<code>false</code> otherwise
	 * @see #markReadOnly()
	 */
	public boolean isReadOnly() {
		return (flags & M_READ_ONLY) == M_READ_ONLY;
	}

	/**
	 * Sets this model object and all of its descendents to be read-only.
	 * Subclasses may extend this implementation.
	 *
	 * @see #isReadOnly()
	 */
	public void markReadOnly() {
		flags |= M_READ_ONLY;
	}

	/**
	 * Optimization to replace a non-localized key with its localized value.  Avoids having
	 * to access resource bundles for further lookups.
	 * 
	 * @param value the localized name of this model object
	 */
	public void setLocalizedName(String value) {
		name = value;
	}

	/**
	 * Sets the name of this element.
	 * 
	 * @param value the new name of this element.  May be <code>null</code>.
	 */
	public void setName(String value) {
		assertIsWriteable();
		name = value;
	}

	/**
	 * Set the line number for the start tag for this plug-in object. This is the
	 * line number for the element declaration from the plug-in manifest file.
	 * This value can only be set once, subsequent calls to this method will be
	 * ignored.
	 * 
	 * @param lineNumber the line number of this object's declaration in the file
	 */
	public void setStartLine(int lineNumber) {
		if (getStartLine() == -1)
			flags = (lineNumber + 1) | (flags & M_READ_ONLY);
	}

	/**
	 * Return a string representation of this object. This value is not to be relied
	 * on and can change at any time. To be used for debugging purposes only.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.getClass() + "(" + getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}

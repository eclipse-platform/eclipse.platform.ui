package org.eclipse.core.runtime.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.runtime.Assert;

/**
 * An object which has the general characteristics of all elements
 * in a plug-in manifest.
 * <p>
 * This class may be subclassed.
 * </p>
 */

public abstract class PluginModelObject {

	// DTD properties (included in plug-in manifest)
	private String name = null;

	// transient properties (not included in plug-in manifest)
	private boolean readOnly = false;
/**
 * Checks that this model object is writeable.  A runtime exception
 * is thrown if it is not.
 */
protected final void assertIsWriteable() {
	Assert.isTrue(!isReadOnly(), "Model is read-only");
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
 * Returns whether or not this model object is read-only.
 * 
 * @return <code>true</code> if this model object is read-only,
 *		<code>false</code> otherwise
 * @see #markReadOnly
 */
public boolean isReadOnly() {
	return readOnly;
}
/**
 * Sets this model object and all of its descendents to be read-only.
 * Subclasses may extend this implementation.
 *
 * @see #isReadOnly
 */
public void markReadOnly() {
	readOnly = true;
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
}

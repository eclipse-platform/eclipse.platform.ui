package org.eclipse.update.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * Non-plug-in entry model object.
 * <p>
 * This class may be instantiated or subclassed by clients. However, in most 
 * cases clients should instead instantiate or subclass the provided 
 * concrete implementation of this model.
 * </p>
 * @see org.eclipse.update.core.NonPluginEntry
 * @since 2.0
 */
public class NonPluginEntryModel extends ContentEntryModel {

	private String id = null;

	/**
	 * Creates a uninitialized non-plug-in entry model object.
	 * 
	 * @since 2.0
	 */
	public NonPluginEntryModel() {
		super();
	}

	/**
	 * Returns the entry identifier.
	 *
	 * @return entry identifier, or <code>null</code>
	 * @since 2.0
	 */
	public String getIdentifier() {
		return id;
	}

	/**
	 * Sets the entry identifier.
	 * Throws a runtime exception if this object is marked read-only.
	 *
	 * @param id entry identifier.
	 * @since 2.0
	 */
	public void setIdentifier(String id) {
		assertIsWriteable();
		this.id = id;
	}
}
package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

/**
 * An object which represents a non-plug-in entry in the
 * packaging manifest. Non-plug-in entries are arbitrary
 * files handled by custom implementations
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
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
	 * Returns the entry identifier. This is an uninterpreted string.
	 *
	 * @return the entry identifier or <code>null</code>
	 * @since 2.0
	 */
	public String getIdentifier() {
		return id;
	}
	
	/**
	 * Sets the entry identifier.
	 * This object must not be read-only.
	 *
	 * @param id the entry identifier. May be <code>null</code>.
	 * @since 2.0
	 */	
	public void setIdentifier(String id) {
		assertIsWriteable();
		this.id = id;
	}
}

package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

interface Referable {

	/**
	 * Adds a reference for this entity to track. Returns the current count.
	 */
	int addReference();
	
	/**
	 * Tests for existing references.
	 */
	boolean hasReferences();
	
	/**
	 * Removes a reference.  Returns the current count.
	 */
	int removeReference();

}

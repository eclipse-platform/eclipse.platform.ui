package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A region describes a certain range in an indexed text store.
 * Text stores are for example documents or strings. A region is 
 * defined by its offset into the text store and its length.<p>
 * A region is considered a value object. Its offset or length 
 * do not change over time. <p>
 * Clients may implement this interface or use the standard implementation
 * <code>Region</code>.
 */
public interface IRegion {
		
	/**
	 * Returns the length of the region.
	 *
	 * @return the length of the region
	 */
	int getLength();
	
	/**
	 * Returns the offset of the region.
	 *
	 * @return the offset of the region
	 */
	int getOffset();
}

package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * A default implementation of the <code>IRegion</code> interface. 
 */
public class Region implements IRegion {
	
	/** The region offset */
	private int fOffset;
	/** The region length */
	private int fLength;
	
	/**
	 * Create a new region.
	 *
	 * @param offset the offset of the region
	 * @param length the length of the region
	 */
	public Region(int offset, int length) {
		fOffset= offset;
		fLength= length;
	}
	
	/*
	 * @see IRegion#getLength
	 */
	public int getLength() {
		return fLength;
	}
	
	/*
	 * @see IRegion#getOffset
	 */
	public int getOffset() {
		return fOffset;
	}
	
	/**
	 * Two regions are equal if they have the same offset and length.
	 *
	 * @see Object#equals
	 */
	public boolean equals(Object o) {
	 	if (o instanceof IRegion) {
	 		IRegion r= (IRegion) o;
	 		return r.getOffset() == fOffset && r.getLength() == fLength;
	 	}
	 	return false;
	}
	 
	/*
	 * @see Object#hashCode
	 */
	public int hashCode() {
	 	return (fOffset << 24) | (fLength << 16);
	}
}

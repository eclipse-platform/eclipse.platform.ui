package org.eclipse.jface.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */


/**
 * Describes a region of an indexed text store such as document or string.
 * The region consists of offset, length, and type. The type is defines as 
 * a string. A typed region can, e.g., be used to described document partitions.
 * Clients may implement this interface or use the standard impementation
 * <code>TypedRegion</code>.
 */
public interface ITypedRegion extends IRegion {
	
	/**
	 * Returns the content type of the region.
	 *
	 * @return the content type of the region
	 */
	String getType();
}

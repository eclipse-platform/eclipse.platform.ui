/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare;

/**
 * Common interface for objects with editable contents.
 * Typically it is implemented by objects that also implement
 * the <code>IStreamContentAccessor</code> interface.
 * <p>
 * Clients may implement this interface.
 * <p>
 * Note that implementing <code>IEditableContent</code> does not
 * automatically mean that it is editable. An object is only editable if
 * it implements <code>IEditableContent</code> and the <code>isEditable</code> method returns <code>true</code>.
 *
 * @see IStreamContentAccessor
 */
public interface IEditableContent {
	
	/**
	 * Returns <code>true</code> if this object can be modified.
	 * If it returns <code>false</code> the other methods of this API must not be called.
	 * 
	 * @return <code>true</code> if this object can be modified
	 */
	boolean isEditable();
			
	/**
	 * Replaces the current content with the given new bytes.
	 * 
	 * @param newContent this new contents replaces the old contents
	 */
	void setContent(byte[] newContent); 

	/**
	 * This method is called on a parent to
	 * - add a child,
	 * - remove a child,
	 * - copy the contents of a child
	 * 
	 * What to do is encoded in the two arguments as follows:
	 * add:	child == null		other != null
	 * remove:	child != null		other == null
	 * copy:	child != null		other != null
	 */
	ITypedElement replace(ITypedElement child, ITypedElement other);
}

package org.eclipse.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A working set holds a number of IAdaptable elements. 
 * A working set is intended to group elements for presentation to 
 * the user or for operations on a set of elements.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 2.0
 */
public interface IWorkingSet {
	/**
	 * Change event id when the working set contents changed
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SET_CONTENT_CHANGE = "workingSetContentChange";	//$NON-NLS-1$
	/**
	 * Change event id when the working set name changed
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SET_NAME_CHANGE = "workingSetNameChange";	//$NON-NLS-1$	
		
	/**
	 * Adds a property change listener.
	 * 
	 * @param listener the property change listener to add
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener);
	/**
	 * Returns the name of the working set.
	 * 
	 * @return	the name of the working set
	 */
	public String getName();
	/**
	 * Returns the elements that are contained in this working set.
	 * 
	 * @return	the working set's elements
	 */
	public IAdaptable[] getElements();
	/**
	 * Removes the property change listener.
	 * 
	 * @param listener the property change listener to remove
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener);
	/**
	 * Sets the elements that are contained in this working set.
	 * 
	 * @param elements the elements to set in this working set
	 */
	public void setElements(IAdaptable[] elements);
	/**
	 * Sets the name of the working set. 
	 * The working set name should be unique.
	 * The working set name must not have leading or trailing 
	 * whitespace.
	 * 
	 * @param name the name of the working set
	 */
	public void setName(String name);
}

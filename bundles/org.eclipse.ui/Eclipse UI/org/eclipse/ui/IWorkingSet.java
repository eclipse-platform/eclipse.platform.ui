/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * <p>
 * This interface is for internal use only due to issue below. Once
 * the issues is solved there will be an official API.
 * </p>
 * <p>
 * [Issue: Working set must be provided by platform.]
 * </p>
 * 
 * @since 2.0
 */
public interface IWorkingSet {
	public static final String CHANGE_WORKING_SET_CONTENT_CHANGE = "workingSetContentChange";	//$NON-NLS-1$
	public static final String CHANGE_WORKING_SET_NAME_CHANGE = "workingSetNAmeChange";	//$NON-NLS-1$	
		
	public void addPropertyChangeListener(IPropertyChangeListener listener);

	/**
	 * Returns the name of the working set.
	 * 
	 * @return	the name of the working set as <code>String</code>
	 */
	public String getName();
	
	/**
	 * Returns the resources that are contained in this working set.
	 * 
	 * @return	the working set's resources as array of <code>IResource</code>
	 */
	public IAdaptable[] getItems();
	
	public void removePropertyChangeListener(IPropertyChangeListener listener);
	public void setItems(IAdaptable[] elements);


}

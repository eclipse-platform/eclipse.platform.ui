/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;

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
}

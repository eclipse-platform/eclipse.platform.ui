/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.ui;

import org.eclipse.core.resources.IResource;

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
 * @deprecated use org.eclipse.ui.IWorkingSet - this class will be removed soon
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
	public IResource[] getResources();
}

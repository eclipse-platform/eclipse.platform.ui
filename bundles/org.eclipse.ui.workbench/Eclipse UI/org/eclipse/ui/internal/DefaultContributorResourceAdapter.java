/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IContributorResourceAdapter;

/**
 * The DefaultContributorResourceAdapter is the default
 * implementation of the IContributorResourceAdapter used for 
 * one to one resource adaption.
 */

public class DefaultContributorResourceAdapter
	implements IContributorResourceAdapter {
		
	private static IContributorResourceAdapter singleton;

	/**
	 * Constructor for DefaultContributorResourceAdapter.
	 */
	public DefaultContributorResourceAdapter() {
		super();
	}

	/**
	 * Return the default instance used for TaskList adapting.
	 */
	public static IContributorResourceAdapter getDefault(){
		if(singleton == null)
			singleton = new DefaultContributorResourceAdapter();
		return singleton;
	}
	
	/*
	 * @see IContributorResourceAdapter#getAdaptedResource(IAdaptable)
	 */
	public IResource getAdaptedResource(IAdaptable adaptable) {
		return (IResource) adaptable.getAdapter(IResource.class);
	}

}


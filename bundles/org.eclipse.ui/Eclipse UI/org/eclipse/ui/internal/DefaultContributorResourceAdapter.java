package org.eclipse.ui.internal;

/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

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


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


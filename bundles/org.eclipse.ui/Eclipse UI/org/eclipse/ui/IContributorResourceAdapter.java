package org.eclipse.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

public interface IContributorResourceAdapter {
	
	/**
	 * Get the resource that the supplied adaptable 
	 * adapts to. An IContributorResourceAdapter assumes
	 * that any object passed to it adapts to one equivalent
	 * resource.
	 */
	
	public IResource getAdaptedResource(IAdaptable adaptable);

}


package org.eclipse.ui;

import org.eclipse.core.resources.IResource;

/**
 * The IResourceContributorProvider is an interface for types
 * that contribute to resource actions but are not instances
 * of org.eclipse.core.resources.IResource. The contribution
 * mechanaism for IResource operations on decorators, 
 * object contributions and property pages will ask an 
 * org.eclipse.core.runtime.IAdaptable
 * if it adapts to IResourceContributorProvider and if so
 * will apply its contribution to the result of 
 * getResourceContribution().
 */

public interface IResourceContributorProvider {
	
	/**
	 * Get the IResource that a resource operation should
	 * be applied to for this provider.
	 */
	
	public IResource getResourceContribution();

}


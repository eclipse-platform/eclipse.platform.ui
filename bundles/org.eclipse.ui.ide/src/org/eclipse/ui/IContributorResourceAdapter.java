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
package org.eclipse.ui;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * The IContributorResourceAdapter is an interface that defines
 * the API required to get a resource that an object adapts to
 * for use of object contributions, decorators and property
 * pages that have adaptable = true.
 * Implementors of this interface are typically registered with an
 * IAdapterFactory for lookup via the getAdapter() mechanism.
 */

public interface IContributorResourceAdapter {
	
	/**
	 * Return the resource that the supplied adaptable 
	 * adapts to. An IContributorResourceAdapter assumes
	 * that any object passed to it adapts to one equivalent
	 * resource.
	 * @return <code>IResource</code> or <code>null</code> if there
	 * 	is no adapted resource for this type.
	 * @param IAdaptable the adaptable being queried.
	 */
	
	public IResource getAdaptedResource(IAdaptable adaptable);

}


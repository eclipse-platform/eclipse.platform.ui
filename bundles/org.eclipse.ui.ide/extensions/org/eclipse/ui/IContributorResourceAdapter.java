/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	 *
	 * @param adaptable the adaptable being queried
	 * @return a resource, or <code>null</code> if there
	 * 	is no adapted resource for this type
	 */
	public IResource getAdaptedResource(IAdaptable adaptable);

}


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
package org.eclipse.update.core;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Site Feature reference.
 * A reference to a feature on a particular update site.
 * <p>
 * Clients may implement this interface. However, in most cases clients should 
 * directly instantiate or subclass the provided implementation of this 
 * interface.
 * </p>
 * @see org.eclipse.update.core.SiteFeatureReference
 * @since 2.1
 */
public interface ISiteFeatureReference extends IFeatureReference, IAdaptable {

	/**
	 * Returns an array of categories the referenced feature belong to.
	 * 
	 * @return an array of categories, or an empty array
	 * @since 2.1 
	 */
	public ICategory[] getCategories();

	/**
	 * Adds a category to the referenced feature.
	 * 
	 * @param category new category
	 * @since 2.1 
	 */
	public void addCategory(ICategory category);

}

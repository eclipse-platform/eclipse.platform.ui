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
package org.eclipse.ui.internal.registry;


/**
 * The view registry maintains a list of views explicitly registered
 * against the view extension point..
 * <p>
 * [Issue: This interface is not exposed in API, but time may
 * demonstrate that it should be.  For the short term leave it be.
 * In the long term its use should be re-evaluated. ]
 * </p>
 * <p>
 * The description of a given view is kept in a IViewDescriptor
 * </p>
 */
public interface IViewRegistry {
/**
 * Return a view descriptor with the given extension ID.  If no view exists
 * with the ID return null.
 */
public IViewDescriptor find(String id);
/**
 * Returns an enumeration of view categories, if defined.
 */
public ICategory [] getCategories();
/**
 * Return the view category count.
 */
public int getCategoryCount();
/**
 * Return the view count.
 */
public int getViewCount();
/**
 * Return a list of views defined in the registry.
 */
public IViewDescriptor [] getViews();
}

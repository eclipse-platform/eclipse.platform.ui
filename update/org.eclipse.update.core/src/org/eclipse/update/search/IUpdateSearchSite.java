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
package org.eclipse.update.search;

/**
 * This interface is used to represent sites that need to be
 * searched within the search scope. In addition to being an
 * update site adapter, it also returns an array of categories
 * within the site that need not be searched (skipping categories
 * makes the search faster because fewer features need to
 * be checked and potentially downloaded from the server).
 */
public interface IUpdateSearchSite extends IUpdateSiteAdapter {
/**
 * Returns an array of categories that need not be searched
 * when scanning this site or <samp>null</samp> if all the
 * features must be tested.
 * @return an array of category names or <samp>null</samp> if
 * all the features must be tested.
 */
	String[] getCategoriesToSkip();
}
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui;

import org.eclipse.help.internal.search.ISearchScope;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Creates search scope objects from the provided preference store.
 * Classes that implement this interface should take settings
 * manipulated in the search scope preference pages and
 * create scope objects that are required by search engines.
 * Search engine and scope factory are defined together
 * in the same extension point, hence the actual
 * implementation of ISearchScope is up to the contributor.
 * @since 3.1
 */
public interface ISearchScopeFactory {
/**
 * Returns the new search scope object created from the
 * data in the preference store.
 * @param store the preference store that holds the scope data
 * @return the new search scope object
 */
	ISearchScope createSearchScope(IPreferenceStore store);
}
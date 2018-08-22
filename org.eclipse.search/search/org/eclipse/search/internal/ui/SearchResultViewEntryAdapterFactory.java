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

package org.eclipse.search.internal.ui;


import org.eclipse.core.runtime.IAdapterFactory;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.search.ui.ISearchResultViewEntry;

/**
 * Implements basic UI support for Java elements.
 * Implements handle to persistent support for Java elements.
 * @deprecated old search
 */
@Deprecated
public class SearchResultViewEntryAdapterFactory implements IAdapterFactory {

	private static Class<?>[] PROPERTIES= new Class[] {
		IResource.class, IMarker.class,
	};


	@Override
	public Class<?>[] getAdapterList() {
		return PROPERTIES;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object element, Class<T> key) {

		ISearchResultViewEntry entry= (ISearchResultViewEntry) element;

		if (IMarker.class.equals(key)) {
			return (T) entry.getSelectedMarker();
		}
		if (IResource.class.equals(key)) {
			IResource resource= entry.getResource();
			/*
			 * This is a trick to filter out dummy markers that
			 * have been attached to a project because there is no
			 * corresponding resource in the workspace.
			 */
			int type= resource.getType();
			if (type != IResource.PROJECT && type != IResource.ROOT)
				return (T) resource;
		}
		return null;
	}
}

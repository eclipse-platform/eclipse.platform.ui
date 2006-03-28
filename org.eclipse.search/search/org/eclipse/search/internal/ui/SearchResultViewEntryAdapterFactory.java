/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.search.internal.ui;


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;

import org.eclipse.search.ui.ISearchResultViewEntry;

/**
 * Implements basic UI support for Java elements.
 * Implements handle to persistent support for Java elements.
 * @deprecated old search
 */
public class SearchResultViewEntryAdapterFactory implements IAdapterFactory {
	
	private static Class[] PROPERTIES= new Class[] {
		IResource.class, IMarker.class,
	};
	

	public Class[] getAdapterList() {
		return PROPERTIES;
	}
	
	public Object getAdapter(Object element, Class key) {
		
		ISearchResultViewEntry entry= (ISearchResultViewEntry) element;
		
		if (IMarker.class.equals(key)) {
			return entry.getSelectedMarker();
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
				return resource;
		}
		return null; 
	}
}

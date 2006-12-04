/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.sorters;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorSorterService;

/**
 * 
 * Provides a default implementation of {@link INavigatorSorterService}.
 * 
 * @since 3.2
 * 
 */
public class NavigatorSorterService implements INavigatorSorterService {

	private final NavigatorContentService contentService;

	/* A map of (CommonSorterDescriptor, ViewerSorter)-pairs */
	private final Map sorters = new HashMap();

	/**
	 * Create a sorter service attached to the given content service.
	 * 
	 * @param aContentService
	 *            The content service used by the viewer that will use this
	 *            sorter service.
	 */
	public NavigatorSorterService(NavigatorContentService aContentService) {
		contentService = aContentService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorSorterService#findSorterForParent(java.lang.Object)
	 */
	public ViewerSorter findSorterForParent(Object aParent) {

		CommonSorterDescriptor[] descriptors = CommonSorterDescriptorManager
				.getInstance().findApplicableSorters(contentService, aParent);
		if (descriptors.length > 0) {
			return getSorter(descriptors[0]);
		}
		return SkeletonViewerSorter.INSTANCE;
	}

	private ViewerSorter getSorter(CommonSorterDescriptor descriptor) {
		ViewerSorter sorter = null;
		synchronized (sorters) {
			sorter = (ViewerSorter) sorters.get(descriptor);
			if (sorter == null) {
				sorters.put(descriptor, sorter = descriptor.createSorter());
			}
		}
		return sorter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorSorterService#findSorterForParent(org.eclipse.ui.navigator.INavigatorContentDescriptor,
	 *      java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	public ViewerSorter findSorter(INavigatorContentDescriptor source, 
			Object parent, Object lvalue, Object rvalue) { 

		CommonSorterDescriptor[] descriptors = CommonSorterDescriptorManager
				.getInstance().findApplicableSorters(contentService, source, parent, lvalue, rvalue);
		if(descriptors.length > 0) {
			return getSorter(descriptors[0]);
		} 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorSorterService#findAvailableSorters(org.eclipse.ui.navigator.INavigatorContentDescriptor)
	 */
	public Map findAvailableSorters(INavigatorContentDescriptor theSource) {
		
		CommonSorterDescriptor[] descriptors = CommonSorterDescriptorManager.getInstance().findApplicableSorters(theSource);
		Map sorters = new HashMap();

		int count = 0;
		for (int i = 0; i < descriptors.length; i++) {
			if(descriptors[i].getId() != null && descriptors[i].getId().length() > 0)
				sorters.put(descriptors[i].getId(), getSorter(descriptors[i]));
			else 
				sorters.put(theSource.getId()+".sorter."+ (++count), getSorter(descriptors[i])); //$NON-NLS-1$
		}
		return sorters;
	} 
	 

}

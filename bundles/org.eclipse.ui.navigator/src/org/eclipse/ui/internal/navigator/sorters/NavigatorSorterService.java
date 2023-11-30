/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.sorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.VisibilityAssistant.VisibilityListener;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptorManager;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorSorterService;

/**
 *
 * Provides a default implementation of {@link INavigatorSorterService}.
 *
 * @since 3.2
 */
public class NavigatorSorterService implements INavigatorSorterService, VisibilityListener  {

	private final NavigatorContentService contentService;

	/* A map of (CommonSorterDescriptor, ViewerSorter)-pairs */
	private final Map<CommonSorterDescriptor, ViewerSorter> sorters = new HashMap<>();

	private INavigatorContentDescriptor[] sortOnlyDescriptors;

	/**
	 * Create a sorter service attached to the given content service.
	 *
	 * @param aContentService
	 *            The content service used by the viewer that will use this
	 *            sorter service.
	 */
	public NavigatorSorterService(NavigatorContentService aContentService) {
		contentService = aContentService;
		computeSortOnlyDescriptors();
	}

	private synchronized void computeSortOnlyDescriptors() {
		List<INavigatorContentDescriptor> sortOnlyList = new ArrayList<>();
		for (INavigatorContentDescriptor descriptor : NavigatorContentDescriptorManager.getInstance()
				.getSortOnlyContentDescriptors()) {
			if (contentService.isActive(descriptor.getId())) {
				sortOnlyList.add(descriptor);
			}
		}

		sortOnlyDescriptors = sortOnlyList.toArray(new INavigatorContentDescriptor[] {});
	}

	@Override
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
			sorter = sorters.get(descriptor);
			if (sorter == null) {
				sorters.put(descriptor, sorter = descriptor.createSorter());
			}
		}
		return sorter;
	}

	@Override
	public synchronized ViewerSorter findSorter(INavigatorContentDescriptor source,
			Object parent, Object lvalue, Object rvalue) {

		CommonSorterDescriptorManager dm = CommonSorterDescriptorManager
				.getInstance();
		CommonSorterDescriptor[] descriptors;

		INavigatorContentDescriptor lookupDesc;
		for (int i = 0; i < sortOnlyDescriptors.length; i++) {
			lookupDesc = sortOnlyDescriptors[i];
			if (source!= null && source.getSequenceNumber() < lookupDesc.getSequenceNumber()) {
				lookupDesc = source;
				source = null;
				i--;
			}
			descriptors = dm. findApplicableSorters(contentService, lookupDesc, parent);
			if (descriptors.length > 0) {
				return getSorter(descriptors[0]);
			}
		}

		if (source != null) {
			descriptors = dm. findApplicableSorters(contentService, source, parent);
			if (descriptors.length > 0) {
				return getSorter(descriptors[0]);
			}
		}
		return null;
	}

	@Override
	public Map findAvailableSorters(INavigatorContentDescriptor theSource) {

		CommonSorterDescriptor[] descriptors = CommonSorterDescriptorManager.getInstance().findApplicableSorters(theSource);
		Map<String, ViewerSorter> sorters = new HashMap<>();

		int count = 0;
		for (CommonSorterDescriptor descriptor : descriptors) {
			if(descriptor.getId() != null && descriptor.getId().length() > 0)
				sorters.put(descriptor.getId(), getSorter(descriptor));
			else
				sorters.put(theSource.getId()+".sorter."+ (++count), getSorter(descriptor)); //$NON-NLS-1$
		}
		return sorters;
	}


	@Override
	public void onVisibilityOrActivationChange() {
		computeSortOnlyDescriptors();
	}


}

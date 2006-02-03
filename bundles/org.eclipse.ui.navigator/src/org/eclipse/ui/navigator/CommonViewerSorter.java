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

package org.eclipse.ui.navigator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.navigator.internal.NavigatorContentService;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor;

/**
 * 
 * Provides an implementation of TreeViewerSorter that uses the given parent to
 * determine the correct sort order based on the defined
 * <b>org.eclipse.ui.navigator.navigatorContent/navigatorContent/commonSorter</b>
 * elements available in the set of <i>visible</i> content extensions.
 * 
 * <p>
 * The CommonViewerSorter must be assigned to a {@link CommonViewer}. This is required
 * so that the sorter has the correct content service and sorting service available
 * to guide it in sorting the elements from the viewer. No guarantees are made
 * for uses of this sorter class outside of a CommonViewer. 
 * </p>
 * <p>
 * A CommonViewerSorter may not be attached to more than one CommonViewer.
 * </p>
 * 
 * <p>
 * Clients may not extend this class.
 * </p>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * 
 * @since 3.2
 * 
 */
public final class CommonViewerSorter extends TreeViewerSorter {

	private NavigatorContentService contentService;

	private INavigatorSorterService sorterService;

	/**
	 * Create a sorter service attached to the given content service.
	 * 
	 * @param aContentService
	 *            The content service used by the viewer that will use this
	 *            sorter service.
	 */
	protected void setContentService(NavigatorContentService aContentService) {
		contentService = aContentService;
		sorterService = contentService.getSorterService(); 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
	 */
	public int category(Object element) {

		NavigatorContentDescriptor source = contentService
				.getSourceOfContribution(element);
		return source != null ? source.getPriority()
				: Priority.LOW_PRIORITY_VALUE;
	}

	public void sort(final Viewer viewer, final Object parent, Object[] elements) {

		Arrays.sort(elements, new Comparator() {
			public int compare(Object a, Object b) {
				return CommonViewerSorter.this.compare(viewer, parent, a, b);
			}
		});
	}

	public int compare(Viewer viewer, Object parent, Object e1, Object e2) {
		INavigatorContentDescriptor sourceOfLvalue = getSource(e1);
		INavigatorContentDescriptor sourceOfRvalue = getSource(e2);

		// identity comparison
		if (sourceOfLvalue != null && sourceOfLvalue == sourceOfRvalue) {
			ViewerSorter sorter = sorterService.findSorter(sourceOfLvalue, parent, e1, e2);
			if(sorter != null)
				return sorter.compare(viewer, e1, e2);
		}
		int categoryDelta = category(e1) - category(e2);
		if (categoryDelta == 0) {
			super.compare(viewer, e1, e2);
		}
		return categoryDelta;
	} 

	private INavigatorContentDescriptor getSource(Object o) {
		Set descriptors = contentService.findDescriptorsWithPossibleChild(o);
		if(descriptors != null && descriptors.size() > 0) {
			return (INavigatorContentDescriptor) descriptors.iterator().next();
		}
		return null;		 
	}
	

}

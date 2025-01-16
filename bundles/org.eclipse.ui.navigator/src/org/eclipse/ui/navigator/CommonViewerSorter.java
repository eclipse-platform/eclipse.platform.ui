/*******************************************************************************
 * Copyright (c) 2006, 2025 IBM Corporation and others.
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

package org.eclipse.ui.navigator;

import java.util.Set;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.NavigatorContentServiceContentProvider;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.Policy;

/**
 *
 * Provides an implementation of TreeViewerSorter that uses the given parent to determine the
 * correct sort order based on the defined
 * <b>org.eclipse.ui.navigator.navigatorContent/navigatorContent/commonSorter</b> elements
 * available in the set of <i>visible</i> content extensions.
 *
 * <p>
 * The CommonViewerSorter must be assigned a {@link INavigatorContentService} to drive its sorting
 * algorithm. Without a valid content service, the sorter will return the default ordering.
 * </p>
 * <p>
 * A CommonViewerSorter may not be attached to more than one CommonViewer.
 * </p>
 *
 * @since 3.2
 */
public final class CommonViewerSorter extends TreePathViewerSorter {

	private NavigatorContentService contentService;

	private INavigatorSorterService sorterService;

	/**
	 * Create a sorter service attached to the given content service.
	 *
	 * @param aContentService
	 *            The content service used by the viewer that will use this sorter service.
	 * @since 3.3
	 */
	public void setContentService(INavigatorContentService aContentService) {
		contentService = (NavigatorContentService) aContentService;
		sorterService = contentService.getSorterService();
	}

	@Override
	public int category(Object element) {
		if (contentService == null)
			return 0;

		INavigatorContentDescriptor source = getSource(element);
		return source != null ? source.getSequenceNumber() : Priority.NORMAL_PRIORITY_VALUE;
	}

	private void logMissingExtension(Object parent, Object object) {
		NavigatorPlugin.logError(0, NLS.bind(CommonNavigatorMessages.CommonViewerSorter_NoContentExtensionForObject,
				object != null ? object.toString() : "<null>", parent != null ? parent.toString() : "<null>"), null); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public int compare(Viewer viewer, TreePath parentPath, Object e1, Object e2) {
		if (contentService == null)
			return -1;
		INavigatorContentDescriptor sourceOfLvalue = getSource(e1);
		INavigatorContentDescriptor sourceOfRvalue = getSource(e2);

		Object parent;
		if (parentPath == null) {
			parent = viewer.getInput();
		} else {
			parent = parentPath.getLastSegment();
		}

		if (sourceOfLvalue == null) {
			logMissingExtension(parent, e1);
			return -1;
		}
		if (sourceOfRvalue == null) {
			logMissingExtension(parent, e2);
			return -1;
		}

		ViewerComparator sorter = null;

		// shortcut if contributed by same source
		if (sourceOfLvalue == sourceOfRvalue) {
			sorter = sorterService.findComparator(sourceOfLvalue, parent, e1, e2);
		} else {
			// findSorter returns the sorter specified at the source or if it has a higher priority a sortOnly sorter that is registered for the parent
			ViewerComparator lSorter = findApplicableSorter(sourceOfLvalue, parent, e1, e2);
			ViewerComparator rSorter = findApplicableSorter(sourceOfRvalue, parent, e1, e2);
			sorter = rSorter;

			if (rSorter == null || (lSorter != null && sourceOfLvalue.getSequenceNumber() < sourceOfRvalue.getSequenceNumber())) {
				sorter = lSorter;
			}
		}

		if (sorter != null) {
			return sorter.compare(viewer, e1, e2);
		}

		int categoryDelta = category(e1) - category(e2);
		if (categoryDelta == 0) {
			return super.compare(viewer, e1, e2);
		}
		return categoryDelta;
	}

	private ViewerComparator findApplicableSorter(INavigatorContentDescriptor descriptor, Object parent,
			Object e1,
			Object e2) {
		ViewerComparator sorter = sorterService.findComparator(descriptor, parent, e1, e2);
		if (!descriptor.isSortOnly()) { // for compatibility
			if (!(descriptor.isTriggerPoint(e1) && descriptor.isTriggerPoint(e2))) {
				return null;
			}
		}
		return sorter;
	}

	@Override
	public boolean isSorterProperty(Object element, String property) {
		// Have to get the parent path from the content provider
		NavigatorContentServiceContentProvider cp = (NavigatorContentServiceContentProvider) contentService.createCommonContentProvider();
		TreePath[] parentPaths = cp.getParents(element);
		for (TreePath parentPath : parentPaths) {
			if (isSorterProperty(parentPath, element, property))
				return true;
		}
		return false;
	}

	@Override
	public boolean isSorterProperty(TreePath parentPath, Object element, String property) {
		INavigatorContentDescriptor contentDesc = getSource(element);
		if (parentPath.getSegmentCount() == 0)
			return false;
		ViewerComparator sorter = sorterService.findComparator(contentDesc, parentPath.getLastSegment(), element, null);
		if (sorter != null)
			return sorter.isSorterProperty(element, property);
		return false;
	}


	private INavigatorContentDescriptor getSource(Object o) {
		// Fast path - just an optimization for the common case
		INavigatorContentDescriptor ncd = contentService.getSourceOfContribution(o);
		if (ncd != null) {
			if (Policy.DEBUG_SORT)
				System.out.println("sort: " + ncd + " object: " + o); //$NON-NLS-1$//$NON-NLS-2$
			return ncd;
		}

		Set descriptors = contentService.findDescriptorsByTriggerPoint(o, NavigatorContentService.CONSIDER_OVERRIDES);
		if (descriptors != null && descriptors.size() > 0) {
			ncd = (INavigatorContentDescriptor) descriptors.iterator().next();
			if (Policy.DEBUG_SORT)
				System.out.println("sort: " + ncd + " object: " + o); //$NON-NLS-1$//$NON-NLS-2$
			return ncd;
		}
		if (Policy.DEBUG_SORT)
			System.out.println("sort: NULL object: " + o); //$NON-NLS-1$
		return null;
	}


}

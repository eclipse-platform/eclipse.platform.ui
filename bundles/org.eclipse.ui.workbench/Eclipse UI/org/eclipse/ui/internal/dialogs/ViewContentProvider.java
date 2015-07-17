/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 430603, 450817, 472654
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * Provides content for viewers that wish to show Views.
 */
public class ViewContentProvider implements ITreeContentProvider {

	/**
	 * Child cache. Map from Object->Object[]. Our hasChildren() method is
	 * expensive so it's better to cache the results of getChildren().
	 */
	private Map<Object, Object[]> childMap = new HashMap<>();

	private MApplication application;
	private IViewRegistry viewRegistry;

	public ViewContentProvider(MApplication application) {
		this.application = application;
		viewRegistry = WorkbenchPlugin.getDefault().getViewRegistry();
	}

	@Override
	public void dispose() {
		childMap.clear();
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		childMap.clear();
		application = (MApplication) newInput;
	}

	@Override
	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof MApplication) {
			return true;
		} else if (element instanceof String) {
			return true;
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object element) {
		Object[] children = childMap.get(element);
		if (children == null) {
			children = createChildren(element);
			childMap.put(element, children);
		}
		return children;
	}

	/**
	 * Determines the categories and views
	 *
	 * Views are identified as PartDescriptors which have the tag "View"
	 *
	 */
	private Object[] createChildren(Object element) {
		if (element instanceof MApplication) {
			return determineTopLevelElements(element).toArray();
		} else if (element instanceof String) {
			return determineViewsInCategory((String) element).toArray();
		}
		return new Object[0];
	}

	/**
	 * @param categoryDescription
	 * @return views with the category tag
	 */
	private Set<MPartDescriptor> determineViewsInCategory(String categoryDescription) {
		List<MPartDescriptor> descriptors = application.getDescriptors();
		Set<MPartDescriptor> categoryDescriptors = new HashSet<>();
		for (MPartDescriptor descriptor : descriptors) {
			if (isFilteredByActivity(descriptor.getElementId()) || isIntroView(descriptor.getElementId())) {
				continue;
			}
			String category = descriptor.getCategory();
			if (categoryDescription.equals(category)) {
				categoryDescriptors.add(descriptor);
			}
		}
		return categoryDescriptors;
	}

	/**
	 * Determines the views and categories for the top level
	 */
	private Set<Object> determineTopLevelElements(Object element) {
		List<MPartDescriptor> descriptors = ((MApplication) element).getDescriptors();
		Set<String> categories = new HashSet<>();
		Set<MPartDescriptor> visibleViews = new HashSet<>();
		for (MPartDescriptor descriptor : descriptors) {
			// only process views and hide views which are filtered by
			// activities
			if (!isView(descriptor) || isFilteredByActivity(descriptor.getElementId())) {
				continue;
			}

			// determine the categories
			String category = descriptor.getCategory();

			// if view has not category show it directly
			if (category == null) {
				visibleViews.add(descriptor);
				// otherwise just show the category
			} else {
				categories.add(category);
			}
		}

		Set<Object> combinedTopElements = new HashSet<>();
		combinedTopElements.addAll(categories);
		combinedTopElements.addAll(visibleViews);
		return combinedTopElements;
	}


	/**
	 * Determines if the part is a view or and editor
	 *
	 * @param descriptor
	 *
	 * @return true if part is tagged as view
	 */
	private boolean isView(MPartDescriptor descriptor) {
		return descriptor.getTags().contains("View"); //$NON-NLS-1$
	}

	/**
	 * Remove Eclipse introview from this list, as it opened via the Help ->
	 * Welcome menu
	 */
	private boolean isIntroView(String id) {
		return (id.equals(IIntroConstants.INTRO_VIEW_ID));
	}

	/**
	 * Evaluates if the view is filtered by an activity
	 *
	 * @param elementId
	 * @return result of the check
	 */
	private boolean isFilteredByActivity(String elementId) {
		IViewDescriptor[] views = viewRegistry.getViews();
		for (IViewDescriptor descriptor : views) {
			if (descriptor.getId().equals(elementId) && WorkbenchActivityHelper.filterItem(descriptor)) {
				return true;
			}
		}
		return false;
	}
}

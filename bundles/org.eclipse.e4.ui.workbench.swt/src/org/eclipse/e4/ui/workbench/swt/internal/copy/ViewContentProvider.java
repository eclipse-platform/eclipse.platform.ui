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
package org.eclipse.e4.ui.workbench.swt.internal.copy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Based on org.eclipse.ui.internal.dialogs.ViewContentProvider.
 */
public class ViewContentProvider implements ITreeContentProvider {

	final private static String CATEGORY_TAG = "categoryTag:";
	final private static int CATEGORY_TAG_LENGTH = CATEGORY_TAG.length();

	/**
	 * Child cache. Map from Object->Object[]. Our hasChildren() method is
	 * expensive so it's better to cache the results of getChildren().
	 */
	private final Map<Object, Object[]> childMap = new HashMap<>();

	private MApplication application;

	public ViewContentProvider(MApplication application) {
		this.application = application;
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

	private Object[] createChildren(Object element) {
		if (element instanceof MApplication) {
			List<MPartDescriptor> descriptors = ((MApplication) element)
					.getDescriptors();
			Set<String> categoryTags = new HashSet<>();
			Set<MPartDescriptor> noCategoryDescriptors = new HashSet<>();
			for (MPartDescriptor descriptor : descriptors) {
				List<String> tags = descriptor.getTags();
				String category = null;
				boolean isView = false;
				for (String tag : tags) {
					if (tag.equals("View")) {
						isView = true;
					} else if (tag.startsWith(CATEGORY_TAG)) {
						category = tag.substring(CATEGORY_TAG_LENGTH);
					}
				}
				if (isView) {
					if (category != null) {
						categoryTags.add(category);
					} else {
						noCategoryDescriptors.add(descriptor);
					}
				}
			}

			Set<Object> combinedTopElements = new HashSet<>();
			combinedTopElements.addAll(categoryTags);
			combinedTopElements.addAll(noCategoryDescriptors);
			return combinedTopElements.toArray();
		} else if (element instanceof String) {
			List<MPartDescriptor> descriptors = application.getDescriptors();
			Set<MPartDescriptor> categoryDescriptors = new HashSet<>();
			for (MPartDescriptor descriptor : descriptors) {
				List<String> tags = descriptor.getTags();
				for (String tag : tags) {
					if (!tag.startsWith(CATEGORY_TAG)) {
						continue;
					}
					String categoryTag = tag.substring(CATEGORY_TAG_LENGTH);
					if (element.equals(categoryTag)) {
						categoryDescriptors.add(descriptor);
					}
				}
			}
			return categoryDescriptors.toArray();
		}
		return new Object[0];
	}
}

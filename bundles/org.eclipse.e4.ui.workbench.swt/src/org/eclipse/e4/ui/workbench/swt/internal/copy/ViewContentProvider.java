/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	private Map<Object, Object[]> childMap = new HashMap<Object, Object[]>();

	private MApplication application;

	public ViewContentProvider(MApplication application) {
		this.application = application;
	}

	public void dispose() {
		childMap.clear();
	}

	public Object getParent(Object element) {
		return null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		childMap.clear();
		application = (MApplication) newInput;
	}

	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	public boolean hasChildren(Object element) {
		if (element instanceof MApplication) {
			return true;
		} else if (element instanceof String) {
			return true;
		}
		return false;
	}

	public Object[] getChildren(Object element) {
		Object[] children = (Object[]) childMap.get(element);
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
			Set<String> categoryTags = new HashSet<String>();
			Set<MPartDescriptor> noCategoryDescriptors = new HashSet<MPartDescriptor>();
			for (MPartDescriptor descriptor : descriptors) {
				List<String> tags = descriptor.getTags();
				String category = null;
				boolean isView = false;
				for (String tag : tags) {
					if (tag.equals("View"))
						isView = true;
					else if (tag.startsWith(CATEGORY_TAG)) {
						category = tag.substring(CATEGORY_TAG_LENGTH);
					}
				}
				if (isView) {
					if (category != null)
						categoryTags.add(category);
					else
						noCategoryDescriptors.add(descriptor);
				}
			}

			Set<Object> combinedTopElements = new HashSet<Object>();
			combinedTopElements.addAll(categoryTags);
			combinedTopElements.addAll(noCategoryDescriptors);
			return combinedTopElements.toArray();
		} else if (element instanceof String) {
			List<MPartDescriptor> descriptors = application.getDescriptors();
			Set<MPartDescriptor> categoryDescriptors = new HashSet<MPartDescriptor>();
			for (MPartDescriptor descriptor : descriptors) {
				List<String> tags = descriptor.getTags();
				for (String tag : tags) {
					if (!tag.startsWith(CATEGORY_TAG))
						continue;
					String categoryTag = tag.substring(CATEGORY_TAG_LENGTH);
					if (element.equals(categoryTag))
						categoryDescriptors.add(descriptor);
				}
			}
			return categoryDescriptors.toArray();
		}
		return new Object[0];
	}
}

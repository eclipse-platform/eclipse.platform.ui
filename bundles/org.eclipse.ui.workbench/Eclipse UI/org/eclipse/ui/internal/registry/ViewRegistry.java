/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.*;

/**
 * The central manager for view descriptors.
 */
public class ViewRegistry implements IViewRegistry {
	private List views;
	private List categories;
	private Category miscCategory;
/**
 * Create a new ViewRegistry.
 */
public ViewRegistry() {
	views = new ArrayList();
	categories = new ArrayList();
}
/**
 * Add a category to the registry.
 */
public void add(ICategory desc) {
	/* fix for 1877 */
	if (findCategory(desc.getId()) == null)
		categories.add(desc);
}
/**
 * Add a descriptor to the registry.
 */
public void add(IViewDescriptor desc) {
	views.add(desc);
}
/**
 * Find a descriptor in the registry.
 */
public IViewDescriptor find(String id) {
	Iterator enum = views.iterator();
	while (enum.hasNext()) {
		IViewDescriptor desc = (IViewDescriptor) enum.next();
		if (id.equals(desc.getID())) {
			return desc;
		}
	}
	return null;
}
/**
 * Find a category with a given name.
 */
public ICategory findCategory(String id) {
	Iterator enum = categories.iterator();
	while (enum.hasNext()) {
		Category cat = (Category) enum.next();
		if (id.equals(cat.getRootPath())) {
			return cat;
		}
	}
	return null;
}
/**
 * Get the list of view categories.
 */
public ICategory [] getCategories() {
	int nSize = categories.size();
	ICategory [] retArray = new ICategory[nSize];
	categories.toArray(retArray);
	return retArray;
}
/**
 * Return the view category count.
 */
public int getCategoryCount() {
	return categories.size();
}
/**
 * Returns the Misc category.
 * This may be null if there are no miscellaneous views.
 */
public ICategory getMiscCategory() {
	return miscCategory;
}
/**
 * Return the view count.
 */
public int getViewCount() {
	return views.size();
}
/**
 * Get an enumeration of view descriptors.
 */
public IViewDescriptor [] getViews() {
	int nSize = views.size();
	IViewDescriptor [] retArray = new IViewDescriptor[nSize];
	views.toArray(retArray);
	return retArray;
}
/**
 * Adds each view in the registry to a particular category.
 * The view category may be defined in xml.  If not, the view is
 * added to the "misc" category.
 */
public void mapViewsToCategories() {
	Iterator enum = views.iterator();
	while (enum.hasNext()) {
		IViewDescriptor desc = (IViewDescriptor) enum.next();
		Category cat = null;
		String [] catPath = desc.getCategoryPath();
		if (catPath != null) {
			String rootCat = catPath[0];
			cat = (Category)findCategory(rootCat);
		}	
		if (cat != null) {
			cat.addElement(desc);
		} else {
			if (miscCategory == null) {
				miscCategory = new Category();
				categories.add(miscCategory);
			}
			miscCategory.addElement(desc);
		}
	}
}
}

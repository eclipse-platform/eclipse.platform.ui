/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.NotDefinedException;

/**
 * Provides labels for <code>ICategory</code> objects. They may be passed
 * directly or as <code>String</code> identifiers that are matched against
 * the activity manager.
 * 
 * @since 3.0
 */
public class CategoryLabelProvider extends LabelProvider {

	private IActivityManager activityManager;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param activityManager
	 * @since 3.0
	 */
	public CategoryLabelProvider(IActivityManager activityManager) {
		this.activityManager = activityManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		return null;
	}

	/**
	 * @param category
	 * @return @since 3.0
	 */
	private String getCategoryText(ICategory category) {
		try {
			return category.getName();
		} catch (NotDefinedException e) {
			return category.getId();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof String) {
			return getCategoryText(activityManager.getCategory((String) element));
		} else if (element instanceof ICategory) {
			return getCategoryText((ICategory) element);
		} else {
			throw new IllegalArgumentException();
		}
	}
}

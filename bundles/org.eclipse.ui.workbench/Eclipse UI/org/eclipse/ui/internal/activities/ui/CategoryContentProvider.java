/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ui;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.activities.IActivityManager;

/**
 * The CategoryContentProvider is a class that supplies the contents for the viewer
 * in the RolePreferencePage.
 * TODO: kim.. RolePreferencePage can't be the correct name here.
 */
public class CategoryContentProvider implements IStructuredContentProvider {

	/**
	 * Create a new instance of the receiver.
	 */
	public CategoryContentProvider() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		Object[] categories = new Object[0];
		if (inputElement instanceof IActivityManager) {
			categories = ((IActivityManager) inputElement).getDefinedCategoryIds().toArray();
		} else if (inputElement instanceof Collection) {
			categories = ((Collection) inputElement).toArray();
		}
		return categories;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}

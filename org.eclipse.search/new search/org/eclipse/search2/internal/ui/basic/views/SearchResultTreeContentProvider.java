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
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Thomas Mäder
 *
 */
public class SearchResultTreeContentProvider implements ITreeContentProvider {
	private DefaultSearchViewPage fPage;

	/**
	 * @param page
	 */
	SearchResultTreeContentProvider(DefaultSearchViewPage page) {
		super();
		fPage= page;
	}
	
	public Object[] getChildren(Object parentElement) {
		return fPage.getModel().getChildren(parentElement);
	}

	public Object getParent(Object element) {
		return ((SearchResultTreeModel)fPage.getModel()).getParent(element);
	}

	public boolean hasChildren(Object element) {
		return ((SearchResultTreeModel)fPage.getModel()).hasChildren(element);
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		// nothing to do
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// stateless model, ignore.
	}

}

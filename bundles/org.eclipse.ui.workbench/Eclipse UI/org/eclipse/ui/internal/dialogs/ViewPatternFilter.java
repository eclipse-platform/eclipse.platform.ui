/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * A class that handles filtering view node items based on a supplied
 * matching string.
 *  
 * @since 3.2
 *
 */
public class ViewPatternFilter extends PatternItemFilter {

	/**
	 * Create a new instance of a ViewPatternFilter 
	 * @param isMatchItem
	 */
	public ViewPatternFilter(boolean isMatchItem) {
		super(isMatchItem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		ITreeContentProvider contentProvider = (ITreeContentProvider) ((TreeViewer) viewer)
				.getContentProvider();

		String text = null;
		Object[] children = null;
		if (element instanceof IViewCategory) {
			IViewCategory desc = (IViewCategory) element;
			children = contentProvider.getChildren(desc);
			text = desc.getLabel();
		} else if (element instanceof IViewDescriptor) {
			IViewDescriptor desc = (IViewDescriptor) element;
			children = contentProvider.getChildren(desc);
			text = desc.getLabel();
		}

		if (wordMatches(text))
			return true;

		if (matchItem && children != null) {
			// Will return true if any subnode of the element matches the search
			if (filter(viewer, element, children).length > 0)
				return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementSelectable(java.lang.Object)
	 */
	protected boolean isElementSelectable(Object element) {
		return element instanceof IViewDescriptor;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementMatch(org.eclipse.jface.viewers.Viewer, java.lang.Object)
	 */
	protected boolean isElementMatch(Viewer viewer, Object element) {
		String text = null;
		if (element instanceof IViewCategory) {
			IViewCategory desc = (IViewCategory) element;
			text = desc.getLabel();
		} else if (element instanceof IViewDescriptor) {
			IViewDescriptor desc = (IViewDescriptor) element;
			text = desc.getLabel();
		}

		if (wordMatches(text))
			return true;

		return false;
	}
}

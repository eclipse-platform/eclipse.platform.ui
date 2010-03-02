/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * A class that handles filtering view node items based on a supplied
 * matching string.
 *  
 * @since 3.2
 *
 */
public class ViewPatternFilter extends PatternFilter {

	/**
	 * Create a new instance of a ViewPatternFilter 
	 * @param isMatchItem
	 */
	public ViewPatternFilter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementSelectable(java.lang.Object)
	 */
	public boolean isElementSelectable(Object element) {
		return element instanceof IViewDescriptor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.PatternFilter#isElementMatch(org.eclipse.jface.viewers.Viewer, java.lang.Object)
	 */
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		if (element instanceof IViewCategory) {
			return false;
		}

		String text = null;
		if (element instanceof IViewDescriptor) {
			IViewDescriptor desc = (IViewDescriptor) element;
			text = desc.getLabel();
			if (wordMatches(text)) {
				return true;
			}
		}

		return false;
	}
}

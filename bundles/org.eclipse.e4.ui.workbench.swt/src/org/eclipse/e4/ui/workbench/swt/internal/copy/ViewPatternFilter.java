/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.internal.copy;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.LocalizationHelper;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.jface.viewers.Viewer;

/**
 * Based on of org.eclipse.ui.internal.dialogs.ViewPatternFilter.
 */
public class ViewPatternFilter extends PatternFilter {

	private IEclipseContext context;

	/**
	 * Create a new instance of a ViewPatternFilter
	 */
	public ViewPatternFilter(IEclipseContext context) {
		super();
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.dialogs.PatternFilter#isElementSelectable(java
	 * .lang.Object)
	 */
	public boolean isElementSelectable(Object element) {
		return element instanceof MPartDescriptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.dialogs.PatternFilter#isElementMatch(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object)
	 */
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		if (element instanceof String) {
			return false;
		}

		String text = null;
		if (element instanceof MPartDescriptor) {
			MPartDescriptor desc = (MPartDescriptor) element;
			text = LocalizationHelper.getLocalized(desc.getLabel(), desc,
					context);
			if (wordMatches(text)) {
				return true;
			}
		}

		return false;
	}
}

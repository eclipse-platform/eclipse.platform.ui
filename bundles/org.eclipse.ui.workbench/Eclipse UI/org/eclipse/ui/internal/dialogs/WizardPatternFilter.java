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

/**
 * A class that handles filtering wizard node items based on a supplied
 * matching string.
 *  
 * @since 3.2
 *
 */
public class WizardPatternFilter extends PatternFilter {

	/**
	 * Create a new instance of a WizardPatternFilter 
	 * @param isMatchItem
	 */
	public WizardPatternFilter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementSelectable(java.lang.Object)
	 */
	public boolean isElementSelectable(Object element) {
		return element instanceof WorkbenchWizardElement;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementMatch(org.eclipse.jface.viewers.Viewer, java.lang.Object)
	 */
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		if (element instanceof WizardCollectionElement) {
			return false;
		}
		
		if (element instanceof WorkbenchWizardElement) {
			WorkbenchWizardElement desc = (WorkbenchWizardElement) element;
			String text = desc.getLabel();
			if (wordMatches(text)) {
				return true;
			}
		}
		return false;
	}

}

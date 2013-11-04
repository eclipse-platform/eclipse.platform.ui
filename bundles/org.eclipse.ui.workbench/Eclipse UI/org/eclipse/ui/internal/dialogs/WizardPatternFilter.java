/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * A class that handles filtering wizard node items based on a supplied matching
 * string and keywords
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

			String[] keywordLabels = desc.getKeywordLabels();
			for (int i = 0; i < keywordLabels.length; i++) {
				if (wordMatches(keywordLabels[i]))
					return true;
			}
		}
		return false;
	}

	@Override
	public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
		ArrayList<Object> result = new ArrayList<Object>();
		ViewerFilter viewerFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return WizardPatternFilter.this.select(viewer, parentElement, element)
						|| hasChildren(element);
			}
			private boolean hasChildren(Object element) {
				return element instanceof WorkbenchWizardElement
						&& ((WorkbenchWizardElement) element).getCollectionElement().size() > 0;
			}
		};

		for (Object elem : super.filter(viewer, parent, elements)) {
			if (elem instanceof WizardCollectionElement) {
				Object wizardCollection = WizardCollectionElement.filter(viewer, viewerFilter,
						(WizardCollectionElement) elem);
				if (wizardCollection != null) {
					result.add(wizardCollection);
				}
			} else {
				result.add(elem);
			}
		}

		return result.toArray();
	}
}

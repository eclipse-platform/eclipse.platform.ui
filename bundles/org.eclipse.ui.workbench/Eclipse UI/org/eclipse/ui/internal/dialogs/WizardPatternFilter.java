/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import org.eclipse.jface.viewers.Viewer;
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
	 * Create a new instance of a WizardPatternFilter.
	 */
	public WizardPatternFilter() {
		super();
	}

	@Override
	public boolean isElementSelectable(Object element) {
		return element instanceof WorkbenchWizardElement;
	}

	@Override
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
			String wizDesc = desc.getDescription();
			if (wordMatches(wizDesc)) {
				return true;
			}

			for (String keywordLabel : desc.getKeywordLabels()) {
				if (wordMatches(keywordLabel))
					return true;
			}
		}
		return false;
	}

	@Override
	public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
		ArrayList<Object> result = new ArrayList<>();

		for (Object elem : super.filter(viewer, parent, elements)) {
			if (elem instanceof WizardCollectionElement) {
				Object wizardCollection = WizardCollectionElement.filter(viewer, this, (WizardCollectionElement) elem);
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

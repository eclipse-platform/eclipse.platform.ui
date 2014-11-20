/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Wim Jongman - Maintenance (391086)
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

@SuppressWarnings("restriction")
public class PatternFilter extends org.eclipse.e4.ui.workbench.swt.internal.copy.PatternFilter {

	/**
	 * This constructor will call {@link #setIncludeLeadingWildcard(boolean)} with boolean=true.
	 */
	public PatternFilter() {
		setIncludeLeadingWildcard(true);
	}

	/**
	 * Check if the current (leaf) element is a match with the filter text. The
	 * default behavior checks that the label of the element is a match.
	 *
	 * Subclasses should override this method.
	 *
	 * @param viewer
	 *            the viewer that contains the element
	 * @param element
	 *            the tree element to check
	 * @return true if the given element's label matches the filter text
	 */
	@Override
	protected boolean isLeafMatch(Viewer viewer, Object element) {

		final Object labelProvider = ((StructuredViewer) viewer).getLabelProvider();
		String labelText = null;

		if (labelProvider instanceof ILabelProvider) {
			labelText = ((ILabelProvider) labelProvider).getText(element);
		} else {
			if (element != null) {
				labelText = element.toString();
			}
		}

		if (labelText == null) {
			return false;
		}
		return wordMatches(labelText);
	}

}

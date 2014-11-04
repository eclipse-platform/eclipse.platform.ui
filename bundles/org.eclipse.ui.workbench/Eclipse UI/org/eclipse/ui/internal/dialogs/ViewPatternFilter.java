/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 430603
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.e4.ui.model.LocalizationHelper;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * A class that handles filtering view node items based on a supplied matching
 * string.
 *
 * @since 3.2
 *
 */
public class ViewPatternFilter extends PatternFilter {


	@Override
	public boolean isElementSelectable(Object element) {
		return element instanceof MPartDescriptor;
	}

	@Override
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		if (element instanceof String) {
			return false;
		}

		String text = null;
		if (element instanceof MPartDescriptor) {
			MPartDescriptor desc = (MPartDescriptor) element;
			text = LocalizationHelper.getLocalized(desc.getLabel(), desc);
			if (wordMatches(text)) {
				return true;
			}
		}

		return false;
	}
}

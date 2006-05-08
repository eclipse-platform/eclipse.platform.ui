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

import org.eclipse.jface.preference.PreferenceLabelProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * This PreferenceBoldLabelProvider will bold those elements which really match
 * the search contents
 */
public class PreferenceBoldLabelProvider extends PreferenceLabelProvider
		implements IFontProvider {

	private FilteredTree filterTree;
	private PatternFilter filterForBoldElements = new PreferencePatternFilter();
	
	PreferenceBoldLabelProvider(FilteredTree filterTree) {
		this.filterTree = filterTree;
	}

	public Font getFont(Object element) {
		return FilteredTree.getBoldFont(element, filterTree,
				filterForBoldElements);
	}

}

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

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.search.ui.text.ISearchElementPresentation;

/**
 * @author Thomas Mäder
 *
 */
public class DelegatingLabelProvider extends LabelProvider {

	private DefaultSearchViewPage fPage;

	DelegatingLabelProvider(DefaultSearchViewPage page) {
		fPage= page;
	}

	public Image getImage(Object element) {
		ISearchElementPresentation presentation= fPage.getSearchResultCategoryAdapter();
		return presentation.getImage(element);
	}

	public String getText(Object element) {
		ISearchElementPresentation presentation= fPage.getSearchResultCategoryAdapter();
		int matchCount= fPage.getCurrentSearch().getMatchCount(element);
		if (matchCount > 0)
			return presentation.getText(element)+ " (" + matchCount + " matches)"; //$NON-NLS-1$ //$NON-NLS-2$
		else
			return presentation.getText(element);
	}

}

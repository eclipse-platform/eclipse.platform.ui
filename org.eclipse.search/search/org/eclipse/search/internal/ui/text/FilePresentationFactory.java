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
package org.eclipse.search.internal.ui.text;

import org.eclipse.ui.IViewPart;

import org.eclipse.search.ui.ISearchResultPresentation;
import org.eclipse.search.ui.text.IPresentationFactory;
import org.eclipse.search.ui.text.ISearchElementPresentation;

/**
 * @author Thomas Mäder
 *
 */
public class FilePresentationFactory implements IPresentationFactory {


	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.model.text.IPresentationFactory#createSearchResultPresentation(org.eclipse.search.ui.ISearchView)
	 */
	public ISearchResultPresentation createSearchResultPresentation(IViewPart view) {
		return new FileSearchCategory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.model.text.IPresentationFactory#createElementPresentation(org.eclipse.search.ui.ISearchView)
	 */
	public ISearchElementPresentation createElementPresentation(IViewPart view) {
		return new FileUIAdapter(view);
	}

}

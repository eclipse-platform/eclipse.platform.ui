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

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultPresentation;
import org.eclipse.search.ui.text.ITextSearchResult;

/**
 * @author Thomas Mäder
 *
 */
public class FileSearchCategory implements ISearchResultPresentation {

	public FileSearchCategory() {
	}

	public String getText(ISearchResult search) {
		FileSearchDesription desc= (FileSearchDesription) search.getUserData();
		return desc.getSearchString()+ " ( "+((ITextSearchResult)search).getMatchCount()+" Occurrences in "+desc.getScopeDescription() + " )";
	}

	public ImageDescriptor getImageDescriptor(ISearchResult search) {
		// TODO Auto-generated method stub
		return null;
	}

	public void dispose() {
	}

	public String getTooltip(ISearchResult search) {
		return getText(search);
	}

}

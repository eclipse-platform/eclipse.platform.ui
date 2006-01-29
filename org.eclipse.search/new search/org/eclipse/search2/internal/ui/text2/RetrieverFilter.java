/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.regex.Pattern;

import org.eclipse.search.internal.core.text.PatternConstructor;

import org.eclipse.search2.internal.ui.SearchMessages;

public class RetrieverFilter {
	private static String sLastCauseForError;
	private Pattern fFilterPattern;
	private String fFilterText;
	private int fAcceptedLocations;
	private boolean fIsRegex;
	private boolean fHideMatching;
	private int fOriginatorsHash;

	public RetrieverFilter(RetrieverPage page, int acceptedLocations, String filterText, boolean isRegex, boolean hideMatching, int originatorsHash) {

		fOriginatorsHash= originatorsHash;
		fFilterPattern= null;
		fFilterText= null;
		if (filterText != null) {
			if (filterText.length() != 0) {
				try {
					fFilterPattern= PatternConstructor.createPattern(filterText, isRegex, false, false, false);
					fFilterText= filterText;
				} catch (Exception e) {
					if (page != null && !filterText.equals(sLastCauseForError)) {
						page.showError(SearchMessages.RetrieverFindTab_Error_invalidRegex + e.getMessage());
					}
				}
				sLastCauseForError= filterText;
			}
		}
		fAcceptedLocations= acceptedLocations;
		fIsRegex= isRegex;
		fHideMatching= hideMatching;
	}

	public RetrieverFilter() {
		fAcceptedLocations= IRetrieverKeys.ALL_LOCATIONS;
	}

	public int getOriginatorsHash() {
		return fOriginatorsHash;
	}

	public boolean getHideMatching() {
		return fHideMatching;
	}

	public int getAcceptedLocations() {
		return fAcceptedLocations;
	}

	public Pattern getPattern() {
		return fFilterPattern;
	}

	public boolean getIsRegex() {
		return fIsRegex;
	}

	public String getFilterString() {
		return fFilterText;
	}

}

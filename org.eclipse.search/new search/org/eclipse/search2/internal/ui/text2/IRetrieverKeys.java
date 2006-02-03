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

import org.eclipse.search.ui.text.SearchMatchInformationProvider;

interface IRetrieverKeys {
	final String KEY_EXT_COMBO_CONTENT= ".combo"; //$NON-NLS-1$

	final String KEY_SPLITTER_W1= "splitter.w1"; //$NON-NLS-1$
	final String KEY_SPLITTER_W2= "splitter.w2"; //$NON-NLS-1$

	final String KEY_USE_FLAT_LAYOUT= "flat-layout"; //$NON-NLS-1$

	final String KEY_CASE_SENSITIVE_SEARCH= "case-sensitive"; //$NON-NLS-1$
	final String KEY_REGULAR_EXPRESSION_SEARCH= "regular-expression"; //$NON-NLS-1$
	final String KEY_WHOLE_WORD= "whole-word"; //$NON-NLS-1$
	final String KEY_SEARCH_STRINGS= "search-string"; //$NON-NLS-1$

	final String KEY_SEARCH_SCOPE= "search-scope"; //$NON-NLS-1$
	final String KEY_SCOPE_HISTORY= "scope-history"; //$NON-NLS-1$
	final String KEY_SCOPE_DESCRIPTOR_CLASS= "scope-descriptor-class"; //$NON-NLS-1$
	final String KEY_FILE_PATTERNS= "file-patterns"; //$NON-NLS-1$
	final String KEY_CONSIDER_DERIVED_RESOURCES= "consider-derived"; //$NON-NLS-1$
	final String KEY_USE_CASE_SENSITIVE_FILE_PATTERNS= "case-sensitive-file-patterns"; //$NON-NLS-1$

	final String KEY_ENABLE_FILTER= "filter-action"; //$NON-NLS-1$
	final String KEY_ENABLE_LOCATION_FILTER= "use-location-filter"; //$NON-NLS-1$
	final String KEY_COMMENT_FILTER= "filter-comment"; //$NON-NLS-1$
	final String KEY_STRING_FILTER= "filter-string"; //$NON-NLS-1$
	final String KEY_INCLUDE_FILTER= "filter-include"; //$NON-NLS-1$
	final String KEY_PREP_FILTER= "filter-prep"; //$NON-NLS-1$
	final String KEY_FUNCTION_FILTER= "filter-function"; //$NON-NLS-1$
	final String KEY_ELSEWHERE_FILTER= "filter-elsewhere"; //$NON-NLS-1$

	final String KEY_ENABLE_TEXT_FILTER= "use-filter-pattern"; //$NON-NLS-1$
	final String KEY_FILTER_PATTERNS= "filter-pattern"; //$NON-NLS-1$
	final String KEY_REGULAR_EXPRESSION_FILTER= "filter-regexp"; //$NON-NLS-1$
	final String KEY_INVERT_FILTER= "filter-invert"; //$NON-NLS-1$

	final String KEY_REPLACEMENT_STRING= "replacement-string"; //$NON-NLS-1$

	final int VISIBLE_ITEMS_IN_COMBO= 8;

	final int MAX_FILTER_PATTERNS= 8;
	final int MAX_SEARCH_STRINGS= 8;
	final int MAX_FILE_PATTERNS= 8;
	final int MAX_SCOPES= 8;
	final int MAX_REPLACEMENT_STRINGS= 8;

	final int MAX_COMBINED_LINE_LENGTH= 1024 * 8;

	final String SECTION_SCOPE= "scope"; //$NON-NLS-1$

	final int ALL_LOCATIONS= (1 << SearchMatchInformationProvider.LOCATION_OTHER) | (1 << SearchMatchInformationProvider.LOCATION_COMMENT) | (1 << SearchMatchInformationProvider.LOCATION_FUNCTION) | (1 << SearchMatchInformationProvider.LOCATION_IMPORT_OR_INCLUDE_STATEMENT) | (1 << SearchMatchInformationProvider.LOCATION_PREPROCESSOR_DIRECTIVE) | (1 << SearchMatchInformationProvider.LOCATION_STRING_LITERAL);

}

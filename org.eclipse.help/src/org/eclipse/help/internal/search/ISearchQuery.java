/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

import java.util.Collection;

public interface ISearchQuery {
	/**
	 * Obtains names of fields in addition to default field
	 */
	public Collection getFieldNames();
	/**
	 * Obtains search word (user query)
	 */
	public String getSearchWord();
	/**
	 * @return true if search only in specified fields, not the default field
	 */
	public boolean isFieldSearch();
	/**
	 * Obtains locale
	 */
	public String getLocale();
}
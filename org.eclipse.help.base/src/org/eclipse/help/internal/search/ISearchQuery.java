/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.util.Collection;

public interface ISearchQuery {
	/**
	 * Obtains names of fields in addition to default field
	 */
	public Collection<String> getFieldNames();
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

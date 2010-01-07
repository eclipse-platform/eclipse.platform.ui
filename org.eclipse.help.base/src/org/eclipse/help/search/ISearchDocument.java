/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.search;

/**
 * Represents a document in the search index. This interface is used by
 * clients which implement the search participant extension point. While the 
 * help system uses the Lucene classes internally this interface does not
 * import anything from Lucene and enables the API to remain binary compatible 
 * whatever changes are made in Lucene.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.5
 */

public interface ISearchDocument {
	
	/**
	 * @param title the title which will be shown in the search results summary	 
	 */
	public void addTitle(String title);
	
	/**
	 * @param summary the summary which will be used when displaying search results.
	 */
	public void addSummary(String summary);
	
	/**
	 * @param contents the text which will be used when performing a search. The search index 
	 * will parse the contents and store the results in a database for rapid retrieval.
	 */
	public void addContents(String contents);

	/**
	 * Allows search participant to indicate that a document will be processed
	 * at runtime and that parts of the contents may not be displayed to the 
	 * user, causing a match in the search to be shown as a potential match.
	 * By default documents are considered to be unfiltered so this function 
	 * need only be called to indicate that a document is filtered
	 */
	public void setHasFilters(boolean hasFilters);

}

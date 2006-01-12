/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.search.core.text;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;


import org.eclipse.search.internal.core.text.TextSearchVisitor;

/**
 * A {@link TextSearchEngine} searches the content of a workspace file resources
 * for matches to a given search pattern.
 * <p>
 * {@link #create()} gives access to an instance of the search engine. By default this is the default
 * text search engine (see {@link #createDefault()}) but extensions can offer more sophisticated
 * search engine implementations.
 * </p>
 * @since 3.2
 */
public abstract class TextSearchEngine {
	
	/**
	 * Creates an instance of the search engine. By default this is the default text search engine (see {@link #createDefault()}),
	 * but extensions can offer more sophisticated search engine implementations.
	 * @return the created {@link TextSearchEngine}.
	 */
	public static TextSearchEngine create() {
		// TODO: An future extension point will allow to contribute a new text search engine
		return createDefault();
	}
	
	/**
	 * Creates the default, built-in, text search engine that implements a brute-force search, not using
	 * any search index.
	 * Note that clients should always use the search engine provided by {@link #create()}.
	 * @return an instance of the default text search engine {@link TextSearchEngine}.
	 */
	public static TextSearchEngine createDefault() {
		return new TextSearchEngine() {
			public IStatus search(TextSearchScope scope, TextSearchRequestor requestor, Pattern searchPattern, IProgressMonitor monitor) {
				return TextSearchVisitor.search(scope, requestor, searchPattern, monitor);
			}
		};
	}
		
	/**
	 * Uses a given search pattern to find matches in the content of a workspace file resource. If the file is open in an editor, the
	 * editor buffer is searched.

	 * @param requestor the search requestor that gets the search results
	 * @param scope the scope defining the resources to search in
	 * 	@param searchPattern The search pattern used to find matches in the file contents.
	 * @param monitor the progress monitor to use
	 * @return the status containing information about problems in resources searched.
	 */
	public abstract IStatus search(TextSearchScope scope, TextSearchRequestor requestor, Pattern searchPattern, IProgressMonitor monitor);

}

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

import java.util.Comparator;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.search.internal.core.text.TextSearchVisitor;
import org.eclipse.search.internal.ui.SearchPlugin;

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
		return SearchPlugin.getDefault().getTextSearchEngineRegistry().getPreferred();
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
				 return new TextSearchVisitor(requestor, searchPattern, monitor).search(scope, getSearchOrderHint());
			}
		};
	}

    private Comparator fSearchOrderHint;
		
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
    
    /**
     * Sets a comparator to be used by the search engine to order the files before searching them.
     * The passed comparator will be used with instances of type {@link org.eclipse.core.resources.IFile}.
     * The comparator on serves as a hint: The search engine is free to ignore the comparator and 
     * search the files in any order.
     * 
     * @param comparator a comparator that is capable of comparing instances of type {@link org.eclipse.core.resources.IFile}
     * or <code>null</code> not not provide a hint for the search order.
     */
    public void setSearchOrderHint(Comparator comparator) {
        fSearchOrderHint= comparator;
    }
    
    /**
     * Returns the comparator provided to give a hint of the order the search result should be provided. <code>null</code>
     * is returned if no such order has been configured with {@link #setSearchOrderHint(Comparator)}.
     * 
     * @return the comparator that can be used to order the files before searching, or
     * <code>null</code> if no order hint has been configured.
     */
    protected Comparator getSearchOrderHint() {
        return fSearchOrderHint;
    }
}

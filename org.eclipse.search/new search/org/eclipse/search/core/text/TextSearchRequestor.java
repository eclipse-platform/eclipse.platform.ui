/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.search.core.text;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceProxy;

/**
 * Collects the results from a search engine query. 
 * Clients implement a subclass to pass to {@link TextSearchEngine#search(TextSearchScope,
 * TextSearchRequestor, java.util.regex.Pattern, org.eclipse.core.runtime.IProgressMonitor)}
 * and implement the {@link #acceptPatternMatch(TextSearchMatchAccess)} 
 * method, and possibly override other life cycle methods.
 * <p>
 * The search engine calls {@link #beginReporting()} when a search starts,
 * then calls {@link #acceptFile(IFile)} for a file visited followed by
 * {@link #acceptPatternMatch(TextSearchMatchAccess)} for each pattern match found
 * in this file. The end of the search is signaled with a call to {@link #endReporting()}.
 * Note that {@link #acceptFile(IFile)} is called for all files in the search scope,
 * even if no match can be found.
 * </p>
 * <p>
 * The order of the search results is unspecified and may vary from request to request;
 * when displaying results, clients should not rely on the order but should instead arrange the results
 * in an order that would be more meaningful to the user.
 * </p>
 *
 * @see TextSearchEngine
 * @since 3.2
 */
public abstract class TextSearchRequestor {
	
	/**
	 * Notification sent before starting the search action.
	 * Typically, this would tell a search requestor to clear previously
	 * recorded search results.
	 * <p>
	 * The default implementation of this method does nothing. Subclasses
	 * may override.
	 * </p>
	 */
	public void beginReporting() {
		// do nothing
	}

	/**
	 * Notification sent after having completed the search action.
	 * Typically, this would tell a search requestor collector that no more
	 * results will be forthcoming in this search.
	 * <p>
	 * The default implementation of this method does nothing. Subclasses
	 * may override.
	 * </p>
	 */
	public void endReporting() {
		// do nothing
	}
	
	/**
	 * Notification sent before search starts in the given file. This method is called for all files that are contained
	 * in the search scope. The <code>fileProxy</code> is guaranteed to be of type {@link org.eclipse.core.resources.IResource#FILE}
	 * Implementors can decide if the file content should be searched for search matches or not.
	 * 
	 * @param fileProxy proxy of a file resource to be searched.
	 * @return If false, no pattern matches will be reported for the content of this file.
	 * @throws CoreException implementors can throw a {@link CoreException} if accessing the resource fails or another
	 * problem prevented the processing of the search match.
	 * 
	 * @deprecated This API will be removed before M5. Use {@link #acceptFile(IFile)} instead.
	 */
	public boolean acceptFile(IResourceProxy fileProxy) throws CoreException {
		return true;
	}
	
	/**
	 * Notification sent before search starts in the given file. This method is called for all files that are contained
	 * in the search scope. The <code>fileProxy</code> is guaranteed to be of type {@link org.eclipse.core.resources.IResource#FILE}
	 * Implementors can decide if the file content should be searched for search matches or not.
	 * 
	 * @param file the file resource to be searched.
	 * @return If false, no pattern matches will be reported for the content of this file.
	 * @throws CoreException implementors can throw a {@link CoreException} if accessing the resource fails or another
	 * problem prevented the processing of the search match.
	 */
	public boolean acceptFile(IFile file) throws CoreException {
		return true;
	}
	
	/**
	 * Accepts the given search match.
	 *
	 * @param matchAccess gives access to information of the match found. The matchAccess is not a value
	 * object. Its value might change after this method is finished, and the element might be reused.
	 * @return If false is returned no further matches will be reported for this file.
	 * @throws CoreException implementors can throw a {@link CoreException} if accessing the resource fails or another
	 * problem prevented the processing of the search match.
	 */
	public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess) throws CoreException {
		return true;
	}

}

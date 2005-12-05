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

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;


import org.eclipse.search.internal.core.text.TextSearchVisitor;

/**
 * A {@link TextSearchEngine} searches the content of a workspace file resources
 * for matches to a given search pattern.
 * <p>
 * This class is not intended to be instantiated or implemented by clients.
 * </p>
 * @since 3.2
 */
public final class TextSearchEngine {
	
	/**
	 * Creates a {@link TextSearchEngine}. 
	 * @return the created {@link TextSearchEngine}.
	 */
	public static TextSearchEngine create() {
		return new TextSearchEngine();
	}
	
	private TextSearchEngine() {
		// private
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
	public IStatus search(TextSearchScope scope, TextSearchRequestor requestor, Pattern searchPattern, IProgressMonitor monitor) {
		return TextSearchVisitor.search(scope, requestor, searchPattern, monitor);
	}

}

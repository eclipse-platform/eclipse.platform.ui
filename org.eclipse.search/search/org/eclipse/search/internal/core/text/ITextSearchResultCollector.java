/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ITextSearchResultCollector {

	/**
	 * Returns the progress monitor used to setup and report progress.
	 */
	public IProgressMonitor getProgressMonitor();
	
	/**
	 * Called before the actual search starts.
	 */
	public void aboutToStart() throws CoreException;
	 
	/**
	 * Accepts the given search result.
	 * @param proxy proxy the resource proxy in which the match has been found.
	 * @param line the line containing the match
	 * @param start position from the beginning of the file. Start position
	 *  is zero based.
	 * @param length the length of the match.
	 * @param lineNumber the line number of the match.
	 */
	public void accept(IResourceProxy proxy, String line, int start, int length, int lineNumber) throws CoreException;
	
	/**
	 * Called when the search has ended.
	 */
	public void done() throws CoreException; 	
}

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
	 * @return The progress monitor
	 */
	public IProgressMonitor getProgressMonitor();
	
	/**
	 * Called before the actual search starts.
	 * @throws CoreException Throws when starting failed.
	 */
	public void aboutToStart() throws CoreException;
	 
	/**
	 * Accepts the given search result.
	 * @param proxy proxy the resource proxy in which the match has been found.
	 * @param start position from the beginning of the file. Start position
	 *  is zero based.
	 * @param length the length of the match.
	 * @throws CoreException Processing failed
	 */
	public void accept(IResourceProxy proxy, int start, int length) throws CoreException;
	
	/**
	 * Called when the search has ended.
	 * @throws CoreException Throws when finish failed.
	 */
	public void done() throws CoreException; 	
}

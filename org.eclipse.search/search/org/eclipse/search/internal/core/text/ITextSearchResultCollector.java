/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.core.text;

import org.eclipse.core.resources.IResource;
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
	 * @param resource the resource in which the match has been found.
	 * @param line the line containing the match
	 * @param int start position from the beginning of the file. Start position
	 *  is zero based.
	 * @param length the length of the match.
	 * @param lineNumber the line number of the match.
	 */
	public void accept(IResource resource, String line, int start, int length, int lineNumber) throws CoreException;
	
	/**
	 * Called when the search has ended.
	 */
	public void done() throws CoreException; 	
}
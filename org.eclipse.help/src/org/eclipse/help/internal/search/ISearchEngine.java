package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
/**
 * Search Engine interface
 */
public interface ISearchEngine {
	/**
	 * Returns highlight information for a search result
	 * Note: it is recommeded to synchronize this method.
	 */
	public int[][] getHighlightInfo(String locale, byte[] docBuffer, int docNumber);
	/**
	 * Searches index for documents containing an expression.
	 * If the index hasn't been built then return null.
	 * Note: it is recommeded to synchronize this method, to serialize access to search.
	 * @return an XML representation of the results <topics><topic>...
	 * @param query query to search for in the index, contains locale
	 */
	public String getSearchResults(String query);
	/**
	 * Returns true when updates to the infoset requires index updates
	 */
	public boolean isIndexingNeeded(String locale);
	/**
	 * Updates index.  Checks if all contributions were indexed.
	 * If not, it indexes them (Currently reindexes everything).
	 */
	public void updateIndex(IProgressMonitor pm, String locale)
		throws OperationCanceledException, Exception;
}
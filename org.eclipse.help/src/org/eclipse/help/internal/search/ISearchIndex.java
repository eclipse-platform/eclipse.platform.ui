package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.InputStream;
import java.util.Collection;
/**
 * Interface that must be implemented by the searchengine extension point implementers
 * Request to search engine operations are performed througt this interface
 */
public interface ISearchIndex {
	/**
	 * Finish updating the index, by undoing, not committing
	 * all documents addedd since last call to endUpdate().
	 * To be called after adding documents.
	 */
	public boolean abortUpdate();
	/**
	 * Indexes one document in a string buffer
	 * All the addDocument() calls must be enclosed by
	 * beginUpdate() and endUpdate().
	 * @param name the document identifier (could be a URL)
	 * @param text the text of the document
	 * @return true if success
	 * @deprecated use addDocument(String, InputStream);
	 */
	public boolean addDocument(String name, byte[] buf, int size);
	/**
	 * Indexes one document read form a stream
	 * All the addDocument() calls must be enclosed by
	 * beginUpdate() and endUpdate().
	 * @param name the document identifier (could be a URL)
	 * @param stream the input stream containing the document
	 * @return true if success
	 */
	public boolean addDocument(String name, InputStream stream);
	/**
	 * Starts updating the index.
	 * To be called before adding documents.
	 */
	public boolean beginUpdate();
	/**
	 * Closes the index.  If index was modified
	 * the mapping will be saved.
	 * @return true if success
	 */
	public boolean close();
	/**
	 * Creates index
	 * @return true if index was created;
	 */
	public boolean create();
	/**
	 * Deletes Index
	 * @return true if index was deleted;
	 */
	public boolean delete();
	/**
	 * Deletes a single document from the GTR index.
	 * @param name - document name
	 * @return true if success
	 */
	public boolean removeDocument(String name);
	/**
	 * Finish updating the index.
	 * To be called after adding documents.
	 */
	public boolean endUpdate();
	/**
	 * Checks if index exists.
	 * @return true if index exists
	 */
	public boolean exists();
	public int[][] getHighlightInfo(byte[] docBuffer, int docNumber);
	/** 
	 * Initializes index 
	 */
	public boolean open();
	/** 
	 * Performs a boolean query search on this index 
	 * @param qry - a boolean query string
	 *  You can use parentheses, and boolean characters: 
	 *   * AND
	 *   + OR
	 *   ! NOT
	 *  You can also use wildcard searches
	 *   "data*"
	 *  and enclose multiword queries in quotes.
	 * @param fieldNames - Collection of field names of type String
	 *  the search will be performed on the given fields only
	 * @param fieldSearch - boolean indicating if field only search
	 *  should be performed
	 * @return - an array of document ids. 
	 * Later, we can extend this to return more data (rank, # of occs, etc.)
	 */
	public String[] search(
		String query,
		Collection fieldNames,
		boolean fieldSearch,
		int maxhits);
}
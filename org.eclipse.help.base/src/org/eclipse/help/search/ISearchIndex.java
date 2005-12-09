package org.eclipse.help.search;

import java.net.URL;

import org.apache.lucene.document.Document;
import org.eclipse.core.runtime.IStatus;

/**
 * Represents a Lucene index for one locale. The interface is used
 * to allow participants to delegate indexing of documents outside
 * of the TOC using the same algorithms as those in TOC.
 */

public interface ISearchIndex {

	/**
	 * Adds a document to the search index by parsing it using one of the file-based search
	 * participants, or the default HTML search participant. Use this method when encountering
	 * documents outside of TOC that are nevertheless of the known format and help system knows how
	 * to handle.
	 * 
	 * @param pluginId
	 *            the id of the contributing plug-in
	 * @param name
	 *            the name of the document
	 * @param url
	 *            the URL of the document using format '/pluginId/href'
	 * @param id
	 *            the unique id of this document as defined in the participant
	 * @param locale
	 *            the index locale
	 * @param doc
	 *            the Lucene document
	 * @return the status of the operation
	 */
	IStatus addDocument(String pluginId, String name, URL url, String id, Document doc);
	
	/**
	 * A search index is created for each locale.
	 * @return the locale associated with this index.
	 */
	String getLocale();
}
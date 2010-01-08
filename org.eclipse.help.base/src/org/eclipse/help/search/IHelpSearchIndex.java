/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.search;

import java.net.URL;


import org.eclipse.core.runtime.IStatus;

/**
 * Represents a Lucene index for one locale. The interface is used
 * to allow participants to delegate indexing of documents outside
 * of the TOC using the same algorithms as those in TOC.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.5
 */

public interface IHelpSearchIndex {

	/**
	 * Adds a document to the search index database by parsing it using one of the file-based search
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
	 * @param doc
	 *            the document to be added
	 * @return the status of the operation
	 */
	IStatus addSearchableDocument(String pluginId, String name, URL url, String id, ISearchDocument doc);
	
	/**
	 * A search index is created for each locale.
	 * @return the locale associated with this index.
	 */
	String getLocale();
}
